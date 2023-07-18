package com.jingrui.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jingrui.usercenter.common.BaseResponse;
import com.jingrui.usercenter.common.ErrorCode;
import com.jingrui.usercenter.common.ResultUtils;
import com.jingrui.usercenter.exception.BusinessException;
import com.jingrui.usercenter.model.domain.User;
import com.jingrui.usercenter.model.domain.request.UserLoginRequest;
import com.jingrui.usercenter.model.domain.request.UserRegisterRequest;
import com.jingrui.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.jingrui.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
//@CrossOrigin(origins = {"http://user.code-center.cn"},methods = {RequestMethod.DELETE},allowCredentials = "true")
//@CrossOrigin(origins = {"http://localhost:8000"},methods = {RequestMethod.DELETE},allowCredentials = "true")
@CrossOrigin(origins = "http://127.0.0.1:5173/", allowCredentials = "true")
@Slf4j
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 注册
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if (userRegisterRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if(StringUtils.isAnyBlank(userPassword,userAccount,checkPassword,planetCode)){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"密码，账户，校验码，星球码不能为空");
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
        //return new BaseResponse<>(0,result,"ok");
        return ResultUtils.success(result);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUserByTags(@RequestParam(required = false) List<String> tagNameList){
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    //584CBD2368BE17F30C58D41C053F5058
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) attribute;
        if(currentUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //暂时这么写，还没有获取到账号的状态，有可能被封号的
        User user = userService.getById(currentUser.getId());
        User satetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(satetyUser);
    }

    /**
     * 登录
     * @param userLoginRequest
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if (userLoginRequest == null){
            return ResultUtils.error(ErrorCode.NULL_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if(StringUtils.isAnyBlank(userPassword,userAccount)){
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 登录
     * @param
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request){

        if (request == null){
            throw  new BusinessException(ErrorCode.NULL_ERROR);
        }
        int i = userService.userLogout(request);
        return ResultUtils.success(i);
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username,HttpServletRequest request){
        if(!userService.isAdmin(request)){
            //return new ArrayList<>();
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        if(StringUtils.isNoneBlank(username)){
            userQueryWrapper.like("username",username);
        }
        List<User> userList = userService.list(userQueryWrapper);

        List<User> collect = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());

        return ResultUtils.success(collect);
    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize,long pageNum,HttpServletRequest request){
        User loginUser = userService.getLoginUser(request);
        ValueOperations<String, Object> stringObjectValueOperations = redisTemplate.opsForValue();
        //如果有缓存，直接读缓存
        String redisKey = String.format("jingrui:user:recommend:%s",loginUser.getId());
        Page<User> userPage = (Page<User> )redisTemplate.opsForValue().get(redisKey);
        if(userPage!=null){
            return ResultUtils.success(userPage);
        }
        //无缓存
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userPage = userService.page(new Page<>(pageNum,pageSize),userQueryWrapper);
        //写缓存
        try {
            stringObjectValueOperations.set(redisKey,userPage,30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
          log.error("redis set key error",e);
        }
        return ResultUtils.success(userPage);
    }

    @PostMapping("/update")
    private BaseResponse<Integer> updateUser(@RequestBody User user,HttpServletRequest request){
        //1. 校验参数是否为空
        if(user == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        int result = userService.updateUser(user,loginUser);
        return ResultUtils.success(result);
    }
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id,HttpServletRequest request){
        if(!userService.isAdmin(request)){
            throw  new BusinessException(ErrorCode.NO_AUTH);
        }
        if(id <= 0){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);//框架会自动把我们删除改称为逻辑删除 在删除时会自动改为更新
        return ResultUtils.success(b);
    }
}
