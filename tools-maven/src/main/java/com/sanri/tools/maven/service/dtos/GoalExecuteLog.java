package com.sanri.tools.maven.service.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.io.File;
import java.text.ParseException;
import java.util.Date;

@Data
@Slf4j
public class GoalExecuteLog {
    /**
     * 执行 maven 命令操作者
     */
    private String username;
    /**
     * 任务完成时间, 即文件最后修改时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finish;
    /**
     * 开始时间,取的文件名
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date begin;
    /**
     * 文件名
     */
    private String filename;
    /**
     * 文件相对路径
     */
    private String relativePath;

    /**
     * 文件大小, 单位: 字节
     */
    private long fileSize;

    public GoalExecuteLog() {
    }

    public GoalExecuteLog(File file) {
        final String name = file.getName();
        final String baseName = FilenameUtils.getBaseName(name);
        final String time = FilenameUtils.getBaseName(baseName);
        try {
            this.filename = name;
            this.finish = new Date(file.lastModified());
            this.begin =  DateUtils.parseDate(time, TIME_FORMAT);
        } catch (ParseException e) {
            log.error("文件日期格式解析错误: {}",e.getMessage());
        }
    }

    public static final String TIME_FORMAT = "yyyyMMddHHmmssS";
}
