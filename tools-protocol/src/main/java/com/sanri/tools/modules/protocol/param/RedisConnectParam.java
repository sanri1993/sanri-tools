package com.sanri.tools.modules.protocol.param;

import lombok.Data;

@Data
public class RedisConnectParam {
    private ConnectIdParam connectIdParam;
    private ConnectParam connectParam;
    private AuthParam authParam;
}
