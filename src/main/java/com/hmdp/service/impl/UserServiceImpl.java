package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.BLOG_LIKED_KEY;
import static com.hmdp.utils.RedisConstants.LOGIN_USER_KEY;

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
    public List<UserDTO> findLikedUserOfBolg(Long blogId) {
        String key = BLOG_LIKED_KEY + blogId;
        // 只显示10个头像
        Set<String> userIdSet = stringRedisTemplate.opsForZSet().reverseRange(key, 0L, 10L);
        if (userIdSet.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> userIdList = userIdSet.stream().map(Long::valueOf).collect(Collectors.toList());
        return query()
                .in("id", userIdList)
                .last("order by field(id," + StrUtil.join(",", userIdList) + ")")
                .list()
                .stream()
                .map(e -> BeanUtil.toBean(e, UserDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Boolean sendCode(String phone) {
        // 验证手机号
//        if(RegexUtils.isPhoneInvalid(phone)) {
//            return Result.fail("手机号无效");
//        }// TODO

        // 生成验证码并保存1分钟
        String code = RandomUtil.randomNumbers(6);
        stringValueOperations.set("login:code:" + phone, code, 1L, TimeUnit.MINUTES);

        // 发送验证码
        log.debug("Login code for " + phone + " is " + code);
        return true;
    }

    @Override
    public String registerOrLogin(LoginFormDTO loginForm) {
        String phone = loginForm.getPhone();
        String code = loginForm.getCode();
        if(phone == null || code == null) {
            return "手机号或验证码为空";
        }

        String rightCode = stringValueOperations.get("login:code:" + phone);
        if(!code.equals(rightCode)) {
            return "验证码不正确";
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
        return "token:" + token;
    }

    @Override
    public void logout() {
        stringRedisTemplate.delete(LOGIN_USER_KEY + UserHolder.getUser().getId());
        UserHolder.removeUser();
    }
}
