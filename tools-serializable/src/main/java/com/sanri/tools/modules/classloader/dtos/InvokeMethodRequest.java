package com.sanri.tools.modules.classloader.dtos;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class InvokeMethodRequest {
    private MethodReq methodReq;

    /**
     * 参数列表
     * 原始类型时传   string
     * String       string
     * Date         时间戳 long
     * BigDecimal   string
     * 复杂类型      json
     */
    private List params = new ArrayList<>();
}
