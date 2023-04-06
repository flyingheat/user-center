package com.jingrui.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jingrui.usercenter.common.ErrorCode;
import com.jingrui.usercenter.exception.BusinessException;
import com.jingrui.usercenter.service.UserService;
import com.jingrui.usercenter.model.domain.User;
import com.jingrui.usercenter.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码不存在");
        }
        User safetyUser = getSatetyUser(user);
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
    public User getSatetyUser(User originUser){
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

        return safetyUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {

        //移除登录态
        HttpSession session = request.getSession();

        session.removeAttribute(USER_LOGIN_STATE);
        return 1;

    }


}




