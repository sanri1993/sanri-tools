package systest;

import lombok.Data;

import java.io.File;

/**
 * @author LZJ
 * @create 2018-08-18 11:41
 **/
@Data
public class Params {
 
    private String groupId;
 
    private String artifactId;
 
    private String version;
 
    //远程maven仓库的URL地址 http://" + host + ":" + port + "/nexus/content/groups/public/
    private String repository;
 
    //下载的jar包存放的目标地址
    private File target;
 
    private String username;
 
    private String password;

    public Params(String groupId, String artifactId, String version, String repository, File target) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.repository = repository;
        this.target = target;
    }
}