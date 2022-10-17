package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Follow;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.service.IFollowService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.User_Followee_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Resource(name = "stringRedisTemplate")
    private SetOperations<String, String> setOperations;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IUserService userService;

    @Override
    public Boolean follow(Long id) {
        Long followerId = UserHolder.getUser().getId();
        checkCache(followerId);
        String key = User_Followee_KEY + followerId;
        if (setOperations.isMember(key, id.toString())) {
            QueryWrapper<Follow> wrapper = new QueryWrapper<>();
            wrapper.eq("follower_id", followerId).eq("followee_id", id);
            remove(wrapper);
            setOperations.remove(User_Followee_KEY + followerId, id.toString());
            return false;
        } else {
            Follow f = new Follow();
            f.setFollowerId(followerId);
            f.setFolloweeId(id);
            save(f);
            setOperations.add(key, id.toString());
            return true;
        }
    }

    @Override
    public Boolean followOrNot(Long id) {
        Long followerId = UserHolder.getUser().getId();
        String key = User_Followee_KEY + followerId;
        checkCache(followerId);
        return setOperations.isMember(key, id.toString());
    }

    /**
     * 检查是否缓存该用户的关注列表
     * @param followerId 用户id
     */
    private void checkCache(Long followerId) {
        String key = User_Followee_KEY + followerId;
        if (!stringRedisTemplate.hasKey(key)) {
            QueryWrapper<Follow> wrapper = new QueryWrapper<>();
            wrapper.eq("follower_id", followerId).select("followee_id");
            List<Follow> follows = getBaseMapper().selectList(wrapper);
            follows.forEach(e -> setOperations.add(key, e.getFolloweeId().toString()));
        }
    }

    @Override
    public List<UserDTO> commonFollows(Long otherId) {
        Long id = UserHolder.getUser().getId();
        checkCache(id);
        checkCache(otherId);
        Set<String> commonIds = setOperations.intersect(User_Followee_KEY + id, User_Followee_KEY + otherId);
        if (commonIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> ids = commonIds.stream().map(Long::valueOf).collect(Collectors.toList());
        return userService.listByIds(ids)
                .stream()
                .map(e -> BeanUtil.toBean(e, UserDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> findAllFolloweeIds() {
        Long id = UserHolder.getUser().getId();
        checkCache(id);
        return setOperations.members(User_Followee_KEY + id)
                .stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> findAllFollowerIds(Long userId) {
        List<Follow> followers = query().eq("followee_id", userId).list();
        return followers.stream().map(Follow::getFollowerId).collect(Collectors.toList());
    }
}
