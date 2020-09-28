package com.sanri.tools.modules.redis.controller;

import com.sanri.tools.modules.redis.dtos.KeyScanResult;
import com.sanri.tools.modules.redis.dtos.RangeParam;
import com.sanri.tools.modules.redis.dtos.SubKeyParam;
import com.sanri.tools.modules.redis.dtos.params.ConnParam;
import com.sanri.tools.modules.redis.dtos.params.ScanParam;
import com.sanri.tools.modules.redis.dtos.params.SerializerParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/redis")
@RestController
public class RedisController {

    @GetMapping("/key/scan")
    public KeyScanResult scan(ConnParam connParam, ScanParam scanParam,int hostIndex){

        return null;
    }

    @PostMapping("/key/drop")
    public void dropKeys(ConnParam connParam,String [] keys){

    }

    /**
     * 适用于类型是 hash , set , zset 类型的
     * @param connParam
     * @param key
     * @param scanParam
     */
    @GetMapping("/key/subKeys")
    public void subKeys(ConnParam connParam,String key,ScanParam scanParam){

    }

    /**
     * 适用于类型是 list , zset 类型
     * @param connParam
     * @param key
     * @param rangeParam
     */
    @GetMapping("/key/rangeKeys")
    public void rangeKeys(ConnParam connParam, String key, RangeParam rangeParam){

    }

    @GetMapping("/data")
    public void data(ConnParam connParam, SubKeyParam subKeyParam,SerializerParam serializerParam){

    }
}
