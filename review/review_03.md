# Java开发专家面试题及答案 - 模拟面试03
## 面试重点：微服务架构与系统设计

### 一、自我介绍环节

**面试官：** 请介绍一下您在微服务架构方面的经验。

**参考答案：**
您好，我是仰海元，有10年以上的Java开发经验，目前在喜马拉雅担任技术负责人。我在微服务架构方面有丰富的实践经验，主要负责分布式系统架构设计和核心业务系统开发。

我的主要技术栈包括Spring Cloud、Nacos、Gateway、Sentinel等微服务相关技术，在服务拆分、服务治理、分布式事务等方面有深入实践。

核心项目经验包括：
1. **会员精细化运营系统**：采用微服务架构，按业务域拆分服务
2. **内容付费系统**：设计商品、交易、支付、履约等微服务
3. **家庭会员系统**：支持家庭组建、成员管理、权益管理等微服务
4. **Xi-Event异步事件框架**：解决微服务间异步通信问题

### 二、微服务架构设计

#### 2.1 服务拆分原则

**面试官：** 在会员精细化运营系统中，您是如何进行服务拆分的？遵循什么原则？

**参考答案：**

**1. 服务拆分原则：**

**按业务域拆分：**
- **用户服务（User Service）**：用户信息管理、认证授权
- **权益服务（Benefit Service）**：权益定义、发放、使用
- **运营服务（Operation Service）**：运营策略、活动管理
- **通知服务（Notification Service）**：消息推送、通知管理

**2. 拆分考虑因素：**

**高内聚低耦合：**
- 相关功能聚合在一个服务内
- 服务间通过API或事件进行通信
- 避免服务间的强依赖

**数据边界：**
- 每个服务拥有自己的数据存储
- 避免跨服务的数据查询
- 通过事件同步数据状态

**团队边界：**
- 按团队职责划分服务
- 减少跨团队协作成本
- 提高开发效率

**3. 具体实现：**
```java
// 用户服务示例
@Service
public class UserService {
    
    public UserInfo getUserInfo(Long userId) {
        return userMapper.selectById(userId);
    }
    
    public void updateUserInfo(UserInfo userInfo) {
        userMapper.updateById(userInfo);
        // 发布用户信息变更事件
        eventPublisher.publish(new UserInfoChangedEvent(userInfo));
    }
}

// 权益服务示例
@Service
public class BenefitService {
    
    @EventListener
    public void handleUserInfoChanged(UserInfoChangedEvent event) {
        // 处理用户信息变更，更新相关权益
        updateUserBenefits(event.getUserId());
    }
}
```

#### 2.2 服务治理

**面试官：** 在微服务架构中，如何解决服务治理问题？包括服务发现、负载均衡、熔断降级等。

**参考答案：**

**1. 服务发现与注册：**
```java
// 使用Nacos进行服务注册发现
@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}

// 服务调用
@Service
public class BenefitServiceClient {
    
    @Autowired
    private LoadBalancerClient loadBalancerClient;
    
    public UserInfo getUserInfo(Long userId) {
        ServiceInstance instance = loadBalancerClient.choose("user-service");
        String url = instance.getUri() + "/api/user/" + userId;
        return restTemplate.getForObject(url, UserInfo.class);
    }
}
```

**2. 熔断降级：**
```java
@Service
public class UserService {
    
    @HystrixCommand(fallbackMethod = "getUserInfoFallback",
        commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000"),
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50")
        })
    public UserInfo getUserInfo(Long userId) {
        return userMapper.selectById(userId);
    }
    
    public UserInfo getUserInfoFallback(Long userId) {
        // 降级逻辑：返回默认用户信息
        return UserInfo.builder()
            .userId(userId)
            .nickname("用户" + userId)
            .build();
    }
}
```

**3. 限流配置：**
```java
@RestController
public class UserController {
    
    @SentinelResource(value = "getUserInfo", 
        blockHandler = "getUserInfoBlockHandler",
        fallback = "getUserInfoFallback")
    @GetMapping("/api/user/{userId}")
    public UserInfo getUserInfo(@PathVariable Long userId) {
        return userService.getUserInfo(userId);
    }
    
    public UserInfo getUserInfoBlockHandler(Long userId, BlockException ex) {
        // 限流处理逻辑
        return new UserInfo();
    }
    
    public UserInfo getUserInfoFallback(Long userId, Throwable ex) {
        // 降级处理逻辑
        return new UserInfo();
    }
}
```

#### 2.3 分布式事务

**面试官：** 在内容付费系统中，如何解决跨服务的分布式事务问题？

**参考答案：**

**1. 分布式事务场景：**
- 用户下单 -> 扣减库存 -> 创建订单 -> 支付
- 支付成功 -> 更新订单状态 -> 发放权益 -> 发送通知

**2. 解决方案：**

**Saga模式：**
```java
@Service
public class OrderSagaService {
    
    @Transactional
    public void createOrder(OrderRequest request) {
        // 1. 创建订单
        Order order = orderService.createOrder(request);
        
        // 2. 扣减库存
        try {
            inventoryService.deductStock(request.getProductId(), request.getQuantity());
        } catch (Exception e) {
            // 补偿：恢复库存
            inventoryService.restoreStock(request.getProductId(), request.getQuantity());
            throw e;
        }
        
        // 3. 支付
        try {
            paymentService.pay(order.getOrderId(), request.getAmount());
        } catch (Exception e) {
            // 补偿：退款
            paymentService.refund(order.getOrderId());
            throw e;
        }
    }
}
```

**事件驱动模式：**
```java
@Component
public class OrderEventHandler {
    
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        // 异步处理库存扣减
        CompletableFuture.runAsync(() -> {
            try {
                inventoryService.deductStock(event.getProductId(), event.getQuantity());
                eventPublisher.publish(new StockDeductedEvent(event.getOrderId()));
            } catch (Exception e) {
                eventPublisher.publish(new StockDeductFailedEvent(event.getOrderId()));
            }
        });
    }
    
    @EventListener
    public void handleStockDeducted(StockDeductedEvent event) {
        // 处理支付
        try {
            paymentService.pay(event.getOrderId(), event.getAmount());
            eventPublisher.publish(new PaymentSuccessEvent(event.getOrderId()));
        } catch (Exception e) {
            eventPublisher.publish(new PaymentFailedEvent(event.getOrderId()));
        }
    }
}
```

### 三、系统设计题

#### 3.1 设计一个支持千万级用户的会员系统

**面试官：** 设计一个支持千万级用户的会员系统，要求支持实时权益查询、权益发放、权益使用等功能。

**参考答案：**

**1. 系统架构设计：**
```
用户层 -> 网关层 -> 业务服务层 -> 数据层
                ↓
            缓存层(Redis集群)
                ↓
            存储层(MySQL分库分表)
```

**2. 核心服务设计：**

**用户服务：**
- 用户注册、登录、信息管理
- 用户状态缓存，提高查询性能
- 支持用户分片，按用户ID哈希分布

**权益服务：**
- 权益定义、发放、使用、过期管理
- 权益数据分片存储
- 使用Redis缓存热点权益数据

**运营服务：**
- 运营策略配置
- 权益发放规则
- 活动管理

**3. 数据设计：**
```sql
-- 用户表分库分表
CREATE TABLE user_info_${tableIndex} (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    nickname VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    status TINYINT DEFAULT 1,
    created_time DATETIME,
    updated_time DATETIME,
    INDEX idx_user_id (user_id),
    INDEX idx_phone (phone)
) PARTITION BY HASH(user_id) PARTITIONS 16;

-- 权益表
CREATE TABLE user_benefit_${tableIndex} (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    benefit_type VARCHAR(50) NOT NULL,
    benefit_value DECIMAL(10,2),
    expire_time DATETIME,
    status TINYINT DEFAULT 1,
    created_time DATETIME,
    updated_time DATETIME,
    INDEX idx_user_type (user_id, benefit_type),
    INDEX idx_expire (expire_time)
) PARTITION BY HASH(user_id) PARTITIONS 16;
```

**4. 性能优化策略：**

**缓存策略：**
```java
@Service
public class UserCacheService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public UserInfo getUserInfo(Long userId) {
        String key = "user:info:" + userId;
        
        // 先从缓存获取
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return (UserInfo) cached;
        }
        
        // 缓存未命中，从数据库获取
        UserInfo userInfo = userMapper.selectById(userId);
        if (userInfo != null) {
            // 设置缓存，过期时间30分钟
            redisTemplate.opsForValue().set(key, userInfo, 30, TimeUnit.MINUTES);
        }
        
        return userInfo;
    }
}
```

**分库分表策略：**
```java
@Component
public class ShardingStrategy {
    
    public String getTableIndex(Long userId) {
        return String.valueOf(userId % 16);
    }
    
    public String getDataSource(Long userId) {
        return "ds" + (userId % 4);
    }
}
```

#### 3.2 设计一个高并发的秒杀系统

**面试官：** 设计一个支持高并发的秒杀系统，要求防止超卖、保证数据一致性。

**参考答案：**

**1. 系统架构：**
```
用户层 -> 网关层 -> 秒杀服务 -> 库存服务 -> 订单服务
                ↓
            缓存层(Redis集群)
                ↓
            消息队列(Kafka)
```

**2. 核心设计：**

**库存预扣减：**
```java
@Service
public class StockService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public boolean preDeductStock(String productId, int quantity) {
        String key = "stock:" + productId;
        
        // 使用Lua脚本保证原子性
        String script = "if redis.call('get', KEYS[1]) >= tonumber(ARGV[1]) then " +
                       "return redis.call('decrby', KEYS[1], ARGV[1]) " +
                       "else return -1 end";
        
        Long result = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
            Collections.singletonList(key), String.valueOf(quantity));
        
        return result != null && result >= 0;
    }
}
```

**秒杀接口设计：**
```java
@RestController
public class SeckillController {
    
    @PostMapping("/seckill")
    public Result seckill(@RequestBody SeckillRequest request) {
        // 1. 参数校验
        if (!validateRequest(request)) {
            return Result.fail("参数错误");
        }
        
        // 2. 用户限流
        if (!rateLimiter.tryAcquire(request.getUserId())) {
            return Result.fail("请求过于频繁");
        }
        
        // 3. 库存预扣减
        if (!stockService.preDeductStock(request.getProductId(), 1)) {
            return Result.fail("库存不足");
        }
        
        // 4. 异步创建订单
        CompletableFuture.runAsync(() -> {
            try {
                orderService.createOrder(request);
            } catch (Exception e) {
                // 补偿：恢复库存
                stockService.restoreStock(request.getProductId(), 1);
            }
        });
        
        return Result.success("秒杀成功");
    }
}
```

**3. 防超卖措施：**

**数据库层面：**
```sql
-- 使用乐观锁防止超卖
UPDATE product_stock 
SET stock = stock - 1, version = version + 1 
WHERE product_id = ? AND stock > 0 AND version = ?
```

**应用层面：**
```java
@Service
public class SeckillService {
    
    @Transactional
    public boolean seckill(Long userId, Long productId) {
        // 1. 查询商品信息
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() != 1) {
            return false;
        }
        
        // 2. 检查用户是否已购买
        if (orderMapper.existsByUserAndProduct(userId, productId)) {
            return false;
        }
        
        // 3. 扣减库存（乐观锁）
        int updated = productMapper.decreaseStock(productId, product.getVersion());
        if (updated == 0) {
            return false; // 库存不足或版本冲突
        }
        
        // 4. 创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setStatus(OrderStatus.CREATED);
        orderMapper.insert(order);
        
        return true;
    }
}
```

### 四、技术深度考察

#### 4.1 Spring Cloud微服务架构

**面试官：** 请详细介绍一下Spring Cloud微服务架构的核心组件。

**参考答案：**

**1. 核心组件：**

**服务注册与发现（Eureka/Nacos）：**
```java
// 服务注册
@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}

// 服务发现
@Service
public class UserServiceClient {
    
    @Autowired
    private DiscoveryClient discoveryClient;
    
    public List<ServiceInstance> getInstances(String serviceId) {
        return discoveryClient.getInstances(serviceId);
    }
}
```

**配置中心（Config Server/Nacos Config）：**
```yaml
# bootstrap.yml
spring:
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        namespace: dev
        group: DEFAULT_GROUP
        file-extension: yml
```

**网关（Gateway）：**
```java
@Configuration
public class GatewayConfig {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user-service", r -> r
                .path("/api/user/**")
                .filters(f -> f
                    .addRequestHeader("X-Response-Time", System.currentTimeMillis() + "")
                    .retry(3))
                .uri("lb://user-service"))
            .route("benefit-service", r -> r
                .path("/api/benefit/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("benefit-circuit-breaker")
                        .setFallbackUri("forward:/fallback")))
                .uri("lb://benefit-service"))
            .build();
    }
}
```

**熔断器（Hystrix/Sentinel）：**
```java
@Service
public class UserService {
    
    @HystrixCommand(fallbackMethod = "getUserInfoFallback",
        commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000"),
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50"),
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "5000")
        })
    public UserInfo getUserInfo(Long userId) {
        return userMapper.selectById(userId);
    }
    
    public UserInfo getUserInfoFallback(Long userId) {
        return UserInfo.builder()
            .userId(userId)
            .nickname("用户" + userId)
            .build();
    }
}
```

#### 4.2 分布式锁

**面试官：** 在微服务架构中，如何实现分布式锁？有哪些实现方式？

**参考答案：**

**1. Redis分布式锁：**
```java
@Component
public class RedisDistributedLock {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public boolean tryLock(String key, String value, long expireTime) {
        Boolean result = redisTemplate.opsForValue()
            .setIfAbsent(key, value, expireTime, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(result);
    }
    
    public boolean releaseLock(String key, String value) {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                       "return redis.call('del', KEYS[1]) " +
                       "else return 0 end";
        
        Long result = redisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
            Collections.singletonList(key), value);
        
        return Long.valueOf(1).equals(result);
    }
}
```

**2. Zookeeper分布式锁：**
```java
@Component
public class ZookeeperDistributedLock {
    
    @Autowired
    private CuratorFramework client;
    
    public boolean tryLock(String lockPath, long timeout) {
        try {
            InterProcessMutex lock = new InterProcessMutex(client, lockPath);
            return lock.acquire(timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            return false;
        }
    }
    
    public void releaseLock(String lockPath) {
        try {
            InterProcessMutex lock = new InterProcessMutex(client, lockPath);
            if (lock.isAcquiredInThisProcess()) {
                lock.release();
            }
        } catch (Exception e) {
            log.error("Release lock failed", e);
        }
    }
}
```

**3. 数据库分布式锁：**
```java
@Component
public class DatabaseDistributedLock {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public boolean tryLock(String lockKey, String lockValue, long expireTime) {
        try {
            String sql = "INSERT INTO distributed_lock (lock_key, lock_value, expire_time) " +
                        "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE " +
                        "lock_value = IF(expire_time < NOW(), VALUES(lock_value), lock_value), " +
                        "expire_time = IF(expire_time < NOW(), VALUES(expire_time), expire_time)";
            
            int updated = jdbcTemplate.update(sql, lockKey, lockValue, 
                new Timestamp(System.currentTimeMillis() + expireTime));
            
            return updated > 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean releaseLock(String lockKey, String lockValue) {
        String sql = "DELETE FROM distributed_lock WHERE lock_key = ? AND lock_value = ?";
        int deleted = jdbcTemplate.update(sql, lockKey, lockValue);
        return deleted > 0;
    }
}
```

### 五、总结

**面试官：** 您认为微服务架构的核心价值是什么？有哪些挑战？

**参考答案：**

**1. 核心价值：**
- **技术异构性**：不同服务可以使用不同的技术栈
- **独立部署**：服务可以独立开发、测试、部署
- **故障隔离**：单个服务故障不会影响整个系统
- **团队自治**：不同团队可以独立负责不同服务

**2. 主要挑战：**
- **分布式复杂性**：网络延迟、服务发现、负载均衡等
- **数据一致性**：跨服务的数据一致性难以保证
- **运维复杂性**：需要管理更多的服务实例
- **测试复杂性**：需要测试服务间的集成

**3. 最佳实践：**
- 合理进行服务拆分，避免过度拆分
- 建立完善的监控和日志体系
- 使用API网关统一管理服务接口
- 采用事件驱动架构解耦服务依赖 