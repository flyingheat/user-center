package com.jingrui.usercenter.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 队伍和用户信息封装类 （脱敏）
 */
@Data
public class TeamUserVO implements Serializable {

    /**
     *
     */
    @TableId(type = IdType.AUTO)
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
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 创建人id
     */
    private Long userId;

    /**
     * 过期时间
     */
    private Date expireTime;


    /**
     * 状态  0-公开 ， 1-私有，2-加密
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 2280025616177467982L;

    /**
     * 入队用户列表
     */
   // List<UserVo> userList;

    /**
     * 创建人用户信息
     */
    private UserVo createUser;

    /**
     * 加入的用户 数
     */
    private Integer hasJoinNum;
    /**
     * 是否已经加入
     */
    private boolean hasJoin;

}
