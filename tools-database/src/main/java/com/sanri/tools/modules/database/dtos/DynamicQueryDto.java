package com.sanri.tools.modules.database.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DynamicQueryDto {
    private String sql;
    private List<Header> headers;
    private List<List<Object>> rows = new ArrayList<>();

    public DynamicQueryDto() {
    }

    public DynamicQueryDto(String sql) {
        this.sql = sql;
    }

    public void addRow(List<Object> row){
        rows.add(row);
    }

    public void addHeader(Header header) {
        headers.add(header);
    }

    @Data
    public static class Header{
        private String columnName;
        private int dataType;
        private String typeName;

        public Header() {
        }

        public Header(String columnName, int dataType, String typeName) {
            this.columnName = columnName;
            this.dataType = dataType;
            this.typeName = typeName;
        }
    }
}
