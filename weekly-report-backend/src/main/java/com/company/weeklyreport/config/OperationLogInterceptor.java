package com.company.weeklyreport.config;

import com.company.weeklyreport.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 操作日志拦截器：在请求完成后记录 URL、用户、耗时等
 */
@Component
public class OperationLogInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "operationLogStartTime";

    @Autowired
    private OperationLogService operationLogService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        try {
            Long start = (Long) request.getAttribute(START_TIME);
            long duration = start != null ? System.currentTimeMillis() - start : 0;
            Long userId = (Long) request.getAttribute("userId");
            String username = (String) request.getAttribute("username");
            String uri = request.getRequestURI();
            String method = request.getMethod();
            String ip = getClientIp(request);
            String[] moduleOperation = resolveModuleOperation(uri, method);
            operationLogService.saveLog(
                    userId, username,
                    moduleOperation[0], moduleOperation[1],
                    method, uri, ip, duration
            );
        } catch (Exception e) {
            // 记录日志失败不影响业务
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip.split(",")[0].trim() : "";
    }

    private String[] resolveModuleOperation(String uri, String method) {
        if (uri.startsWith("/auth")) return new String[]{"认证", method + " " + uri};
        if (uri.startsWith("/user")) return new String[]{"用户管理", method + " " + uri};
        if (uri.startsWith("/role")) return new String[]{"角色管理", method + " " + uri};
        if (uri.startsWith("/department")) return new String[]{"组织机构", method + " " + uri};
        if (uri.startsWith("/report")) return new String[]{"周报", method + " " + uri};
        if (uri.startsWith("/statistics")) return new String[]{"统计分析", method + " " + uri};
        if (uri.startsWith("/operation-log")) return new String[]{"操作日志", method + " " + uri};
        return new String[]{"系统", method + " " + uri};
    }
}
