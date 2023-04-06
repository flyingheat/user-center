package com.jingrui.usercenter.service;

import com.jingrui.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author 陆璟瑞
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2023-02-11 13:32:02
*/
public interface UserService extends IService<User> {



    /**
     * 用户注释
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @param planetCode 星球编号
     * @return id 用户id
     */
    long userRegister(String userAccount,String userPassword,String checkPassword,String planetCode);

    /**
     * 返回托名后的用户登录
     * @param userAccount
     * @param userPassword
     * @return
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    User getSatetyUser(User originUser);

    /**
     *  请求用户注销
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

}
