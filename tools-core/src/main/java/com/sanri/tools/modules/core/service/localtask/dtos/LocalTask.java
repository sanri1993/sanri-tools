package com.sanri.tools.modules.core.service.localtask.dtos;

public interface LocalTask {

    /**
     * 执行一个任务
     * @param localTaskDto 任务信息
     * @param taskInfo 信息运行时信息
     */
    void execute(LocalTaskDto localTaskDto, TaskInfo taskInfo);
}
