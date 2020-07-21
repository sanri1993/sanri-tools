package com.sanri.tools.modules.protocol.param;

import lombok.Data;

@Data
public class RedisConnectParam extends AbstractConnectParam {
    private AuthParam authParam;
}
