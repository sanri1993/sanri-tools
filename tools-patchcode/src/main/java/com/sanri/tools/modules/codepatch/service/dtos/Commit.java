package com.sanri.tools.modules.codepatch.service.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class Commit {
    private String message;
    private String author;
    private String commitId;
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date time;

    public Commit() {
    }

    public Commit(String message, String author, String commitId, Date time) {
        this.message = message;
        this.author = author;
        this.commitId = commitId;
        this.time = time;
    }
}
