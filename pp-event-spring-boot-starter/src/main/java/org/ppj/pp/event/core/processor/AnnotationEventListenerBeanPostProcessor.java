package org.ppj.pp.event.core.processor;

import org.ppj.pp.event.core.async.AsyncDisruptor;
import org.ppj.pp.event.core.async.AsyncMethodInvoker;
import org.ppj.pp.event.core.eventhandle.EventBus;
import org.ppj.pp.event.core.eventhandle.EventListener;
import org.ppj.pp.event.core.eventhandle.annotation.EventHandler;
import org.ppj.pp.event.core.register.RegisterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import java.lang.reflect.Method;

public class AnnotationEventListenerBeanPostProcessor implements BeanPostProcessor, ApplicationContextAware, ApplicationListener<ContextClosedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationEventListenerBeanPostProcessor.class);

    private EventBus eventBus = EventBus.INSTANCE;

    private ApplicationContext applicationContext;

    public AnnotationEventListenerBeanPostProcessor() {
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        Class<?> targetClass = bean.getClass();
        final ClassLoader classLoader = targetClass.getClassLoader();

        if (hasEventHandlerMethod(targetClass)) {
            final EventListener eventListener = createEventListener(bean, true, classLoader);
            subscribe(eventListener);

            for (Method method : eventListener.getMethods()) {
                AsyncDisruptor.ensureStart(method);
            }
        }

        return bean;
    }

    private void subscribe(EventListener proxy) {
        eventBus.subscribe(proxy);
    }

    private EventListener createEventListener(Object annotatedHandler, boolean proxyTargetClass, ClassLoader classLoader) {
        return new EventListener(annotatedHandler);
    }

    private boolean hasEventHandlerMethod(Class<?> beanClass) {
        for (Method method : beanClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(EventHandler.class)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {

        AsyncMethodInvoker asyncMethodInvoker = AsyncMethodInvoker.getInstance();

        logger.info("pp-event-framework shutdown...");

        asyncMethodInvoker.shutdown();

        logger.info("pp-event-framework shutdowned");

    }
}
