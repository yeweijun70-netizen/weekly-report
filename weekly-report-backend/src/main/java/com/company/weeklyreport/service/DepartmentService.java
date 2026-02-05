package com.company.weeklyreport.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.company.weeklyreport.config.RedisCacheConfig;
import com.company.weeklyreport.entity.Department;
import com.company.weeklyreport.mapper.DepartmentMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService extends ServiceImpl<DepartmentMapper, Department> {

    @Cacheable(value = RedisCacheConfig.CACHE_DEPARTMENT_TREE, key = "'tree'")
    public List<Department> getTree() {
        List<Department> all = this.list();
        return buildTree(all, 0L);
    }

    @CacheEvict(value = RedisCacheConfig.CACHE_DEPARTMENT_TREE, allEntries = true)
    public void evictDepartmentCache() {
    }

    private List<Department> buildTree(List<Department> all, Long parentId) {
        return all.stream()
                .filter(d -> parentId.equals(d.getParentId()))
                .peek(d -> d.setChildren(buildTree(all, d.getId())))
                .collect(Collectors.toList());
    }
}

