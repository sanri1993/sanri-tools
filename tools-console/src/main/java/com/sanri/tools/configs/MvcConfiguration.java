package com.sanri.tools.configs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanri.tools.modules.core.service.file.FileManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class MvcConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("*")
                .allowedOrigins("*")
                .allowedHeaders("*");
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> converter : converters) {
            if(converter instanceof MappingJackson2HttpMessageConverter){
                MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = (MappingJackson2HttpMessageConverter) converter;
                final ObjectMapper objectMapper = mappingJackson2HttpMessageConverter.getObjectMapper();
                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            }
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            final ApplicationHome applicationHome = new ApplicationHome(getClass());
            final File publicDir = new File(applicationHome.getSource().getParentFile(), "public");

            final String property = System.getProperty("user.home");
            String publicPath2 = new File(property,"public").toURI().toURL().toString();

            // 部署路径
            final String publicPath = publicDir.toURI().toURL().toString();
            // 开发路径
            final String path = new File("D:\\currentproject\\sanritoolsvue\\dist").toURI().toURL().toString();

            log.info("加载前端路径列表: \n {} \n", StringUtils.join(Arrays.asList(path,publicPath,publicPath2),"\n"));

            registry.addResourceHandler("/public/**","/page/**")
                    .addResourceLocations(path,publicPath,publicPath2);
        } catch (MalformedURLException e) {
            // ignore
        }

    }
}
