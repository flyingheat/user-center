package com.jingrui.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jingrui.usercenter.common.ErrorCode;
import com.jingrui.usercenter.exception.BusinessException;
import com.jingrui.usercenter.mapper.TeamMapper;
import com.jingrui.usercenter.model.domain.Team;
import com.jingrui.usercenter.model.domain.User;
import com.jingrui.usercenter.model.domain.UserTeam;
import com.jingrui.usercenter.model.domain.request.TeamJoinRequest;
import com.jingrui.usercenter.model.domain.request.TeamQuitRequest;
import com.jingrui.usercenter.model.domain.request.TeamUpdateRequest;
import com.jingrui.usercenter.model.dto.TeamQuery;
import com.jingrui.usercenter.model.enums.TeamStatusEnum;
import com.jingrui.usercenter.model.vo.TeamUserVO;
import com.jingrui.usercenter.model.vo.UserVo;
import com.jingrui.usercenter.service.TeamService;
import com.jingrui.usercenter.service.UserService;
import com.jingrui.usercenter.service.UserTeamService;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.builder.BuilderException;
import org.apache.poi.ss.formula.functions.T;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


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

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private UserService userService;
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

    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery,boolean isAdmin) {
        QueryWrapper<Team> teamQueryQueryWrapper = new QueryWrapper<>();
        //组合查询条件
        if(teamQuery != null){
            Long id = teamQuery.getId();
            if(id != null && id > 0){
                teamQueryQueryWrapper.eq("id",id);
            }

            List<Long> idList = teamQuery.getIdList();
            if(CollectionUtils.isNotEmpty(idList)){
                teamQueryQueryWrapper.in("id",idList);
            }

            String searchText = teamQuery.getSearchText();
            if(StringUtils.isNotEmpty(searchText)){
                teamQueryQueryWrapper.and(qw -> qw.like("name",searchText).or().like("description",searchText));
            }
            String name = teamQuery.getName();
            if(StringUtils.isNotEmpty(name)){
                teamQueryQueryWrapper.like("name",name);
            }
            String description = teamQuery.getDescription();
            if(StringUtils.isNotEmpty(description)){
                teamQueryQueryWrapper.like("description",description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            if(maxNum != null && maxNum > 0){
                teamQueryQueryWrapper.eq("maxNum",maxNum);
            }
            //根据创建人来查询
            Long userId = teamQuery.getUserId();
            if(userId != null && userId > 0){
                teamQueryQueryWrapper.eq("userId",userId);
            }
            //根据状态来判断
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if(statusEnum == null){
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            if(!isAdmin && statusEnum.equals(TeamStatusEnum.PRIVATE)){
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
                teamQueryQueryWrapper.eq("status",statusEnum.getValue());
        }
        //不展示已过期的队伍
        teamQueryQueryWrapper.and(qw -> qw.gt("expireTime",new Date())).or().isNull("expireTime");
        List<Team> teamList = this.list(teamQueryQueryWrapper);
        if(CollectionUtils.isEmpty(teamList)){
            return new ArrayList<>();
        }
        ArrayList<TeamUserVO> teamUserVOList = new ArrayList<>();
        //关联查询创建人的用户信息
        //@todo 自己实现关联查询
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if(userId == null){
                continue;
            }
            User user = userService.getById(userId);
            //脱敏
            User safetyUser = userService.getSafetyUser(user);
            TeamUserVO teamUserVO = new TeamUserVO();
            try {
                BeanUtils.copyProperties(teamUserVO,team);
                if(user != null){
                    UserVo userVo = new UserVo();
                    BeanUtils.copyProperties(userVo,safetyUser);
                    teamUserVO.setCreateUser(userVo);
                }
                teamUserVOList.add(teamUserVO);
            } catch (Exception e) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
        Team team = new Team();
        try {
            BeanUtils.copyProperties(team,teamQuery);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        return teamUserVOList;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {

        if(teamUpdateRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        Team oldTeam = getTeamById(id);
        //只有管理员和队伍创建者可以修改
        if(oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        //如果他本身就是有密码的
        if(statusEnum.equals(TeamStatusEnum.SECRET)){
            if(StringUtils.isBlank(teamUpdateRequest.getPassword()) && oldTeam.getPassword() == null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"加密房间必须要设置密码");
            }
        }
        Team updateTeam = new Team();
        try {
            BeanUtils.copyProperties(updateTeam,teamUpdateRequest);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return this.updateById(updateTeam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean joinTeam(TeamJoinRequest teamJoinRequest,User loginUser) {

        if (teamJoinRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamJoinRequest.getTeamId();
        Team team = getTeamById(teamId);
        Date expireTime = team.getExpireTime();
        if(team.getExpireTime() != null &&  expireTime.before(new Date())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍已过期");
        }
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if(teamStatusEnum.equals(TeamStatusEnum.PRIVATE)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"禁止加入私有队伍");
        }
        String password = teamJoinRequest.getPassword();
        if(TeamStatusEnum.SECRET.equals(teamStatusEnum)){
            if(StringUtils.isBlank(password) || !password.equals(team.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");
            }
        }

        //该用户已加入的队伍数量
        long userId = loginUser.getId();
        //String.valueOf(userId).intern() 加入的是一个对象
        //这里加锁是因为前端有可能超快的连续点击多次，导致数据还没有来得及跟新到数据库中，出现并发的问题，同一个用户多次加入一个队伍
        //这里有个案例 ： 我和小红都去食堂打菜，但是我们抢的菜不一样，所以我们呢可以同时进行，没必要去等待我打完，小红再去打，所以我们需要降级锁的力度
        //01.26.48视频
        //如果锁队伍id，有一个问题，那就是同一个用户同时加入10个队伍就能突破这个限制
        //可以使用锁，这个是一个单机锁
        //这里可以在redisson的key中加入队伍的id或者userId，用来区分获取不同的资源==》课程可以看一个鱼皮在知识星球中的redis课程
        //redisson的key 可以设置一个常量，去管理，以免key冲突
        //只有一个线程可以获取到锁
        //todo 有个问题：这个分布式锁的力度很大，然后需要我们对不同的用户就不用抢锁的，只针对一个用户
        RLock lock = redissonClient.getLock("jingrui:join_team");
        try {
            //可以设置一个计数器，用来防止死锁，当i大于一个灵界值，我们就直接跳出
            int i = 0;
            //只有一个线程能获取到锁
            while (true) {
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    System.out.println("getlock:" + Thread.currentThread().getId());
                    QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId", userId);
                    long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
                    if (hasJoinNum >= 5) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "最多加入和创建5个队伍");
                    }
                    //不能重复加入已经加入的队伍
                    userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("teamId", teamId);
                    userTeamQueryWrapper.eq("userId", userId);
                    long hasUserJoinNum = userTeamService.count(userTeamQueryWrapper);
                    if (hasUserJoinNum > 0) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户已经加入该队伍");
                    }
                    //已加入队伍的人数
                    long teamHasJoinNum = this.countUserByTeamId(teamId);
                    if (teamHasJoinNum >= team.getMaxNum()) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "队伍已满");
                    }
                    //修改队伍信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(userId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            }
        } catch(InterruptedException e){
                log.error("doCacheRecommendUser error", e);
                return false;
        }finally{
                //为了以防万一，上面报错了就不会执行释放锁了，在 try - catch语句中一定要注意这个事项
                //只能释放自己的锁
                if (lock.isHeldByCurrentThread()) {
                    System.out.println("unlock:" + Thread.currentThread().getId());
                    lock.unlock();
                }
            }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if(teamQuitRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team team = getTeam(teamId == null || teamId <= 0, teamId, ErrorCode.NULL_ERROR);
        long userId = loginUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setUserId(userId);
        queryUserTeam.setTeamId(teamId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper(queryUserTeam);
        long count = userTeamService.count(queryWrapper);
        if(count == 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"未加入当前队伍");
        }
        long teamHasJoinNum = this.countUserByTeamId(teamId);
        //队伍只剩1人
        if(teamHasJoinNum == 1){
           //删除队伍和所有加入队伍关系
           this.removeById(teamId);
        }else {
            //队伍至少还剩2人
            //是否为队长
            if(team.getUserId() == userId){
                //把队伍转移成给最早加入的用户
                // 1. 查询已加入队伍的所有用户和加入时间
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId",teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if(CollectionUtils.isEmpty(userTeamList)  || userTeamList.size() <= 1){
                    throw  new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long nextTeamLeaderId = nextUserTeam.getUserId();
                //更新当前队伍队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                boolean result = this.updateById(updateTeam);
                if(!result){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新队伍队长失败");
                }
            }
            //移除关系
        }
        return userTeamService.remove(queryWrapper);
    }

    private Team getTeam(boolean teamId, Long teamId1, ErrorCode nullError) {
        if(teamId){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = this.getById(teamId1);
        if(team == null){
            throw new BusinessException(nullError,"队伍不存在");
        }
        return team;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(Long id,User loginUser) {

        //2. 校验队伍是否存在
        Team team = getTeamById(id);
        long teamId = team.getId();
        //3. 校验你是不是队长
        if(team.getUserId() != loginUser.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH,"无权限");
        }
        //4. 移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        boolean remove = userTeamService.remove(userTeamQueryWrapper);
        if(!remove){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"删除队伍关联信息失败");
        }
        //5. 删除队伍
        return this.removeById(teamId);

    }

    /**
     * 获取某队伍当前人数
     * @param teamId
     * @return
     */
    private long countUserByTeamId(long teamId){
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }

    /**
     * 根据id获取队伍信息
     * @param id
     * @return
     */
    private Team getTeamById(Long id) {
        if(id == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team oldTeam = this.getById(id);
        if(oldTeam == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }
        return oldTeam;
    }
}