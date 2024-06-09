package com.rzp.middleware.dynamic.thread.pool.sdk.trigger.listener;

import com.alibaba.fastjson.JSON;
import com.rzp.middleware.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import com.rzp.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import com.rzp.middleware.dynamic.thread.pool.sdk.registry.IRegistry;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;


// 监听动态线程池变更
public class ThreadPoolConfigAdjustListener implements MessageListener<ThreadPoolConfigEntity> {

    private Logger logger = LoggerFactory.getLogger(ThreadPoolConfigAdjustListener.class);

    private final IDynamicThreadPoolService dynamicThreadPoolService;

    private final IRegistry registry;

    public ThreadPoolConfigAdjustListener(IDynamicThreadPoolService dynamicThreadPoolService, IRegistry registry) {
        this.dynamicThreadPoolService = dynamicThreadPoolService;
        this.registry = registry;
    }

    // 监听消息 使用 redis实现.
    @Override
    public void onMessage(CharSequence charSequence, ThreadPoolConfigEntity threadPoolConfigEntity) {
        logger.info("动态线程池 调整线程池配置。线程池名称: {}",threadPoolConfigEntity.getThreadPoolName());
        dynamicThreadPoolService.updateThreadPoolConfig(threadPoolConfigEntity);

        // 更新上报数据.
        List<ThreadPoolConfigEntity> poolList = dynamicThreadPoolService.queryThreadPoolList();
        registry.reportThreadPool(poolList);

        // 上报修改了的这一条的方法.
        ThreadPoolConfigEntity poolConfig = dynamicThreadPoolService.queryThreadPoolConfigByName(threadPoolConfigEntity.getThreadPoolName());
        registry.reportThreadPoolConfigParameter(poolConfig);
        logger.info("动态线程池 上报线程池配置: {}", JSON.toJSONString(poolConfig));
    }


}

