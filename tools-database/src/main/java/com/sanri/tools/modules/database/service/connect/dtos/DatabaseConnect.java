package com.sanri.tools.modules.database.service.connect.dtos;

import lombok.Data;

/**
 * 数据源连接
 */
@Data
public class DatabaseConnect {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
}
