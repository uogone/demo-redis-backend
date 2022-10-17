package com.hmdp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.entity.Blog;

import java.util.List;
import java.util.Optional;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IBlogService extends IService<Blog> {

    Page<Blog> findHotBlog(Integer current);

    Optional<Blog> findById(Long id);

    /**
     *
     * @param id
     * @return true: 点赞成功 false: 取消点赞成功
     */
    Boolean like(Long id);

    Page<Blog> findBlogOfUser(Long userId, Integer pageNo);

    List<Blog> readNewBlogOfFollowee(Long lastId);
}
