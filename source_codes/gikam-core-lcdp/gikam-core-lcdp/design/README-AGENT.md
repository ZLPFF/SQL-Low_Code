# LCDP AI Agent 模块

基于本地部署模型的低代码平台智能开发助手。

## 功能特性

- **本地模型支持**: 基于 Ollama 运行 Qwen/LLaMA 等开源模型
- **集成到 design 模块**: 无需独立部署，与 LCDP 平台无缝集成
- **检出-修改-同步机制**: 复用 Codex 工作空间工具，实现资源管理
- **实时生效**: 基于数据库直接操作，修改即时生效

## 技术架构

```
┌─────────────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  LCDP Design 前端    │────▶│   Design 模块     │────▶│   Ollama 本地模型 │
│  (agent-chat.html)  │◀────│  (Agent Service) │◀────│   (Qwen/LLaMA)  │
└─────────────────────┘     └──────────────────┘     └─────────────────┘
                                       │
                                       ▼
                              ┌──────────────────┐
                              │  LCDP 数据库       │
                              │ t_lcdp_resource等  │
                              └──────────────────┘
```

## 环境要求

1. **Ollama 服务**: 本地运行的 LLM 推理服务
2. **模型**: Qwen2.5、Llama3.2 等开源模型

### 安装 Ollama

```bash
# macOS/Linux
curl -fsSL https://ollama.ai/install.sh | sh

# Windows
# 从 https://ollama.ai/download 下载

# 启动服务
ollama serve

# 下载模型（推荐 Qwen2.5）
ollama pull qwen2.5:7b
```

## 配置说明

在 `application.yml` 或环境变量中配置：

```yaml
lcdp:
  agent:
    enabled: true
    model: qwen2.5:7b              # 模型名称
    ollama-base-url: http://localhost:11434  # Ollama 地址
    max-tokens: 4096               # 最大生成 token 数
    temperature: 0.7               # 采样温度
    timeout-seconds: 120           # 超时时间
    api-base: /open/core/module/lcdp/agent  # API 前缀
    workspace:
      base-path: /tmp/lcdp-workspace
    checkout:
      auto-submit: false           # 自动提交（默认关闭）
```

## API 接口

### Chat 对话

```
POST /open/core/module/lcdp/agent/chat
Content-Type: application/json

{
    "sessionId": "optional-session-id",
    "message": "帮我修改登录页面的标题",
    "userId": "user001",
    "userName": "张三"
}
```

### 响应格式

```json
{
    "sessionId": "sess_xxx",
    "message": "好的，我来帮您...",
    "status": "success",
    "action": {
        "actionType": "CHECKOUT",
        "success": true,
        "resourceId": "123",
        "resourceName": "登录页面"
    },
    "history": [...]
}
```

### 其他接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /open/core/module/lcdp/agent/history/{sessionId} | 获取对话历史 |
| DELETE | /open/core/module/lcdp/agent/history/{sessionId} | 清除对话历史 |
| GET | /open/core/module/lcdp/agent/health | 健康检查 |
| GET | /agent/chat | AI 对话界面（HTML） |

## 页面访问

启动 design 模块后，访问：

```
http://localhost:8182/agent/chat
```

或通过 LCDP 平台的 iframe 嵌入：

```html
<iframe src="http://localhost:8182/agent/chat" style="width: 100%; height: 600px; border: none;"></iframe>
```

## AI 指令系统

| 指令 | 说明 | 示例 |
|------|------|------|
| `CHECKOUT:<resourceId>` | 检出资源 | `CHECKOUT:123` |
| `WRITEBACK:<resourceId>:<content>` | 同步修改 | `WRITEBACK:123:{"title":"新标题"}` |
| `SUBMIT:<resourceId>` | 提交修改 | `SUBMIT:123` |
| `SEARCH:<keyword>` | 搜索资源 | `SEARCH:登录页面` |
| `QUERY:<sql>` | 执行SQL | `QUERY:SELECT * FROM t_lcdp_resource` |
| `LIST_TABLES` | 列出表 | - |

## 工作流程

```
1. 用户对话 → AI 理解意图
2. AI 解析指令 → CHECKOUT/MODIFY/WRITEBACK
3. 检出资源 → 创建草稿
4. 用户修改 → 同步到数据库
5. 提交生效 → 完成开发
```

## 文件结构

```
design/src/main/java/com/sunwayworld/cloud/module/lcdp/agent/
├── config/
│   ├── AgentConfig.java          # Agent 配置
│   └── OllamaConfig.java         # Ollama HTTP 客户端
├── controller/
│   ├── AgentController.java      # REST API
│   └── AgentPageController.java   # HTML 页面
├── dto/
│   ├── ChatRequest.java
│   ├── ChatResponse.java
│   ├── ChatMessage.java
│   ├── OllamaRequest.java
│   └── OllamaResponse.java
└── service/
    ├── AgentService.java          # 核心 Agent 逻辑
    ├── OllamaService.java         # Ollama API 调用
    └── WorkspaceService.java      # 检出-修改-同步

design/src/main/resources/static/agent/
└── agent-chat.html                # 前端对话界面
```

## 注意事项

1. **Ollama 服务**: 确保 Ollama 服务在运行，且模型已下载
2. **首次响应慢**: 本地模型首次推理可能较慢（约10-30秒）
3. **内存需求**: 7B 模型建议 8GB+ 内存，3B 模型 4GB+ 内存
4. **数据库权限**: Agent 使用的数据库账号需要有读写权限

## 故障排查

### Ollama 服务不可用

```bash
# 检查 Ollama 服务状态
curl http://localhost:11434/api/tags

# 如果失败，启动 Ollama
ollama serve
```

### 模型加载失败

```bash
# 列出已安装的模型
ollama list

# 重新拉取模型
ollama pull qwen2.5:7b
```

### API 调用超时

调整 `application.yml` 中的 `timeout-seconds` 配置，或使用更小的模型。
