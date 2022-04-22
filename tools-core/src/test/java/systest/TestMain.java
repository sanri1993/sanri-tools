package systest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dtflys.forest.annotation.Body;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.backend.okhttp3.OkHttp3Backend;
import com.dtflys.forest.schema.ForestClientBeanDefinitionParser;
import com.sanri.tools.modules.core.utils.RandomUtil;
import okhttp3.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class TestMain {
    public static void main(String[] args) {
        for (int i = 0; i < 256; i++) {
            final boolean[] booleans = byteToBoolArray((byte) i);
//            System.out.println(StringUtils.join(booleans,","));
            for (boolean aBoolean : booleans) {
                System.out.print(aBoolean+" ");
            }
            System.out.println();
        }

//        ArrayUtils.addAll()
    }

    public static boolean[] byteToBoolArray(byte value){
        boolean[] booleans = new boolean[8];
        for (int i = 0; i < booleans.length; i++) {
            booleans[i] = ((byte)(value >> i) & 0x1 ) == 0x1;
        }
        return booleans;
    }

    @Test
    public void test3() throws IOException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(32);
        final OkHttpClient client = new OkHttpClient();
        for (int i = 0; i < 30020; i++) {
            new Thread(){
                @Override
                public void run() {
                    for (int j = 0; j < 2; j++) {
                        String url = "https://www.databuff.com:19091/api/saasApply/trialApply";
                        JSONObject jsonObject = JSON.parseObject("{\"emailAddr\":\"222@163.com\",\"applyPw\":\"42eea062752b3cbe127f031f3c23b5c9\",\"applyName\":\"asdfasdf\",\"applyPhone\":\"13800138000\",\"companyName\":\"dsafasdfasdf\"}");
                        jsonObject.put("emailAddr", RandomUtil.email(RandomUtils.nextInt(3,50)));

                        System.out.println("注册数据: "+ jsonObject);

                        RequestBody body = RequestBody.create(
                                MediaType.parse("application/json"), jsonObject.toJSONString());

                        Request request = new Request.Builder()
                                .url(url)
                                .post(body)
                                .build();

                        Call call = client.newCall(request);
                        try {
                            Response response = call.execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    countDownLatch.countDown();
                }
            }.start();
        }

        countDownLatch.await();
    }

    public interface MyClient{

        @Post("https://www.databuff.com:19091/api/saasApply/trialApply")
        public void test(@Body JSONObject data );
    }
}
