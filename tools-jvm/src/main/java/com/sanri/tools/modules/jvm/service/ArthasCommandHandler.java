package com.sanri.tools.modules.jvm.service;

import com.sanri.tools.modules.jvm.service.dtos.CommandResultContext;

public interface ArthasCommandHandler {

    /**
     * 命令结果处理
     * @param commandResultContext
     */
    void process(CommandResultContext commandResultContext);
}
