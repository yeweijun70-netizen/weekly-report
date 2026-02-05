package com.company.weeklyreport.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.weeklyreport.entity.Department;
import com.company.weeklyreport.entity.WeeklyReport;
import com.company.weeklyreport.mapper.DepartmentMapper;
import com.company.weeklyreport.mapper.WeeklyReportMapper;
import com.company.weeklyreport.config.RedisCacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WeeklyReportService extends ServiceImpl<WeeklyReportMapper, WeeklyReport> {

    @Autowired
    private DepartmentMapper departmentMapper;

    public List<WeeklyReport> getMyReports(Long userId) {
        return baseMapper.selectByUserId(userId);
    }

    public WeeklyReport getCurrentWeekReport(Long userId, Integer year, Integer weekNumber) {
        LambdaQueryWrapper<WeeklyReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeeklyReport::getUserId, userId)
                .eq(WeeklyReport::getYear, year)
                .eq(WeeklyReport::getWeekNumber, weekNumber);
        return this.getOne(wrapper);
    }

    public WeeklyReport getLastWeekReport(Long userId, Integer year, Integer weekNumber) {
        // 绠€鍖栧鐞嗭細涓婂懆
        int lastWeek = weekNumber - 1;
        int lastYear = year;
        if (lastWeek < 1) {
            lastWeek = 52;
            lastYear = year - 1;
        }
        LambdaQueryWrapper<WeeklyReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WeeklyReport::getUserId, userId)
                .eq(WeeklyReport::getYear, lastYear)
                .eq(WeeklyReport::getWeekNumber, lastWeek);
        return this.getOne(wrapper);
    }

    public boolean saveDraft(WeeklyReport report) {
        report.setStatus(0);
        return this.saveOrUpdate(report);
    }

    public boolean submit(WeeklyReport report) {
        report.setStatus(1);
        report.setSubmitTime(LocalDateTime.now());
        return this.saveOrUpdate(report);
    }

    public IPage<WeeklyReport> getTeamReports(Page<WeeklyReport> page, Integer year, Integer weekNumber,
            Long departmentId, String startDate, String endDate, Integer status, String keyword) {

        // 濡傛灉閫夋嫨浜嗛儴闂紝鑾峰彇璇ラ儴闂ㄥ強鍏舵墍鏈夊瓙閮ㄩ棬ID
        List<Long> departmentIds = null;
        if (departmentId != null) {
            departmentIds = new ArrayList<>();
            collectDepartmentIds(departmentId, departmentIds);
        }

        return baseMapper.selectTeamReports(page, year, weekNumber, departmentIds, startDate, endDate, status, keyword);
    }

    /**
     * 閫掑綊鏀堕泦閮ㄩ棬鍙婂叾鎵€鏈夊瓙閮ㄩ棬ID
     */
    private void collectDepartmentIds(Long parentId, List<Long> ids) {
        ids.add(parentId);
        // 鏌ヨ瀛愰儴闂?
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getParentId, parentId)
                .eq(Department::getDeleted, 0);
        List<Department> children = departmentMapper.selectList(wrapper);
        for (Department child : children) {
            collectDepartmentIds(child.getId(), ids);
        }
    }

    public boolean review(Long id, BigDecimal score, String comment, Long reviewerId) {
        WeeklyReport report = this.getById(id);
        if (report == null) {
            return false;
        }
        report.setScore(score);
        report.setComment(comment);
        report.setReviewerId(reviewerId);
        report.setReviewTime(LocalDateTime.now());
        return this.updateById(report);
    }

    @Cacheable(value = RedisCacheConfig.CACHE_STATS_SUBMIT_RATE, key = "#year + '_' + #weekNumber")
    public Map<String, Object> getSubmitStats(Integer year, Integer weekNumber) {
        return baseMapper.getSubmitStats(year, weekNumber);
    }

    @CacheEvict(value = { RedisCacheConfig.CACHE_STATS_SUBMIT_RATE, RedisCacheConfig.CACHE_STATS_SCORE_TREND }, allEntries = true)
    public void evictStatsCache() {
    }
}

