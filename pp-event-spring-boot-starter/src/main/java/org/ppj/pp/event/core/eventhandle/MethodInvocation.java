package org.ppj.pp.event.core.eventhandle;

import com.alibaba.fastjson.JSONObject;
import org.ppj.pp.event.core.factory.SpringBeanFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.reflect.Method;

@Component
public class MethodInvocation implements Serializable, ApplicationContextAware {
    private transient static final Logger LOGGER = LoggerFactory.getLogger(MethodInvocation.class);
    private static final long serialVersionUID = -7969140711432461165L;

    private static ApplicationContext applicationContext;

    private transient Method method;

    private Class targetClass;

    private String methodName;

    private Class parameterType;

    private Object args;

    private Object convertedArgs;

    public MethodInvocation(Class targetClass, Method method, Object args) {
        this.targetClass = targetClass;
        this.methodName = method.getName();
        this.parameterType = method.getParameterTypes()[0];
        this.args = args;
        this.method = method;
        this.convertedArgs = convertArgument(args, parameterType);
    }

    public MethodInvocation(Class targetClass, String methodName, Class parameterType, Object args) {
        this.targetClass = targetClass;
        this.methodName = methodName;
        this.parameterType = parameterType;
        this.args = args;
        this.convertedArgs = convertArgument(args, parameterType);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
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

    private Object convertArgument(Object args, Class parameterType) {
        if (args == null) {
            return null;
        }

        if (args instanceof JSONObject && parameterType != null) {
            try {
                return ((JSONObject) args).toJavaObject(parameterType);
            } catch (Exception e) {
                LOGGER.warn("Failed to convert JSONObject to type {}: {}", parameterType.getName(), e.getMessage());
                return args;
            }
        }

        return args;
    }

    public boolean proceed() {
        if (applicationContext == null) {
            LOGGER.error("ApplicationContext is not initialized");
            return false;
        }

        Object target;
        try {
            target = applicationContext.getBean(this.getTargetClass());
            if (target == null) {
                LOGGER.error("Target bean not found for class: {}", this.getTargetClass().getName());
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to get target bean for class {}: {}", this.getTargetClass().getName(), e.getMessage());
            return false;
        }

        try {
            Method methodToInvoke = this.method;
            if (methodToInvoke == null) {
                methodToInvoke = target.getClass().getMethod(this.methodName, this.getParameterType());
                this.method = methodToInvoke;
            }

            Object argumentToUse = this.convertedArgs != null ? this.convertedArgs : this.args;
            methodToInvoke.invoke(target, argumentToUse);
            return true;
        } catch (NoSuchMethodException e) {
            LOGGER.error("Method {} not found in class {}: {}",
                this.methodName, this.getTargetClass().getName(), e.getMessage());
            return false;
        } catch (IllegalAccessException e) {
            LOGGER.error("No access to method {} in class {}: {}",
                this.methodName, this.getTargetClass().getName(), e.getMessage());
            return false;
        } catch (Exception e) {
            LOGGER.error("Exception caught when invoking method {} on class {}: {}",
                this.methodName, this.getTargetClass().getName(), e.getMessage(), e);
            return false;
        }
    }

    public Method getMethod() {
        return method;
    }

    public Object getConvertedArgs() {
        return convertedArgs;
    }
}
