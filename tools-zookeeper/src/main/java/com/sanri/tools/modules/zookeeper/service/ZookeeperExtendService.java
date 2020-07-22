package com.sanri.tools.modules.zookeeper.service;

import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.zookeeper.dto.PathFavorite;
import com.sanri.tools.modules.zookeeper.dto.PathFavoriteParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 扩展功能 : 收藏路径,几个重要的路径可以初始化加载
 */
@Service
@Slf4j
public class ZookeeperExtendService {
    // 路径收藏  connName ==> PathFavorite
    private Map<String, List<PathFavorite>> pathFavorites = new HashMap<>();

    @Autowired
    FileManager fileManager;

    /**
     * 添加收藏 ,前端需要把所有的收藏全拿过来,后端直接覆盖
     * @param pathFavoriteParam
     */
    public void addFavorites(PathFavoriteParam pathFavoriteParam){
        pathFavorites.put(pathFavoriteParam.getConnName(),pathFavoriteParam.getFavorites());
        serializer();
    }

    /**
     * 序列化收藏列表到文件
     */
    private void serializer() {

    }
}
