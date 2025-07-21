package org.ppj.pp.event.sample.domain.eventhandlers;

import com.alibaba.fastjson.JSON;
import org.ppj.pp.event.core.eventhandle.annotation.EventHandler;
import org.ppj.pp.event.sample.domain.event.UserCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserEventMessageHandler {

    @EventHandler
    public void handleMessage(UserCreatedEvent userCreatedEvent) {
        log.info("handleMessage: {}", JSON.toJSONString(userCreatedEvent));

//        throw new RuntimeException("handleMessage error");
    }

    @EventHandler
    public void handleMessage2(UserCreatedEvent userCreatedEvent) {
        log.info("handleMessage2: {}", JSON.toJSONString(userCreatedEvent));
    }
}
