package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.hmdp.utils.LuaScripts;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.hmdp.utils.RedisConstants.SECKILL_INFO_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
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

    @PostConstruct
    public void init() {
        StreamOperations<String, String, String> so = stringRedisTemplate.opsForStream();
        so.createGroup("orders", ReadOffset.from("0"), "ordergroup");
        executor.submit(() -> {
           while (true) {
               VoucherOrder order = null;
               try {
                   List<MapRecord<String, String, String>> msg = so.read(Consumer.from("ordergroup", "c1"),
                           StreamReadOptions.empty().count(1).block(Duration.ZERO),
                           StreamOffset.create("orders", ReadOffset.lastConsumed()));
                   Map<String, String> values = msg.get(0).getValue();
                   order = new VoucherOrder();
                   BeanUtil.fillBeanWithMap(values, order, false);
                   seckillVoucherService
                           .update()
                           .setSql("stock = stock - 1")
                           .eq("voucher_id", order.getVoucherId())
                           .update();
                   save(order);
                   so.acknowledge("ordergroup", msg.get(0));
               } catch (Exception e) {
                   log.error("保存订单失败：" + order);
                   e.printStackTrace();
               }
           }
        });
    }

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
        Long orderId = redisIdWorker.nextId("order");
        // 检查查询时的库存
        Long result = stringRedisTemplate.execute(
                LuaScripts.CHECK_SECKILL_VOUCHER,
                Collections.emptyList(),
                userId.toString(), voucherId.toString(), orderId.toString());
        if (result != 0) {
            return Result.fail(result == 1 ? "库存不足" : "你已拥有该优惠券");
        } else {
            return Result.ok(orderId);
        }
    }
}
