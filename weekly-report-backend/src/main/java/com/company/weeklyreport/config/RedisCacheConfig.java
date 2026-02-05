package com.company.weeklyreport.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 缓存配置：仅当 app.cache.redis=true 时启用；默认使用 SimpleCacheConfig 内存缓存。
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "app.cache.redis", havingValue = "true")
public class RedisCacheConfig {

    /** 部门树缓存：变更少，1 小时 */
    public static final String CACHE_DEPARTMENT_TREE = "department:tree";
    /** 角色列表缓存：变更少，1 小时 */
    public static final String CACHE_ROLE_LIST = "role:list";
    /** 角色详情缓存：按 id，1 小时 */
    public static final String CACHE_ROLE_DETAIL = "role:detail";
    /** 用户信息缓存：按 id，30 分钟 */
    public static final String CACHE_USER_INFO = "user:info";
    /** 统计-提交率：按年周，5 分钟 */
    public static final String CACHE_STATS_SUBMIT_RATE = "stats:submitRate";
    /** 统计-评分趋势：5 分钟 */
    public static final String CACHE_STATS_SCORE_TREND = "stats:scoreTrend";

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(om);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put(CACHE_DEPARTMENT_TREE, defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigs.put(CACHE_ROLE_LIST, defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigs.put(CACHE_ROLE_DETAIL, defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigs.put(CACHE_USER_INFO, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigs.put(CACHE_STATS_SUBMIT_RATE, defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put(CACHE_STATS_SCORE_TREND, defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}
