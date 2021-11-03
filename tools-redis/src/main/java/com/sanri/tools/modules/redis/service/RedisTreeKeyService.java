package com.sanri.tools.modules.redis.service;

import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.redis.dtos.TreeKey;
import com.sanri.tools.modules.redis.dtos.in.ConnParam;
import com.sanri.tools.modules.redis.service.dtos.RedisConnection;
import com.sanri.tools.modules.redis.service.dtos.RedisNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.*;

@Service
@Slf4j
public class RedisTreeKeyService {
    @Autowired
    private RedisService redisService;

    // 1 万条以下的数据可以使用
    public static final long supportKeys = 10000;

    /**
     *
     * @param connParam
     * @return
     * @throws IOException
     */
    public List<TreeKey> treeKeys(ConnParam connParam) throws IOException {
        final RedisConnection redisConnection = redisService.redisConnection(connParam);
        final List<RedisNode> masterNodes = redisConnection.getMasterNodes();
        long totalKeys = 0 ;
        for (RedisNode masterNode : masterNodes) {
            final Map<String, Long> dbSizes = masterNode.getDbSizes();
            final Long dbsize = dbSizes.get(connParam.getIndex() + "");
            totalKeys += dbsize;
        }
        if (totalKeys > supportKeys){
            log.error("数据量过大 {} > {}, 不支持树结构",totalKeys,supportKeys);
            throw new ToolException("key 数据量"+totalKeys+"超过 "+supportKeys+" 不支持使用树结构");
        }

        TreeKey top = new TreeKey("","virtual");
        final TreeKey virtual = new TreeKey("virtual","virtual");
        for (RedisNode masterNode : masterNodes) {
            final Jedis jedis = masterNode.browerJedis();
            try {
                // 需要支持 keys * 命令
                Set<String> keys = jedis.keys("*");

                for (String key : keys) {
                    final String[] parts = StringUtils.split(key, ":");
                    appendTree(parts,virtual,0);
                }

                // 对于目录上有值的, 单独添加一个节点
                for (String key : keys) {
                    final String[] parts = StringUtils.split(key, ":");
                    TreeKey treeKey = findPath(parts,virtual);
                    if (treeKey.isFolder()){
                        final String[] subarray = ArrayUtils.subarray(parts, 0, parts.length - 1);
                        final TreeKey parent = findPath(subarray, virtual);
                        parent.addChild(new TreeKey(treeKey.getKey(),treeKey.getName()));
                    }
                }

            }finally {
                if (jedis != null){
                    jedis.close();
                }
            }
        }
        return virtual.getChilds();
    }

    /**
     * 追加树
     * @param top
     * @param parts
     */
    public void appendTree(String [] parts, TreeKey parent, int deep){
        if (deep >= parts.length){
            return ;
        }
        final String part = parts[deep];
        final List<TreeKey> childs = parent.getChilds();
        if (CollectionUtils.isNotEmpty(childs)) {
            final Iterator<TreeKey> iterator = childs.iterator();
            while (iterator.hasNext()){
                final TreeKey child = iterator.next();
                if (child.getName().equals(part)) {
                    appendTree(parts, child, ++deep);
                    return ;
                }
            }
        }
        addTree(parts,parent,deep);
    }

    public void addTree(String [] parts,TreeKey parent,int deep){
        if (deep >= parts.length){
            return ;
        }
        for (int i = deep; i < parts.length; i++) {
            final TreeKey treeKey = new TreeKey(StringUtils.join(parts,':'),parts[i]);
            parent.addChild(treeKey);
            parent.setFolder(true);
            parent = treeKey;
        }
    }

    public TreeKey findPath(String [] parts,TreeKey top){
        TreeKey parent = top;
        for (String part : parts) {
            final List<TreeKey> childs = parent.getChilds();
            for (TreeKey child : childs) {
                if (child.getName().equals(part)){
                    parent = child;
                    continue;
                }
            }
        }
        return parent;
    }
}
