package com.company.weeklyreport.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.weeklyreport.config.RedisCacheConfig;
import com.company.weeklyreport.entity.Role;
import com.company.weeklyreport.mapper.RoleMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService extends ServiceImpl<RoleMapper, Role> {

    @Cacheable(value = RedisCacheConfig.CACHE_ROLE_LIST, key = "'list'")
    public List<Role> listAll() {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Role::getId);
        return this.list(wrapper);
    }

    @Cacheable(value = RedisCacheConfig.CACHE_ROLE_DETAIL, key = "#id")
    public Role getRoleWithCount(Long id) {
        return baseMapper.selectRoleWithCount(id);
    }

    @CacheEvict(value = { RedisCacheConfig.CACHE_ROLE_LIST, RedisCacheConfig.CACHE_ROLE_DETAIL }, allEntries = true)
    public void evictRoleCache() {
    }

    public boolean checkCodeExists(String code, Long excludeId) {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getCode, code);
        if (excludeId != null) {
            wrapper.ne(Role::getId, excludeId);
        }
        return this.count(wrapper) > 0;
    }
}

