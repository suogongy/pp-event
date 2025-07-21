package org.ppj.pp.event.sample.domain.eventhandlers;

import com.alibaba.fastjson.JSON;
import org.ppj.pp.event.core.eventhandle.annotation.EventHandler;
import org.ppj.pp.event.sample.domain.event.UserCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserEventHandler {

    @EventHandler
    public void handleUserCreatedEvent(UserCreatedEvent userCreatedEvent) {
        log.info("handleUserCreatedEvent: {}", JSON.toJSONString(userCreatedEvent));
    }

    @EventHandler
    public void handleUserCreatedEvent2(UserCreatedEvent userCreatedEvent) {
        log.info("handleUserCreatedEvent2: {}", JSON.toJSONString(userCreatedEvent));
//        throw new RuntimeException("handleUserCreatedEvent2 error");
    }

    @EventHandler
    public void handleUserCreatedEvent3(UserCreatedEvent userCreatedEvent) {
        log.info("handleUserCreatedEvent3: {}", JSON.toJSONString(userCreatedEvent));
//        throw new RuntimeException("handleUserCreatedEvent3 error");
    }
}
