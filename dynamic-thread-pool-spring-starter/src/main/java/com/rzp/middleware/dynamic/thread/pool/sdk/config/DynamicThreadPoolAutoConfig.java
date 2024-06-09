package com.rzp.middleware.dynamic.thread.pool.sdk.config;

// 动态配置入口
import com.rzp.middleware.dynamic.thread.pool.sdk.domain.IDynamicThreadPoolService;
import com.rzp.middleware.dynamic.thread.pool.sdk.domain.model.DynamicThreadPoolService;
import com.rzp.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import com.rzp.middleware.dynamic.thread.pool.sdk.domain.model.valobj.RegistryEnumVO;
import com.rzp.middleware.dynamic.thread.pool.sdk.registry.IRegistry;
import com.rzp.middleware.dynamic.thread.pool.sdk.registry.redis.RedisRegistry;
import com.rzp.middleware.dynamic.thread.pool.sdk.trigger.job.ThreadPoolDataReportJob;
import com.rzp.middleware.dynamic.thread.pool.sdk.trigger.listener.ThreadPoolConfigAdjustListener;
import io.micrometer.core.instrument.util.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableConfigurationProperties(DynamicThreadPoolAutoProperties.class)
@EnableScheduling
public class DynamicThreadPoolAutoConfig {

    private final Logger logger = LoggerFactory.getLogger(DynamicThreadPoolAutoConfig.class);

    private String applicationName;

    @Bean("redissonClient")
    public RedissonClient redissonClient(DynamicThreadPoolAutoProperties properties){
        Config config = new Config();
        config.setCodec(JsonJacksonCodec.INSTANCE);
        config.useSingleServer()
                .setAddress("redis://" + properties.getHost() + ":" + properties.getPort())
                .setPassword(properties.getPassword())
                .setConnectionPoolSize(properties.getPoolSize())
                .setConnectionMinimumIdleSize(properties.getMinIdleSize())
                .setIdleConnectionTimeout(properties.getConnectTimeout())
                .setConnectTimeout(properties.getConnectTimeout())
                .setRetryAttempts(properties.getRetryAttempts())
                .setRetryInterval(properties.getRetryInterval())
                .setPingConnectionInterval(properties.getPingInterval())
                .setKeepAlive(properties.isKeepAlive())
        ;
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }

    // 初始化注册中心
    @Bean
    public IRegistry redisRegistry(RedissonClient redissonClient){
        return new RedisRegistry(redissonClient);
    }

    @Bean("dynamicThreadPoolService")
    public DynamicThreadPoolService dynamicThreadPoolService(ApplicationContext applicationContext, Map<String,ThreadPoolExecutor> threadPoolExecutorMap,RedissonClient redissonClient){
        applicationName = applicationContext.getEnvironment().getProperty("spring.application.name");

        if (StringUtils.isBlank(applicationName)) {
            applicationName = "缺省的";
            logger.warn("动态线程池，启动提示。SpringBoot 应用未配置 spring.application.name 无法获取到应用名称！");
        }

        // 防止应用启动时 已经配置了线程池的信息
        Set<String> keySet = threadPoolExecutorMap.keySet();
        for (String threadPoolKey : keySet) {
            ThreadPoolConfigEntity threadPoolConfig = redissonClient.<ThreadPoolConfigEntity>getBucket(RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey() + "_" + applicationName + "_" + threadPoolKey).get();
            if(null == threadPoolConfig) continue;
            ThreadPoolExecutor executor = threadPoolExecutorMap.get(threadPoolKey);
            executor.setCorePoolSize(threadPoolConfig.getCorePoolSize());
            executor.setMaximumPoolSize(threadPoolConfig.getMaximumPoolSize());
        }

        return new DynamicThreadPoolService(applicationName,threadPoolExecutorMap);
    }

    // 创建动态注册组件任务.... 定时将数据库信息刷新到redis中...
    @Bean
    public ThreadPoolDataReportJob threadPoolDataReportJob(IDynamicThreadPoolService dynamicThreadPoolService,IRegistry registry){
        return new ThreadPoolDataReportJob(dynamicThreadPoolService,registry);
    }

    @Bean
    public ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener(IDynamicThreadPoolService dynamicThreadPoolService, IRegistry registry) {
        return new ThreadPoolConfigAdjustListener(dynamicThreadPoolService, registry);
    }


    // 添加一个监听
    @Bean(name = "dynamicThreadPoolRedisTopic")
    public RTopic threadPoolConfigAdjustListener(RedissonClient redissonClient,ThreadPoolConfigAdjustListener threadPoolConfigAdjustListener){
        RTopic topic = redissonClient.getTopic(RegistryEnumVO.DYNAMIC_THREAD_POOL_REDIS_TOPIC.getKey() + "_" + applicationName);
        topic.addListener(ThreadPoolConfigEntity.class,threadPoolConfigAdjustListener);
        return topic;
    }

}
