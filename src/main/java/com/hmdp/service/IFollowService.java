package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.UserDTO;
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

    /**
     *
     * @param id
     * @return true: 关注成功 false: 取消关注成功
     */
    Boolean follow(Long id);

    Boolean followOrNot(Long id);

    List<UserDTO> commonFollows(Long otherId);

    List<Long> findAllFolloweeIds();

    List<Long> findAllFollowerIds(Long userId);
}
