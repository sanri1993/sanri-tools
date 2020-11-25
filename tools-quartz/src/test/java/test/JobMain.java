package test;

import com.sanri.tools.modules.quartz.service.QuartzService;
import net.sf.cglib.proxy.Enhancer;
import org.junit.Test;
import org.quartz.simpl.CascadingClassLoadHelper;

public class JobMain {

    @Test
    public void customClassloader(){
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(CascadingClassLoadHelper.class);
        enhancer.setCallback(new QuartzService.LoadClassCallback(ClassLoader.getSystemClassLoader()));
        Object object = enhancer.create();
        String name = object.getClass().getName();

        System.out.println(name);
    }
}
