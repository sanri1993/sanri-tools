package com.sanri.tools.modules.fastdfs.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;

import com.sanri.tools.modules.core.dtos.DictDto;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.csource.common.MyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.core.utils.StreamUtil;
import com.sanri.tools.modules.fastdfs.service.FastdfsService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Controller
@Slf4j
@RequestMapping("/fastdfs")
@Validated
public class FileController {

    @Autowired
    private FastdfsService fastdfsService;

    @Autowired
    private StreamUtil streamUtil;

    /**
     * 文件预览
     * @param connName
     * @param dfsId
     * @param response
     * @throws IOException
     * @throws MyException
     */
    @GetMapping("/preview")
    public void preview(@NotBlank String connName, @NotBlank String dfsId, HttpServletResponse response) throws IOException, MyException {
        final byte[] bytes = fastdfsService.downloadStream(connName, dfsId);
        if (bytes == null){
            throw new ToolException("文件丢失");
        }
        streamUtil.preview(new ByteArrayInputStream(bytes), StreamUtil.MimeType.STREAM,response);
    }

    /**
     * 文件上传
     * @param connName
     * @param files
     * @throws IOException
     * @throws MyException
     * @return
     */
    @PostMapping("/uploadFiles")
    @ResponseBody
    public List<DictDto<String>> uploadFiles(@NotBlank String connName , @RequestParam("files") MultipartFile[] files) throws IOException, MyException {
        List<DictDto<String>> dictDtos = new ArrayList<>();
        for (MultipartFile file : files) {
            final String originalFilename = file.getOriginalFilename();
            try(final InputStream inputStream = file.getInputStream()){
                final byte[] bytes = IOUtils.toByteArray(inputStream);
                final String dfsId = fastdfsService.uploadFile(connName, bytes, originalFilename);
                dictDtos.add(new DictDto<>(originalFilename,dfsId));
            }
        }
        return dictDtos;
    }

    /**
     * fastdfs 批量下载
     * @param connName
     * @param dfsIds
     * @param response
     * @throws IOException
     */
    @GetMapping("/download")
    public void download(@NotBlank String connName,@NotBlank String dfsIds,HttpServletResponse response) throws IOException {
        final String[] split = StringUtils.split(dfsIds, ',');
        final List<String> dfsIdArray = Arrays.stream(split)
                .map(item -> StringUtils.split(item, '\n'))
                .flatMap(Arrays::stream)
                .map(StringUtils::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(dfsIdArray)) {
            log.warn("没有收到任何需要下载的文件");
            return ;
        }

        final String extension = dfsIdArray.size() == 1 ? FilenameUtils.getExtension(dfsIdArray.get(0)) : "zip";

        String downloadFilename = "download_"+dfsIdArray.size()+"_"+ DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(System.currentTimeMillis()) + "."+ extension;

        streamUtil.downloadSetResponse(StreamUtil.MimeType.STREAM,downloadFilename,response);
        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        response.setHeader("filename", downloadFilename);

        // 单文件下载
        if (dfsIdArray.size() == 1){
            final String dfsId = dfsIdArray.get(0);
            // 如果只有一个文件, 不用打包下载
            try {
                byte[] bytes = fastdfsService.downloadStream(connName, dfsId);
                if (bytes == null){
                    log.warn("文件丢失: {}",dfsId);
                    throw new ToolException("文件丢失: "+ dfsId);
                }
                response.getOutputStream().write(bytes);
            } catch (MyException e) {
                log.warn("文件异常: {}",dfsId);
                throw new ToolException("文件异常,dfsId: "+ dfsId);
            }
            return ;
        }

        // 多文件时打包下载
        ZipArchiveOutputStream archiveOutputStream = new ZipArchiveOutputStream(response.getOutputStream());
        for (String dfsId : dfsIdArray) {
            final String filename = StringUtils.substringAfterLast(dfsId, "/");
            try {
                byte[] bytes = fastdfsService.downloadStream(connName, dfsId);
                if (bytes == null){
                    log.warn("dfsId: {} 文件丢失", dfsId);
                    ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(new File(dfsId), filename+" 文件丢失");
                    archiveOutputStream.putArchiveEntry(zipArchiveEntry);
                    archiveOutputStream.closeArchiveEntry();
                    continue;
                }
                ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(new File(dfsId), filename);
                archiveOutputStream.putArchiveEntry(zipArchiveEntry);
                archiveOutputStream.write(bytes);
                archiveOutputStream.closeArchiveEntry();
            } catch (Exception e) {
                log.warn("dfsId: {} 文件异常", dfsId);
                ZipArchiveEntry zipArchiveEntry = new ZipArchiveEntry(new File(dfsId), filename+" 文件异常");
                archiveOutputStream.putArchiveEntry(zipArchiveEntry);
                archiveOutputStream.closeArchiveEntry();
            }
        }

        if (archiveOutputStream != null) {
            archiveOutputStream.close();
        }
    }
}
