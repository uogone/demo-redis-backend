package com.hmdp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.LOCK_EXPIRE;
import static com.hmdp.utils.RedisConstants.LOCK_SECKILL_KEY;

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
    private ISeckillVoucherService seckillVoucherService;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public Result seckill(Long voucherId) {
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
        Long userId = UserHolder.getUser().getId();
        // 检查时间条件
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voucher.getBeginTime())) {
            return Result.fail("秒杀尚未开始");
        } else if (now.isAfter(voucher.getEndTime())) {
            return Result.fail("秒杀已结束");
        }
        // 检查查询时的库存
        if (voucher.getStock() < 1) {
            return Result.fail("库存不足");
        }

        String lockKey = LOCK_SECKILL_KEY + voucherId + ":" + userId;
        // 防止其他线程释放分布式锁，使用UUID标识，避免多个应用线程ID相同
        String threadId = UUID.randomUUID().toString();
        Boolean locked = stringRedisTemplate
                .opsForValue()
                .setIfAbsent(lockKey, threadId, LOCK_EXPIRE, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            return Result.fail("稍后再试");
        }
        try {
            // 检查一人一单
            Integer count = query().eq("voucher_id", voucherId).eq("user_id", userId).count();
            if (count > 0) {
                return Result.fail("你已拥有该优惠券");
            }
            // 再次检查库存，防止超卖
            boolean success = seckillVoucherService.update()
                    .setSql("stock = stock - 1")
                    .eq("voucher_id", voucherId)
                    .gt("stock", 0)
                    .update();
            if (!success) {
                return Result.fail("库存不足");
            } else {
                VoucherOrder order = new VoucherOrder();
                Long orderId = redisIdWorker.nextId("order");
                order.setVoucherId(voucherId);
                order.setId(orderId);
                order.setUserId(userId);
                save(order);
                return Result.ok(orderId);
            }
        } finally {
            if(threadId.equals(stringRedisTemplate.opsForValue().get(lockKey))) {
                stringRedisTemplate.delete(lockKey);
            }
        }
    }
}
