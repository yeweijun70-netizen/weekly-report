# 周报系统接入 AI 智能体：建议与实现方法

## 一、接入建议

### 1. 推荐形态：轻量级「AI 助手」

- **不做**：复杂多轮对话、自动执行系统操作（删用户、改权限等）、RAG 知识库、私有化部署大模型。
- **建议做**：
  - **通用问答**：在系统里加一个「AI 助手」入口，用户输入问题（如「如何提交周报？」「忘记密码怎么办？」），由大模型基于固定说明生成回复。
  - **写周报辅助（可选）**：在「写周报」页面加「AI 辅助撰写」，用户输入简短描述（如「本周做了登录和权限优化」），由大模型生成本周工作/下周计划草稿，用户可复制进表单。

这样改造成本小、易维护，且不涉及敏感操作，适合作为第一版。

### 2. 技术选型建议

| 项目     | 建议 |
|----------|------|
| 大模型   | 任选一种「OpenAI 兼容」的 API：OpenAI、国内通义/文心/豆包/智谱等，统一用同一套 HTTP 调用方式。 |
| 调用方式 | 后端用 RestTemplate/WebClient 调大模型 API，**API Key 只存在后端**，前端只调你自己的接口。 |
| 认证     | 现有 JWT 不变，AI 接口也走同一套登录校验（仅登录用户可用助手）。 |

---

## 二、实现方法概览

```
前端（Vue）                    后端（Spring Boot）                大模型
   |                                  |                            |
   |  POST /api/ai/assist             |                            |
   |  { "prompt": "如何提交周报？" }   |                            |
   | ---------------------------------->  |                            |
   |                                  |  校验 JWT、拼 system prompt  |
   |                                  |  HTTP POST 大模型 API        |
   |                                  | ------------------------------->
   |                                  |                            |
   |                                  | <------------------------------- 返回文本
   |  { "reply": "..." }              |                            |
   | <----------------------------------  |                            |
```

- 前端：一个「AI 助手」入口（如导航栏图标 + 抽屉/弹窗），输入框 + 发送，展示返回的 `reply`。
- 后端：一个 Controller（如 `AiController`）、一个 Service（如 `AiService`），读配置里的 API Key 和 base-url，用 RestTemplate 调大模型，返回 `{ "reply": "..." }`。

---

## 三、后端实现步骤

### 1. 配置（application.yml）

```yaml
# AI 助手（使用 OpenAI 兼容接口，可替换为国内大模型地址）
ai:
  enabled: true
  api-url: https://api.openai.com/v1/chat/completions   # 国内可改为对应地址
  api-key: sk-xxx                                       # 从环境变量读取更安全，如 ${AI_API_KEY:}
  model: gpt-3.5-turbo                                  # 或通义/文心等模型名
  max-tokens: 512
  system-prompt: |
    你是企业周报系统的智能助手。请根据用户问题简要回答，涉及操作时说明步骤。
    只回答与周报系统使用相关的问题，不要编造系统没有的功能。
```

- 生产环境建议用环境变量：`api-key: ${AI_API_KEY:}`，不在仓库里写死 Key。

### 2. 依赖

- 不需要新依赖，用 Spring 自带的 `RestTemplate` 或 `WebClient` 即可。若已包含 `spring-boot-starter-web`，用 `RestTemplate` 即可。

### 3. 后端接口设计

- **POST /api/ai/assist**  
  - 请求体：`{ "prompt": "用户输入的问题或描述" }`  
  - 可选：`{ "prompt": "...", "type": "report" }`，`type=report` 时用另一段 system prompt 专门生成周报草稿。  
  - 响应：`{ "code": 200, "data": { "reply": "大模型返回的文本" } }`  
  - 需登录：该接口注册在需要 JWT 的路径下（不要放进 `excludePathPatterns`）。

### 4. 调用大模型（OpenAI 兼容）

- URL：`POST {api-url}`（如 `https://api.openai.com/v1/chat/completions`）。  
- 请求体示例（JSON）：

```json
{
  "model": "gpt-3.5-turbo",
  "messages": [
    { "role": "system", "content": "上面配置的 system-prompt" },
    { "role": "user", "content": "用户输入" }
  ],
  "max_tokens": 512
}
```

- 响应中取：`choices[0].message.content` 作为 `reply` 返回给前端。  
- 国内大模型（通义、文心等）一般也提供「兼容 OpenAI」的 endpoint，只需改 `api-url` 和 `model`，请求/响应格式通常一致。

### 5. 异常与安全

- 网络超时、API 限流、Key 无效等：在 Service 里 try-catch，返回友好提示（如「助手暂时不可用，请稍后再试」），不要把 Key 或内部错误抛到前端。  
- 若 `ai.enabled=false`，可直接返回「AI 助手未启用」，便于关闭功能。

---

## 四、前端实现步骤

### 1. 入口与界面

- 在 **Layout.vue** 的导航/顶栏加一个「AI 助手」图标（或文字），点击后打开一个**抽屉（Drawer）**或**对话框（Dialog）**。  
- 抽屉/对话框内：  
  - 上方：多行输入框（用户输入问题或周报描述）。  
  - 下方：展示历史一问一答或仅当前回复（`reply` 文本），可用白板或简单 Markdown 渲染（若需再加依赖）。  
  - 底部：发送按钮，点击后 `POST /api/ai/assist`，请求体 `{ prompt: 输入内容 }`，把返回的 `data.reply` 显示出来。

### 2. 调用方式

- 使用现有封装的 `request`（如 `src/api/index.js`），新增方法，例如：

```javascript
// api/index.js
export const aiAssist = (data) => request.post('/ai/assist', data);
```

- 在助手组件里：`await aiAssist({ prompt: userInput })`，取 `res.data.reply` 展示；loading、错误提示用 Element Plus 的 Message 即可。

### 3. 写周报页「AI 辅助撰写」（可选）

- 在写周报页增加一个按钮「AI 辅助撰写」，点击后弹出小框输入「本周工作简述」。  
- 调用同一接口时可带 `type: 'report'`，后端对 `type=report` 使用另一段 system prompt（例如：根据简述生成本周工作、下周计划两段话，用固定格式）。  
- 返回的 `reply` 展示在弹窗里，用户复制到「本周工作」「下周计划」输入框即可，无需自动填表（减少复杂度）。

---

## 五、可选扩展（后续再做）

- **流式输出**：若希望回复一字一字打出，可后端改为 SSE（Server-Sent Events），前端用 EventSource 或 fetch 流式读取。  
- **简单「工具」**：例如用户问「本周谁没交周报」，后端先调你现有的统计/列表接口，把结果拼进 prompt 再调大模型，让模型整理成一句话回复（仍由后端调接口，不把接口暴露给模型自动执行）。  
- **提示词模板**：把 system-prompt 做成多套（通用问答 / 周报撰写 / 数据解读），通过 `type` 或 `scene` 选择。

---

## 六、小结

| 步骤 | 内容 |
|------|------|
| 1 | 在 application.yml 增加 `ai` 配置（api-url、api-key、model、system-prompt）。 |
| 2 | 后端新增 AiController（POST /ai/assist）、AiService（用 RestTemplate 调大模型，拼 messages，取 reply）。 |
| 3 | 前端在 Layout 加「AI 助手」入口，抽屉内输入 + 发送，调用 `/api/ai/assist`，展示 reply。 |
| 4 | （可选）写周报页加「AI 辅助撰写」，传 type=report，后端用周报专用 prompt 生成草稿。 |

按上述方式接入，即可在现有系统上有一个「不复杂」的 AI 智能体，后续再按需加流式、多轮或简单工具调用。
