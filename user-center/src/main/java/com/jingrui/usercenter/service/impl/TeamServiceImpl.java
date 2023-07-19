package com.jingrui.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jingrui.usercenter.common.ErrorCode;
import com.jingrui.usercenter.exception.BusinessException;
import com.jingrui.usercenter.mapper.TeamMapper;
import com.jingrui.usercenter.model.domain.Team;
import com.jingrui.usercenter.model.domain.User;
import com.jingrui.usercenter.model.domain.UserTeam;
import com.jingrui.usercenter.model.enums.TeamStatusEnum;
import com.jingrui.usercenter.service.TeamService;
import com.jingrui.usercenter.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Optional;


/**
* @author 陆璟瑞
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2023-07-18 15:02:46
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Override
    @Transactional(rollbackFor = Exception.class)  // 外层加个事务
    public long addTeam(Team team, User loginUser) {

//        1. 求参数是否为空
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
//        2. 是否登录，未登录不允许创建
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        final long userId = loginUser.getId();
//        3. 校验信息
//        a. 队伍人数 > 1 且 20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if(maxNum < 1 || maxNum > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数不满足要求");
        }
//        b. 队伍标题 <= 20
        String name = team.getName();
        if(StringUtils.isNotBlank(name) && name.length() > 20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍标题不满足要求");
        }
//        c. 描述 <= 512
        String description = team.getDescription();
        if(StringUtils.isBlank(description) || description.length() > 512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍描述过长");
        }
//        d. status是加密状态且密码有的话 <= 32
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if(statusEnum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍状态不满足要求");
        }
//        e. status是否公开（int） 不传默认为0 公开 一定要有密码 ， 且密码 <= 32
        String pwd = team.getPassword();
        if(TeamStatusEnum.SECRET.equals(statusEnum) ){
            if(StringUtils.isBlank(pwd) || pwd.length() > 32){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码设置不正确");
            }
        }
//        f. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if(new Date().after(expireTime)){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"超时时间 > 当前时间");
        }
//        g. 校验用户最多创建5个队伍
        //@todo 有bug 可能同时创建100个队伍
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.eq("userId",userId);
        long hasTeamNum = this.count(teamQueryWrapper);
        if(hasTeamNum >= 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户最多创建5个队伍");
        }
//        4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean save = this.save(team);
        Long teamId = team.getId();
        if(!save || teamId == null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"创建队伍失败");
        }
//        5. 插入用户 => 队伍关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        save = userTeamService.save(userTeam);
        if(!save){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"创建队伍失败");
        }
        return teamId;
    }
}




