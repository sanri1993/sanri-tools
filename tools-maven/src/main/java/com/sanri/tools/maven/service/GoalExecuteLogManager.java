package com.sanri.tools.maven.service;

import com.sanri.tools.maven.service.dtos.GoalExecuteLog;
import com.sanri.tools.modules.core.dtos.PageResponseDto;
import com.sanri.tools.modules.core.dtos.param.PageParam;
import com.sanri.tools.modules.core.security.UserService;
import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.core.utils.OnlyPath;
import com.sanri.tools.modules.core.utils.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.sanri.tools.maven.service.dtos.GoalExecuteLog.TIME_FORMAT;

@Service
@Slf4j
public class GoalExecuteLogManager {

    @Autowired(required = false)
    private UserService userService;
    @Autowired
    private FileManager fileManager;

    /**
     * 读取一个日志文件
     * @param logPath
     * @return
     * @throws IOException
     */
    public String readLog(String logPath) throws IOException {
        final File file = mavenLogDir();
        final File logfile = new File(file, logPath);
        if (logfile.exists()) {
            return FileUtils.readFileToString(logfile, StandardCharsets.UTF_8);
        }
        return null;
    }

    /**
     * 获取日志文件
     * @param logPath
     * @return
     */
    public File logFile(String logPath){
        final File file = mavenLogDir();
        return new File(file, logPath);
    }

    /**
     * 分页加载日志列表
     * @param PageParam
     * @return
     */
    public PageResponseDto<List<GoalExecuteLog>> listByPage(PageParam pageParam){
        final File mavenLogDir = mavenLogDir();
        IOFileFilter fileFilter = new WildcardFileFilter("*out.log");
        final Collection<File> listFiles = FileUtils.listFiles(mavenLogDir, fileFilter, TrueFileFilter.INSTANCE);
        final List<GoalExecuteLog> collect = listFiles.stream().map(file -> {
            final GoalExecuteLog goalExecuteLog = new GoalExecuteLog(file);
            final OnlyPath relativize = new OnlyPath(mavenLogDir).relativize(new OnlyPath(file));
            goalExecuteLog.setRelativePath(relativize.toString());
            goalExecuteLog.setUsername(mavenLogDir.getName());
            goalExecuteLog.setFileSize(file.length());
            return goalExecuteLog;
        }).collect(Collectors.toList());

        final List<GoalExecuteLog> goalExecuteLogs = PageUtil.splitListByPageParam(collect, pageParam);
        return new PageResponseDto<List<GoalExecuteLog>>(goalExecuteLogs,new Long(listFiles.size()));
    }

    /**
     * 获取日志文件数量
     * @return
     */
    public long logFileCount(){
        final File mavenLogDir = mavenLogDir();
        IOFileFilter fileFilter = new WildcardFileFilter("*out.log");
        final Collection<File> listFiles = FileUtils.listFiles(mavenLogDir, fileFilter, TrueFileFilter.INSTANCE);
        return listFiles.size();
    }

    /**
     * 创建新的日志文件
     * @return
     */
    public MavenPluginService.MavenExecuteLogFiles createNewLogFiles(){
        // 获取 maven 日志输出文件
        final File mavenLogDir = mavenLogDir();

        final String yyyyMMddHHmmss = DateFormatUtils.format(System.currentTimeMillis(), TIME_FORMAT);
        File mavenLogOutFile = new File(mavenLogDir,yyyyMMddHHmmss+".out.log");
        File mavenLogErrFile = new File(mavenLogDir,yyyyMMddHHmmss+".err.log");
        return new MavenPluginService.MavenExecuteLogFiles(mavenLogDir(),mavenLogOutFile, mavenLogErrFile);
    }

    File mavenLogDir(){
        String userName = "admin";
        if (userService != null){
            userName = userService.username();
        }

        return fileManager.mkTmpDir("mavenlog/" + userName);
    }

    public OnlyPath downloadLog(String logPath) {
        final File file = mavenLogDir();
        final File logfile = new File(file, logPath);
        return fileManager.relativePath(new OnlyPath(logfile));
    }
}
