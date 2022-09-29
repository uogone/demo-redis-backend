package com.hmdp.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

public class LuaScripts {

    public static final DefaultRedisScript<Long> CHECK_SECKILL_VOUCHER = new DefaultRedisScript<>();

    static {
        CHECK_SECKILL_VOUCHER.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/秒杀券.lua")));
        CHECK_SECKILL_VOUCHER.setResultType(Long.class);
    }
}
