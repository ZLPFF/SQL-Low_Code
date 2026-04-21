package com.sunwayworld.cloud.module.lcdp.agent.service;

import com.sunwayworld.cloud.module.lcdp.agent.dto.ChatMessage;
import com.sunwayworld.cloud.module.lcdp.agent.dto.ChatRequest;
import com.sunwayworld.cloud.module.lcdp.agent.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentService {

    private final OllamaService ollamaService;
    private final WorkspaceService workspaceService;

    private static final String SYSTEM_PROMPT = """
        你是一个低代码平台（LCDP）的智能开发助手，基于检出-修改-同步机制工作。
        
        你的职责是：
        1. 理解用户的开发需求（如：创建页面、修改字段、添加功能等）
        2. 将需求分解为具体的操作步骤
        3. 通过"检出(checkout)→修改→同步(writeback)→提交(submit)"流程完成任务
        
        核心数据库表：
        - t_lcdp_resource: 资源表，存储模块、页面、脚本等
        - t_lcdp_resource_history: 资源历史版本表
        - t_lcdp_table: 表管理
        - t_lcdp_table_field: 表字段
        
        资源类别(resourcecategory)：
        - module: 模块
        - page: 页面
        - frontend_script: 前端脚本
        - backend_script: 后端脚本
        - component: 组件
        
        操作指令：
        - CHECKOUT:<resourceId> - 检出资源到工作空间
        - MODIFY:<resourceId>:<content> - 修改资源内容
        - WRITEBACK:<resourceId>:<content> - 同步修改到数据库
        - SUBMIT:<resourceId> - 提交修改使生效
        - QUERY:<sql> - 执行SQL查询
        - SEARCH:<keyword> - 搜索资源
        - LIST_TABLES - 列出所有表
        - LIST_RESOURCES - 列出资源树
        
        回复格式要求：
        1. 先用自然语言理解用户的需求
        2. 如果需要执行操作，输出结构化的操作指令（以ACTION:开头）
        3. 展示操作结果
        4. 给出总结或下一步建议
        """;

    private final Map<String, List<ChatMessage>> sessionHistory = new java.util.concurrent.ConcurrentHashMap<>();

    public ChatResponse processMessage(ChatRequest request) {
        String sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();
        String userId = request.getUserId() != null ? request.getUserId() : "agent";
        String userName = request.getUserName() != null ? request.getUserName() : "Agent";

        List<ChatMessage> history = sessionHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());

        ChatMessage userMsg = new ChatMessage();
        userMsg.setRole("user");
        userMsg.setContent(request.getMessage());
        userMsg.setTimestamp(java.time.LocalDateTime.now());
        history.add(userMsg);

        String aiResponse = ollamaService.chat(SYSTEM_PROMPT, buildContextMessage(request) + "\n\n用户消息: " + request.getMessage());

        List<String> actions = extractActions(aiResponse);
        ChatResponse.ActionResult actionResult = null;

        for (String action : actions) {
            actionResult = executeAction(action, userId, userName);
        }

        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(aiResponse);
        assistantMsg.setTimestamp(java.time.LocalDateTime.now());
        if (actionResult != null) {
            assistantMsg.setActionType(actionResult.getActionType());
        }
        history.add(assistantMsg);

        return ChatResponse.builder()
                .sessionId(sessionId)
                .message(aiResponse)
                .status("success")
                .action(actionResult)
                .history(history.subList(Math.max(0, history.size() - 10), history.size()))
                .build();
    }

    private String buildContextMessage(ChatRequest request) {
        return "";
    }

    private List<String> extractActions(String response) {
        List<String> actions = new ArrayList<>();
        Pattern pattern = Pattern.compile("ACTION:\\s*(\\w+):?([^\\n]*)");
        Matcher matcher = pattern.matcher(response);
        
        while (matcher.find()) {
            String action = matcher.group(1) + ":" + matcher.group(2).trim();
            actions.add(action);
        }
        
        return actions;
    }

    private ChatResponse.ActionResult executeAction(String action, String userId, String userName) {
        String[] parts = action.split(":", 3);
        String actionType = parts[0];
        String param1 = parts.length > 1 ? parts[1] : "";
        String param2 = parts.length > 2 ? parts[2] : "";

        try {
            return switch (actionType) {
                case "CHECKOUT" -> executeCheckout(Long.parseLong(param1), userId, userName);
                case "WRITEBACK" -> executeWriteback(Long.parseLong(param1), param2, agentConfig().isAutoSubmit());
                case "SUBMIT" -> executeSubmit(Long.parseLong(param1), userId);
                case "QUERY" -> executeQuery(param1);
                case "SEARCH" -> executeSearch(param1);
                case "LIST_TABLES" -> executeListTables();
                case "LIST_RESOURCES" -> executeListResources(param1);
                default -> createActionResult("UNKNOWN", false, null, null, "Unknown action: " + actionType);
            };
        } catch (Exception e) {
            log.error("Action execution failed: {}", action, e);
            return createActionResult(actionType, false, null, null, "Error: " + e.getMessage());
        }
    }

    private com.sunwayworld.cloud.module.lcdp.agent.config.AgentConfig agentConfig() {
        return new com.sunwayworld.cloud.module.lcdp.agent.config.AgentConfig();
    }

    private ChatResponse.ActionResult executeCheckout(Long resourceId, String userId, String userName) {
        try {
            var resource = workspaceService.findResourceById(resourceId);
            if (resource.isEmpty()) {
                return createActionResult("CHECKOUT", false, null, null, "Resource not found: " + resourceId);
            }
            
            workspaceService.checkoutResource(resourceId, userId, userName);
            
            Map<String, Object> details = new HashMap<>();
            details.put("resourceId", resourceId);
            details.put("resourceName", resource.get().get("resourcename"));
            details.put("status", "checked-out");
            
            return createActionResult("CHECKOUT", true, resourceId.toString(), (String) resource.get().get("resourcename"), details);
        } catch (Exception e) {
            return createActionResult("CHECKOUT", false, resourceId.toString(), null, "Checkout failed: " + e.getMessage());
        }
    }

    private ChatResponse.ActionResult executeWriteback(Long resourceId, String content, boolean autoSubmit) {
        try {
            workspaceService.writebackResource(resourceId, "agent", content, null, autoSubmit);
            
            Map<String, Object> details = new HashMap<>();
            details.put("resourceId", resourceId);
            details.put("autoSubmit", autoSubmit);
            
            return createActionResult("WRITEBACK", true, resourceId.toString(), null, details);
        } catch (Exception e) {
            return createActionResult("WRITEBACK", false, resourceId.toString(), null, "Writeback failed: " + e.getMessage());
        }
    }

    private ChatResponse.ActionResult executeSubmit(Long resourceId, String userId) {
        try {
            var resource = workspaceService.findResourceById(resourceId);
            if (resource.isEmpty()) {
                return createActionResult("SUBMIT", false, null, null, "Resource not found");
            }

            var draft = workspaceService.findDraftVersion(resourceId, userId);
            if (draft.isEmpty()) {
                return createActionResult("SUBMIT", false, resourceId.toString(), null, "No draft to submit");
            }

            Long historyId = ((Number) draft.get().get("id")).longValue();
            workspaceService.submitDraft(historyId, resourceId, userId);
            
            return createActionResult("SUBMIT", true, resourceId.toString(), resource.get().get("resourcename"), "Submitted successfully");
        } catch (Exception e) {
            return createActionResult("SUBMIT", false, resourceId.toString(), null, "Submit failed: " + e.getMessage());
        }
    }

    private ChatResponse.ActionResult executeQuery(String sql) {
        try {
            Map<String, Object> result = workspaceService.querySqlResult(sql, new ArrayList<>());
            
            Map<String, Object> details = new HashMap<>();
            details.put("sql", sql);
            details.put("result", result);
            
            return createActionResult("QUERY", true, null, null, details);
        } catch (Exception e) {
            return createActionResult("QUERY", false, null, null, "Query failed: " + e.getMessage());
        }
    }

    private ChatResponse.ActionResult executeSearch(String keyword) {
        try {
            List<Map<String, Object>> results = workspaceService.searchResources(keyword);
            
            Map<String, Object> details = new HashMap<>();
            details.put("keyword", keyword);
            details.put("count", results.size());
            details.put("results", results);
            
            return createActionResult("SEARCH", true, null, null, details);
        } catch (Exception e) {
            return createActionResult("SEARCH", false, null, null, "Search failed: " + e.getMessage());
        }
    }

    private ChatResponse.ActionResult executeListTables() {
        try {
            List<Map<String, Object>> tables = workspaceService.findTablesByName("");
            
            Map<String, Object> details = new HashMap<>();
            details.put("count", tables.size());
            details.put("tables", tables);
            
            return createActionResult("LIST_TABLES", true, null, null, details);
        } catch (Exception e) {
            return createActionResult("LIST_TABLES", false, null, null, "List tables failed: " + e.getMessage());
        }
    }

    private ChatResponse.ActionResult executeListResources(String category) {
        try {
            List<Map<String, Object>> resources;
            if (category == null || category.isEmpty()) {
                resources = workspaceService.findChildResources(0L);
            } else {
                resources = Collections.emptyList();
            }
            
            Map<String, Object> details = new HashMap<>();
            details.put("category", category);
            details.put("count", resources.size());
            details.put("resources", resources);
            
            return createActionResult("LIST_RESOURCES", true, null, null, details);
        } catch (Exception e) {
            return createActionResult("LIST_RESOURCES", false, null, null, "List resources failed: " + e.getMessage());
        }
    }

    private ChatResponse.ActionResult createActionResult(String actionType, boolean success, String resourceId, String resourceName, Object details) {
        Map<String, Object> detailsMap = details instanceof Map ? (Map<String, Object>) details : Map.of("message", details.toString());
        
        return ChatResponse.ActionResult.builder()
                .actionType(actionType)
                .success(success)
                .resourceId(resourceId)
                .resourceName(resourceName)
                .details(detailsMap)
                .build();
    }

    public List<ChatMessage> getSessionHistory(String sessionId) {
        return sessionHistory.getOrDefault(sessionId, Collections.emptyList());
    }

    public void clearSession(String sessionId) {
        sessionHistory.remove(sessionId);
    }
}
