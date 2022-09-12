package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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
    ValueOperations<String, String> valueOperations;

    @Override
    public Result findById(Long id) {
        String key = "cache:shop:" + id;
        // visit cache
        String shopJson = valueOperations.get(key);
        if (StrUtil.isNotBlank(shopJson)) {
            return Result.ok(JSONUtil.toBean(shopJson, Shop.class));
        }
        // visit db then cache
        Shop shop = getById(id);
        if (shop == null) {
            return Result.fail("店铺不存在");
        }
        shopJson = JSONUtil.toJsonStr(shop);
        valueOperations.set(key, shopJson);
        return Result.ok(shop);
    }
}
