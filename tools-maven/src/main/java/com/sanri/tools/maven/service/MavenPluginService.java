package com.sanri.tools.maven.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.settings.Settings;
import org.apache.maven.shared.invoker.*;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sanri.tools.maven.service.dtos.ExecuteMavenPluginParam;
import com.sanri.tools.maven.service.dtos.GoalExecuteResult;
import com.sanri.tools.modules.core.service.NamedThreadFactory;
import com.sanri.tools.modules.core.service.connect.ConnectService;
import com.sanri.tools.modules.core.utils.OnlyPath;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * maven 插件执行, 使用 maven-invoke
 */
@Service
@Slf4j
public class MavenPluginService {

    @Autowired
    private MavenSettingsResolve mavenSettingsResolve;
    @Autowired
    private LocalMavenManager mavenManager;
    @Autowired
    private GoalExecuteLogManager goalExecuteLogManager;
    @Autowired
    private ConnectService connectService;

    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1,10,0,TimeUnit.SECONDS,new ArrayBlockingQueue<>(100),new NamedThreadFactory("mavenGoalExecutePool"));

    /**
     * 使用这个线程池, 以便监听任务
     */
    private ListeningExecutorService listeningExecutorService = MoreExecutors.listeningDecorator(threadPoolExecutor);

    /**
     * 执行 maven 插件目标的调用, 并将结果输出到文件, 用户需要自己监听文件变更得到 maven 执行日志 <br/>
     * @param settingsName
     * @param executeMavenPluginParam
     */
    public GoalExecuteResult executeMavenPluginGoals(String settingsName, ExecuteMavenPluginParam executeMavenPluginParam) throws IOException, XmlPullParserException, MavenInvocationException {
        final List<String> goals = executeMavenPluginParam.getGoals();
        final MavenExecuteLogFiles newLogFiles = goalExecuteLogManager.createNewLogFiles();
        File settingsFile = connectService.connectFile("maven", settingsName);
        String command = "mvn -s "+ settingsFile.getAbsolutePath() + " -f "+ executeMavenPluginParam.getPomFile()+ " -Dmaven.test.skip=true " + StringUtils.join(goals," ");
        log.info("执行 maven 插件命令: mvn {}, 输出到文件: {}, 错误输出到文件: {}", command,newLogFiles.getOut(),newLogFiles.getErr());
        FileUtils.writeStringToFile(newLogFiles.getOut(),command+"\n");

        // 文件输出日志收集器
        final FileCollectOutputHandler fileCollectOutputHandler = new FileCollectOutputHandler(newLogFiles.getOut());
        final FileCollectOutputHandler fileCollectErrHandler = new FileCollectOutputHandler(newLogFiles.getErr());

        // 获取 settings 设置
        final Settings settings = mavenSettingsResolve.parseSettings(settingsName);
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile( executeMavenPluginParam.getPomFile());
        request.setGoals( goals );
        request.setOutputHandler(fileCollectOutputHandler);
        request.setErrorHandler(fileCollectErrHandler);
        request.setUserSettingsFile(mavenSettingsResolve.settingsFile(settingsName));
        request.setLocalRepositoryDirectory(new File(settings.getLocalRepository()));
        request.setInputStream(null);

        if (executeMavenPluginParam.isSkipTest()){
            // 跳过测试
            Properties properties = request.getProperties();
            if (properties == null){
                properties = new Properties();
                request.setProperties(properties);
            }
            properties.setProperty("maven.test.skip","true");
        }

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(mavenManager.getMavenHome());
//        invoker.setOutputHandler(fileCollectOutputHandler);
//        invoker.setErrorHandler(fileCollectErrHandler);
        invoker.setLocalRepositoryDirectory(new File(settings.getLocalRepository()));
        final MavenExecutor mavenExecutor = new MavenExecutor(invoker, request);
        final ListenableFuture<InvocationResult> invocationResultFuture = listeningExecutorService.submit(mavenExecutor);
        return new GoalExecuteResult(invocationResultFuture,newLogFiles);
    }

    public static final class MavenExecutor implements Callable<InvocationResult>{
        private Invoker invoker;
        private InvocationRequest request;

        public MavenExecutor(Invoker invoker, InvocationRequest request) {
            this.invoker = invoker;
            this.request = request;
        }

        @Override
        public InvocationResult call() throws Exception {
            InvocationResult execute = invoker.execute(request);
            final FileCollectOutputHandler outputHandler = (FileCollectOutputHandler) request.getOutputHandler(null);
            outputHandler.flushAndClose();
            final FileCollectOutputHandler errorHandler = (FileCollectOutputHandler) request.getErrorHandler(null);
            errorHandler.flushAndClose();
            return execute;
        }
    }

    @Getter
    public static final class MavenExecuteLogFiles {
        @JsonIgnore
        private File out;
        @JsonIgnore
        private File err;
        @JsonIgnore
        private File baseDir;

        public MavenExecuteLogFiles(File baseDir,File out, File err) {
            this.baseDir = baseDir;
            this.out = out;
            this.err = err;
        }

        @Override
        public String toString() {
            return "out: "+ out.getName() +" err:"+err.getName();
        }

        public String getOutFilePath(){
            return new OnlyPath(baseDir).relativize(new OnlyPath(out)).toString();
        }

        public String getErrFilePath(){
            return new OnlyPath(baseDir).relativize(new OnlyPath(err)).toString();
        }
    }

    /**
     * 使用文件收集 maven 插件执行日志
     */
    public static final class FileCollectOutputHandler implements InvocationOutputHandler {
        private File out;

        private BufferedWriter bufferedWriter;

        public FileCollectOutputHandler(File out) throws FileNotFoundException {
            this.out = out;
            assert out != null;

            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out,true), StandardCharsets.UTF_8));
        }

        @Override
        public void consumeLine(String line) throws IOException {
            bufferedWriter.write(line);
            bufferedWriter.write("\n");
        }

        public void flushAndClose() throws IOException {
            bufferedWriter.flush();
            bufferedWriter.close();
        }
    }
}
