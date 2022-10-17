package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IUserService extends IService<User> {

    List<UserDTO> findLikedUserOfBolg(Long blogId);

    Boolean sendCode(String phone);

    String registerOrLogin(LoginFormDTO loginForm);

    void logout();
}
