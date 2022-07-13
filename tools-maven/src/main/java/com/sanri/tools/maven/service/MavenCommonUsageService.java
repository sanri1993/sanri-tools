package com.sanri.tools.maven.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sanri.tools.maven.service.dtos.GoalExecuteResult;
import org.apache.commons.io.FileUtils;
import org.apache.maven.shared.invoker.InvocationResult;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sanri.tools.maven.service.dtos.ExecuteMavenPluginParam;

import lombok.extern.slf4j.Slf4j;

/**
 * maven 模块分析, 主要是需要得到编译顺序
 */
@Service
@Slf4j
public class MavenCommonUsageService {
    @Autowired
    private MavenPluginService mavenPluginService;

    /**
     * 执行常用命令  clean , compile , package , install ,deploy
     * @param settingsName
     * @param pomFile
     * @return
     */
    public GoalExecuteResult command(String settingsName, File pomFile, String goal) throws MavenInvocationException, XmlPullParserException, IOException, ExecutionException, InterruptedException {
        final ExecuteMavenPluginParam param = new ExecuteMavenPluginParam(pomFile, goal);
        return mavenPluginService.executeMavenPluginGoals(settingsName, param);
    }

    /**
     * 模块顺序分析, 执行 validate 命令
     * @param settingsName
     * @param pomFile
     */
    public List<String> validate(String settingsName, File pomFile) throws MavenInvocationException, XmlPullParserException, IOException, InterruptedException, ExecutionException, TimeoutException {
        final ExecuteMavenPluginParam validate = new ExecuteMavenPluginParam(pomFile, "validate");
        final GoalExecuteResult goalExecuteResult = mavenPluginService.executeMavenPluginGoals(settingsName, validate);

        // 实测 1s 不到可以执行完毕, 在 40 个模块的项目, 所以只等待 5s
        final Future<InvocationResult> invocationResultFuture = goalExecuteResult.getInvocationResultFuture();
        final InvocationResult invocationResult = invocationResultFuture.get(5, TimeUnit.SECONDS);
        final File out = goalExecuteResult.getMavenExecuteLogFiles().getOut();
        return MavenLogModuleOrderParser.parse(out);
    }

    /**
     * 在 maven 的执行日志中, 找到模块顺序
     */
    public static final class MavenLogModuleOrderParser{

        public static List<String> parse(File logFile) throws IOException {
            List<String> orders = new ArrayList<>();
            final List<String> logs = FileUtils.readLines(logFile, StandardCharsets.UTF_8);
            boolean start = false;
            for (String line : logs) {
                if (line.contains("Reactor Build Order")){
                    start = true;
                    continue;
                }
                if (start && line.contains("---------------")){
                    break;
                }

                if (start){
                    final String[] split = StringUtils.split(line);
                    if (split.length == 3){
                        orders.add(StringUtils.trim(split[1]));
                    }
                }
            }
            return orders;
        }
    }
}
