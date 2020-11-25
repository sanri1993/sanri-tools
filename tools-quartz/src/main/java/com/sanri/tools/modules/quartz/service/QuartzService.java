package com.sanri.tools.modules.quartz.service;

import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.core.service.classloader.ClassloaderService;
import com.sanri.tools.modules.core.service.classloader.ExtendClassloader;
import com.sanri.tools.modules.core.service.plugin.PluginManager;
import com.sanri.tools.modules.database.service.JdbcService;
import com.sanri.tools.modules.quartz.dtos.ExScheduler;
import com.sanri.tools.modules.quartz.dtos.TriggerCron;
import com.sanri.tools.modules.quartz.dtos.TriggerTask;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.quartz.simpl.CascadingClassLoadHelper;
import org.quartz.spi.ClassLoadHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.quartz.impl.StdSchedulerFactory.PROP_SCHED_CLASS_LOAD_HELPER_CLASS;

@Service
public class QuartzService {
    @Autowired
    private JdbcService jdbcService;
    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private ClassloaderService classloaderService;

    @Autowired
    private SpringJobFactory springJobFactory;

    private Map<String, ExScheduler> schedulerMap = new ConcurrentHashMap<>();

    /**
     * 对于 quartz 需要绑定类加载器
     * @param connName
     * @param classloaderName
     */
    public void bindClassloader(String connName,String classloaderName) throws Exception {
        ClassLoader classloader = classloaderService.getClassloader(classloaderName);
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(CascadingClassLoadHelper.class);
        enhancer.setCallback(new LoadClassCallback(classloader));
        Object object = enhancer.create();
        createSchedule(connName,classloader,object.getClass());
    }

    public static class LoadClassCallback implements MethodInterceptor{
        private ClassLoader classLoader;
        public LoadClassCallback(ClassLoader classloader) {
            this.classLoader = classloader;
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            if(method.getDeclaringClass() == Object.class){
                return method.invoke(obj,objects);
            }

            String methodName = method.getName();
            int parameterCount = method.getParameterCount();
            if (methodName.equals("loadClass") && parameterCount == 1){
                Object invoke = methodProxy.invoke(obj, objects);
                if (invoke == null){
                    return classLoader.loadClass(Objects.toString(objects[0]));
                }

                return invoke;
            }

            // 其它情况调用原方法
            return methodProxy.invoke(obj,objects);
        }
    }

    /**
     * 一般任务数不会过万 , 一次性查出来即可
     * @return
     */
    public List<TriggerTask> triggerTasks(String connName,String catalog,String schema) throws IOException, SQLException {
        String namespace = catalog;
        if (StringUtils.isNotBlank(schema)){
            namespace = schema;
        }
        String sql = "select trigger_group,trigger_name,job_group,job_name,start_time,prev_fire_time,next_fire_time  from "+namespace+".qrtz_triggers qct  ";
        List<TriggerTask> triggerTasks = jdbcService.executeQuery(connName, sql, triggerTaskProcessor);
        return triggerTasks;
    }

    /**
     * 查询某个 trigger 的 cron 表达式
     * @param connName
     * @param triggerKey
     * @return
     */
    public TriggerCron triggerCron(String connName,String catalog,String schema,TriggerKey triggerKey) throws IOException, SQLException {
        String namespace = catalog;
        if (StringUtils.isNotBlank(schema)){
            namespace = schema;
        }

        String sql = "select  trigger_group,trigger_name,cron_expression from "+namespace+".qrtz_cron_triggers qct  where qct.trigger_group = '"+triggerKey.getGroup()+"' and qct.trigger_name = '"+triggerKey.getName()+"'";
        TriggerCron triggerCron = jdbcService.executeQuery(connName, sql, triggerCronProcessor);
        if (triggerCron == null) return triggerCron;

        String cron = triggerCron.getCron();
        List<String> nextTimes = new ArrayList<>();
        CronSequenceGenerator cronSequenceGenerator = new CronSequenceGenerator(cron);
        Date current = new Date();
        for (int i = 0; i < 10; i++) {
            current = cronSequenceGenerator.next(current);
            nextTimes.add(DateFormatUtils.format(current,"yyyy-MM-dd HH:mm:ss"));
        }
        triggerCron.setNextTimes(nextTimes);
        return triggerCron;
    }

    /**
     * 触发一个任务
     * @param connName
     * @param jobKey
     * @throws SchedulerException
     */
    @GetMapping("/trigger")
    public void trigger(String connName,JobKey jobKey) throws Exception {
        Scheduler scheduler = loadScheduler(connName);
        scheduler.triggerJob(jobKey);
    }

    /**
     * 暂停一个任务
     * @param connName
     * @param jobKey
     * @throws SchedulerException
     */
    @GetMapping("/pause")
    public void pause(String connName,JobKey jobKey) throws Exception {
        Scheduler scheduler = loadScheduler(connName);
        scheduler.pauseJob(jobKey);
    }

    /**
     * 恢复一个任务
     * @param connName
     * @param jobKey
     */
    @GetMapping("/resume")
    public void resume(String connName,JobKey jobKey) throws Exception {
        Scheduler scheduler = loadScheduler(connName);
        scheduler.resumeJob(jobKey);
    }

    /**
     * 删除一个任务
     * @param connName
     * @param triggerKey
     */
    @GetMapping("/remove")
    public void remove(String connName,TriggerKey triggerKey,JobKey jobKey) throws Exception {
        Scheduler scheduler = loadScheduler(connName);
        // 停止触发器
        scheduler.pauseTrigger(triggerKey);

        // 停止任务调度
        scheduler.unscheduleJob(triggerKey);

        // 删除任务
        scheduler.deleteJob(jobKey);
    }

    static TriggerTaskProcessor triggerTaskProcessor = new TriggerTaskProcessor();
    static TriggerCronProcessor triggerCronProcessor = new TriggerCronProcessor();

    static class TriggerTaskProcessor implements ResultSetHandler<List<TriggerTask>>{
        @Override
        public List<TriggerTask> handle(ResultSet rs) throws SQLException {
            List<TriggerTask> triggerTasks = new ArrayList<>();
            while (rs.next()){
                String triggerGroup = rs.getString("trigger_group");
                String triggerName = rs.getString("trigger_name");
                TriggerKey triggerKey = new TriggerKey(triggerGroup, triggerName);
                String jobGroup = rs.getString("job_group");
                String jobName = rs.getString("job_name");
                JobKey jobKey = new JobKey(jobGroup, jobName);

                long startTime = rs.getLong("start_time");
                long prevFireTime = rs.getLong("prev_fire_time");
                long nextFireTime = rs.getLong("next_fire_time");
                TriggerTask triggerTask = new TriggerTask(triggerKey, jobKey, startTime, prevFireTime, nextFireTime);
                triggerTasks.add(triggerTask);
            }
            return triggerTasks;
        }
    }
    static class TriggerCronProcessor implements ResultSetHandler<TriggerCron>{

        @Override
        public TriggerCron handle(ResultSet rs) throws SQLException {
            boolean next = rs.next();

            TriggerCron triggerCron = null;
            if (next) {
                String triggerGroup = rs.getString("trigger_group");
                String triggerName = rs.getString("trigger_name");
                TriggerKey triggerKey = new TriggerKey(triggerGroup, triggerName);

                String cronExpression = rs.getString("cron_expression");
                triggerCron = new TriggerCron(triggerKey, cronExpression);
            }
            return triggerCron;
        }
    }


    /**
     * 加载一个日程调度器
     * @param connName
     * @return
     */
    public Scheduler loadScheduler(String connName){
        ExScheduler exScheduler = schedulerMap.get(connName);
        if (exScheduler == null){
            throw new ToolException("未找到对应连接 "+connName+" 的调度器,需要先创建并绑定类加载器");
        }
        return exScheduler.getScheduler();
    }

    /**
     * 获取当前连接调度器绑定的类加载器
     * @param connName
     * @return
     */
    public ClassLoader getClassLoader(String connName){
        ExScheduler exScheduler = schedulerMap.get(connName);
        if (exScheduler == null){
            throw new ToolException("未找到对应连接 "+connName+" 的调度器,需要先创建并绑定类加载器");
        }
        return exScheduler.getClassLoader();
    }

    /**
     * 创建一个日程调度工具
     * @param connName
     * @param classLoader
     * @param classloaderHelper
     * @return
     * @throws Exception
     */
    public ExScheduler createSchedule(String connName, ClassLoader classLoader, Class classloaderHelper) throws Exception {
        ExScheduler exScheduler = schedulerMap.get(connName);
        if (exScheduler == null){
            DataSource dataSource = jdbcService.dataSource(connName);
            SchedulerFactoryBean factory = new SchedulerFactoryBean();
            factory.setAutoStartup(true);
            factory.setDataSource(dataSource);
            Properties properties = new Properties();
            properties.setProperty("org.quartz.jobStore.tablePrefix","qrtz_");
            properties.setProperty("org.quartz.scheduler.instanceName","SchedulerFactory");
            properties.setProperty(PROP_SCHED_CLASS_LOAD_HELPER_CLASS,classloaderHelper.getName());
            factory.setQuartzProperties(properties);
            factory.setJobFactory(springJobFactory);
            factory.afterPropertiesSet();
            Scheduler scheduler = factory.getScheduler();

            exScheduler = new ExScheduler(classLoader, classloaderHelper, scheduler);
            schedulerMap.put(connName,exScheduler);
        }
        return exScheduler;
    }

    static class CustomClassLoaderHelper extends CascadingClassLoadHelper implements ClassLoadHelper {
        private ExtendClassloader extendClassloader;

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            Class<?> aClass = super.loadClass(name);
            if (aClass == null){
                return extendClassloader.loadClass(name);
            }
            return aClass;
        }
    }
}
