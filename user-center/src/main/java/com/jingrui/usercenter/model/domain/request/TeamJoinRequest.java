package com.jingrui.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * 用户加入队伍
 */
@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = 7776530272185831294L;

    /**
     * id
     */
    private Long teamId;


    /**
     * 密码
     */
    private String password;


}

