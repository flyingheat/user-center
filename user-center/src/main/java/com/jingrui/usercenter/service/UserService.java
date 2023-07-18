package com.jingrui.usercenter.service;

import com.jingrui.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.jingrui.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.jingrui.usercenter.constant.UserConstant.USER_LOGIN_STATE;

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
    User getSafetyUser(User originUser);

    /**
     *  请求用户注销
     * @param request
     * @return
     */
    int userLogout(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     * @param tagNameList 用户需要拥有的标签
     * @return
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    /**
     * 用户跟新信息
     * @param user
     * @return
     */
    int updateUser(User user,User loginUser);

    /**
     * 获取当前登录用户信息
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     * @param request
     * @return
     */
     boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     * @param loginUser
     * @return
     */
    boolean isAdmin(User loginUser);
}
