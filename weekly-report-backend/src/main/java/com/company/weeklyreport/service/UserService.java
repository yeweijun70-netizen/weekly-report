package com.company.weeklyreport.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.weeklyreport.config.RedisCacheConfig;
import com.company.weeklyreport.entity.Department;
import com.company.weeklyreport.entity.Role;
import com.company.weeklyreport.entity.User;
import com.company.weeklyreport.mapper.DepartmentMapper;
import com.company.weeklyreport.mapper.RoleMapper;
import com.company.weeklyreport.mapper.UserMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private RoleMapper roleMapper;

    public User login(String email, String password) {
        if (email == null || email.trim().isEmpty()) return null;
        String emailNorm = email.trim().toLowerCase();
        User user = baseMapper.selectByEmail(emailNorm);
        if (user == null) return null;
        String encryptedPassword = DigestUtil.md5Hex(password);
        if (!encryptedPassword.equals(user.getPassword())) return null;
        if (user.getStatus() == null || user.getStatus() != 1) return null;
        return user;
    }

    @Cacheable(value = RedisCacheConfig.CACHE_USER_INFO, key = "#id")
    public User getUserWithInfo(Long id) {
        return baseMapper.selectUserWithInfo(id);
    }

    @CacheEvict(value = RedisCacheConfig.CACHE_USER_INFO, key = "#id")
    public void evictUserCache(Long id) {
    }

    public IPage<User> pageList(Page<User> page, String keyword, Long departmentId, Long roleId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0);
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword).or().like(User::getEmail, keyword));
        }
        if (departmentId != null) {
            List<Long> departmentIds = new ArrayList<>();
            collectDepartmentIds(departmentId, departmentIds);
            wrapper.in(User::getDepartmentId, departmentIds);
        }
        if (roleId != null) {
            wrapper.eq(User::getRoleId, roleId);
        }
        wrapper.orderByDesc(User::getId);
        IPage<User> result = this.page(page, wrapper);
        fillDepartmentAndRoleName(result.getRecords());
        return result;
    }

    private void fillDepartmentAndRoleName(List<User> users) {
        if (users == null || users.isEmpty()) return;
        List<Long> deptIds = users.stream().map(User::getDepartmentId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<Long> roleIds = users.stream().map(User::getRoleId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, String> deptNames = deptIds.isEmpty() ? Collections.emptyMap() : departmentMapper.selectBatchIds(deptIds).stream().collect(Collectors.toMap(Department::getId, Department::getName, (a, b) -> a));
        Map<Long, String> roleNames = roleIds.isEmpty() ? Collections.emptyMap() : roleMapper.selectBatchIds(roleIds).stream().collect(Collectors.toMap(Role::getId, Role::getName, (a, b) -> a));
        for (User u : users) {
            if (u.getDepartmentId() != null) u.setDepartmentName(deptNames.get(u.getDepartmentId()));
            if (u.getRoleId() != null) u.setRoleName(roleNames.get(u.getRoleId()));
        }
    }

    /**
     * 递归收集部门及其所有子部门ID
     */
    private void collectDepartmentIds(Long parentId, List<Long> ids) {
        ids.add(parentId);
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getParentId, parentId)
                .eq(Department::getDeleted, 0);
        List<Department> children = departmentMapper.selectList(wrapper);
        for (Department child : children) {
            collectDepartmentIds(child.getId(), ids);
        }
    }

    public boolean saveUser(User user) {
        if (user.getId() == null) {
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                throw new IllegalArgumentException("密码不能为空");
            }
            user.setPassword(DigestUtil.md5Hex(user.getPassword()));
        }
        return this.saveOrUpdate(user);
    }

    public boolean checkEmailExists(String email, Long excludeId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        if (excludeId != null) {
            wrapper.ne(User::getId, excludeId);
        }
        return this.count(wrapper) > 0;
    }

    /**
     * 注册新用户：校验邮箱唯一，密码 MD5 加密，默认角色为员工(roleId=3)，状态启用
     */
    public User register(String username, String email, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("密码不能少于6位");
        }
        String emailNorm = email.trim().toLowerCase();
        if (checkEmailExists(emailNorm, null)) {
            throw new IllegalArgumentException("该邮箱已被注册");
        }
        User user = new User();
        user.setUsername(username.trim());
        user.setEmail(emailNorm);
        user.setPassword(DigestUtil.md5Hex(password));
        user.setRoleId(3L);   // 默认角色：员工
        user.setDepartmentId(null);
        user.setStatus(1);
        this.save(user);
        return user;
    }
}

