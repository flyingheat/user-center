package com.jingrui.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册 请求体  习惯性的在传输值的时候 实现 序列接口化
 */
@Data
public class UserRegisterRequest implements Serializable {


    private static final long serialVersionUID = -8422672868757682814L;

    private String userAccount;
    private String userPassword;
    private String checkPassword;
    private String planetCode;


}

