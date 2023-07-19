package com.jingrui.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingrui.usercenter.common.BaseResponse;
import com.jingrui.usercenter.common.ErrorCode;
import com.jingrui.usercenter.common.ResultUtils;
import com.jingrui.usercenter.exception.BusinessException;
import com.jingrui.usercenter.model.domain.Team;
import com.jingrui.usercenter.model.domain.User;
import com.jingrui.usercenter.model.domain.request.TeamAddRequest;
import com.jingrui.usercenter.model.domain.request.UserLoginRequest;

import com.jingrui.usercenter.model.dto.TeamQuery;
import com.jingrui.usercenter.service.TeamService;
import com.jingrui.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.List;


/**
 * 用户接口
 */
@RestController
@RequestMapping("/team")
//@CrossOrigin(origins = {"http://user.code-center.cn"},methods = {RequestMethod.DELETE},allowCredentials = "true")
//@CrossOrigin(origins = {"http://localhost:8000"},methods = {RequestMethod.DELETE},allowCredentials = "true")
@CrossOrigin(origins = "http://127.0.0.1:5173/", allowCredentials = "true")
@Slf4j
public class TeamController {
    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) throws InvocationTargetException, IllegalAccessException {
        if(teamAddRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(team,teamAddRequest);
        long teamId = teamService.addTeam(team,loginUser);
        return ResultUtils.success(teamId);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody Long id){
        if(id <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean delete = teamService.removeById(id);
        if(!delete){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
        }
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> deleteTeam(@RequestBody Team team){
        if(team == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = teamService.updateById(team);
        if(!result){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新失败");
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(Long id){
        if(id <= 0){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Team team = teamService.getById(id);

        if(team == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(team);
    }

    @GetMapping("/list")
    public BaseResponse<List<Team>> listTeams(TeamQuery teamQuery){

        if(teamQuery == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Team team = new Team();
        try {
            BeanUtils.copyProperties(team,teamQuery);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        QueryWrapper<Team> teamQueryQueryWrapper = new QueryWrapper<>(team);
        List<Team> teamList = teamService.list(teamQueryQueryWrapper);
        return ResultUtils.success(teamList);
    }

    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery){

        if(teamQuery == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Team team = new Team();
        try {
            BeanUtils.copyProperties(team,teamQuery);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        Page page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> teamQueryQueryWrapper = new QueryWrapper<>(team);
        Page<Team> resultPage = teamService.page(page, teamQueryQueryWrapper);
        return ResultUtils.success(resultPage);
    }


}
