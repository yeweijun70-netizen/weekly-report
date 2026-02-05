package com.company.weeklyreport.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.weeklyreport.entity.OperationLog;
import com.company.weeklyreport.mapper.OperationLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 操作日志 Service
 */
@Service
public class OperationLogService extends ServiceImpl<OperationLogMapper, OperationLog> {

    /**
     * 分页查询：支持用户名、模块、开始时间、结束时间
     */
    public IPage<OperationLog> pageList(Page<OperationLog> page, String username, String module,
                                         String startTime, String endTime) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(OperationLog::getUsername, username);
        }
        if (StringUtils.hasText(module)) {
            wrapper.like(OperationLog::getModule, module);
        }
        if (StringUtils.hasText(startTime)) {
            wrapper.ge(OperationLog::getCreateTime, startTime);
        }
        if (StringUtils.hasText(endTime)) {
            wrapper.le(OperationLog::getCreateTime, endTime + " 23:59:59");
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);
        return this.page(page, wrapper);
    }

    /**
     * 记录一条操作日志（供拦截器/切面调用）
     */
    public void saveLog(Long userId, String username, String module, String operation,
                        String requestMethod, String requestUrl, String ip, Long durationMs) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setModule(module);
        log.setOperation(operation);
        log.setRequestMethod(requestMethod);
        log.setRequestUrl(requestUrl);
        log.setIp(ip);
        log.setDurationMs(durationMs);
        this.save(log);
    }
}
