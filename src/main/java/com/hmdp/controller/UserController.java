package com.hmdp.controller;


import cn.hutool.core.bean.BeanUtil;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.entity.UserInfo;
import com.hmdp.service.IUserInfoService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    /**
     * 发送手机验证码
     */
    @PostMapping("/code")
    public ResponseEntity<Boolean> sendCode(@RequestParam("phone") String phone) {
        // 发送短信验证码并保存验证码
        return ResponseEntity.ok(userService.sendCode(phone));
    }

    @GetMapping("/liker/of/blog/{blogId}")
    public ResponseEntity<List<UserDTO>> getLikerOfBlog(@PathVariable Long blogId) {
        return ResponseEntity.ok(userService.findLikedUserOfBolg(blogId));
    }

    /**
     * 登录功能
     *
     * @param loginForm 登录参数，包含手机号、验证码；或者手机号、密码
     */
    @PostMapping("/login")
    public ResponseEntity<HashMap<String, Object>> login(@RequestBody LoginFormDTO loginForm) {
        String s = userService.registerOrLogin(loginForm);
        HashMap<String, Object> data = new HashMap<>();
        if (s.startsWith("token:")) {
            data.put("success", true);
            data.put("token", s.split(":")[1]);
        } else {
            data.put("success", false);
            data.put("msg", s);
        }
        return ResponseEntity.ok(data);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> me(){
        return ResponseEntity.ok(UserHolder.getUser());
    }

    @GetMapping("/info/{id}")
    public ResponseEntity<UserInfo> info(@PathVariable("id") Long userId){
        // 查询详情
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            // 没有详情，应该是第一次查看详情
            return ResponseEntity.ok(null);
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        // 返回
        return ResponseEntity.ok(info);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> queryUserById(@PathVariable("id") Long userId){
        // 查询详情
        User user = userService.getById(userId);
        if (user == null) {
            return ResponseEntity.noContent().build();
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        // 返回
        return ResponseEntity.ok(userDTO);
    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout() {
        userService.logout();
        return ResponseEntity.noContent().build();
    }
}
