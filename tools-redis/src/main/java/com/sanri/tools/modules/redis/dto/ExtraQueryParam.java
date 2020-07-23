package com.sanri.tools.modules.redis.dto;

import lombok.Data;

@Data
public class ExtraQueryParam {
    // 作用于 hash ,查某一个 key, 可以使用正则
    private String hashKey;

    // 作用于 List ,范围查询
    private Long begin;
    private Long end;
}
