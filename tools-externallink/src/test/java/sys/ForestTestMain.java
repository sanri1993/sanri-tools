package sys;

import com.dtflys.forest.Forest;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestResponse;
import com.dtflys.forest.utils.TypeReference;
import org.junit.Test;

public class ForestTestMain {
    @Test
    public void test1(){
        String postUrl = "http://192.168.61.71:8084/login";
        String username = "admin";
        String password = "0";

        final ForestRequest<?> forestRequest = Forest.post(postUrl)
                .contentTypeJson()
                .addBody("username", username)
                .addBody("account", username)
                .addBody("password", password);

        final ForestResponse<String> execute = forestRequest.execute(new TypeReference<ForestResponse<String>>() {});
        System.out.println(execute);
    }
}
