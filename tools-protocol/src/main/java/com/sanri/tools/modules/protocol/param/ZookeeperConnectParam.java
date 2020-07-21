package com.sanri.tools.modules.protocol.param;

import lombok.Data;

@Data
public class ZookeeperConnectParam  extends AbstractConnectParam{
    public static final int DEFAULT_SESSION_TIMEOUT = 30000;
    public static final int DEFAULT_CONNECTION_TIMEOUT = 5000;

    private int sessionTimeout = DEFAULT_SESSION_TIMEOUT;
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
}
