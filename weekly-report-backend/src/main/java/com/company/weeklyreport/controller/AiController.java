package com.company.weeklyreport.controller;

import com.company.weeklyreport.common.Result;
import com.company.weeklyreport.service.AiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Api(tags = "AI 助手")
@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @ApiOperation("对话助手（支持多轮历史与上下文，更智能）")
    @PostMapping("/assist")
    public Result<Map<String, String>> assist(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        if (body == null) {
            return Result.error("请输入问题");
        }
        Object typeObj = body.get("type");
        String type = typeObj != null ? typeObj.toString().trim() : null;
        Object msgObj = body.get("message");
        if (msgObj == null) {
            msgObj = body.get("prompt");
        }
        String message = msgObj != null ? msgObj.toString().trim() : "";
        // type=report：把用户在各栏填写的草稿（draft）拼进 message，供 AI 总结丰富后模板化输出
        if ("report".equalsIgnoreCase(type)) {
            Object draftObj = body.get("draft");
            String thisWeek = "";
            String nextWeek = "";
            String coordination = "";
            if (draftObj instanceof Map) {
                Map<?, ?> draft = (Map<?, ?>) draftObj;
                thisWeek = draft.get("thisWeekWork") != null ? draft.get("thisWeekWork").toString().trim() : "";
                nextWeek = draft.get("nextWeekPlan") != null ? draft.get("nextWeekPlan").toString().trim() : "";
                coordination = draft.get("coordination") != null ? draft.get("coordination").toString().trim() : "";
            }
            message = "请将我下面的简略输入填入你已知的周报模板（一、本周工作完成情况表格 + 二、问题风险与思考；需支持事项；三、下周核心计划），进行丰富总结后输出 JSON（键：thisWeekWork、nextWeekPlan、coordination）。不要忽略我写的任何要点。\n\n【本周工作完成情况-简略输入】\n" + (thisWeek.isEmpty() ? "（未填）" : thisWeek) + "\n\n【下周工作计划-简略输入】\n" + (nextWeek.isEmpty() ? "（未填）" : nextWeek) + "\n\n【需协调/需支持事项-简略输入】\n" + (coordination.isEmpty() ? "（未填）" : coordination);
        } else if (message.isEmpty()) {
            return Result.error("请输入问题");
        }
        // 多轮历史：前端传 { role, content } 数组
        @SuppressWarnings("unchecked")
        List<Map<String, String>> history = (List<Map<String, String>>) body.get("history");
        // 上下文：当前用户（从 JWT 注入）、当前周次（前端可传）
        String username = request.getAttribute("username") != null ? request.getAttribute("username").toString() : "";
        Object ctxObj = body.get("context");
        Integer currentWeek = null;
        if (ctxObj instanceof Map) {
            Object w = ((Map<?, ?>) ctxObj).get("currentWeek");
            if (w instanceof Number) {
                currentWeek = ((Number) w).intValue();
            }
        }
        StringBuilder contextInfo = new StringBuilder();
        if (username != null && !username.isEmpty()) {
            contextInfo.append("当前用户：").append(username).append("。");
        }
        if (currentWeek != null && currentWeek > 0) {
            contextInfo.append("当前为第 ").append(currentWeek).append(" 周。");
        }
        String reply = aiService.chatWithContext(message, history, contextInfo.length() > 0 ? contextInfo.toString() : null, type);
        return Result.success(Collections.singletonMap("reply", reply));
    }
}
