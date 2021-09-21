package com.sanri.tools.modules.core.service.eval;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 *  代码片段实时编译, 适用于 hibernate , jpa 中那种 sql 拼接
 *  如果需要将 sql 复制出来将会特别麻烦, 用这个工具就方便多了
 */
@Service
@Slf4j
public class CodeSnippet {

    @Autowired
    private Configuration configuration;



    /**
     * 构建一个临时的完整 java 代码
     */
    public final class BuildTmpJava{
        public static final String filename = "Snippet.java.ftl";

        public BuildTmpJava(){

        }

        /**
         * 从一串字符串构建 Java 完整类
         * @return
         */
        public String buildFromString(String snippet) throws IOException, TemplateException {
            Map<String,Object> context = new HashMap<>();
            String content = FileUtils.readFileToString(new File("templates/code/Snippet.java.ftl"), StandardCharsets.UTF_8);
            Template template = new Template("Snippet.java", content, configuration);
            StringBuilderWriter stringBuilderWriter = new StringBuilderWriter();
            template.process(context,stringBuilderWriter);
            return stringBuilderWriter.toString();
        }

        public String exec(){

            return null;
        }
    }
}
