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
}
