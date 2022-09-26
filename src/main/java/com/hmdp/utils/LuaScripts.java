package com.hmdp.utils;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

public class LuaScripts {

    public static final RedisScript<Long> CHECK_SECKILL_VOUCHER = new DefaultRedisScript<>(
            "local userId = ARGV[1]\n" +
            "local voucherKey = ARGV[2]\n" +
            "local orderKey = ARGV[3]\n" +
            "\n" +
            "-- 检查库存\n" +
            "if(tonumber(redis.call('hget', voucherKey, 'stock')) <= 0) then\n" +
            "    return 1\n" +
            "end\n" +
            "\n" +
            "-- 检查一人一单\n" +
            "if(redis.call('sismember', orderKey, userId) == 1) then\n" +
            "    return 2\n" +
            "end\n" +
            "\n" +
            "-- 减库存\n" +
            "redis.call('hincrby', voucherKey, 'stock', -1)\n" +
            "\n" +
            "-- 保存用户id\n" +
            "redis.call('sadd', orderKey, userId)\n" +
            "\n" +
            "return 0", Long.class);
}
