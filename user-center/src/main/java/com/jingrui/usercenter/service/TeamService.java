package com.jingrui.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jingrui.usercenter.model.domain.Team;
import com.jingrui.usercenter.model.domain.User;


/**
* @author 陆璟瑞
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-07-18 15:02:46
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     * @param team
     * @return
     */
    long addTeam(Team team,User loginUser);

}
