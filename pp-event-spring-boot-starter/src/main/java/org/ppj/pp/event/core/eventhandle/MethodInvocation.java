package org.ppj.pp.event.core.eventhandle;

import com.alibaba.fastjson.JSONObject;
import org.ppj.pp.event.core.factory.FactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Method;

public class MethodInvocation implements Serializable {
    private transient static final Logger LOGGER = LoggerFactory.getLogger(MethodInvocation.class);
    private static final long serialVersionUID = -7969140711432461165L;

    private transient Method method;

    private Class targetClass;

    private String methodName;

    private Class parameterType;

    private Object args;

    public MethodInvocation(Class targetClass, Method method, Object args) {
        this.targetClass = targetClass;
        this.methodName = method.getName();
        this.parameterType = method.getParameterTypes()[0];
        this.args = args;
        this.method = method;
    }

    public MethodInvocation(Class targetClass, String methodName, Class parameterType, Object args) {
        this.targetClass = targetClass;
        this.methodName = methodName;
        this.parameterType = parameterType;
        this.args = args;
    }

    public Class getParameterType() {
        return parameterType;
    }

    public void setParameterType(Class parameterType) {
        this.parameterType = parameterType;
    }

    public Object getArgs() {
        return args;
    }

    public void setArgs(Object args) {
        this.args = args;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public boolean proceed() {

        Object target = FactoryBuilder.factoryOf(this.getTargetClass()).getInstance();

        try {
            Method method = target.getClass().getMethod(this.methodName, this.getParameterType());
            if (args instanceof JSONObject) {
                method.invoke(target, ((JSONObject) this.args).toJavaObject(parameterType));
            } else {
                method.invoke(target, args);
            }
            return true;
        } catch (Exception e) {
            LOGGER.warn("exception caught when process pp-event method! ", e);
            return false;
        }
    }

    public Method getMethod() {
        return method;
    }
}
