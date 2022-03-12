package com.sanri.tools.modules.database.service.code.rename;


import com.sanri.tools.modules.database.service.code.dtos.JavaBeanInfo;
import com.sanri.tools.modules.database.service.dtos.meta.TableMetaData;

public interface RenameStrategy {
    JavaBeanInfo mapping(TableMetaData tableMetaData);
}
