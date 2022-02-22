package com.sanri.tools.modules.database.service.meta.dtos;

import lombok.Data;

@Data
public class Namespace {
    private String connName;
    private String catalog;
    private String schema;
}
