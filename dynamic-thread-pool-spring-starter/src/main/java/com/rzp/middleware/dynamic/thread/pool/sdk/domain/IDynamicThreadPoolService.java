package com.rzp.middleware.dynamic.thread.pool.sdk.domain;

// 动态线程池服务


import com.rzp.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;

import java.util.List;

public interface IDynamicThreadPoolService {

    // 查询全部的线程池
    List<ThreadPoolConfigEntity> queryThreadPoolList();

    // 根据名字查询线程池
    ThreadPoolConfigEntity queryThreadPoolConfigByName(String threadPoolName);

    // 更新线程池配置
    void updateThreadPoolConfig(ThreadPoolConfigEntity threadPoolConfigEntity);

}
