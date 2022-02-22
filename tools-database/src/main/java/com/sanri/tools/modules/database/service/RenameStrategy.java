package com.sanri.tools.modules.database.service;

import com.sanri.tools.modules.database.service.meta.dtos.TableMetaData;
import com.sanri.tools.modules.database.service.rename.JavaBeanInfo;

public interface RenameStrategy {
    JavaBeanInfo mapping(TableMetaData tableMetaData);
}
