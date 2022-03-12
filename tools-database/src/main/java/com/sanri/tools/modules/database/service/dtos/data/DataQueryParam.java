package com.sanri.tools.modules.database.service.dtos.data;

import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.database.service.meta.dtos.Namespace;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
public class DataQueryParam {
    @NotNull
    private String connName;
    private List<String> sqls = new ArrayList<>();
    private String traceId;
    private Namespace namespace;

    public String getFirstSql(){
        if (CollectionUtils.isNotEmpty(sqls)){
            return sqls.get(0);
        }
        throw new ToolException("必须要有一句 sql 做为查询条件");
    }
}
