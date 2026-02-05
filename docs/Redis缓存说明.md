# Redis 缓存说明

## 一、依赖与配置

- **依赖**：`spring-boot-starter-cache` + `spring-boot-starter-data-redis`（已在 pom.xml 中）
- **配置**：`application.yml` 中的 `spring.redis`（host、port、password、database、lettuce 连接池）
- **启用方式**：通过 `@EnableCaching` 和自定义 `CacheManager` 使用 Redis 作为缓存存储

## 二、缓存项与 TTL

| 缓存名称 | 说明 | TTL | 失效时机 |
|----------|------|-----|----------|
| department:tree | 部门树 | 1 小时 | 部门新增/修改/删除 |
| role:list | 角色列表 | 1 小时 | 角色新增/修改/删除/权限更新 |
| role:detail | 角色详情（按 id） | 1 小时 | 同上 |
| user:info | 用户信息（按 id） | 30 分钟 | 该用户新增/修改/删除 |
| stats:submitRate | 提交率统计（按年_周） | 5 分钟 | 周报提交/评分后清空 |

## 三、代码位置

- **配置类**：`com.company.weeklyreport.config.RedisCacheConfig`  
  - 定义缓存名常量、CacheManager Bean、各缓存 TTL 与 JSON 序列化
- **使用缓存**：  
  - `DepartmentService.getTree()`：`@Cacheable`  
  - `DepartmentService.evictDepartmentCache()`：`@CacheEvict`，在部门保存/删除后由 Controller 调用  
  - `RoleService.listAll()` / `getRoleWithCount(id)`：`@Cacheable`  
  - `RoleService.evictRoleCache()`：在角色保存/权限更新/删除后调用  
  - `UserService.getUserWithInfo(id)`：`@Cacheable`  
  - `UserService.evictUserCache(id)`：在用户保存/更新/删除后调用  
  - `WeeklyReportService.getSubmitStats()`：`@Cacheable`  
  - `WeeklyReportService.evictStatsCache()`：在周报提交/评分后调用  

## 四、运行要求

- 启动前请确保 **Redis 已启动**，且 `application.yml` 中的连接信息正确，否则应用启动会报错。
- 若暂时不用 Redis，可注释掉 `RedisCacheConfig` 上的 `@EnableCaching` 或排除 Redis 自动配置，并去掉各 Service 中的 `@Cacheable` / `@CacheEvict`，否则需保留可用 Redis。
