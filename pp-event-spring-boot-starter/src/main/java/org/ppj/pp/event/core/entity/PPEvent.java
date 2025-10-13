package org.ppj.pp.event.core.entity;

import com.alibaba.fastjson.JSON;
import org.ppj.pp.event.core.enums.PPEventStatus;
import org.ppj.pp.event.core.eventhandle.MethodInvocation;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
public class PPEvent implements Serializable {

    private static final long serialVersionUID = 6069959414023532600L;

    private long id;
    private String eventNo;
    private int status;
    private int retriedCount;
    private String methodInvocationContent;
    private MethodInvocation methodInvocation;
    private Date createTime = new Date();
    private Date updateTime = new Date();
    private int version = 1;

    public PPEvent() {
    }

    public PPEvent(MethodInvocation methodInvocation) {
        this.eventNo = UUID.randomUUID().toString();
        this.status = PPEventStatus.TRYING.getId();
        this.methodInvocation = methodInvocation;
        this.retriedCount = 0;
    }

    public String getRawMethodInvocationContent() {
        return methodInvocationContent;
    }

    public String getMethodInvocationContent() {
        return JSON.toJSONString(methodInvocation);
    }

    public MethodInvocation getMethodInvocation() {

        if (!Objects.isNull(methodInvocation)) {
            return methodInvocation;
        }

        return JSON.parseObject(methodInvocationContent, MethodInvocation.class);
    }

    public boolean isTrying() {
        return PPEventStatus.TRYING.getId().equals(status);
    }

    public void markAsDoing() {

        if (!Objects.equals(this.status, PPEventStatus.TRYING.getId())) {
            throw new RuntimeException("event is in illegal status");
        }

        this.status = PPEventStatus.DOING.getId();
        updateTime = new Date();
        version++;
    }

    public void markAsFailed() {
        this.status = PPEventStatus.FAILED.getId();
        updateTime = new Date();
        version++;
    }

    public void resetAsTrying() {
        this.status = PPEventStatus.TRYING.getId();
        retriedCount++;
        updateTime = new Date();
        version++;
    }

    public void reset() {
        this.status = PPEventStatus.TRYING.getId();
        this.retriedCount = 0;
        updateTime = new Date();
        version++;
    }
}
