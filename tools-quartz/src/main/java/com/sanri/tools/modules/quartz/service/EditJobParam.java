package com.sanri.tools.modules.quartz.service;

import lombok.Data;
import org.quartz.JobKey;

@Data
public class EditJobParam {
//    private JobKey jobKey;
    private String name;
    private String group;

    private String description;
    private String className;
    private String classloaderName;
    private String jobMethodName;
    private String cron;

    public JobKey getJobKey(){
        return JobKey.jobKey(name,group);
    }
}
