package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Component
public class RedisIdWorker {

    public static final Long BEGIN_TIME = 1663594747L;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public Long nextId(String keyPrefix) {
        LocalDateTime now = LocalDateTime.now();
        long cur = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = cur - BEGIN_TIME;

        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        String key = "icr:" + keyPrefix + ":" + date;
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count == 1) {
            stringRedisTemplate.expireAt(key, Instant.now().plus(1L, ChronoUnit.DAYS));
        }
        return timestamp << 32 | count;
    }
}
