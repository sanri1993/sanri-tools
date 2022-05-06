package com.sanri.tools.modules.core.utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

@Component
public class StreamUtil {
    /**
     * 作者:sanri <br/>
     * 时间:2017-10-31下午4:10:50<br/>
     * 功能:预览 <br/>
     *
     * @param input
     * @param mime
     * @throws IOException e
     */
    public void preview(InputStream input, MimeType mime, HttpServletResponse response) throws IOException {
        if (input == null) {
            return;
        }
        response.setContentType(mime.getContentType());
        response.setHeader("Set-Cookie", "fileDownload=true; path=/");
        ServletOutputStream output = response.getOutputStream();
        IOUtils.copy(input, output);
        output.flush();
    }

    /**
     * 作者:sanri <br/>
     * 时间:2017-10-31下午4:18:54<br/>
     * 功能:下载 <br/>
     *
     * @param input
     * @param mime
     * @param fileName
     * @param response
     * @throws IOException
     */
    public void download(InputStream input, MimeType mime, String fileName, HttpServletResponse response) throws IOException {
        if (input == null) {
            return;
        }
        boolean isAuto = false;
        if(mime == MimeType.AUTO){
            mime = parseMIME(fileName);
            isAuto = true;
        }
        response.setContentType(mime.getContentType());
        response.setHeader("Set-Cookie", "fileDownload=true; path=/");

        String suffix = mime.getSuffix();
        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        String encodeFileName = encodeFilename(fileName, request);
        if(StringUtils.isNotBlank(suffix) && !isAuto){
            encodeFileName  += ("."+ mime.getSuffix());
        }

        response.setHeader("Content-Disposition", "attachment;filename=\"" + encodeFileName + "\"");
        long length = input.available();
        if (length != -1) {
            response.setContentLength((int) length);
        }
        ServletOutputStream output = response.getOutputStream();
        IOUtils.copy(input, output);
        output.flush();
    }

    /**
     *
     * 作者:sanri <br/>
     * 时间:2018-5-28下午5:08:06<br/>
     * 功能:mime 类型解析  <br/>
     * @param fileName
     * @return
     */
    private MimeType parseMIME(String fileName) {
        String extension = FilenameUtils.getExtension(fileName);
        MimeType parseMIME = MimeType.parseMIME(extension);
        if(parseMIME != null){
            return parseMIME;
        }
        throw new IllegalArgumentException("不支持的 mime 类型，文件名为:"+fileName);
    }

    private static String encodeFilename(String filename, HttpServletRequest request) {
        /**
         * 获取客户端浏览器和操作系统信息
         * 在IE浏览器中得到的是：User-Agent=Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; Maxthon; Alexa Toolbar)
         * 在Firefox中得到的是：User-Agent=Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.7.10) Gecko/20050717 Firefox/1.0.6
         */
        String agent = request.getHeader("USER-AGENT");
        try {
            if ((agent != null) && (-1 != agent.indexOf("MSIE"))) {
                String newFileName = URLEncoder.encode(filename, "UTF-8");
                newFileName = StringUtils.replace(newFileName, "+", "%20");
                if (newFileName.length() > 150) {
                    newFileName = new String(filename.getBytes("GB2312"), "ISO8859-1");
                    newFileName = StringUtils.replace(newFileName, " ", "%20");
                }
                return newFileName;
            }
            if ((agent != null) && (-1 != agent.indexOf("Mozilla"))) {
                return new String(filename.getBytes("UTF-8"), "ISO8859-1");
            }
            return filename;
        } catch (Exception ex) {
            return filename;
        }
    }

    public enum MimeType {
        AUTO("自动获取",""),
        STREAM("application/octet-stream", "jpg"),

        PDF("application/pdf", "pdf"),
        ZIP("application/zip", "zip"),
        RAR("application/zip", "rar"),
        EXCEL2003("application/vnd.ms-excel", "xls"),
        EXCEL2007("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx"),
        EXE("application/octet-stream", "exe"),
        WORD("application/octet-stream","doc"),

        TXT("text/plain", "txt"),
        MARKDOWN("text/plain", "md"),
        JAVA("text/plain", "java"),
        PYTHON("text/plain", "py"),
        JAVASCRIPT("text/plain", "js"),
        CSS("text/plain", "css"),
        XML("text/xml","xml"),
        SQL("text/plain","sql"),

        JPG("application/x-jpg", "jpg"),
        JPEG("image/jpeg", "jpg"),
        GIF("image/gif", "gif"),
        PNG("application/x-png", "png"),
        GZ("application/octet-stream","gz");


        private String contentType;
        private static final String CHARSET = "UTF-8";
        private String suffix;

        public String getContentType() {
            return contentType + ";charset=" + CHARSET;
        }

        private MimeType(String contentType, String suffix) {
            this.contentType = contentType;
            this.suffix = suffix;
        }

        public String getSuffix() {
            return suffix;
        }

        /**
         * @param fileType
         * @return
         */
        public static MimeType parseMIME(String fileType) {
            if (StringUtils.isBlank(fileType)) {
                throw new IllegalArgumentException("不支持的 MIME类型");
            }
            MimeType[] values = MimeType.values();
            for (MimeType mimeType : values) {
                if (mimeType.getSuffix().equalsIgnoreCase(fileType)) {
                    return mimeType;
                }
            }
            throw new IllegalArgumentException("不支持的 MIME类型");
        }
    }

}
