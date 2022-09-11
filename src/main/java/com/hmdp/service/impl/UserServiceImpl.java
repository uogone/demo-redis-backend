package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result sendCode(String phone, HttpSession session) {
        // 验证手机号
        if(RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号不正确");
        }

        // 生成验证码并保存
        String code = RandomUtil.randomNumbers(6);
        session.setAttribute("loginCode", code);
        session.setAttribute("phone", phone);

        // 发送验证码
        log.debug("Login code for " + phone + " is " + code);
        return Result.ok();
    }

    @Override
    public Result registerOrLogin(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        String code = loginForm.getCode();
        if(phone == null || code == null) {
            return Result.fail("手机号或验证码为空");
        }

        if(!phone.equals(session.getAttribute("phone"))) {
            return Result.fail("手机号不正确");
        }

        if(!code.equals(session.getAttribute("loginCode"))) {
            return Result.fail("验证码不正确");
        }

        User user = query().eq("phone", phone).one();
        // register
        if(user == null) {
            user = new User();
            user.setPhone(phone);
            user.setNickName("用户" + RandomUtil.randomString(6));
            save(user);
        }
        //login
        session.setAttribute("user", BeanUtil.copyProperties(user, UserDTO.class));
        return Result.ok();
    }
}
