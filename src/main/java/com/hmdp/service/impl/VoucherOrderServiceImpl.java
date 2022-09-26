package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.utils.LuaScripts;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.hmdp.utils.RedisConstants.SECKILL_INFO_KEY;
import static com.hmdp.utils.RedisConstants.SUCCESS_USER_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource(name = "stringRedisTemplate")
    private HashOperations<String, String, String> hashOperations;

    @Override
    @Transactional
    public Result seckill(Long voucherId) {
        // 检查时间
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        String key = SECKILL_INFO_KEY + voucherId;
        LocalDateTime beginTime = LocalDateTime.parse(
                hashOperations.get(key, "beginTime"), formatter);
        LocalDateTime endTime = LocalDateTime.parse(
                hashOperations.get(key, "endTime"), formatter);
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(beginTime)) {
            return Result.fail("秒杀尚未开始");
        } else if (now.isAfter(endTime)) {
            return Result.fail("秒杀已结束");
        }
        Long userId = UserHolder.getUser().getId();
        // 检查查询时的库存
        Long result = stringRedisTemplate.execute(
                LuaScripts.CHECK_SECKILL_VOUCHER,
                Collections.emptyList(),
                userId.toString(), SECKILL_INFO_KEY + voucherId, SUCCESS_USER_KEY + voucherId);
        if (result != 0) {
            return Result.fail(result == 1 ? "库存不足" : "你已拥有该优惠券");
        } else {
            Long orderId = redisIdWorker.nextId("order");
            executor.submit(() -> {
                VoucherOrder order = new VoucherOrder();
                order.setVoucherId(voucherId);
                order.setId(orderId);
                order.setUserId(userId);
                seckillVoucherService.update().setSql("stock = stock - 1").eq("voucher_id", voucherId).update();
                save(order);
            });
            return Result.ok(orderId);
        }
    }
}
