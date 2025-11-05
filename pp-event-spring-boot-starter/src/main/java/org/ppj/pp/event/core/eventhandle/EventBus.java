package org.ppj.pp.event.core.eventhandle;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.ppj.pp.event.core.async.AsyncMethodInvoker;
import org.ppj.pp.event.core.entity.EventMessage;
import org.ppj.pp.event.core.entity.PPEvent;
import org.ppj.pp.event.core.factory.FactoryBuilder;
import org.ppj.pp.event.core.mapper.PPEventMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ppj.pp.event.core.eventhandle.EventListener;
import org.ppj.pp.event.core.eventhandle.MethodInvocation;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class EventBus {

    public static final EventBus INSTANCE = new EventBus();
    private static final int BATCH_INSERT_SIZE = 20;
    private static final Logger logger = LoggerFactory.getLogger(EventBus.class.getSimpleName());
    private final Set<EventListener> listeners = new CopyOnWriteArraySet<EventListener>();

    public void publish(EventMessage eventMessage) {

        PPEventMapper ppEventMapper = FactoryBuilder.factoryOf(PPEventMapper.class).getInstance();

        List<PPEvent> ppEvents = Lists.newArrayList();

        for (EventListener eventListener : listeners) {
            ppEvents.addAll(handle(eventListener, eventMessage));
        }

        List<List<PPEvent>> ppEventsList = Lists.partition(ppEvents, BATCH_INSERT_SIZE);

        ppEventsList.stream().forEach(ppEventMapper::batchInsert);
    }

    public void subscribe(EventListener eventListener) {

        for (EventListener listener : listeners) {
            EventListener thisListener = listener;
            EventListener thatListener = eventListener;
            if (thisListener.getTargetType().equals(thatListener.getTargetType())) {
                return;
            }
        }

        listeners.add(eventListener);
    }

    private List<PPEvent> handle(EventListener eventListener, EventMessage eventMessage) {

        List<MethodInvocation> methodInvocations = eventListener.matchHandler(eventMessage);

        List<PPEvent> ppEvents = Lists.newArrayList();

        for (MethodInvocation methodInvocation : methodInvocations) {
            ppEvents.add(new PPEvent(methodInvocation));
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {

                for (PPEvent ppEvent : ppEvents) {

                    logger.info("async handle ppEvent: {}", JSON.toJSONString(ppEvent));

                    AsyncMethodInvoker asyncMethodInvoker = AsyncMethodInvoker.getInstance();
                    asyncMethodInvoker.invoke(ppEvent.getMethodInvocation(), ppEvent.getId());
                }
            }
        });

        return ppEvents;
    }
}
