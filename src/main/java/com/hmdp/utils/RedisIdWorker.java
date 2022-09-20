package com.hmdp.utils;

import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWorker {

    public static final Long BEGIN_TIME = 1663594747L;

    @Resource(name = "stringRedisTemplate")
    private ValueOperations<String, String> valueOperations;

    public Long nextId(String keyPrefix) {
        LocalDateTime now = LocalDateTime.now();
        long cur = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = cur - BEGIN_TIME;

        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        long count = valueOperations.increment("icr:" + keyPrefix + ":" + date);

        return timestamp << 32 | count;
    }
}
