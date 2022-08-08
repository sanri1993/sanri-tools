package com.sanri.tools.modules.core.service.connect.events;

import org.springframework.context.ApplicationEvent;

public class SecurityConnectEvent extends ApplicationEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public SecurityConnectEvent(Object source) {
        super(source);
    }
}
