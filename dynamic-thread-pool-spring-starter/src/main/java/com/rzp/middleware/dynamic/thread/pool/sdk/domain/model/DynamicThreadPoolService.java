package com.rzp.middleware.dynamic.thread.pool.sdk.domain.model;

import com.rzp.middleware.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import com.rzp.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;



public class DynamicThreadPoolService implements IDynamicThreadPoolService {

    private final Logger logger = LoggerFactory.getLogger(DynamicThreadPoolService.class);

    private final String applicationName;

    private final Map<String, ThreadPoolExecutor> threadPoolExecutorMap;

    public DynamicThreadPoolService(String applicationName, Map<String, ThreadPoolExecutor> threadPoolExecutorMap) {
        this.applicationName = applicationName;
        this.threadPoolExecutorMap = threadPoolExecutorMap;
    }

    @Override
    public List<ThreadPoolConfigEntity> queryThreadPoolList() {
        Set<String> threadPoolBeanNames = threadPoolExecutorMap.keySet();
        List<ThreadPoolConfigEntity> threadPoolVOS = new ArrayList<>(threadPoolBeanNames.size());
        for (String beanName : threadPoolBeanNames) {
            ThreadPoolExecutor executor = threadPoolExecutorMap.get(beanName);
            ThreadPoolConfigEntity threadPoolConfigVO = new ThreadPoolConfigEntity(applicationName,beanName);

            threadPoolConfigVO.setPoolSize(executor.getPoolSize());
            threadPoolConfigVO.setCorePoolSize(executor.getCorePoolSize());
            threadPoolConfigVO.setMaximumPoolSize(executor.getMaximumPoolSize());
            threadPoolConfigVO.setActiveCount(executor.getActiveCount());
            threadPoolConfigVO.setQueueType(executor.getQueue().getClass().getSimpleName());
            threadPoolConfigVO.setQueueSize(executor.getQueue().size());
            threadPoolConfigVO.setRemainingCapacity(executor.getQueue().remainingCapacity());

            threadPoolVOS.add(threadPoolConfigVO);
        }
        return threadPoolVOS;
    }

    @Override
    public ThreadPoolConfigEntity queryThreadPoolConfigByName(String threadPoolName) {
        ThreadPoolExecutor executor = threadPoolExecutorMap.get(threadPoolName);
        if(null == executor) return new ThreadPoolConfigEntity(applicationName,threadPoolName);

        ThreadPoolConfigEntity threadPoolConfigVO = new ThreadPoolConfigEntity(applicationName,threadPoolName);
        threadPoolConfigVO.setPoolSize(executor.getPoolSize());
        threadPoolConfigVO.setCorePoolSize(executor.getCorePoolSize());
        threadPoolConfigVO.setMaximumPoolSize(executor.getMaximumPoolSize());
        threadPoolConfigVO.setActiveCount(executor.getActiveCount());
        threadPoolConfigVO.setQueueType(executor.getQueue().getClass().getSimpleName());
        threadPoolConfigVO.setQueueSize(executor.getQueue().size());
        threadPoolConfigVO.setRemainingCapacity(executor.getQueue().remainingCapacity());

        return threadPoolConfigVO;
    }

    @Override
    public void updateThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity) {
        if(null == threadPoolConfigEntity || !applicationName.equals(threadPoolConfigEntity.getAppName())) return;
        ThreadPoolExecutor executor = threadPoolExecutorMap.get(threadPoolConfigEntity.getThreadPoolName());
        if(null == executor) return;

        // 设置参数
        executor.setCorePoolSize(threadPoolConfigEntity.getCorePoolSize());
        executor.setMaximumPoolSize(threadPoolConfigEntity.getMaximumPoolSize());
    }

}
