package com.sunwayworld.cloud.module.lcdp.agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sunwayworld.cloud.framework.jdbc.JdbcTemplate;
import com.sunwayworld.cloud.framework.utilsecurity.utilscode.Base64SecurityUtils;
import com.sunwayworld.cloud.module.lcdp.agent.config.AgentConfig;
import com.sunwayworld.cloud.module.lcdp.agent.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkspaceService {

    private final JdbcTemplate jdbcTemplate;
    private final AgentConfig agentConfig;
    private final ObjectMapper objectMapper;

    public Path checkoutResource(Long resourceId, String userId, String userName) throws IOException {
        Path workspace = Paths.get(agentConfig.getWorkspace().getBasePath());
        Files.createDirectories(workspace);
        
        Map<String, Object> resource = findResourceById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found: " + resourceId));
        
        updateCheckoutStatus(resourceId, userId);
        acquireResourceLock(resourceId, userId);
        
        Optional<Map<String, Object>> effectiveVersion = findEffectiveVersion(resourceId);
        if (effectiveVersion.isPresent()) {
            Map<String, Object> version = effectiveVersion.get();
            createDraftFromVersion(resourceId, version, userId, userName);
            
            Path resourcePath = workspace.resolve("resource-" + resourceId);
            Files.createDirectories(resourcePath);
            
            Path manifestPath = resourcePath.resolve("manifest.json");
            Map<String, Object> manifest = new HashMap<>();
            manifest.put("resourceId", resourceId);
            manifest.put("resourceName", resource.get("resourcename"));
            manifest.put("status", "checked-out");
            manifest.put("content", version.get("content"));
            manifest.put("classContent", version.get("classcontent"));
            Files.writeString(manifestPath, objectMapper.writeValueAsString(manifest));
            
            return resourcePath;
        }
        
        return workspace.resolve("resource-" + resourceId);
    }

    public void writebackResource(Long resourceId, String userId, String content, String classContent, boolean autoSubmit) {
        Optional<Map<String, Object>> draftOpt = findDraftVersion(resourceId, userId);
        if (draftOpt.isEmpty()) {
            throw new RuntimeException("No draft version found for resource: " + resourceId);
        }

        Map<String, Object> draft = draftOpt.get();
        Long historyId = ((Number) draft.get("id")).longValue();
        
        String updateSql = "UPDATE t_lcdp_resource_history SET content = ?, classcontent = ?, modifyversion = modifyversion + 1 WHERE id = ?";
        jdbcTemplate.update(updateSql, content, classContent, historyId);
        
        if (autoSubmit) {
            submitDraft(historyId, resourceId, userId);
        }
    }

    public void submitDraft(Long historyId, Long resourceId, String userId) {
        Optional<Map<String, Object>> draftOpt = findDraftVersion(resourceId, userId);
        if (draftOpt.isEmpty()) {
            throw new RuntimeException("No draft to submit");
        }

        Map<String, Object> draft = draftOpt.get();
        
        jdbcTemplate.update("UPDATE t_lcdp_resource_history SET effectflag = 'no' WHERE resourceid = ?", resourceId);
        
        jdbcTemplate.update("UPDATE t_lcdp_resource_history SET submitflag = 'yes', effectflag = 'yes' WHERE id = ?", historyId);
        
        String content = (String) draft.get("content");
        String classContent = (String) draft.get("classcontent");
        Long version = ((Number) draft.get("version")).longValue();
        
        jdbcTemplate.update(
            "UPDATE t_lcdp_resource SET content = ?, classcontent = ?, effectversion = ?, modifytime = NOW() WHERE id = ?",
            content, classContent, version, resourceId
        );
        
        clearCheckoutStatus(resourceId);
        releaseResourceLock(resourceId);
    }

    public Optional<Map<String, Object>> findResourceById(Long resourceId) {
        String sql = "SELECT * FROM t_lcdp_resource WHERE id = ? AND deletetype = '0'";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, resourceId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<Map<String, Object>> findResourceByPath(String path) {
        String sql = "SELECT * FROM t_lcdp_resource WHERE path = ? AND deletetype = '0'";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, path);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<Map<String, Object>> findChildResources(Long parentId) {
        String sql = "SELECT * FROM t_lcdp_resource WHERE parentid = ? AND deletetype = '0' ORDER BY orderno";
        return jdbcTemplate.queryForList(sql, parentId);
    }

    public List<Map<String, Object>> searchResources(String keyword) {
        String sql = "SELECT * FROM t_lcdp_resource WHERE (resourcename LIKE ? OR resourcedesc LIKE ? OR path LIKE ?) AND deletetype = '0' ORDER BY path";
        String pattern = "%" + keyword + "%";
        return jdbcTemplate.queryForList(sql, pattern, pattern, pattern);
    }

    public List<Map<String, Object>> findResourceHistory(Long resourceId) {
        String sql = "SELECT * FROM t_lcdp_resource_history WHERE resourceid = ? ORDER BY version DESC";
        return jdbcTemplate.queryForList(sql, resourceId);
    }

    public Optional<Map<String, Object>> findEffectiveVersion(Long resourceId) {
        String sql = "SELECT * FROM t_lcdp_resource_history WHERE resourceid = ? AND effectflag = 'yes' ORDER BY version DESC LIMIT 1";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, resourceId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public Optional<Map<String, Object>> findDraftVersion(Long resourceId, String userId) {
        String sql = "SELECT * FROM t_lcdp_resource_history WHERE resourceid = ? AND submitflag = 'no' AND (checkoutuserid = ? OR checkoutuserid IS NULL) ORDER BY version DESC LIMIT 1";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, resourceId, userId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<Map<String, Object>> findTablesByName(String tableName) {
        String sql = "SELECT * FROM t_lcdp_table WHERE tablename LIKE ? ORDER BY tablename";
        return jdbcTemplate.queryForList(sql, "%" + tableName + "%");
    }

    public Optional<Map<String, Object>> findTableById(Long tableId) {
        String sql = "SELECT * FROM t_lcdp_table WHERE id = ?";
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, tableId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<Map<String, Object>> findTableFields(Long tableId) {
        String sql = "SELECT * FROM t_lcdp_table_field WHERE tableid = ? ORDER BY orderno";
        return jdbcTemplate.queryForList(sql, tableId);
    }

    public Map<String, Object> querySqlResult(String sql, List<Object> params) {
        try {
            if (sql.trim().toLowerCase().startsWith("select")) {
                List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, params.toArray());
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("data", results);
                result.put("count", results.size());
                return result;
            } else {
                int affected = jdbcTemplate.update(sql, params.toArray());
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("affected", affected);
                return result;
            }
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    private void updateCheckoutStatus(Long resourceId, String checkoutUserId) {
        String sql = "UPDATE t_lcdp_resource SET checkoutuserid = ?, checkouttime = NOW() WHERE id = ?";
        jdbcTemplate.update(sql, checkoutUserId, resourceId);
    }

    private void clearCheckoutStatus(Long resourceId) {
        String sql = "UPDATE t_lcdp_resource SET checkoutuserid = NULL, checkouttime = NULL WHERE id = ?";
        jdbcTemplate.update(sql, resourceId);
    }

    private void acquireResourceLock(Long resourceId, String userId) {
        String sql = "INSERT INTO t_lcdp_resource_lock (id, resourceid, lockuserid, locktime) VALUES (?, ?, ?, NOW()) ON DUPLICATE KEY UPDATE lockuserid = VALUES(lockuserid), locktime = NOW()";
        jdbcTemplate.update(sql, System.currentTimeMillis(), resourceId, userId);
    }

    private void releaseResourceLock(Long resourceId) {
        String sql = "DELETE FROM t_lcdp_resource_lock WHERE resourceid = ?";
        jdbcTemplate.update(sql, resourceId);
    }

    private Long createDraftFromVersion(Long resourceId, Map<String, Object> version, String userId, String userName) {
        Long id = System.currentTimeMillis();
        String sql = "INSERT INTO t_lcdp_resource_history (id, resourceid, resourcename, content, classcontent, version, submitflag, effectflag, modifyversion, compiledversion, checkoutuserid, checkouttime, createdtime) " +
                "VALUES (?, ?, ?, ?, ?, ?, 'no', 'no', 0, 0, ?, ?, NOW())";
        jdbcTemplate.update(sql, id, resourceId, version.get("resourcename"),
                version.get("content"), version.get("classcontent"), 
                ((Number) version.get("version")).longValue() + 1,
                userId);
        return id;
    }
}
