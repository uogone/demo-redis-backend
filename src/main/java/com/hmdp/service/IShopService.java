package com.hmdp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.entity.Shop;

import java.util.Optional;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopService extends IService<Shop> {

    Optional<Shop> findById(Long id);

    Boolean update(Shop shop);

    Page<Shop> findByType(Integer typeId, Integer pageNo);

    Page<Shop> findByName(String name, Integer pageNo);
}
