package com.rzp.middleware.dynamic.thread.pool.sdk.registry;

// 注册中心接口上报线程数据

import com.rzp.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;

import java.util.List;

public interface IRegistry {

    // 上报线程池
    void reportThreadPool(List<ThreadPoolConfigEntity> threadPoolConfigEntites);

    // 上报线程池配置参数
    void reportThreadPoolConfigParameter(ThreadPoolConfigEntity threadPoolConfigEntity);

}
