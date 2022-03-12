package com.sanri.tools.modules.core.dtos;

import lombok.Data;

import java.util.Objects;

/**
 * 字典数据
 * @param <T>
 */
@Data
public class DictDto<T> {
    private String key;
    private T value;

    public DictDto() {
    }

    public DictDto(String key, T value) {
        this.key = key;
        this.value = value;
    }

    public String getLabel(){
        return Objects.toString(value);
    }
}
