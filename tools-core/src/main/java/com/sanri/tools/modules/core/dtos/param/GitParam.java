package com.sanri.tools.modules.core.dtos.param;

import lombok.Data;

@Data
public class GitParam extends AbstractConnectParam{
    private AuthParam authParam;
    private String sshKey;
}
