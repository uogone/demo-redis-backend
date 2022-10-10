package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Follow;
import com.hmdp.entity.User;
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

import static com.hmdp.utils.RedisConstants.USER_FOLLOW_KEY;

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
    public Result follow(Long authorId, Boolean follow) {
        Long fansId = UserHolder.getUser().getId();
        if (follow) {
            Follow f = new Follow();
            f.setUserId(fansId);
            f.setFollowUserId(authorId);
            save(f);
            String key = USER_FOLLOW_KEY + fansId;
            setOperations.add(key, authorId.toString());
        } else {
            QueryWrapper<Follow> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", fansId).eq("follow_user_id", authorId);
            remove(wrapper);
            setOperations.remove(USER_FOLLOW_KEY + fansId, authorId.toString());
        }
        return Result.ok();
    }

    @Override
    public Result followOrNot(Long id) {
        Long fansId = UserHolder.getUser().getId();
        String key = USER_FOLLOW_KEY + fansId;
        checkCache(fansId);
        return Result.ok(setOperations.isMember(key, id.toString()));
    }

    /**
     * 检查是否缓存该用户的关注列表
     * @param id 用户id
     */
    private void checkCache(Long id) {
        String key = USER_FOLLOW_KEY + id;
        if (!stringRedisTemplate.hasKey(key)) {
            List<Follow> follows = query().eq("user_id", id).list();
            follows.forEach(e -> setOperations.add(key, e.getFollowUserId().toString()));
        }
    }

    @Override
    public Result commonFollows(Long id) {
        Long fansId = UserHolder.getUser().getId();
        checkCache(id);
        checkCache(fansId);
        Set<String> commonIds = setOperations.intersect(USER_FOLLOW_KEY + fansId, USER_FOLLOW_KEY + id);
        if (commonIds == null || commonIds.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        List<Long> ids = commonIds.stream().map(Long::valueOf).collect(Collectors.toList());
        List<User> commonFollows = userService.listByIds(ids);
        return Result.ok(commonFollows
                .stream()
                .map(e -> BeanUtil.toBean(e, UserDTO.class))
                .collect(Collectors.toList()));
    }
}
