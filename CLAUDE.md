# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

pp-event是一个基于Spring Boot、MyBatis和XXL-Job的异步事件框架，用于处理业务系统中的异步操作和事件驱动架构。

## Project Structure

The project consists of 4 main modules:

- **pp-event-spring-boot-starter**: 核心框架模块，提供事件处理、异步执行、重试机制等核心功能
- **pp-event-sample**: 示例应用，展示如何使用pp-event框架
- **pp-event-sample-api**: 示例应用的API定义，包含Thrift接口
- **pp-event-control-center**: 调度中心，基于XXL-Job的任务调度管理界面

## Build Commands

### Maven Commands
```bash
# Build entire project
mvn clean install

# Build specific module
mvn clean install -pl pp-event-spring-boot-starter

# Run tests
mvn test

# Skip tests during build
mvn clean install -DskipTests

# Package application
mvn clean package
```

### Running Applications
```bash
# Run sample application
java -jar pp-event-sample/target/pp-event-sample.jar

# Run control center
java -jar pp-event-control-center/target/pp-event-control-center.jar
```

## Architecture Overview

### Core Components

1. **Event Processing Pipeline**:
   - 事件发布：业务实体通过`apply()`方法发布事件
   - 事件存储：事件同步存储到数据库的`PP_EVENT`表
   - 异步处理：通过Disruptor消息队列进行异步事件处理
   - 重试机制：失败事件通过XXL-Job进行重试

2. **Key Classes**:
   - `BaseModel`: 所有业务实体的基类，提供事件发布能力
   - `PPEvent`: 事件载体，包含事件信息和处理方法
   - `@EventHandler`: 标记事件处理方法的注解
   - `EventHandleRecoverJob`: 事件恢复重试任务
   - `FailedEventWarnJob`: 失败事件告警任务

3. **Database Schema**:
   - `PP_EVENT`: 存储待处理的事件记录
   - `xxl_job_*`: XXL-Job调度相关表

### Configuration

Required configuration in `application.yml`:
```yaml
server:
  port: 8080  # HTTP服务端口，必填

spring:
  application:
    name: your-app-name  # 应用名称，必填

PPEvent:
  pageSize: 100  # 单次查询事件记录的页大小
  retryThreshold: 36  # 事件最大重试次数
  recoverJobPeriodInSeconds: 30  # 重试job执行间隔
  failedEventWarnJobPeriodInSeconds: 120  # 失败告警job执行间隔
  job:
    author: your-name  # 任务负责人
    group:
      title: 中文应用名称  # 执行器标题
```

## Development Workflow

### Adding New Event Handlers

1. 创建自定义事件类（实现`Serializable`）
2. 在业务实体中实现`applyXxEvent()`方法
3. 创建事件处理器类，使用`@EventHandler`注解标记处理方法
4. 在业务事务中调用`applyXxEvent()`发布事件

### Testing

- 单元测试位于各模块的`src/test/java`目录
- 测试类继承`AbstractTestCase`获取测试基础配置
- 使用`@SpringBootTest`进行集成测试

### Database Setup

1. 在业务数据库中创建`PP_EVENT`表（DDL位于`pp-event-spring-boot-starter/src/main/doc/ddl.sql`）
2. 配置XXL-Job相关数据库表（位于`pp-event-control-center/src/main/doc/xxl_job.sql`）

## Important Notes

- 所有事件处理必须在业务事务内完成
- 事件处理器方法只能有一个参数（事件对象）
- 框架集成了XXL-Job用于失败重试和任务调度
- 需要为业务系统单独配置HTTP端口用于XXL-Job执行器通信
- 支持钉钉告警通知（需配置相关参数）

## Common Issues

1. **端口冲突**: 确保server.port配置正确且未被占用
2. **数据库连接**: 检查PP_EVENT表是否正确创建
3. **XXL-Job配置**: 确保调度中心地址和访问令牌配置正确
4. **事务管理**: 事件发布必须在@Transactional注解的方法内执行