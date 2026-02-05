package com.company.weeklyreport.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletResponse;

/**
 * 全局异常处理：将未捕获异常转为统一 JSON（code 500 + message），便于前端展示与排查。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception ex, HttpServletResponse response) {
        log.error("未捕获异常", ex);
        response.setStatus(500);
        String message = ex.getMessage() != null ? ex.getMessage() : "服务器内部错误";
        return Result.error(500, message);
    }
}
