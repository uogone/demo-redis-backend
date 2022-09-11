package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
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
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

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

    @Resource(name = "stringRedisTemplate")
    ValueOperations<String, String> stringValueOperations;

    @Resource(name = "stringRedisTemplate")
    HashOperations<String, String, Object> hashOperations;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone) {
        // 验证手机号
        if(RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号不正确");
        }

        // 生成验证码并保存1分钟
        String code = RandomUtil.randomNumbers(6);
        stringValueOperations.set("login:code:" + phone, code, 1L, TimeUnit.MINUTES);

        // 发送验证码
        log.debug("Login code for " + phone + " is " + code);
        return Result.ok();
    }

    @Override
    public Result registerOrLogin(LoginFormDTO loginForm) {
        String phone = loginForm.getPhone();
        String code = loginForm.getCode();
        if(phone == null || code == null) {
            return Result.fail("手机号或验证码为空");
        }

        String rightCode = stringValueOperations.get("login:code:" + phone);
        if(!code.equals(rightCode)) {
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
        // login
        String token = UUID.randomUUID().toString(true);
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        hashOperations.putAll("login:token:" + token, BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .ignoreNullValue()
                        .setFieldValueEditor((field, value) -> value.toString())));
        stringRedisTemplate.expire("login:token:"  + token, 30L, TimeUnit.MINUTES);
        return Result.ok(token);
    }
}
