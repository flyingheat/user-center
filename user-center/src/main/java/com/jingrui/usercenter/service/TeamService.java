package com.jingrui.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jingrui.usercenter.model.domain.Team;
import com.jingrui.usercenter.model.domain.User;
import com.jingrui.usercenter.model.domain.request.TeamJoinRequest;
import com.jingrui.usercenter.model.domain.request.TeamQuitRequest;
import com.jingrui.usercenter.model.domain.request.TeamUpdateRequest;
import com.jingrui.usercenter.model.dto.TeamQuery;
import com.jingrui.usercenter.model.vo.TeamUserVO;

import java.util.List;


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

    /**
     * 搜索队伍
     * @param teamQuery
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery,boolean isAdmin);

    /**
     * 更新队伍
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser);

    /**
     * 用户加入队伍
     * @param teamJoinRequest
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest,User loginUser);

    /**
     * 退出
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 解散某队伍
     * @param id
     * @return
     */
    boolean deleteTeam(Long id,User loginUser);
}
