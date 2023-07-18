package com.jingrui.usercenter.service.impl;

import com.jingrui.usercenter.model.domain.User;
import com.jingrui.usercenter.service.UserService;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.Arrays;
import java.util.List;


@SpringBootTest
class UserServiceImplTest {
    @Resource
    private UserService userService;

    @Test
    void setUserService(){
        String userAccount = "yupi";
        String userPassword = "";
        String checkPassword = "";
        String planetCode = "1";
        long result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1,result);
        userAccount = "yu";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1,result);
        userAccount = "yupi";
        userPassword = "123456";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1,result);
        userAccount = "yu pi";
        userPassword = "12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1,result);
        checkPassword = "123456789";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1,result);
        userAccount = "lujingrui";
        checkPassword = "12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1,result);
        userAccount = "yupi";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1,result);
    }
    @Test
    public void searchUsersByTags(){
        List<String> strings = Arrays.asList("java", "python");
        List<User> userList = userService.searchUsersByTags(strings);
        System.out.println(userList.size());
        Assert.assertNotNull(userList);

    }

}