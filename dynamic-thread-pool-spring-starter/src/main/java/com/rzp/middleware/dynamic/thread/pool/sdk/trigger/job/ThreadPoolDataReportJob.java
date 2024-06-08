package com.rzp.middleware.dynamic.thread.pool.sdk.trigger.job;

// 线程池数据上报任务

import com.alibaba.fastjson.JSON;
import com.rzp.middleware.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import com.rzp.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import com.rzp.middleware.dynamic.thread.pool.sdk.registry.IRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

public class ThreadPoolDataReportJob {

    private Logger logger = LoggerFactory.getLogger(ThreadPoolDataReportJob.class);

    private final IDynamicThreadPoolService dynamicThreadPoolService;

    private final IRegistry registry;

    public ThreadPoolDataReportJob(IDynamicThreadPoolService dynamicThreadPoolService, IRegistry registry) {
        this.dynamicThreadPoolService = dynamicThreadPoolService;
        this.registry = registry;
    }

    @Scheduled(cron = "0/20 * * * * ?")
    public void execReportThreadPoolList(){
        List<ThreadPoolConfigEntity> poolList = dynamicThreadPoolService.queryThreadPoolList();
        registry.reportThreadPool(poolList);
        logger.info("动态线程池 上报线程池信息: {}", JSON.toJSONString(poolList));

        for (ThreadPoolConfigEntity threadPoolConfig : poolList) {
            registry.reportThreadPoolConfigParameter(threadPoolConfig);
            logger.info("动态线程池 上报线程池配置: {}", JSON.toJSONString(threadPoolConfig));
        }
    }

}
