package com.company.weeklyreport.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    @Value("${ai.enabled:true}")
    private boolean enabled;

    @Value("${ai.api-url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.model:deepseek-chat}")
    private String model;

    @Value("${ai.max-tokens:1024}")
    private int maxTokens;

    @Value("${ai.timeout-ms:30000}")
    private int timeoutMs;

    @Value("${ai.system-prompt:你是企业周报系统的智能助手，请简要回答用户问题。}")
    private String systemPrompt;

    @Value("${ai.system-prompt-report:请生成周报草稿，仅输出 JSON：thisWeekWork、nextWeekPlan、coordination。}")
    private String systemPromptReport;

    private RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    public AiService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    @PostConstruct
    public void init() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * 单轮对话（兼容旧调用），无历史、无上下文，易显得像「搜索」。
     */
    public String chat(String userMessage) {
        return chatWithContext(userMessage, null, null, null);
    }

    /**
     * 带多轮历史与上下文的对话；type=report 时使用周报草稿专用 prompt、忽略历史。
     * @param userMessage 用户本条输入
     * @param history 历史消息列表（type=report 时忽略）
     * @param contextInfo 当前上下文描述，会注入到 system，可为 null
     * @param type 若为 "report" 则使用 system-prompt-report 并仅生成周报草稿
     */
    public String chatWithContext(String userMessage, List<Map<String, String>> history, String contextInfo, String type) {
        if (!enabled) {
            return "AI 助手未启用。";
        }
        if (apiKey == null || apiKey.isEmpty()) {
            return "未配置 API Key，请在服务端配置 ai.api-key。";
        }

        try {
            List<Map<String, String>> messages = new ArrayList<>();
            boolean isReport = "report".equalsIgnoreCase(type);
            // 系统提示：report 用专用 prompt，否则用对话 prompt
            String systemContent = isReport ? systemPromptReport : systemPrompt;
            if (contextInfo != null && !contextInfo.trim().isEmpty()) {
                systemContent = systemContent + "\n\n【当前上下文】\n" + contextInfo.trim();
            }
            Map<String, String> system = new HashMap<>();
            system.put("role", "system");
            system.put("content", systemContent);
            messages.add(system);
            // 多轮历史（report 模式不用历史；否则最近 10 条）
            int maxHistory = 10;
            if (!isReport && history != null && !history.isEmpty()) {
                int from = Math.max(0, history.size() - maxHistory);
                for (int i = from; i < history.size(); i++) {
                    Map<String, String> m = history.get(i);
                    String role = m != null ? m.get("role") : null;
                    String content = m != null ? m.get("content") : null;
                    if ("user".equals(role) || "assistant".equals(role)) {
                        Map<String, String> msg = new HashMap<>();
                        msg.put("role", role);
                        msg.put("content", content != null ? content : "");
                        messages.add(msg);
                    }
                }
            }
            Map<String, String> user = new HashMap<>();
            user.put("role", "user");
            user.put("content", userMessage);
            messages.add(user);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", maxTokens);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                return "助手暂时无响应，请稍后再试。";
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode choices = root.path("choices");
            if (choices.isMissingNode() || !choices.isArray() || choices.size() == 0) {
                return "助手返回格式异常，请稍后再试。";
            }
            JsonNode message = choices.get(0).path("message");
            String content = message.path("content").asText("");
            return content.isEmpty() ? "助手未返回有效内容。" : content;
        } catch (Exception e) {
            e.printStackTrace();
            return "助手暂时不可用，请稍后再试。若持续失败，请检查网络与 API Key。";
        }
    }
}
