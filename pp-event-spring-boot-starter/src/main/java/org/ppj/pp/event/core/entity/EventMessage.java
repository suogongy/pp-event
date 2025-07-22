package org.ppj.pp.event.core.entity;

import lombok.Data;

@Data
public class EventMessage<T> {
    private T payload;

    public EventMessage() {
    }

    public EventMessage(T payload) {
        this.payload = payload;
    }

    public Class getPayloadType() {
        return payload.getClass();
    }

    public Object getPayload() {
        return payload;
    }
}
