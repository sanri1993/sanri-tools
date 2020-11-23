package com.sanri.tools.modules.quartz.service;

import com.sanri.tools.modules.core.service.plugin.PluginManager;
import com.sanri.tools.modules.database.service.JdbcService;
import com.sanri.tools.modules.quartz.dtos.TriggerCron;
import com.sanri.tools.modules.quartz.dtos.TriggerTask;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.support.CronSequenceGenerator;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class QuartzService {
    @Autowired
    private JdbcService jdbcService;
    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private SpringJobFactory springJobFactory;

    private Map<String, Scheduler> schedulerMap = new ConcurrentHashMap<>();

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
    public void trigger(String connName,JobKey jobKey) throws SchedulerException {
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
    public void pause(String connName,JobKey jobKey) throws SchedulerException {
        Scheduler scheduler = loadScheduler(connName);
        scheduler.pauseJob(jobKey);
    }

    /**
     * 恢复一个任务
     * @param connName
     * @param jobKey
     */
    @GetMapping("/resume")
    public void resume(String connName,JobKey jobKey) throws SchedulerException {
        Scheduler scheduler = loadScheduler(connName);
        scheduler.resumeJob(jobKey);
    }

    /**
     * 删除一个任务
     * @param connName
     * @param triggerKey
     */
    @GetMapping("/remove")
    public void remove(String connName,TriggerKey triggerKey,JobKey jobKey) throws SchedulerException {
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
     * 加载一个日程调度工具
     * @return
     */
    public Scheduler loadScheduler(String connName){
        Scheduler scheduler = schedulerMap.get(connName);
        if (scheduler == null){
            SchedulerFactoryBean factory = new SchedulerFactoryBean();
            factory.setAutoStartup(true);
//            factory.setStartupDelay(5);//延时5秒启动
//            factory.setQuartzProperties();
            factory.setJobFactory(springJobFactory);

            scheduler = factory.getScheduler();
            schedulerMap.put(connName,scheduler);
        }
        return scheduler;

    }
}
