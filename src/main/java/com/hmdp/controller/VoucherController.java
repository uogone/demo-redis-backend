package com.hmdp.controller;


import com.hmdp.entity.Voucher;
import com.hmdp.service.IVoucherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/voucher")
public class VoucherController {

    @Resource
    private IVoucherService voucherService;

    /**
     * 新增普通券
     *
     * @param voucher 优惠券信息
     * @return 优惠券id
     */
    @PostMapping
    public ResponseEntity<Long> addVoucher(@RequestBody Voucher voucher) {
        voucherService.save(voucher);
        return ResponseEntity.ok(voucher.getId());
    }

    /**
     * 新增秒杀券
     *
     * @param voucher 优惠券信息，包含秒杀信息
     * @return 优惠券id
     */
    @PostMapping("seckill")
    public ResponseEntity<Long> addSeckillVoucher(@RequestBody Voucher voucher) {
        voucherService.addSeckillVoucher(voucher);
        return ResponseEntity.ok(voucher.getId());
    }

    /**
     * 查询店铺的优惠券列表
     *
     * @param shopId 店铺id
     * @return 优惠券列表
     */
    @GetMapping("/list/{shopId}")
    public ResponseEntity<List<Voucher>> queryVoucherOfShop(@PathVariable("shopId") Long shopId) {
       return ResponseEntity.ok(voucherService.queryVoucherOfShop(shopId));
    }
}
