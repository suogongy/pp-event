package org.ppj.pp.event.core.async;

import com.alibaba.fastjson.JSON;
import org.ppj.pp.event.core.eventhandle.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class AsyncMethodInvoker {

    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncMethodInvoker.class);

    private static volatile AsyncMethodInvoker INSTANCE = null;

    private AsyncMethodInvoker() {

    }

    public static AsyncMethodInvoker getInstance() {

        if (INSTANCE == null) {

            synchronized (AsyncMethodInvoker.class) {

                if (INSTANCE == null) {
                    INSTANCE = new AsyncMethodInvoker();
                }
            }
        }

        return INSTANCE;
    }

    public void invoke(MethodInvocation methodInvocation, Long PPEventId) {

        AsyncDisruptor.ensureStart(methodInvocation.getMethod());

        try (AsyncEventTranslator eventTranslator = new AsyncEventTranslator(
                methodInvocation,
                PPEventId)
        ) {

            if (!AsyncDisruptor.tryPublish(eventTranslator)) {
                LOGGER.info(String.format("pp-event ring buffer is full, eventHandler will be executed according your QueueFullPolicy, %s.%s",
                        methodInvocation.getTargetClass().getSimpleName(),
                        methodInvocation.getMethodName()
                        )
                );
                handleRingBufferFull(eventTranslator);
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void handleRingBufferFull(AsyncEventTranslator eventTranslator) {

        LOGGER.warn("ring buffer is full. async handle is cancel. eventInfo: {}", JSON.toJSONString(eventTranslator.getMethodInvocation()));

    }

    public void shutdown() {
        AsyncDisruptor.stop(60, TimeUnit.SECONDS);
    }
}
