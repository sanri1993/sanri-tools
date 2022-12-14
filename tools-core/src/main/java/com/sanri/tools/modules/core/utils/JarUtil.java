package com.sanri.tools.modules.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Administrator
 * 打 jar 包工具
 */
public class JarUtil {

    /**
     * 获取标准格式的 classpath
     * @param classpath
     * @return
     */
    public static String standardClasspath(String classpath){
        final String[] items = StringUtils.split(classpath, ";");
        return StringUtils.join(items," ");
    }

    /**
     * 创建一个临时的 manifest  MANIFEST.MF
     * @param outputDir 文件输出目录, 会自动在这个目录下创建 META-INF/MANIFEST.MF 文件
     * @param manifest 清单信息
     * @return
     */
    public static File createManifestFile(File outputDir, Manifest manifest) throws IOException {
        final File file = new File(outputDir, "META-INF/MANIFEST.MF");
        file.getParentFile().mkdirs();
        try(final FileOutputStream fileOutputStream = new FileOutputStream(file)){
            manifest.write(fileOutputStream);
        }
        return file;
    }

    /**
     * 添加 jar 包
     * @param outputFile 输出文件
     * @param files
     * @return
     */
    public static File jar(File outputFile, File... files) throws IOException {
        if (!outputFile.getParentFile().exists()){
            // 创建父级目录
            outputFile.getParentFile().mkdirs();
        }
        try(final JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputFile));){
            jarOutputStream.setLevel(Deflater.NO_COMPRESSION);
            jarOutputStream.setMethod(ZipOutputStream.STORED);
            for (File file : files) {
                if (file.isFile()){
                    addFile(jarOutputStream,file,file.getName());
                }else if (file.isDirectory()){
                    addDirectory(jarOutputStream,file,new OnlyPath(file.getParentFile()));
                }
            }

            jarOutputStream.finish();
        }

        return outputFile;
    }

    /**
     * jar 中添加一个目录
     * @param jarOutputStream
     * @param file
     * @param path
     * @throws IOException
     */
    private static void addDirectory(JarOutputStream jarOutputStream,File file,OnlyPath path) throws IOException {
        final Collection<File> listFilesAndDirs = FileUtils.listFilesAndDirs(file, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        for (File listFilesAndDir : listFilesAndDirs) {
            final String relativePath = path.relativize(new OnlyPath(listFilesAndDir)).toString();
            if (listFilesAndDir.isDirectory()) {
                // 如果是目录, 先添加一个 entry
                JarEntry jarEntry = new JarEntry(relativePath + "/");
                jarEntry.setTime(listFilesAndDir.lastModified());
                jarEntry.setSize(0);
                jarEntry.setCrc(0);
                jarOutputStream.putNextEntry(jarEntry);
                jarOutputStream.closeEntry();
                continue;
            }
            addFile(jarOutputStream,listFilesAndDir,relativePath);
        }
    }

    /**
     * jar 文件中添加一个文件
     * @param jarOutputStream
     * @param file
     * @param path
     * @throws IOException
     */
    private static void addFile(JarOutputStream jarOutputStream,File file,String path) throws IOException {
        JarEntry jarEntry = new JarEntry(path);
        jarEntry.setTime(file.lastModified());
        jarEntry.setSize(file.length());
        jarEntry.setCrc(fastCalcFileCrc32(file));
        jarOutputStream.putNextEntry(jarEntry);
        try(final FileInputStream fileInputStream = new FileInputStream(file)){
            IOUtils.copy(fileInputStream,jarOutputStream);
            jarOutputStream.closeEntry();
        }
    }
    /**
     * 计算 crc32
     * @param file
     * @return
     * @throws IOException
     */
    public static long fastCalcFileCrc32(File file) throws IOException {
        CRC32 crc32 = new CRC32();
        try(FileInputStream fileInputStream = new FileInputStream(file)){
            byte[] buffer = new byte[8192];
            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
                crc32.update(buffer, 0, length);
            }
            return crc32.getValue();
        }
    }

}
