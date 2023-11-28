package com.jingrui.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class TeamUpdateRequest implements Serializable {

    private static final long serialVersionUID = 7776530272185831294L;

    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 密码
     */
    private String password;

    /**
     * 状态  0-公开 ， 1-私有，2-加密
     */
    private Integer status;



}

