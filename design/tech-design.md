# PP-Event 异步事件框架技术设计方案

## 1. 项目概述

PP-Event是一个基于Spring Boot、MyBatis和XXL-Job的异步事件框架，用于处理业务系统中的异步操作和事件驱动架构。该框架提供了完整的事件发布、存储、异步处理、重试机制和监控管理功能。

### 1.1 核心特性
- **事务一致性**: 事件发布与业务事务强绑定，确保数据一致性
- **异步处理**: 基于Disruptor高性能消息队列实现异步事件处理
- **失败重试**: 集成XXL-Job实现失败事件的自动重试机制
- **监控管理**: 提供完整的事件状态监控和管理界面
- **高可用**: 支持分布式部署和集群管理

### 1.2 模块架构
- **pp-event-spring-boot-starter**: 核心框架模块
- **pp-event-sample**: 示例应用模块
- **pp-event-control-center**: 调度中心管理模块

## 2. 系统架构设计

### 2.1 整体架构图

```mermaid
graph TB
    subgraph BusinessLayer ["业务应用层"]
        BA["业务应用"] --> BM["BaseModel"]
        BM -->|"apply(event)"| EM["EventMessage"]
    end

    subgraph EventProcessingLayer ["事件处理层"]
        EB["EventBus"] -->|"注册监听器"| EL["EventListener"]
        EL -->|"匹配处理器"| MI["MethodInvocation"]
        MI -->|"创建事件"| PE["PPEvent"]

        subgraph AsyncProcessing ["异步处理模块"]
            AMI["AsyncMethodInvoker"] --> AD["AsyncDisruptor"]
            AD --> AWH["AsyncWorkHandler"]
            AWH --> MI
        end

        subgraph EventHandlerModule ["事件处理器模块"]
            EHC["EventHandler注解"] --> EHM["EventHandler方法"]
            EHM --> PEH["PPEventHandler"]
        end
    end

    subgraph StorageLayer ["存储层"]
        DB[("MySQL数据库")]
        PP_Mapper["PPEventMapper"] -->|"CRUD操作"| DB
    end

    subgraph RetrySchedulingLayer ["重试调度层"]
        XXL["XXL-Job调度中心"] -->|"定时任务"| ERJ["EventHandleRecoverJob"]
        ERJ -->|"查询失败事件"| DB
        ERJ -->|"重新处理"| PEH
        FWJ["FailedEventWarnJob"] -->|"失败通知"| DA["DingDingAlert"]
    end

    subgraph MonitoringLayer ["监控管理层"]
        CC["Control Center"] -->|"HTTP API"| PC["PPEventController"]
        CC -->|"任务管理"| XJC["XxlJobClientController"]
        PC -->|"事件查询"| DB
        XJC -->|"任务调度"| XXL
    end

    subgraph ConfigurationLayer ["配置层"]
        PCF["PPEventAutoConfiguration"] --> EP["PPEventProperties"]
        PFI["PPEventFrameworkInitializer"] -->|"框架初始化"| EB
    end

    %% 主要连接关系
    BM --> EB
    EB -->|"同步存储"| PP_Mapper

    %% 事务同步机制
    EB -->|"事务提交后触发"| AMI

    %% 事件处理流程
    AMI --> AD
    AWH --> EHM
    EHM --> PEH
    PEH -->|"更新状态"| PP_Mapper

    %% 重试流程
    ERJ --> PEH

    %% 监控管理连接
    PC --> BusinessLayer
    PC --> StorageLayer
```

### 2.2 架构说明

该架构采用分层设计，包含以下核心层次：

1. **业务应用层 (BusinessLayer)**:
   - 业务实体继承BaseModel基类
   - 通过apply(event)方法发布EventMessage事件消息
   - 与业务逻辑紧密集成，支持事务一致性

2. **事件处理层 (EventProcessingLayer)**:
   - **EventBus事件总线**: 单例模式，负责事件的订阅、发布和路由
   - **EventListener监听器**: 自动扫描@EventHandler注解，匹配事件处理器
   - **异步处理模块**: AsyncMethodInvoker + AsyncDisruptor实现高性能队列处理
   - **事件处理器模块**: 基于注解的事件处理器注册和执行

3. **存储层 (StorageLayer)**:
   - MySQL数据库持久化存储PP_EVENT事件表
   - PPEventMapper提供CRUD操作
   - 支持事件状态的完整生命周期管理

4. **重试调度层 (RetrySchedulingLayer)**:
   - 集成XXL-Job分布式任务调度中心
   - EventHandleRecoverJob负责失败事件的自动重试
   - FailedEventWarnJob提供失败告警通知机制

5. **监控管理层 (MonitoringLayer)**:
   - Control Center提供统一的管理界面
   - PPEventController提供事件查询和管理API
   - XxlJobClientController管理任务调度

6. **配置层 (ConfigurationLayer)**:
   - PPEventAutoConfiguration自动配置框架组件
   - PPEventProperties管理配置参数
   - PPEventFrameworkInitializer负责框架初始化

### 2.3 核心流程说明

**事件发布流程**:
1. 业务代码调用BaseModel.apply(event)发布事件
2. EventBus接收EventMessage并匹配对应的EventListener
3. 创建MethodInvocation和PPEvent实体
4. 同步存储到数据库，注册事务同步回调
5. 事务提交后触发AsyncMethodInvoker异步处理

**异步处理流程**:
1. AsyncMethodInvoker将事件提交到AsyncDisruptor队列
2. AsyncWorkHandler从队列中消费事件
3. 调用实际的事件处理器方法
4. PPEventHandler更新事件状态并处理结果

**失败重试流程**:
1. XXL-Job定时触发EventHandleRecoverJob
2. 查询超时和失败的事件记录
3. 重新执行事件处理逻辑
4. 超过重试阈值则标记为失败并告警

## 3. 核心类设计

### 3.1 核心类图

```mermaid
classDiagram
    class BaseModel {
        <<abstract>>
        -EventBus eventBus
        +apply(Object eventPayload)
    }
    
    class EventBus {
        <<singleton>>
        -Set~EventListener~ listeners
        +publish(EventMessage eventMessage)
        +subscribe(EventListener eventListener)
        -handle(EventListener, EventMessage) List~PPEvent~
    }
    
    class PPEvent {
        -Long id
        -String eventNo
        -Integer status
        -Integer retriedCount
        -String methodInvocationContent
        -MethodInvocation methodInvocation
        -Date createTime
        -Date updateTime
        -Integer version
        +markAsDoing()
        +markAsFailed()
        +resetAsTrying()
        +isTrying() boolean
    }
    
    class EventMessage {
        -Object eventPayload
    }
    
    class EventListener {
        <<interface>>
        +matchHandler(EventMessage) List~MethodInvocation~
        +getTargetType() Class
    }
    
    class PPEventHandler {
        -PPEventMapper ppEventMapper
        +handleEvent(PPEvent ppEvent)
        +handleEvent(Long ppEventId)
    }
    
    class AsyncMethodInvoker {
        <<singleton>>
        +invoke(MethodInvocation, Long)
        -handleRingBufferFull(AsyncEventTranslator)
        +shutdown()
    }
    
    class AsyncDisruptor {
        <<static>>
        -Map~Method, Disruptor~ disruptorMap
        +ensureStart(Method)
        +stop(long, TimeUnit) boolean
        +tryPublish(AsyncEventTranslator) boolean
    }
    
    class MethodInvocation {
        -Class targetClass
        -Method method
        -Object[] args
        +proceed() boolean
    }
    
    class EventHandler {
        <<annotation>>
    }
    
    BaseModel --> EventBus : uses
    EventBus --> EventMessage : processes
    EventBus --> PPEvent : creates
    EventBus --> EventListener : notifies
    PPEventHandler --> PPEvent : handles
    AsyncMethodInvoker --> AsyncDisruptor : uses
    AsyncDisruptor --> MethodInvocation : processes
    EventListener --> MethodInvocation : creates
    MethodInvocation --> EventHandler : annotated by
```

### 3.2 类关系说明

**核心类的职责和关系**:

- **BaseModel**: 所有业务实体的基类，提供事件发布能力
- **EventBus**: 单例事件总线，负责事件的订阅、发布和路由
- **PPEvent**: 事件实体类，包含事件状态、重试次数等元信息
- **EventListener**: 事件监听器接口，负责匹配事件处理器
- **PPEventHandler**: 事件处理器，实际执行事件处理逻辑
- **AsyncMethodInvoker**: 异步方法调用器，将事件提交到Disruptor队列
- **AsyncDisruptor**: 基于LMAX Disruptor的高性能队列实现
- **MethodInvocation**: 方法调用封装，包含目标类、方法和参数
- **EventHandler**: 标注事件处理方法的注解

## 4. 事件处理流程设计

### 4.1 事件发布与处理时序图

```mermaid
sequenceDiagram
    participant User as 用户/业务代码
    participant BM as BaseModel
    participant EB as EventBus
    participant EL as EventListener
    participant DB as 数据库
    participant TX as 事务管理器
    participant AM as AsyncMethodInvoker
    participant AD as AsyncDisruptor
    participant AWH as AsyncWorkHandler
    participant MI as MethodInvocation
    participant EH as EventHandler方法
    participant PEH as PPEventHandler

    User->>BM: 调用业务方法
    BM->>BM: apply(eventPayload)
    BM->>EB: publish(eventMessage)

    EB->>EL: 遍历所有监听器
    EL->>EL: matchHandler(eventMessage)
    EL->>EB: 返回MethodInvocation列表
    EB->>PE: 创建PPEvent实例
    EB->>DB: 批量插入PPEvent记录

    EB->>TX: 注册事务同步
    TX-->>EB: 事务提交后回调

    EB->>AM: invoke(methodInvocation, eventId)
    AM->>AD: ensureStart(method)
    AD->>AD: 创建Disruptor实例
    AM->>AD: tryPublish(event)

    AD->>AWH: 分发事件到工作线程
    AWH->>MI: proceed()
    MI->>EH: 调用事件处理方法
    EH->>PEH: handleEvent(ppEvent)

    PEH->>DB: 更新事件状态为DOING
    PEH->>EH: 执行业务逻辑
    alt 执行成功
        PEH->>DB: 删除事件记录
    else 执行失败
        PEH->>DB: 重置为TRYING状态
    end
```

### 4.2 失败重试处理时序图

```mermaid
sequenceDiagram
    participant XXL as XXL-Job调度中心
    participant ERJ as EventHandleRecoverJob
    participant DB as 数据库
    participant EMP as EventMethodProcessor
    participant PEH as PPEventHandler
    participant EH as EventHandler方法
    participant FWJ as FailedEventWarnJob

    Note over XXL: 定时调度触发
    XXL->>ERJ: eventHandleRecoverJobHandler()
    ERJ->>ERJ: doRecoverEvent()

    loop 分页查询失败事件
        ERJ->>DB: 查询TRYING状态且超时的事件
        DB-->>ERJ: 返回事件列表

        par 并发处理
            ERJ->>EMP: handle(ppEvent)
            EMP->>PEH: handleEvent(ppEvent)

            alt 重试次数未超限
                PEH->>EH: 重新执行事件处理
                alt 执行成功
                    PEH->>DB: 删除事件记录
                else 执行失败
                    PEH->>DB: 更新重试次数+1
                end
            else 重试次数超限
                PEH->>DB: 标记为FAILED状态
                PEH->>FWJ: 触发失败告警
            end
        end
    end
```

## 5. 数据模型设计

### 5.1 事件状态机

```mermaid
stateDiagram-v2
    [*] --> TRYING: 事件创建
    TRYING --> DOING: 开始处理
    DOING --> TRYING: 处理失败(可重试)
    DOING --> [*]: 处理成功
    DOING --> FAILED: 处理失败(不可重试)
    TRYING --> FAILED: 超过重试阈值
    FAILED --> TRYING: 手动重置
```

### 5.2 数据库表设计

**PP_EVENT表结构**:
- `id`: 主键，自增长
- `event_no`: 事件唯一编号
- `status`: 事件状态（0-TRYING, 1-DOING, 2-FAILED）
- `retried_count`: 重试次数
- `method_invocation_content`: 方法调用信息(JSON格式)
- `create_time`: 创建时间
- `update_time`: 更新时间
- `version`: 版本号（乐观锁）

## 6. 配置与部署设计

### 6.1 配置参数设计

```mermaid
graph LR
    subgraph "应用配置"
        SP[server.port] 
        SAN[spring.application.name]
    end
    
    subgraph "PP-Event配置"
        PS[pageSize: 100]
        RT[retryThreshold: 36]
        RJ[recoverJobPeriod: 30s]
        FW[failedWarnJobPeriod: 120s]
    end
    
    subgraph "XXL-Job配置"
        JA[author]
        GT[group.title]
        AC[admin.addresses]
        AT[accessToken]
    end
    
    subgraph "告警配置"
        DD[dingding.webhook]
        DA[dingding.atMobiles]
    end
```

### 6.2 部署架构

```mermaid
graph TB
    subgraph "负载均衡层"
        LB[Load Balancer]
    end
    
    subgraph "应用集群"
        APP1[App Instance 1]
        APP2[App Instance 2]
        APP3[App Instance N]
    end
    
    subgraph "XXL-Job集群"
        XXL1[XXL-Job Admin 1]
        XXL2[XXL-Job Admin 2]
    end
    
    subgraph "数据库集群"
        DB_M[MySQL Master]
        DB_S[MySQL Slave]
    end
    
    subgraph "监控中心"
        CC[Control Center]
    end
    
    LB --> APP1
    LB --> APP2
    LB --> APP3
    
    APP1 --> XXL1
    APP2 --> XXL1
    APP3 --> XXL2
    
    APP1 --> DB_M
    APP2 --> DB_M
    APP3 --> DB_M
    
    DB_M --> DB_S
    
    CC --> APP1
    CC --> APP2
    CC --> APP3
    
    CC --> XXL1
    CC --> XXL2
```

## 7. 性能优化设计

### 7.1 Disruptor高性能队列

```mermaid
graph TB
    subgraph "Disruptor环形缓冲区"
        RB[Ring Buffer<br/>4096 slots]
        P1[Producer 1]
        P2[Producer 2]
        PN[Producer N]
        W1[Worker 1]
        W2[Worker 2]
        WN[Worker N]
    end
    
    P1 --> RB
    P2 --> RB
    PN --> RB
    
    RB --> W1
    RB --> W2
    RB --> WN
    
    Note[多生产者多消费者模式<br/>无锁设计<br/>缓存行优化]
```

### 7.2 批量处理优化

- **批量插入**: 事件以20条为一批进行数据库插入
- **分页查询**: 重试任务分页查询避免内存溢出
- **并发处理**: 使用线程池并发处理失败事件

## 8. 监控与管理设计

### 8.1 监控指标

```mermaid
graph LR
    subgraph "业务指标"
        EC[事件总数]
        ES[事件成功率]
        ERT[平均响应时间]
    end
    
    subgraph "系统指标"
        QB[队列长度]
        TC[线程池状态]
        DBU[数据库连接]
    end
    
    subgraph "告警指标"
        FE[失败事件数]
        RT[重试次数]
        DT[处理延迟]
    end
```

### 8.2 管理功能

- **事件查询**: 支持按应用、状态、时间等条件查询
- **事件重置**: 手动重置失败事件为重试状态
- **事件删除**: 清理不需要的失败事件
- **批量操作**: 支持批量重置和删除操作
- **实时监控**: 实时显示事件处理状态和统计信息

## 9. 异常处理设计

### 9.1 异常处理策略

```mermaid
graph TD
    EH[事件处理异常] --> CT{异常类型}
    
    CT --> |业务异常| BE[标记失败]
    CT --> |系统异常| SE[重试处理]
    CT --> |超时异常| TE[增加重试次数]
    
    BE --> RC{重试次数检查}
    SE --> RC
    TE --> RC
    
    RC --> |未超限| RT[重新调度]
    RC --> |已超限| FA[标记失败]
    
    RT --> XXL[XXL-Job重试]
    FA --> AL[发送告警]
```

### 9.2 容错机制

- **乐观锁**: 使用version字段防止并发更新冲突
- **幂等性**: 事件处理保证幂等性，支持重复执行
- **降级策略**: 队列满时丢弃事件并记录日志
- **熔断机制**: 连续失败时暂停处理并告警

## 10. 扩展性设计

### 10.1 事件处理器扩展

```mermaid
graph TB
    subgraph "事件处理器扩展"
        EBA[EventHandler注解]
        EL[EventListener接口]
        MEP[方法事件处理器]
        AEP[异步事件处理器]
        BEP[批量事件处理器]
    end
    
    EBA --> MEP
    EL --> AEP
    EL --> BEP
```

### 10.2 存储扩展

- **存储适配**: 支持不同的数据库存储（MySQL、PostgreSQL等）
- **分库分表**: 支持事件表的水平拆分
- **归档策略**: 支持历史事件的归档和清理

## 11. 安全设计

### 11.1 权限控制

- **访问控制**: Control Center提供用户认证和权限管理
- **API安全**: HTTP接口提供Token认证
- **数据隔离**: 不同应用的事件数据相互隔离

### 11.2 数据安全

- **敏感数据**: 事件数据支持加密存储
- **审计日志**: 记录所有事件操作的审计日志
- **传输安全**: 支持HTTPS传输加密

## 12. 总结

PP-Event框架通过精心设计的架构和实现，提供了高性能、高可用的异步事件处理能力。主要优势包括：

1. **高性能**: 基于Disruptor的无锁队列设计，支持高并发事件处理
2. **一致性**: 事务驱动的发布机制确保事件与业务数据的一致性
3. **可靠性**: 完善的重试机制和异常处理保证事件最终一致性
4. **可观测**: 丰富的监控指标和管理界面支持运维管理
5. **扩展性**: 模块化设计支持功能扩展和定制化需求

该框架适用于需要异步处理、事件驱动架构的业务系统，特别适合订单处理、消息通知、数据同步等场景。