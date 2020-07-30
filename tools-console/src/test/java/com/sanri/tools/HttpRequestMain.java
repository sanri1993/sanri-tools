package com.sanri.tools;

import com.sanri.tools.modules.name.remote.apis.TranslateApi;
import com.sanri.tools.modules.name.remote.dtos.BaiduTranslateRequest;
import com.sanri.tools.modules.name.remote.dtos.BaiduTranslateResponse;
import com.sanri.tools.modules.name.remote.dtos.YoudaoTranslateRequest;
import com.sanri.tools.modules.name.remote.dtos.YoudaoTranslateResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HttpRequestMain {
    @Autowired
    private TranslateApi youdaoTranslateApi;

    @Value("${translate.baidu.appId}")
    private String appId;
    @Value("${translate.baidu.secret}")
    private String secret;

    @Test
    public void testSendRequest(){
        YoudaoTranslateRequest youdaoQueryDto = new YoudaoTranslateRequest("翻译", "28a343197650d05a","hioXXfQjbWnRDuLrvbvx3tGpo7hDUpHt");
        YoudaoTranslateResponse translate = youdaoTranslateApi.youdaoTranslate(youdaoQueryDto);
        System.out.println(translate);
    }

    @Test
    public void testSendRequest2(){
        BaiduTranslateRequest baiduTranslateRequest = new BaiduTranslateRequest("算法工程师", "20181123000238271","PoT5TnMl_4pVIhosG_Fk");
        BaiduTranslateResponse baiduTranslateResponse = youdaoTranslateApi.baiduTranslate(baiduTranslateRequest);
        System.out.println(baiduTranslateResponse);
    }
}
