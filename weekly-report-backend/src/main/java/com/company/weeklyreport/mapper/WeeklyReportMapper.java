package com.company.weeklyreport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.weeklyreport.entity.WeeklyReport;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface WeeklyReportMapper extends BaseMapper<WeeklyReport> {

        @Select("SELECT w.*, u.username, d.name as department_name " +
                        "FROM weekly_report w " +
                        "LEFT JOIN sys_user u ON w.user_id = u.id " +
                        "LEFT JOIN sys_department d ON u.department_id = d.id " +
                        "WHERE w.user_id = #{userId} AND w.deleted = 0 " +
                        "ORDER BY w.year DESC, w.week_number DESC")
        List<WeeklyReport> selectByUserId(@Param("userId") Long userId);

        /**
         * 鍥㈤槦鍛ㄦ姤鏌ヨ - 鏀寔澶氭潯浠剁瓫閫?
         */
        IPage<WeeklyReport> selectTeamReports(Page<WeeklyReport> page,
                        @Param("year") Integer year,
                        @Param("weekNumber") Integer weekNumber,
                        @Param("departmentIds") List<Long> departmentIds,
                        @Param("startDate") String startDate,
                        @Param("endDate") String endDate,
                        @Param("status") Integer status,
                        @Param("keyword") String keyword);

        @Select("SELECT " +
                        "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as submitted, " +
                        "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as pending " +
                        "FROM weekly_report WHERE year = #{year} AND week_number = #{weekNumber} AND deleted = 0")
        Map<String, Object> getSubmitStats(@Param("year") Integer year, @Param("weekNumber") Integer weekNumber);
}

