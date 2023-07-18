package com.jingrui.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jingrui.usercenter.common.ErrorCode;
import com.jingrui.usercenter.constant.UserConstant;
import com.jingrui.usercenter.exception.BusinessException;
import com.jingrui.usercenter.service.UserService;
import com.jingrui.usercenter.model.domain.User;
import com.jingrui.usercenter.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.jingrui.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author 陆璟瑞
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2023-02-11 13:32:02
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Resource
    private UserMapper userMapper;
    /**
     * 密码盐值
     */
    private static final String SALT = "yupi";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword,String planetCode) {

        //1.校验
        if(StringUtils.isAllBlank(userAccount,userPassword,checkPassword,planetCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if(userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号过短");
        }

        if(userPassword.length() < 8 || checkPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");
        }
        if(planetCode.length() > 5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号不符");
        }
        //账户不能包含特殊符号
        String validPatten = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPatten).matcher(userAccount);
        if(matcher.find()){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"账户不能包含特殊符号");
        }
        //密码和校验密码相同
        if(!(userPassword.equals(checkPassword))){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码和校验码不相同");
        }
        //账户不能重复
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount",userAccount);
        long count = userMapper.selectCount(userQueryWrapper);
        if(count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户已存在");
        }
        //星球编号不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode",planetCode);
        long count1 = userMapper.selectCount(queryWrapper);
        if(count1 > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号不能重复");
        }
        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        //的作用是将给定的 user 对象保存到某个持久化存储（例如数据库）
        boolean saveResult = this.save(user);
        if(!saveResult){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户保存失败");
        }
        user.setId(userMapper.selectOne(userQueryWrapper).getId());
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验
        if(StringUtils.isAllBlank(userAccount,userPassword)){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"用户名密码不能为空");
        }
        if(userAccount.length() < 4){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"账户长度小于4");
        }
        if(userPassword.length() < 8 ){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"账户密码小于8");
        }
        //账户不能包含特殊符号
        String validPatten = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPatten).matcher(userAccount);
        if(matcher.find()){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"账户不能包含特殊符号");
        }
        //2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //查询账户是否 存在
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount",userAccount);
        userQueryWrapper.eq("userPassword",encryptPassword);
        User user = userMapper.selectOne(userQueryWrapper);
        //用户不存在
        if(user == null){
            log.info("user login failed,userAccount can not match userPassword");
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码错误");
        }
        User safetyUser = getSafetyUser(user);
        //记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }
    /**
     * //用户脱敏
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser){
        //用户脱敏
        User safetyUser = new User();
        if(safetyUser== null) return null;
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserStatus(0);
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        //移除登录态
        HttpSession session = request.getSession();
        session.removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签搜索用户 （内存过滤）
     * @param tagNameList 用户需要拥有的标签
     * @return
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        /*if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
*//*        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

            //拼接查询
            //like '%Python%' and like '%c++%'
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags",tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSatetyUser).collect(Collectors.toList());
        *//*

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //1.先查询所有逻辑
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        //2.在内存中判断是否包含要求的标签
        *//*for (User user : userList) {
            String tags = user.getTags();
            Set<String> tempTagNameSet = gson.fromJson(tags,new TypeToken<Set<String>>(){}.getType());
            for (String tagName : tempTagNameSet) {
                if(!tempTagNameSet.contains(tagName)){
                    return false;
                }
            }
            return true;
        }*//*
        //这里可以采用一个语法糖 Java流 filter 过滤用户
        // stream - >  parallelStream() 就是并发执行 ， 有缺点， 需要一个线程池去统一管理，parallelStream使用的线程池 Java1.7 自带的一个forkJoinPut（好像是）
        return userList.stream().filter(user -> {
            String tags = user.getTags();
            if(StringUtils.isBlank(tags)){
                return false;
            }
            Set<String> tempTagNameSet = gson.fromJson(tags,new TypeToken<Set<String>>(){}.getType());
            // 用ofNullable 去 封装一个可能为空的对象 。 or else 给前面一个值的默认值 Optional Java8的一个选择类 相当于 if语句
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if(!tempTagNameSet.contains(tagName)){
                    return false;
                }
            }
            return true;
        }).map(this::getSatetyUser).collect(Collectors.toList());*/

        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1. 先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        // 2. 在内存中判断是否包含要求的标签
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());

    }

    @Override
    public int updateUser(User user,User loginUser) {
        long userId = user.getId();
        if(userId <= 0 || (user.getUsername() == null  && user.getAvatarUrl()== null && user.getGender() == null  && user.getPhone() == null && user.getEmail() == null)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //to do 补充校验，如果用户没有任何需要更新的值，就直接报错，不用执行更新语句
        //如果是管理员允许跟新任意用户
        //如果不是管理员，只允许更新当前自己的
        //判断权限，仅管理员和自己可以修改
        if(!isAdmin(loginUser) && userId != loginUser.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if(oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if(request == null){
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if(userObj == null){
            throw new  BusinessException(ErrorCode.NO_AUTH);
        }
        return (User) userObj;
    }

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        //仅管理员可以查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == UserConstant.ADMIN_ROLE;
    }

    /**
     * 是否为管理员
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == UserConstant.ADMIN_ROLE;
    }

    /**
     * 根据标签搜索用户 (sql版本)
     * @param tagNameList 用户需要拥有的标签
     * @return
     */
    @Deprecated
    private List<User> searchUsersByTagsBySQL(List<String> tagNameList) {

        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            //拼接查询
            //like '%Python%' and like '%c++%'
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags",tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }
}




