package com.jingrui.usercenter.model.vo;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户包装类 脱敏
 */
@Data
public class UserVo implements Serializable {

    /**
     * id
     */
    private long id;

    /**
     * 用户昵称

     */
    private String username;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 性别
     */
    private Integer gender;


    /**
     * 电话
     */
    private String phone;
    /**
     * 标签列表 json
     */
    private String tags;
    /**
     * 邮箱
     */
    private String email;

    /**
     * 状态 0- 正常
     */
    private Integer userStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 角色 0- 普通用户 1 - 管理员
     */
    private Integer userRole;

    /**
     * 星球编号
     */
    private String planetCode;

    private static final long serialVersionUID = 3118061541495525870L;

}
