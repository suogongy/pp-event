package org.ppj.pp.event.core.utils;

import org.ppj.pp.event.core.eventhandle.annotation.EventHandler;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

public class EventHandlerUtils {

    public static boolean isTypeEqual(Type type, Class targetClass) {
        return type.equals(targetClass);
    }

    public static boolean verifyEventHandler(Method method) {

        EventHandler eventHandler = method.getAnnotation(EventHandler.class);

        if (eventHandler != null) {

            Class[] paramClasses = method.getParameterTypes();

            if (paramClasses == null || paramClasses.length > 1) {
                throw new RuntimeException(String.format("Invalid method parameter count, must only one parameter! Class:%s, method:%s, parameter count:%d",
                        method.getClass().getName(),
                        method.getName(),
                        paramClasses == null ? 0 : paramClasses.length));
            }

            if (Map.class.isAssignableFrom(paramClasses[0])) {
                throw new RuntimeException(String.format("Invalid method parameter type, should be normal class type, or Collection type! Class:%s, method:%s, parameter type:%s",
                        method.getClass().getName(),
                        method.getName(),
                        paramClasses[0].getName()));
            }

            return true;
        }

        return false;
    }
}
