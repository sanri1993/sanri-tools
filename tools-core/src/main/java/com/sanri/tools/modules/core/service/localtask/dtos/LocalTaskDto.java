package com.sanri.tools.modules.core.service.localtask.dtos;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public class LocalTaskDto implements Serializable {
    private String id;
    private String title;
    private String username;
    private long createTime;
    private String implClassName;
    private Map<String,Object> extraDatas = new HashMap<>();

    public LocalTaskDto() {
        this.createTime = System.currentTimeMillis();
    }

    public LocalTaskDto(String id, String title) {
        this.id = id;
        this.title = title;
        this.createTime = System.currentTimeMillis();
    }

    public void addExtraData(String key, Object data){
        extraDatas.put(key,data);
    }

    public Object extraData(String key){
        return extraDatas.get(key);
    }
}
