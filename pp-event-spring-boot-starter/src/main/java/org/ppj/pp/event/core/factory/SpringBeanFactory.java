package org.ppj.pp.event.core.factory;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SpringBeanFactory implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public boolean isFactoryOf(Class clazz) {
        Map map = this.applicationContext.getBeansOfType(clazz);
        return map.size() > 0;
    }

    public <T> T getBean(Class<T> var1) {
        return this.applicationContext.getBean(var1);
    }

    public <T> T getBean(String name, Class<T> var1) {
        return this.applicationContext.getBean(name, var1);
    }
}
