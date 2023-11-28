package com.jingrui.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingrui.usercenter.mapper.UserMapper;
import com.jingrui.usercenter.model.domain.User;
import com.jingrui.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 缓存预热任务
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private UserService userService;

    private List<Long> mainUserList = Arrays.asList(1L);

    //每天执行，预热推荐用户
    @Scheduled(cron = "0 36 16 * * *")
    public void doCacheRecommendUser(){
        //分布式锁
        RLock lock = redissonClient.getLock("jingrui:precachejob:docache:lock");
        try {
            //只有一个线程能获取到锁
            if(lock.tryLock(0,-1,TimeUnit.MILLISECONDS)){
                System.out.println("getlock:" + Thread.currentThread().getId());
                for (Long userId : mainUserList) {
                    //这一段可优化可不优化， 优化的化，是因为我们代码是从Controller中粘贴的，所以我们可以封装一个工具类，去总的写入缓存，和读取数据库
                    QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 20), userQueryWrapper);
                    String redisKey = String.format("jingrui:user:recommend:%s",userId);
                    ValueOperations<String,Object> valueOperations = redisTemplate.opsForValue();
                    //写缓存
                    try {
                        valueOperations.set(redisKey,userPage,30000, TimeUnit.MILLISECONDS);
                    }catch (Exception e){
                        log.error("redis set key error",e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error",e);
        }finally {
            //为了以防万一，上面报错了就不会执行释放锁了，在 try - catch语句中一定要注意这个事项
            //只能释放自己的锁
            if(lock.isHeldByCurrentThread()){
                System.out.println("unlock:" + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
}
