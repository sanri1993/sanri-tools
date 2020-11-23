package com.sanri.tools.modules.quartz.dtos;

import lombok.Data;
import org.quartz.TriggerKey;

import java.util.List;

@Data
public class TriggerCron {
    private TriggerKey triggerKey;
    private String cron;

    public TriggerCron() {
    }

    public TriggerCron(TriggerKey triggerKey, String cron) {
        this.triggerKey = triggerKey;
        this.cron = cron;
    }

    // 最近的执行时间
    private List<String> nextTimes;
}
