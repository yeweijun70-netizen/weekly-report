package com.company.weeklyreport.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.weeklyreport.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT u.*, d.name as department_name, r.name as role_name " +
            "FROM sys_user u " +
            "LEFT JOIN sys_department d ON u.department_id = d.id " +
            "LEFT JOIN sys_role r ON u.role_id = r.id " +
            "WHERE u.email = #{email} AND u.deleted = 0")
    User selectByEmail(@Param("email") String email);

    @Select("SELECT u.*, d.name as department_name, r.name as role_name " +
            "FROM sys_user u " +
            "LEFT JOIN sys_department d ON u.department_id = d.id " +
            "LEFT JOIN sys_role r ON u.role_id = r.id " +
            "WHERE u.id = #{id} AND u.deleted = 0")
    User selectUserWithInfo(@Param("id") Long id);
}
