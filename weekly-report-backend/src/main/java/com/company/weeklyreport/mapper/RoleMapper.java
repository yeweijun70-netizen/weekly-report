package com.company.weeklyreport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.weeklyreport.entity.Role;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface RoleMapper extends BaseMapper<Role> {

    @Select("SELECT r.*, (SELECT COUNT(*) FROM sys_user u WHERE u.role_id = r.id AND u.deleted = 0) as user_count " +
            "FROM sys_role r WHERE r.id = #{id} AND r.deleted = 0")
    Role selectRoleWithCount(@Param("id") Long id);
}

