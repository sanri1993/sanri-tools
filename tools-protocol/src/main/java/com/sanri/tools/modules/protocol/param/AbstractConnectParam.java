package com.sanri.tools.modules.protocol.param;

import lombok.Data;

@Data
public abstract class AbstractConnectParam {
    protected ConnectIdParam connectIdParam;
    protected ConnectParam connectParam;
}
