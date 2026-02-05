package com.company.weeklyreport.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;

/**
 * 内存缓存：当未启用 Redis 时使用，避免因 Redis 不可用导致 500。
 * 默认启用（app.cache.redis != true）；需 Redis 时在 application.yml 设置 app.cache.redis: true。
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "app.cache.redis", havingValue = "false", matchIfMissing = true)
public class SimpleCacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager manager = new ConcurrentMapCacheManager();
        manager.setCacheNames(Arrays.asList(
                RedisCacheConfig.CACHE_DEPARTMENT_TREE,
                RedisCacheConfig.CACHE_ROLE_LIST,
                RedisCacheConfig.CACHE_ROLE_DETAIL,
                RedisCacheConfig.CACHE_USER_INFO,
                RedisCacheConfig.CACHE_STATS_SUBMIT_RATE,
                RedisCacheConfig.CACHE_STATS_SCORE_TREND
        ));
        return manager;
    }
}
