package com.sanri.tools.modules.quartz.service;

import com.alibaba.fastjson.JSON;
import com.sanri.tools.modules.core.dtos.PluginDto;
import com.sanri.tools.modules.core.exception.ToolException;
import com.sanri.tools.modules.core.service.classloader.ClassloaderService;
import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.core.service.plugin.PluginManager;
import com.sanri.tools.modules.database.service.JdbcService;
import com.sanri.tools.modules.quartz.dtos.TriggerCron;
import com.sanri.tools.modules.quartz.dtos.TriggerTask;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.quartz.impl.StdSchedulerFactory.PROP_SCHED_CLASS_LOAD_HELPER_CLASS;

@Service
@Slf4j
public class QuartzService {
    @Autowired
    private JdbcService jdbcService;
    @Autowired
    private PluginManager pluginManager;
    @Autowired
    private FileManager fileManager;

    @Autowired
    private ClassloaderService classloaderService;

    @Autowired
    private SpringJobFactory springJobFactory;

    private Map<String, Scheduler> schedulerMap = new ConcurrentHashMap<>();

    /**
     * 将数据库连接绑定到 quartz
     * @param connName
     * @param setttings
     */
    public void bindQuartz(String connName,Map<String,Object> setttings) throws Exception {
        Scheduler scheduler = schedulerMap.get(connName);
        if (scheduler != null) {
            log.info("quartz 已经绑定连接了,不能重复绑定");
            return ;
        }
        DataSource dataSource = jdbcService.dataSource(connName);
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setAutoStartup(true);
        factory.setDataSource(dataSource);

        setttings.put(PROP_SCHED_CLASS_LOAD_HELPER_CLASS,CascadingClassLoadHelperExtend.class.getName());
//        setttings.setProperty("org.quartz.jobStore.tablePrefix","qrtz_");
        setttings.put("org.quartz.scheduler.instanceName","SchedulerFactory");
        Properties properties = new Properties();
        properties.putAll(setttings);
        // 序列化当前配置
        serializer(connName,properties);

        factory.setQuartzProperties(properties);
        factory.setJobFactory(springJobFactory);
        factory.afterPropertiesSet();
        scheduler = factory.getScheduler();

        schedulerMap.put(connName,scheduler);
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
     * 编辑或者添加一个 job
     * @param editJobParam
     */
    public void editJob(String connName,EditJobParam editJobParam) throws Exception {
        Scheduler scheduler = loadScheduler(connName);
        JobDetail jobDetail = scheduler.getJobDetail(editJobParam.getJobKey());
        // 创建 job
        JobKey jobKey = editJobParam.getJobKey();
        if (jobDetail != null){
            scheduler.deleteJob(jobKey);
        }
        ClassLoader classloader = classloaderService.getClassloader(editJobParam.getClassloaderName());
        Class<? extends Job> jobClass = (Class<? extends Job>) classloader.loadClass(editJobParam.getClassName());
        jobDetail = JobBuilder.newJob(jobClass).withIdentity(editJobParam.getJobKey()).withDescription(editJobParam.getDescription()).build();
        jobDetail.getJobDataMap().put("jobMethodName", editJobParam.getJobMethodName());

        // 创建触发器
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(editJobParam.getCron());
        TriggerKey triggerKey = TriggerKey.triggerKey("trigger" + jobKey.getName(), jobKey.getGroup());
        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).startNow().withSchedule(cronScheduleBuilder).build();
        Date scheduleJob = scheduler.scheduleJob(jobDetail, trigger);
    }

    /**
     * 触发一个任务
     * @param connName
     * @param jobKey
     * @throws SchedulerException
     */
    @InvokeClassLoader
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
    @InvokeClassLoader
    public void pause(String connName,JobKey jobKey) throws Exception {
        Scheduler scheduler = loadScheduler(connName);
        scheduler.pauseJob(jobKey);
    }

    /**
     * 恢复一个任务
     * @param connName
     * @param jobKey
     */
    @InvokeClassLoader
    public void resume(String connName,JobKey jobKey) throws Exception {
        Scheduler scheduler = loadScheduler(connName);
        scheduler.resumeJob(jobKey);
    }

    /**
     * 删除一个任务
     * @param connName
     * @param triggerKey
     */
    @InvokeClassLoader
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
     * 创建一个日程调度工具
     * @param connName
     * @return
     * @throws Exception
     */
    public Scheduler loadScheduler(String connName) throws Exception {
        Scheduler scheduler = schedulerMap.get(connName);
        if (scheduler == null){
            throw new ToolException("当前连接 "+connName+" 未找到绑定的调度器,请先执行绑定操作");
        }

        return scheduler;
    }

    public static final String MODULE = "quartz";

    @PostConstruct
    public void register() throws IOException {
        pluginManager.register(PluginDto.builder().module("monitor")
                .name("quartz").author("9420")
                .logo("null.png").desc("可视化任务调度").build());

        // 加载序列化的连接配置
        Set<String> settings = fileManager.simpleConfigNames(MODULE, "settings");
        for (String connName : settings) {
            String setting = fileManager.readConfig(MODULE, "settings/" + connName);
            Properties properties = new Properties();
            StringReader stringReader = new StringReader(setting);
            properties.load(stringReader);stringReader.close();
            try {
                bindQuartz(connName,new HashMap<String,Object>((Map)properties));
            } catch (Exception e) {
                log.error("加载以前绑定的调度器失败,连接名为 {},异常信息为:{}",connName,e.getMessage());
            }
        }
    }

    /**
     * 将当前连接配置序列化到文件
     * @param connName
     * @param properties
     */
    public void serializer(String connName,Properties properties) throws IOException {
        String jsonString = JSON.toJSONString(properties);
        fileManager.writeConfig(MODULE,"settings/"+connName,jsonString);
    }
}
