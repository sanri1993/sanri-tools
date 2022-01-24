package com.sanri.tools.modules.jvm.service.handlers;

import com.sanri.tools.modules.jvm.service.ArthasCommandHandler;
import com.sanri.tools.modules.jvm.service.dtos.CommandResultContext;

public class NoHandleCommandHandler  implements ArthasCommandHandler {
    @Override
    public void process(CommandResultContext commandResultContext) {
        commandResultContext.setResult(commandResultContext.getOrigin());
    }
}
