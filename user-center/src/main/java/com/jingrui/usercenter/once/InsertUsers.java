package com.jingrui.usercenter.once;
import java.util.Date;

import com.jingrui.usercenter.mapper.UserMapper;
import com.jingrui.usercenter.model.domain.User;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

@Component
public class InsertUsers {
    
    @Resource
    private UserMapper userMapper;

    /**
     * 批量插入用户
     */
    //@Scheduled(initialDelay =  5000,fixedRate = Long.MAX_VALUE)
    public void doInsertUsers(){
        //用来统计插入的时间
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000;
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假用户" + i);
            user.setAvatarUrl("https://tupian.qqw21.com/article/UploadPic/2020-3/202032812342852286.jpg");
            user.setUserAccount("fackjingrui"+i);
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("13235208162");
            user.setTags("[]");
            user.setEmail("123@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("11111111");
            userMapper.insert(user);
        }
        stopWatch.stop();
        long totalTimeMillis = stopWatch.getTotalTimeMillis();
        System.out.println(totalTimeMillis);
    }

    
}
