package org.ppj.pp.event.core.entity;

import org.ppj.pp.event.core.eventhandle.EventBus;

public abstract class BaseModel {

    private transient EventBus eventBus = EventBus.INSTANCE;

    protected void apply(Object eventPayload) {

        eventBus.publish(new EventMessage(eventPayload));
    }
}
