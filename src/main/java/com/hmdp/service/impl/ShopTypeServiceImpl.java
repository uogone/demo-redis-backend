package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource(name = "stringRedisTemplate")
    ValueOperations<String, String> valueOperations;

    @Override
    public Result findAllType() {
        String key = "cache:types";
        // cache
        String typeJsonArray = valueOperations.get(key);
        if (typeJsonArray != null) {
            return Result.ok(JSONUtil.toList(typeJsonArray, ShopType.class));
        }
        // visit db then cache
        List<ShopType> typeList = query().orderByAsc("sort").list();
        if(typeList.isEmpty()) {
            return Result.fail("商品类型为空");
        }
        valueOperations.set(key, JSONUtil.toJsonStr(typeList));
        return Result.ok(typeList);
    }
}
