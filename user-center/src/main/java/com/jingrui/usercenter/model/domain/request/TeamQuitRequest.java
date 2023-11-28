package com.jingrui.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户退出队伍
 */
@Data
public class TeamQuitRequest implements Serializable {

    private static final long serialVersionUID = 7776530272185831294L;

    /**
     * id
     */
    private Long teamId;

}

