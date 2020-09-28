package com.sanri.tools.modules.database.dtos;

import com.sanri.tools.modules.database.dtos.meta.ActualTableName;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ExcelImportParam {
    private String connName;
    private ActualTableName actualTableName;
    private int startRow;
    private List<Mapping> mapping;

    @Data
    public static class Mapping{
        private int index = -1;
        private String columnName;
        private String random;
        private String constant;
    }
}
