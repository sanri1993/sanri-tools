package com.sanri.tools.modules.core.service.connect;

import org.springframework.context.ApplicationEvent;

/**
 * 第二版连接管理, 连接更新事件
 * 传入对象为 ConnectInput
 */
public class UpdateSecurityConnectEvent extends ApplicationEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public UpdateSecurityConnectEvent(Object source) {
        super(source);
    }
}
