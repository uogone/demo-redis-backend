package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.Result;
import com.hmdp.entity.Follow;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IFollowService extends IService<Follow> {

    Result follow(Long authorId, Boolean follow);

    Result followOrNot(Long id);

    Result commonFollows(Long id);

    List<Long> findAllFolloweeIds();

    List<Long> findAllFollowerIds(Long userId);
}
