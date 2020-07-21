package com.sanri.tools.modules.protocol.db;

public class ColumnType {
    private String dataType;
    private int precision;
    private int scale;
    private long length;

    public ColumnType(String dataType) {
        this.dataType = dataType;
    }

    public ColumnType(String dataType, int precision, int scale, long length) {
        this.dataType = dataType;
        this.precision = precision;
        this.scale = scale;
        this.length = length;
    }

    public ColumnType(String dataType, int length) {
        this.dataType = dataType;
        this.length = length;
    }

    public String getDataType() {
        return dataType;
    }

    public int getPrecision() {
        return precision;
    }

    public int getScale() {
        return scale;
    }

    public long getLength() {
        return length;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public void setLength(long length) {
        this.length = length;
    }
}
