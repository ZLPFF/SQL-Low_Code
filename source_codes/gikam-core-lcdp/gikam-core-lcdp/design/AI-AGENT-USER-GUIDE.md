# LCDP AI Agent 使用指南

本文档详细介绍如何在低代码平台（LCDP）中启动和使用 AI Agent 功能。

## 目录

1. [环境准备](#一环境准备)
2. [配置说明](#二配置说明)
3. [编译与启动](#三编译与启动)
4. [访问与使用](#四访问与使用)
5. [前端集成](#五前端集成)
6. [使用场景示例](#六使用场景示例)
7. [工作流程图](#七工作流程图)
8. [故障排查](#八故障排查)

---

## 一、环境准备

### 1.1 安装 Ollama

Ollama 是一款本地运行的 LLM 推理服务，支持 Qwen、LLaMA 等开源模型。

**macOS / Linux:**

```bash
curl -fsSL https://ollama.ai/install.sh | sh
```

**Windows:**

从 [https://ollama.ai/download](https://ollama.ai/download) 下载安装包

**验证安装:**

```bash
ollama --version
```

### 1.2 下载模型

推荐使用 Qwen2.5（阿里通义千问），中文能力强，7B 版本适合大多数开发场景：

```bash
# 下载 Qwen2.5 7B 模型（约 4GB）
ollama pull qwen2.5:7b

# 或者更小的 3B 版本（更快，内存需求更低）
ollama pull qwen2.5:3b

# 查看已下载的模型
ollama list
```

**其他可选模型:**

```bash
# LLaMA3.2
ollama pull llama3.2:3b

# 微软 Phi3
ollama pull phi3:3.8b
```

### 1.3 启动 Ollama 服务

```bash
ollama serve
```

服务默认在 `http://localhost:11434` 运行。

---

## 二、配置说明

### 2.1 确认 application.yml 配置

项目中的 `application.yml` 位于 `design/src/main/resources/config/` 目录，需要确认以下两处配置：

#### 数据源配置（已有，不需要改）

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://demo.sunwayworld.com:8621/
    username: gikam_core_pm_test
    password: gikam_core_pm_test
```

这是 LCDP 平台连接数据库的配置，AI Agent 复用此连接执行检出、修改、同步等操作。

#### AI Agent 配置（如不存在请添加）

在 `application.yml` 最底部添加：

```yaml
lcdp:
  agent:
    enabled: true                     # 是否启用 AI Agent
    model: qwen2.5:7b                # 使用的模型
    ollama-base-url: http://localhost:11434  # Ollama 地址
    max-tokens: 4096
    temperature: 0.7
    timeout-seconds: 120
    api-base: /open/core/module/lcdp/agent
    workspace:
      base-path: /tmp/lcdp-workspace
    checkout:
      auto-submit: false              # 是否自动提交
```

### 2.2 配置项说明

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `lcdp.agent.enabled` | 是否启用 AI Agent | `true` |
| `lcdp.agent.model` | Ollama 模型名称 | `qwen2.5:7b` |
| `lcdp.agent.ollama-base-url` | Ollama 服务地址 | `http://localhost:11434` |
| `lcdp.agent.timeout-seconds` | API 超时时间（秒） | `120` |
| `lcdp.agent.workspace.base-path` | 工作空间根目录 | `/tmp/lcdp-workspace` |
| `lcdp.agent.checkout.auto-submit` | 修改后是否自动提交 | `false` |

### 2.3 配置优先级

配置值可以由环境变量覆盖，优先级从高到低：

1. **环境变量**: `OLLAMA_MODEL`, `OLLAMA_BASE_URL`, `OLLAMA_TIMEOUT`
2. **application.yml 中的值**
3. **配置文件中的默认值**

---

## 三、编译与启动

### 3.1 编译 design 模块

```bash
cd /workspace/source_codes/gikam-core-lcdp/gikam-core-lcdp/design

# 编译（跳过测试）
mvn clean package -DskipTests
```

编译成功后，会在 `target/` 目录生成 JAR 文件。

### 3.2 启动服务

**方式一：直接运行 JAR**

```bash
java -jar target/gikam-core-lcdp-design-14.2.0-SNAPSHOT.jar
```

**方式二：带环境变量运行**

```bash
java -jar target/gikam-core-lcdp-design-14.2.0-SNAPSHOT.jar \
  --spring.datasource.url="jdbc:mysql://your-db-host:3306/lcdp" \
  --spring.datasource.username="your_username" \
  --spring.datasource.password="your_password"
```

**方式三：IDE 中运行**

在 IntelliJ IDEA 等 IDE 中运行 `GikamLcdpDesignApplication` 主类。

### 3.3 验证启动

启动日志中看到以下信息表示成功：

```
Started GikamLcdpDesignApplication in X seconds
LCDP AI Agent enabled: true
Model: qwen2.5:7b
```

---

## 四、访问与使用

### 4.1 访问 AI 对话界面

打开浏览器访问：

```
http://localhost:8182/agent/chat
```

### 4.2 界面布局

```
+----------------------------------------------------------+
|  [AI图标] AI 开发助手                           准备就绪   |
+----------------------------------------------------------+
|                                                          |
|   +--------------------------------------------------+   |
|   |  AI: 您好！我是低代码平台的 AI 开发助手...       |   |
|   +--------------------------------------------------+   |
|                                                          |
|   +--------------------------------------------------+   |
|   |                          用户: 帮我修改登录页面   |   |
|   +--------------------------------------------------+   |
|                                                          |
+----------------------------------------------------------+
|  [列出模块]  [搜索登录]  [列出表]           <- 快捷操作   |
+----------------------------------------------------------+
|  [输入框...............................]  [发送]        |
+----------------------------------------------------------+
```

### 4.3 快捷操作

| 按钮 | 功能 |
|------|------|
| 列出模块 | 显示 LCDP 平台中的所有模块 |
| 搜索登录 | 搜索包含"登录"关键词的资源 |
| 列出表 | 显示数据库中的所有表 |

---

## 五、前端集成

### 5.1 iframe 嵌入

在 LCDP 平台任意页面中添加：

```html
<!-- 对话窗口 -->
<div id="ai-chat-container" style="position: fixed; right: 20px; bottom: 20px; width: 400px; height: 600px; display: none; z-index: 9999;">
    <iframe 
        src="http://localhost:8182/agent/chat" 
        style="width: 100%; height: 100%; border: none; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.15);">
    </iframe>
</div>

<!-- 悬浮按钮 -->
<button onclick="toggleAiChat()" style="position: fixed; right: 20px; bottom: 20px; width: 60px; height: 60px; border-radius: 50%; background: #4A90E2; color: white; border: none; cursor: pointer; z-index: 9998;">AI</button>

<script>
function toggleAiChat() {
    const container = document.getElementById('ai-chat-container');
    container.style.display = container.style.display === 'none' ? 'block' : 'none';
}
</script>
```

### 5.2 API 调用集成

```javascript
// 调用 AI 对话
const response = await fetch('/open/core/module/lcdp/agent/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        sessionId: 'user-session-123',
        message: '帮我修改登录页面标题',
        userId: 'current_user_id',
        userName: '张三'
    })
});

const result = await response.json();
console.log(result.message);      // AI 回复
console.log(result.action);      // 执行的操作
console.log(result.history);      // 对话历史
```

### 5.3 API 接口列表

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/open/core/module/lcdp/agent/chat` | 发送对话消息 |
| GET | `/open/core/module/lcdp/agent/history/{sessionId}` | 获取对话历史 |
| DELETE | `/open/core/module/lcdp/agent/history/{sessionId}` | 清除对话历史 |
| GET | `/open/core/module/lcdp/agent/health` | 健康检查 |
| GET | `/agent/chat` | AI 对话界面（HTML） |

---

## 六、使用场景示例

### 场景 1：搜索资源

**用户输入：**

```
帮我搜索登录相关的页面资源
```

**AI 理解后会执行：**

```
ACTION: SEARCH:登录
```

**返回结果：**

```
- 登录页面 (ID: 123)
- 用户登录组件 (ID: 456)
- 登录验证脚本 (ID: 789)
```

### 场景 2：修改资源

**用户输入：**

```
帮我修改登录页面，把标题从"登录"改成"用户登录"
```

**AI 处理流程：**

```
1. 理解意图 → 需要修改登录页面内容
2. 检出资源 → CHECKOUT:123
3. 修改内容 → 更新标题
4. 同步回写 → WRITEBACK:123:{新内容}
5. 询问提交 → "是否提交使修改生效？"
```

### 场景 3：执行 SQL 查询

**用户输入：**

```
帮我查看有哪些模块
```

**AI 执行：**

```
ACTION: QUERY:SELECT resourcename, path FROM t_lcdp_resource WHERE resourcecategory = 'module'
```

**返回结果：**

```
+-----------------+-----------------+
| resourcename    | path            |
+-----------------+-----------------+
| 用户管理        | /module/user    |
| 订单管理        | /module/order   |
+-----------------+-----------------+
```

### 场景 4：查看表结构

**用户输入：**

```
列出所有表
```

**AI 执行：**

```
ACTION: LIST_TABLES
```

---

## 七、工作流程图

```
+------------------------------------------------------------------+
|                           使用流程                                 |
+------------------------------------------------------------------+
|                                                                   |
|   1. [启动 Ollama]                                                |
|      ollama serve                                                 |
|              |                                                     |
|              v                                                     |
|   2. [启动 LCDP]                                                  |
|      java -jar gikam-core-lcdp-design.jar                        |
|              |                                                     |
|              v                                                     |
|   3. [打开界面]                                                    |
|      http://localhost:8182/agent/chat                            |
|              |                                                     |
|              v                                                     |
|   4. [对话交互]                                                    |
|      用户输入 -> AI 理解 -> 执行操作 -> 返回结果                    |
|              |                                                     |
|              v                                                     |
|   5. [自动同步]                                                    |
|      CHECKOUT -> 修改 -> WRITEBACK -> SUBMIT                      |
|              |                                                     |
|              v                                                     |
|   6. [生效完成]                                                    |
|      数据库更新 -> 平台实时生效                                    |
|                                                                   |
+------------------------------------------------------------------+
```

### 资源操作流程详解

```
+-------------+     +-------------+     +-------------+     +-------------+
|   CHECKOUT  | --> |   MODIFY    | --> |  WRITEBACK  | --> |   SUBMIT    |
+-------------+     +-------------+     +-------------+     +-------------+
      |                   |                   |                   |
      v                   v                   v                   v
  检出资源到          用户/AI 修改         同步修改到           提交生效到
  工作空间           资源内容             数据库              数据库
```

---

## 八、故障排查

### 问题 1：Ollama 连接失败

```bash
# 检查 Ollama 是否运行
curl http://localhost:11434/api/tags

# 如果失败，启动 Ollama
ollama serve
```

### 问题 2：模型加载慢

首次调用需要下载/加载模型，约 10-30 秒。后续调用会快很多。

### 问题 3：内存不足

使用更小的模型：

```bash
# 约 2GB 内存
ollama pull qwen2.5:3b

# 或 1GB 内存
ollama pull llama3.2:1b
```

然后修改 `application.yml`：

```yaml
lcdp:
  agent:
    model: qwen2.5:3b
```

### 问题 4：API 调用超时

调整 `application.yml` 中的 `timeout-seconds` 配置，或使用更小的模型。

### 问题 5：数据库连接失败

检查 `application.yml` 中的 `spring.datasource.*` 配置是否正确，确保：
- 数据库服务正常运行
- 用户名密码正确
- 网络连接正常

### 问题 6：模型不存在

```bash
# 列出已安装的模型
ollama list

# 重新拉取模型
ollama pull qwen2.5:7b
```

---

## 九、快速开始清单

```
[ ] 1. 安装 Ollama: curl -fsSL https://ollama.ai/install.sh | sh
[ ] 2. 下载模型: ollama pull qwen2.5:7b
[ ] 3. 启动 Ollama: ollama serve（另开终端）
[ ] 4. 确认 application.yml 中 lcdp.agent.* 配置存在
[ ] 5. 编译 design 模块: mvn clean package -DskipTests
[ ] 6. 启动 LCDP: java -jar target/gikam-core-lcdp-design.jar
[ ] 7. 访问: http://localhost:8182/agent/chat
[ ] 8. 开始对话！
```

---

## 十、相关文件

### 新增的代码文件

```
design/src/main/java/com/sunwayworld/cloud/module/lcdp/agent/
├── config/
│   ├── AgentConfig.java           # Agent 配置属性
│   └── OllamaConfig.java          # Ollama HTTP 客户端配置
├── controller/
│   ├── AgentController.java       # REST API
│   └── AgentPageController.java   # HTML 页面路由
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

design/src/main/resources/
├── static/agent/
│   └── agent-chat.html            # 前端对话界面
└── config/
    └── application.yml            # 已添加 lcdp.agent.* 配置
```

### 静态资源和配置

- `design/src/main/resources/static/agent/agent-chat.html` - 前端对话界面
- `design/src/main/resources/config/application.yml` - 配置文件

---

如有问题，请提供具体的错误信息以便进一步排查。
