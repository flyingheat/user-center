package com.jingrui.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用删除请求参数
 */
@Data
public class DeleteRequest implements Serializable {


    private static final long serialVersionUID = 403808746468713488L;

    private long id;

}
