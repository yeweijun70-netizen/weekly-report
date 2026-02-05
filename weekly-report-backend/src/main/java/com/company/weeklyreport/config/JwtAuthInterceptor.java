package com.company.weeklyreport.config;

import com.company.weeklyreport.common.Result;
import com.company.weeklyreport.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // CORS 预检请求不带 token，必须放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                if (jwtUtils.validateToken(token)) {
                    Long userId = jwtUtils.getUserId(token);
                    if (userId != null) {
                        request.setAttribute("userId", userId);
                        try {
                            request.setAttribute("username", jwtUtils.parseToken(token).getSubject());
                        } catch (Exception e) {
                            request.setAttribute("username", "");
                        }
                    }
                }
            } catch (Exception e) {
                // token 解析异常（如格式错误、userId 类型不对），不设置 userId，下面会返回 401
            }
        }
        // 需登录的接口：若无有效 userId 则返回 401，前端会跳转登录页
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            sendError(response, "请重新登录");
            return false;
        }
        return true;
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, message)));
    }
}

