package com.sanri.tools.modules.versioncontrol.dtos;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class CommitFilterParam {
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date begin;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date end;
    private String [] authors;
    private Integer maxCount = 100;
    private Boolean merge = true;
}
