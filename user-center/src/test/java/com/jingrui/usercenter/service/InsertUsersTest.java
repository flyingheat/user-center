package com.jingrui.usercenter.service;

import com.jingrui.usercenter.mapper.UserMapper;
import com.jingrui.usercenter.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
public class InsertUsersTest {

    @Resource
    private UserService userService;

    /**
     * 1.线程池默认的线程数
     * 2.最大的线程数
     * 3.存活时间
     * 4.时间单位
     * 5.任务队列可以塞多少个任务, 策略，默认是超过极限后，后面的任务会自动决绝掉 （默认是终端策略） 也可以自己定义策略
     * 面试问题：
     *  1. 什么情况下会超过线程的默认线程数  当60个线程的任务队列都满了，需要更多的线程来处理任务，这样子线程就会慢慢扩容，直到1000个线程数量
     *
     *
     */
    //CPU密集型：分配的核心线程数 = CPU -1
    //IO 密集型： 网络传输，数据库，缓存，消息队列等 分配核心线程数可以大于CPU核数
    private ExecutorService executorService = new ThreadPoolExecutor(40,1000,10000, TimeUnit.MINUTES,new ArrayBlockingQueue<>(10000));

    /**
     * 批量插入  顺序的
     */
    @Test
    public void doInsertUsers(){
        //用来统计插入的时间
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        ArrayList<User> users = new ArrayList<>();
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
            users.add(user);
        }
        //10824  10秒 10万条  还是顺序插入
        userService.saveBatch(users,10000);
        stopWatch.stop();
        long totalTimeMillis = stopWatch.getTotalTimeMillis();
        System.out.println(totalTimeMillis);
    }

    /**
     * 并发批量批量插入
     */
    @Test
    public void doConcurrencyInsertUsers(){
        //用来统计插入的时间
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 100000;
        //分十组
        int j = 0;
        int batchSize = 5000;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            ArrayList<User> users = new ArrayList<>();
            while (true){
                    j++;
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
                    users.add(user);
                if(j % batchSize == 0){
                    break;
                }
            }
            //异步操作
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("threadName : " + Thread.currentThread().getName());
                userService.saveBatch(users, batchSize);
            },executorService);
            futureList.add(future);
        }
        //阻塞一下，等待线程池中的任务都执行完在去执行，否则异步执行上述的插入代码，在线程池中还有任务没执行完呢就会执行下面那个stop 的代码
        //异步执行
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        //10824  10秒 10万条  还是顺序插入

        //  3634   10万
        stopWatch.stop();
        long totalTimeMillis = stopWatch.getTotalTimeMillis();
        System.out.println(totalTimeMillis);
    }

}
