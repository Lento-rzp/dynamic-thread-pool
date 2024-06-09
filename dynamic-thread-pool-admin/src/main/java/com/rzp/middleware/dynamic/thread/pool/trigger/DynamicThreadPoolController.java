package com.rzp.middleware.dynamic.thread.pool.trigger;

import com.alibaba.fastjson.JSON;
import com.rzp.middleware.dynamic.thread.pool.sdk.domain.model.entity.ThreadPoolConfigEntity;
import com.rzp.middleware.dynamic.thread.pool.types.Response;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Slf4j
@CrossOrigin("*")
@RequestMapping("/api/v1/dynamic/thread/pool/")
public class DynamicThreadPoolController {

    @Resource
    private RedissonClient redissonClient;

    @RequestMapping(value = "query_thread_pool_list",method = RequestMethod.GET)
    public Response<List<ThreadPoolConfigEntity>> queryThreadPoolList(){
        try {
            RList<ThreadPoolConfigEntity> cacheList = redissonClient.getList("THREAD_POOL_CONFIG_LIST_KEY");
            return Response.<List<ThreadPoolConfigEntity>>builder()
                    .code(Response.Code.SUCCESS.getCode())
                    .info(Response.Code.SUCCESS.getInfo())
                    .data(cacheList)
                    .build();
        }catch (Exception e){
            log.error("查询线程池异常",e);
            return Response.<List<ThreadPoolConfigEntity>>builder()
                    .code(Response.Code.UN_ERROR.getCode())
                    .info(Response.Code.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "query_thread_pool_config",method = RequestMethod.GET)
    public Response<ThreadPoolConfigEntity> queryThreadPoolConfig(
            @RequestParam String appName,
            @RequestParam String threadPoolName
    ){
        try {
            ThreadPoolConfigEntity entity = redissonClient.<ThreadPoolConfigEntity>getBucket("THREAD_POOL_CONFIG_PARAMETER_LIST_KEY" + "_" + appName + "_" + threadPoolName).get();
            return Response.<ThreadPoolConfigEntity>builder()
                    .code(Response.Code.SUCCESS.getCode())
                    .info(Response.Code.SUCCESS.getInfo())
                    .data(entity)
                    .build();
        }catch (Exception e){
            log.error("查询线程池配置异常",e);
            return Response.<ThreadPoolConfigEntity>builder()
                    .code(Response.Code.UN_ERROR.getCode())
                    .info(Response.Code.UN_ERROR.getInfo())
                    .build();
        }
    }

    @RequestMapping(value = "update_thread_pool_config", method = RequestMethod.POST)
    public Response<Boolean> updateThreadPoolConfig(@RequestBody ThreadPoolConfigEntity request) {
        try {
            log.info("修改线程池配置开始 {} {} {}", request.getAppName(), request.getThreadPoolName(), JSON.toJSONString(request));
            RTopic topic = redissonClient.getTopic("DYNAMIC_THREAD_POOL_REDIS_TOPIC" + "_" + request.getAppName());
            topic.publish(request);
            log.info("修改线程池配置完成 {} {}", request.getAppName(), request.getThreadPoolName());
            return Response.<Boolean>builder()
                    .code(Response.Code.SUCCESS.getCode())
                    .info(Response.Code.SUCCESS.getInfo())
                    .data(true)
                    .build();
        }catch (Exception e){
            log.error("修改线程池配置异常 {}", JSON.toJSONString(request), e);
            return Response.<Boolean>builder()
                    .code(Response.Code.UN_ERROR.getCode())
                    .info(Response.Code.UN_ERROR.getInfo())
                    .data(false)
                    .build();
        }
    }
}
