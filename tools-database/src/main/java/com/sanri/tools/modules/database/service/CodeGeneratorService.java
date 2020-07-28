package com.sanri.tools.modules.database.service;

import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.database.dtos.CodeGeneratorConfig;
import com.sanri.tools.modules.database.dtos.meta.TableMetaData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Service
@Slf4j
public class CodeGeneratorService {
    @Autowired
    private JdbcService jdbcService;

    @Autowired
    private FileManager fileManager;

    /**
     * 数据表生成 javaBean ,搬 mybatisplus 的过来
     * 支持 swagger , lombok , persistence-api
     * @param connName
     * @param catalog
     * @param schema
     * @throws IOException
     * @throws SQLException
     */
    public void mapperToBean(String connName,String catalog,String schema) throws IOException, SQLException {
        Collection<TableMetaData> tables = jdbcService.tables(connName, catalog);

        // 删除不需要的数据表,过滤出想要的数据表
        List<TableMetaData> filterTables = new ArrayList<>(tables);
        if (StringUtils.isNotBlank(schema)) {
            Iterator<TableMetaData> iterator = filterTables.iterator();
            while (iterator.hasNext()) {
                TableMetaData tableMetaData = iterator.next();
                String currentSchema = tableMetaData.getActualTableName().getSchema();
                if (!schema.equals(currentSchema)){
                    iterator.remove();
                }
            }
        }

        for (TableMetaData filterTable : filterTables) {

        }
    }

    @PostMapping("/projectBuild")
    public void projectBuild(@RequestBody CodeGeneratorConfig codeGeneratorConfig){
        File targetDir = fileManager.mkTmpDir("code/project/buildSpringBoot");
        //项目基本目录
        File projectDir = new File(targetDir, codeGeneratorConfig.getProjectName());

        //自己生成 maven 骨架
        File javaDir = new File(projectDir, "src/main/java");javaDir.mkdirs();
        File resourcesDir = new File(projectDir, "src/main/resources");resourcesDir.mkdirs();
        File testJavaDir = new File(projectDir, "src/test/java");testJavaDir.mkdirs();
        File testResourcesDir = new File(projectDir, "src/test/resources");testResourcesDir.mkdirs();

        // 生成 service,controller,vo,dto,param
        mkdirs(javaDir,codeGeneratorConfig.getPackageConfig());
    }

    private void mkdirs(File javaDir, CodeGeneratorConfig.PackageConfig packageConfig) {
        PropertyDescriptor[] beanGetters = ReflectUtils.getBeanGetters(CodeGeneratorConfig.PackageConfig.class);
        for (int i = 0; i < beanGetters.length; i++) {
            Method readMethod = beanGetters[i].getReadMethod();
            String path = Objects.toString(ReflectionUtils.invokeMethod(readMethod, packageConfig));
            String[] split = StringUtils.split(path, '.');
            StringBuffer currentPath = new StringBuffer();
            for (String partPath : split) {
                currentPath.append("/").append(partPath);
                File dir = new File(javaDir, currentPath.toString());
                if(!dir.exists()){
                    log.info("创建目录 : {} ",dir);
                    dir.mkdir();
                }
            }
        }
    }
}
