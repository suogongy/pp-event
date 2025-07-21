# pp-event 用户手册

基于spring-boot，mybatis，xxl-job的异步事件框架

## 注意事项
- 因新组件引入xxl-job来实现重试和失败任务调度机制，业务方在使用`pp-event`时，需要单独为业务系统设置http端口
    - 目前的云效支持在`应用`的`概览`设置里，设置应用服务类型。 
    - 若是新服务引用`pp-event`组件，建议直接在初次配置时，直接将`应用服务类型`里的`mainstay3_server`和`web_server`都勾选，然后将系统`server.port`的属性设置为分配的web端口
    - 若非新服务，应用里的服务类型，云效也支持变更

## 用户手册
> 目前仅支持spring-boot项目使用
> 因底层的重试，失败提醒等，是通过xxl-job来实现，需要配置一些xxl-job相关参数。
> 因有一些个性化改造，所以实际需要配置的参数较官方要少，具体参数下面会有说明
> 因内部的事件载体，与业务主事务在一个事务载体，所以需要在业务的mysql数据库里增加一张时间表`PP_EVENT`，具体sql参考下文说明
> 服务引入pp-event并成功启动后，为了后续查看事件重试和失败提示job的运行情况，可以找组件负责人`仰海元`开通账号去调度中心查看。该操作非必须（后续job执行返回结果可能会优化，届时调度中心日志的参考意义会增强）


### 准备工作
- maven引入`pp-event-spring-boot-starter`
   最新版本（2022-12-26）
```
<dependency>
    <groupId>org.ppj.dal</groupId>
    <artifactId>pp-event-spring-boot-starter</artifactId>
    <version>2.0.12.RELEASE</version>
</dependency>
```

### 使用流程
- 在业务的mysql数据库里建表`PP_EVENT`
- 配置运行参数
- 自定义entity，继承 `BaseModel`
- 自定义业务事件 `xxEvent`, 须实现序列化，传递关联异步业务必须的参数
- 对需要关联异步操作的实体entity，内部实现`applyXxEvent`方法，用于发布相关事件  
- 编写 `xxEventHanlder`类，内部实现 `public void handleXx(xxEvent)`方法，处理需要异步操作的业务逻辑。对应方法
需要添加`EventHandler`注解
- 需要关联异步操作的主业务操作需要在事务内，并且主业务数据在执行`crud`前，调用相关entity通过内部实现的`applyXxEvent`方法，用于实际发布事件

`PP_EVENT`建表语句：
```sql
CREATE TABLE `PP_EVENT`
(
    `id`                        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `event_no`                  varchar(50)   DEFAULT NULL,
    `status`                    int(11)       DEFAULT NULL,
    `retried_count`             int(11)       DEFAULT NULL,
    `method_invocation_content` varchar(1000) DEFAULT NULL,
    `create_time`               datetime      DEFAULT NULL,
    `update_time`               datetime      DEFAULT NULL,
    `version`                   int(11)       DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
```

### 其他说明
可个性化定制的参数
```
server:
  # http服务端口，必填。组件集成的xxl-job执行器模块，需要提供http接口与调度中心交互
  port: 8080
spring:
  application:
    # 应用名称，必填。用于在任务调度中心区分不同业务方
    name:pp-event-sample
xievent:
  # 选填，默认100。事件重试和失败事件提醒时，单次查询事件记录的页大小
  pageSize: 100
  # 选填，默认36。事件最大重试次数
  retryThreshold: 36
  # 选填，默认30。事件重试job的执行间隔
  recoverJobPeriodInSeconds: 30
  # 选填，默认120。失败事件提醒job的执行间隔
  failedEventWarnJobPeriodInSeconds: 120
  job:
    # 选填，默认：system。建议设置为项目owner，方便找到相关负责人
    author: rudy.yang
    group:
      # xxl-job执行器标题。选填。默认为：应用名称（对应spring.application.name属性值），建议设置为中文应用名称
      title: VC声音转化任务执行器
```

## 案例参考 `pp-event-sample`
### pom依赖引入
```
<dependency>
    <groupId>org.ppj.dal</groupId>
    <artifactId>pp-event-spring-boot-starter</artifactId>
    <version>2.0.12.RELEASE</version>
</dependency>
```

### 实现自定义实体类 User
继承框架内的`BaseModel`
```java
public class User extends BaseModel {

    private Long id;
    private Long number;
    private String name;
    private Integer age;
    private Integer sex;
    private Date joinTime;
    //此处省略构造函数，getter、setter等
}
```
### 实现自定义事件
用于传递异步业务操作需要的参数等
```java
public class UserCreatedEvent implements Serializable {
    private long number;
    private String name;
    private int age;
    private int sex;
    //此处省略构造函数，getter、setter等
}
```
### 在相关实体内实现应用（发布）自定义事件的方法
一般是在需要触发异步操作的实体内定义
```java
public class User extends BaseModel {

   //其他内容易忽略

   public void applyCreateEvent() {
       //此处的 number，name等属性，一般为该实体的属性，特殊case也可以通过方法的入参传递，仅用于异步操作需要
      this.apply(new UserCreatedEvent(number, name, age, sex));
   }
}
```

### 实际应用（发布）自定义事件
该操作需在与业务事务操作的一个事务内，因为需要同步保存事件到数据库，需共用事务
```java
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Transactional
    public int save(long number,String name,int age,int sex) {

        User user = new User(number,name,age,sex);
        //此处调用事件应用（发布）方法，需与关联的业务操作（此处为新的User记录的插入）在一个事务内
        user.applyCreateEvent();

        return userMapper.insert(user);
    }
}
```
### 自定义事件处理器
用于异步地处理业务操作，内部实现原理简要说明：
- 系统识别出封装异步业务操作的方法，并关联入参（用户自定义的事件，暂时仅支持一个参数的处理），封装成`XiEvent`，并随同步业务逻辑的事务一起保存
- 系统将封装好的XiEvent相关信息，传递给消息队列disruptor
   - disruptor会根据XiEvent相关信息，通过反射调用自定义事件处理器的相关方法
   - 如果调用成功，则删除数据库里对应的 XiEvent，整个流程结束
   - 如果调用失败，则会有job定期遍历数据库中状态为`1（表示待处理）`的事件，并通过反射调用事件处理器方法以进行异步业务操作
   - 如果经最大重试次数后依然失败，则有定期job发现相关事件，提醒业务系统相关负责人去处理
     - 目前的提醒机制比较简单，就是通过打印 ERROR 级别的日志
       - 后期考虑接入更有针对性的钉钉通知机制
     - 目前恢复操作，需要通过执行sql，将相关XiEvent记录的状态重置为 1，并且重试次数设置为 0
       - 目前执行sql需要提交后由dba审核，比较低效和麻烦，后期需考虑使用更简易的方式处理
    
自定义事件处理器参考
```java
@Component
@Slf4j
public class UserEventHandler {

    @EventHandler
    public void handleUserCreatedEvent(UserCreatedEvent userCreatedEvent) {
        log.info("handleUserCreatedEvent: {}", JSON.toJSONString(userCreatedEvent));
    }

    @EventHandler
    public void alsoHandleUserCreatedEvent(UserCreatedEvent userCreatedEvent) {
        log.info("alsoHandleUserCreatedEvent: {}", JSON.toJSONString(userCreatedEvent));
    }
}
```