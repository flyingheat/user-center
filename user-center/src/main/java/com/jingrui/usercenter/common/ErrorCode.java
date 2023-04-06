package com.jingrui.usercenter.common;

/**
 * 错误码
 */
public enum ErrorCode {
    /**
     * 如果严谨的话这边要给下面的枚举值打上注释
     */
    PARAMS_ERROR(40000,"请求参数错误",""),
    SUCCESS(0,"ok",""),
    NULL_ERROR(40001,"请求数据为空",""),
    NO_AUTH(40101,"无权限",""),
    NOT_LOGIN(40100,"未登录",""),
    SYSTEM_ERROR(50000,"系统内部异常","");
    //枚举值
    private final int code;
    /**
     * 状态码信息
     */
    private final String message;

    /**
     * 状态码描述
     */
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
