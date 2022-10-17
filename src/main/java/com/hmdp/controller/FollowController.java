package com.hmdp.controller;


import com.hmdp.dto.UserDTO;
import com.hmdp.service.IFollowService;
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
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private IFollowService followService;

    @GetMapping("/or/not/{id}")
    public ResponseEntity<Boolean> followOrNot(@PathVariable Long id) {
        return ResponseEntity.ok(followService.followOrNot(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Boolean> follow(@PathVariable Long id) {
        return ResponseEntity.ok(followService.follow(id));
    }

    @GetMapping("/common/{otherId}")
    public ResponseEntity<List<UserDTO>> findCommonFollowee(@PathVariable Long otherId) {
        return ResponseEntity.ok(followService.commonFollows(otherId));
    }
}
