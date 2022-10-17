package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.hmdp.utils.SystemConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource(name = "stringRedisTemplate")
    private ValueOperations<String, String> valueOperations;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public Optional<Shop> findById(Long id) {
        Shop shop = queryShopWithMutex(id);
        return Optional.ofNullable(shop);
    }

    /**
     * 用互斥锁解决缓存击穿
     */
    private Shop queryShopWithMutex(Long id) {
        String key = CACHE_SHOP_KEY + id;
        // visit cache
        String shopJson = valueOperations.get(key);
        if (StrUtil.isNotBlank(shopJson)) {
            return JSONUtil.toBean(shopJson, Shop.class);
        } else if ("".equals(shopJson)) {
            return null;
        }
        // visit db then cache
        Boolean ok = valueOperations.setIfAbsent(LOCK_SHOP_KEY + id, "1", LOCK_SHOP_TTL, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(ok)) {
            Shop shop = getById(id);
            if (shop == null) {
                valueOperations.set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
            } else {
                valueOperations.set(key, JSONUtil.toJsonStr(shop), CACHE_SHOP_TTL, TimeUnit.MINUTES);
            }
            return shop;
        } else {
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return queryShopWithMutex(id);
        }
    }

    /**
     * 逻辑过期防止缓存击穿
     */
    private Shop queryShopWithLogicExpire(Long id) {
        // 先返回过期数据，再异步更新缓存
        throw new UnsupportedOperationException("未实现");
    }

    @Override
    @Transactional
    public Boolean update(Shop shop) {
        Long id = shop.getId();
        updateById(shop);
        stringRedisTemplate.delete(CACHE_SHOP_KEY + id);
        return true;
    }

    @Override
    public Page<Shop> findByType(Integer typeId, Integer pageNo) {
        return query()
                .eq("type_id", typeId)
                .page(new Page<>(pageNo, SystemConstants.DEFAULT_PAGE_SIZE));
    }

    @Override
    public Page<Shop> findByName(String name, Integer pageNo) {
        return query().like(StrUtil.isNotBlank(name), "name", name)
                .page(new Page<>(pageNo, SystemConstants.MAX_PAGE_SIZE));
    }
}
