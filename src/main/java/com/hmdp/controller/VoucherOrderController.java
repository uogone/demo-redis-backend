package com.hmdp.controller;


import com.hmdp.service.IVoucherOrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {

    @Resource
    private IVoucherOrderService voucherOrderService;

    @PostMapping("/seckill/{id}")
    public ResponseEntity<HashMap<String, Object>> seckillVoucher(@PathVariable("id") Long voucherId) {
        String s = voucherOrderService.seckill(voucherId);
        HashMap<String, Object> data = new HashMap<>();
        if (s.equals("抢购成功")) {
            data.put("success", true);
        } else {
            data.put("success", false);
            data.put("msg", s);
        }
        return ResponseEntity.ok(data);
    }
}
