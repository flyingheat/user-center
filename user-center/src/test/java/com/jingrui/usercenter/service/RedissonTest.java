package com.jingrui.usercenter.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingrui.usercenter.model.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@Slf4j
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void test(){
        //list
        /*List<String> list = new ArrayList<>();
        list.add("jingrui");
        System.out.println("list:"+list.get(0));
        list.remove(0);


        RList<Object> rList = redissonClient.getList("test-list");
        //rList.add("jingrui");
        System.out.println("rlist:" + rList.get(0));
        rList.remove(0);*/
        //map
        Map<String,Integer> map = new HashMap<>();
        map.put("jingrui",123);
        System.out.println("map->jingrui:"+ map.get("jingrui"));

        RMap<Object, Object> rMap = redissonClient.getMap("text-map");
        //rMap.put("jingrui",312);
        System.out.println("rMap->jingrui:" + rMap.get("jingrui"));
        rMap.remove("jingrui");

        //set
    }

    @Test
    void testWatchDog(){
        RLock lock = redissonClient.getLock("jingrui:precachejob:docach:lock");
        try {
            if(lock.tryLock(0,-1, TimeUnit.MILLISECONDS)){
                Thread.sleep(300000);
                System.out.println("getlock:" + Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage());
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
