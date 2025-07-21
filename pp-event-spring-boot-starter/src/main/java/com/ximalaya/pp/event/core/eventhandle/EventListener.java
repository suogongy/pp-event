package org.ppj.pp.event.core.eventhandle;

import com.google.common.collect.Lists;
import org.ppj.pp.event.core.entity.EventMessage;
import org.ppj.pp.event.core.eventhandle.annotation.EventHandler;
import org.ppj.pp.event.core.utils.EventHandlerUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class EventListener {

    private final Object target;
    private List<Method> methods = null;

    public List<Method> getMethods() {
        return methods;
    }

    public EventListener(Object target) {
        this.target = target;
        this.methods = methodsOf(target.getClass());
    }

    private static List<Method> methodsOf(Class<?> clazz) {

        List<Method> methods = new LinkedList<Method>();

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getAnnotation(EventHandler.class) != null) {
                methods.add(method);
            }
        }

        return Collections.unmodifiableList(methods);
    }

    public List<MethodInvocation> matchHandler(EventMessage eventMessage) {

        List<MethodInvocation> methodInvocations = Lists.newArrayList();

        for (Method method : methods) {

            if (EventHandlerUtils.verifyEventHandler(method)) {

                Type type = method.getGenericParameterTypes()[0];

                if (EventHandlerUtils.isTypeEqual(type, eventMessage.getPayloadType())) {

                    MethodInvocation methodInvocation = new MethodInvocation(
                            this.target.getClass(),
                            method,
                            eventMessage.getPayload());
                    methodInvocations.add(methodInvocation);
                }
            }
        }
        return methodInvocations;
    }

    public Class<?> getTargetType() {
        return target.getClass();
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode += null == this.target ? 0 : this.target.hashCode() * 31;
        return hashCode;
    }

    public boolean equals(Object other) {

        if (null == other) {
            return false;
        }

        if (this == other) {
            return true;
        }

        if (!this.getClass().equals(other.getClass())) {
            return false;
        }

        if (this.target == null) {
            return false;
        }

        EventListener that = (EventListener) other;
        return this.target.equals(that.target);
    }
}
