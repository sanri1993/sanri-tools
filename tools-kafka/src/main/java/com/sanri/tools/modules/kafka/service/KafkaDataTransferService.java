package com.sanri.tools.modules.kafka.service;

import com.sanri.tools.modules.core.service.localtask.LocalTaskManager;
import com.sanri.tools.modules.core.service.localtask.dtos.LocalTask;
import com.sanri.tools.modules.core.service.localtask.dtos.LocalTaskDto;
import com.sanri.tools.modules.core.service.localtask.dtos.TaskInfo;
import com.sanri.tools.modules.kafka.service.dtos.DataTransferDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaDataTransferService {

    @Autowired
    private LocalTaskManager localTaskManager;

    public void transferData(DataTransferDto dataTransferDto) throws ClassNotFoundException {
        String taskId = dataTransferDto.getFrom()+dataTransferDto.getTo()+dataTransferDto.getTopic()+System.currentTimeMillis();
        final LocalTaskDto localTaskDto = new LocalTaskDto(taskId, "kafka 数据迁移");
        localTaskDto.setImplClassName(TransferTask.class.getName());
        localTaskDto.addExtraData("transferInfo",dataTransferDto);
        localTaskManager.register(localTaskDto);

        localTaskManager.startTask(taskId);
    }

    public static final class TransferTask implements LocalTask{

        @Override
        public void execute(LocalTaskDto localTaskDto, TaskInfo taskInfo) {
            final DataTransferDto dataTransferDto = (DataTransferDto) localTaskDto.extraData("transferInfo");

        }
    }
}
