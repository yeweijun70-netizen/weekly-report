package com.company.weeklyreport.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.weeklyreport.common.Result;
import com.company.weeklyreport.entity.WeeklyReport;
import com.company.weeklyreport.service.WeeklyReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Api(tags = "周报管理")
@RestController
@RequestMapping("/report")
public class WeeklyReportController {

    @Autowired
    private WeeklyReportService weeklyReportService;

    @ApiOperation("当前周信息")
    @GetMapping("/current-week")
    public Result<Map<String, Object>> currentWeek() {
        LocalDate now = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.CHINA);
        int weekNumber = now.get(weekFields.weekOfYear());
        int year = now.getYear();

        Map<String, Object> data = new HashMap<>();
        data.put("year", year);
        data.put("weekNumber", weekNumber);
        return Result.success(data);
    }

    @ApiOperation("我的周报列表")
    @GetMapping("/my-list")
    public Result<List<WeeklyReport>> myList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(weeklyReportService.getMyReports(userId));
    }

    @ApiOperation("当前周报")
    @GetMapping("/current")
    public Result<WeeklyReport> current(HttpServletRequest request,
            @RequestParam Integer year,
            @RequestParam Integer weekNumber) {
        Long userId = (Long) request.getAttribute("userId");
        WeeklyReport report = weeklyReportService.getCurrentWeekReport(userId, year, weekNumber);
        return Result.success(report);
    }

    @ApiOperation("导入上周计划")
    @GetMapping("/import-last-week")
    public Result<String> importLastWeek(HttpServletRequest request,
            @RequestParam Integer year,
            @RequestParam Integer weekNumber) {
        Long userId = (Long) request.getAttribute("userId");
        WeeklyReport lastWeek = weeklyReportService.getLastWeekReport(userId, year, weekNumber);
        if (lastWeek != null && lastWeek.getNextWeekPlan() != null) {
            return Result.success(lastWeek.getNextWeekPlan());
        }
        return Result.success("");
    }

    @ApiOperation("保存草稿")
    @PostMapping("/draft")
    public Result<Void> saveDraft(HttpServletRequest request, @RequestBody WeeklyReport report) {
        Long userId = (Long) request.getAttribute("userId");
        report.setUserId(userId);
        weeklyReportService.saveDraft(report);
        return Result.success();
    }

    @ApiOperation("提交周报")
    @PostMapping("/submit")
    public Result<Void> submit(HttpServletRequest request, @RequestBody WeeklyReport report) {
        Long userId = (Long) request.getAttribute("userId");
        report.setUserId(userId);
        weeklyReportService.submit(report);
        weeklyReportService.evictStatsCache();
        return Result.success();
    }

    @ApiOperation("团队周报列表")
    @GetMapping("/team")
    public Result<IPage<WeeklyReport>> teamList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer weekNumber,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        Page<WeeklyReport> page = new Page<>(current, size);
        return Result.success(weeklyReportService.getTeamReports(page, year, weekNumber, departmentId, startDate,
                endDate, status, keyword));
    }

    @ApiOperation("周报详情")
    @GetMapping("/{id}")
    public Result<WeeklyReport> getById(@PathVariable Long id) {
        return Result.success(weeklyReportService.getById(id));
    }

    @ApiOperation("评分评价")
    @PostMapping("/{id}/review")
    public Result<Void> review(HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody ReviewDTO dto) {
        Long reviewerId = (Long) request.getAttribute("userId");
        weeklyReportService.review(id, dto.getScore(), dto.getComment(), reviewerId);
        weeklyReportService.evictStatsCache();
        return Result.success();
    }

    @Data
    public static class ReviewDTO {
        private BigDecimal score;
        private String comment;
    }
}

