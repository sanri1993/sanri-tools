package com.sanri.tools.maven.service.dtos;

import com.google.common.util.concurrent.ListenableFuture;
import com.sanri.tools.maven.service.MavenPluginService;
import lombok.Data;
import org.apache.maven.shared.invoker.InvocationResult;

import java.util.concurrent.Future;

@Data
public class GoalExecuteResult {
    private ListenableFuture<InvocationResult> invocationResultFuture;
    private MavenPluginService.MavenExecuteLogFiles mavenExecuteLogFiles;

    public GoalExecuteResult() {
    }

    public GoalExecuteResult(ListenableFuture<InvocationResult> invocationResultFuture, MavenPluginService.MavenExecuteLogFiles mavenExecuteLogFiles) {
        this.invocationResultFuture = invocationResultFuture;
        this.mavenExecuteLogFiles = mavenExecuteLogFiles;
    }
}
