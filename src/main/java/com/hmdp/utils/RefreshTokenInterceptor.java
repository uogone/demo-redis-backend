package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.USER_TOKEN_TTL;

@Component
public class RefreshTokenInterceptor implements HandlerInterceptor {

    @Resource(name = "stringRedisTemplate")
    HashOperations<String, String, String> hashOperations;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authToken = request.getHeader("authorization");
        if(StrUtil.isBlank(authToken)) {
            return true;
        }
        Map<String, String> userDTOMap = hashOperations.entries("login:token:" + authToken);
        if(userDTOMap.isEmpty()) {
            return true;
        }
        stringRedisTemplate.expire("login:token:" + authToken, USER_TOKEN_TTL, TimeUnit.MINUTES);
        UserDTO userDTO = new UserDTO();
        BeanUtil.fillBeanWithMap(userDTOMap, userDTO, false);
        UserHolder.saveUser(userDTO);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
