package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.User;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IFollowService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.SystemConstants;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.BLOG_LIKED_KEY;
import static com.hmdp.utils.RedisConstants.USER_INBOX_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Resource
    private IUserService userService;

    @Resource
    private IFollowService followService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Page<Blog> findHotBlog(Integer pageNo) {
        Page<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(pageNo, SystemConstants.MAX_PAGE_SIZE));
        // 填充用户信息
        page.getRecords().forEach(this::fillBlogWithUserInfo);
        return page;
    }

    @Override
    public Optional<Blog> findById(Long id) {
        Blog blog = query().eq("id", id).one();
        if (blog != null) {
            fillBlogWithUserInfo(blog);
        }
        return Optional.ofNullable(blog);
    }

    @Override
    public Boolean like(Long id) {
        ZSetOperations<String, String> ops = stringRedisTemplate.opsForZSet();
        String userId = UserHolder.getUser().getId().toString();
        String key = BLOG_LIKED_KEY + id;

        if (ops.score(key, userId) != null) {
            // 取消点赞
            ops.remove(key, userId);
            update().setSql("liked = liked - 1").eq("id", id).update();
            return false;
        } else {
            // 点赞
            ops.add(key, userId, System.currentTimeMillis());
            update().setSql("liked = liked + 1").eq("id", id).update();
            return true;
        }
    }

    @Override
    public Page<Blog> findBlogOfUser(Long userId, Integer pageNo) {
        return query()
                .eq("user_id", userId)
                .page(new Page<>(pageNo, SystemConstants.MAX_PAGE_SIZE));
    }

    @Override
    public List<Blog> readNewBlogOfFollowee(Long lastId) {
        Long userId = UserHolder.getUser().getId();
        String key = USER_INBOX_KEY + userId;
        Set<String> newBlogIds = stringRedisTemplate.opsForZSet()
                .reverseRange(key, 0L, SystemConstants.MAX_PAGE_SIZE);
        stringRedisTemplate.delete(key);
        // 动态推送问题
        return newBlogIds.stream().map(this::getById).collect(Collectors.toList());
    }

    private void fillBlogWithUserInfo(Blog blog) {
        // author info
        Long authorId = blog.getUserId();
        User author = userService.getById(authorId);
        blog.setName(author.getNickName());
        blog.setIcon(author.getIcon());

        // 当前登录用户
        UserDTO user = UserHolder.getUser();
        if (user != null) {
            Double score = stringRedisTemplate.opsForZSet().score(
                    BLOG_LIKED_KEY + blog.getId(), user.getId().toString());
            if (score != null) {
                blog.setIsLike(true);
            }
        }
    }

    @Override
    public boolean save(Blog entity) {
        boolean saved = super.save(entity);
        if (!saved) return false;
        else {
            Long userId = UserHolder.getUser().getId();
            List<Long> followerIds = followService.findAllFollowerIds(userId);
            ZSetOperations<String, String> zset = stringRedisTemplate.opsForZSet();
            String blogId = entity.getId().toString();
            followerIds.forEach(id -> zset.add(USER_INBOX_KEY + id, blogId, System.currentTimeMillis()));
            return true;
        }
    }
}
