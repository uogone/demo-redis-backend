package com.hmdp.util;

import com.hmdp.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class RedisIdWorkerTest {

    @Resource
    private RedisIdWorker redisIdWorker;

    @Test
    public void testExpire() {
        Long id = redisIdWorker.nextId("test");
        System.out.println(id);
    }
}
