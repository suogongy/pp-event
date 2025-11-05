package org.ppj.pp.event.core.factory;

public interface BeanFactory {

    <T> T getBean(Class<T> clazz);

    <T> boolean isFactoryOf(Class<T> clazz);

    <T> T getBean(String name, Class<T> clazz);
}
