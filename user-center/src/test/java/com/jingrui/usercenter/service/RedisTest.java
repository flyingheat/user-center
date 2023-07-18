package com.jingrui.usercenter.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingrui.usercenter.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisTest {

    /**
     * 这个是用的Java原生的序列化器
     */
    @Resource
    private RedisTemplate redisTemplate;

//    @Resource
//    private StringRedisTemplate stringRedisTemplate;

    @Test
    void test(){
        //操作String数据结构
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //增加
        valueOperations.set("jingruiString","lujingrui");
        valueOperations.set("jingruiInt",1);
        valueOperations.set("jingruiDouble",2.0);
        User user = new User();
        user.setId(1L);
        user.setUsername("jingrui");
        valueOperations.set("jingruiUser",user);

        //查
        Object jingruiString = valueOperations.get("jingruiString");
        Assertions.assertTrue("lujingrui".equals((String)jingruiString));
        Object jingruiInt = valueOperations.get("jingruiInt");
        Assertions.assertTrue(1 == ((int) jingruiInt));
        Object jingruiDouble = valueOperations.get("jingruiDouble");
        Assertions.assertTrue(2.0 == ((Double) jingruiDouble));
        Object jingruiUser = valueOperations.get("jingruiUser");
        System.out.println(jingruiUser);
        valueOperations.set("jingruiString","lujingrui");
        redisTemplate.delete("jingruiString");

    }



}
