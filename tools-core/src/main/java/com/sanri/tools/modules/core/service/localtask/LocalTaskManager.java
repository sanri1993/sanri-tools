package com.sanri.tools.modules.core.service.localtask;

import com.sanri.tools.modules.core.dtos.param.PageParam;
import com.sanri.tools.modules.core.service.file.FileManager;
import com.sanri.tools.modules.core.service.localtask.dtos.LocalTask;
import com.sanri.tools.modules.core.service.localtask.dtos.LocalTaskDto;
import com.sanri.tools.modules.core.service.localtask.dtos.TaskInfo;
import com.sanri.tools.modules.core.utils.PageUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cglib.core.ReflectUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 本地任务任务
 */
@Slf4j
@Service
public class LocalTaskManager {

    @Autowired
    private FileManager fileManager;

    @Autowired
    @Qualifier("localTaskThreadPool")
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 本地任务列表 任务ID => LocalTask
     */
    public static final Map<String, LocalTaskDto> LOCAL_TASK_MAP = new ConcurrentHashMap<>();

    /**
     * 本地任务运行时状态 任务ID => 任务运行时信息
     */
    public static final Map<String, TaskInfo> LOCAL_TASK_RUNNING_INFO = new ConcurrentHashMap<>();

    /**
     * 分页查询任务列表
     * @param pageParam
     * @return
     */
    public List<LocalTaskDto> taskListByPage(PageParam pageParam){
        final List<LocalTaskDto> values = new ArrayList<>(LOCAL_TASK_MAP.values());
        Comparator<LocalTaskDto> comparator = (a,b) -> a.getId().compareTo(b.getId());
        Collections.sort(values,comparator);

        final List<LocalTaskDto> localTaskDtos = PageUtil.splitListByPageParam(values, pageParam);
        return localTaskDtos;
    }

    /**
     * 注册任务
     * @param localTask
     * @throws ClassNotFoundException
     */
    public void register(LocalTaskDto localTask) throws ClassNotFoundException {
        // 必须要有 ID 信息
        assert localTask.getId() != null;

        // 断言不重复任务
        assert !LOCAL_TASK_MAP.containsKey(localTask.getId());

        final String className = localTask.getImplClassName();
        final Class<?> implClass = ClassLoader.getSystemClassLoader().loadClass(className);
        assert LocalTask.class.isAssignableFrom(implClass);

        LOCAL_TASK_MAP.put(localTask.getId(),localTask);
    }

    /**
     * 开始任务
     * @param id 任务ID
     * @throws ClassNotFoundException
     */
    public void startTask(final String id) throws ClassNotFoundException {
        final LocalTaskDto localTask = LOCAL_TASK_MAP.get(id);
        final TaskInfo taskInfo = LOCAL_TASK_RUNNING_INFO.computeIfAbsent(id, k -> new TaskInfo());
        final String className = localTask.getImplClassName();
        final Class<?> implClass = ClassLoader.getSystemClassLoader().loadClass(className);

        try {
            final Object taskObject = implClass.newInstance();

            final Method declaredMethod = ReflectUtils.findDeclaredMethod(implClass, "execute", new Class[]{LocalTaskDto.class,TaskInfo.class});

            threadPoolExecutor.submit(() -> {
                try {
                    ReflectionUtils.invokeMethod(declaredMethod, taskObject,new Object[]{localTask,taskInfo});
                }catch (Exception e){
                    log.error("任务[{}]执行失败:{}",id,e.getMessage(),e);
                }
            });
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }

    }
}
