package com.sanri.tools.maven.controller;

import com.sanri.tools.maven.service.GoalExecuteLogManager;
import com.sanri.tools.maven.service.dtos.GoalExecuteLog;
import com.sanri.tools.modules.core.dtos.PageResponseDto;
import com.sanri.tools.modules.core.dtos.param.PageParam;
import com.sanri.tools.modules.core.utils.OnlyPath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
@Validated
@RequestMapping("/goals/logs")
public class GoalExecuteLogManagerController {

    @Autowired
    private GoalExecuteLogManager goalExecuteLogManager;

    /**
     * 分页查询日志信息
     * @param pageParam 分页参数
     * @return
     */
    @GetMapping("/list/page")
    public PageResponseDto<List<GoalExecuteLog>> listPage(@Validated PageParam pageParam){
        return goalExecuteLogManager.listByPage(pageParam);
    }

    /**
     * 读取编译日志
     * @param logPath 日志路径
     * @return
     * @throws IOException
     */
    @GetMapping("/readLog")
    public String readLog(String logPath) throws IOException {
        return goalExecuteLogManager.readLog(logPath);
    }

    /**
     * 下载一个执行日志
     * @param logPath 日志路径
     * @return
     */
    @GetMapping("/dowload")
    public String downloadLog(String logPath){
        final OnlyPath onlyPath = goalExecuteLogManager.downloadLog(logPath);
        return onlyPath.toString();
    }
}
