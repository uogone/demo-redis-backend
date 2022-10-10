package com.hmdp.controller;


import com.hmdp.dto.Result;
import com.hmdp.service.IFollowService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

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
    public Result follow(@PathVariable Long id) {
        return followService.followOrNot(id);
    }

    @PutMapping("/{id}/{follow}")
    public Result follow(@PathVariable Long id, @PathVariable Boolean follow) {
        return followService.follow(id, follow);
    }

    @GetMapping("/common/{id}")
    public Result commonFollows(@PathVariable Long id) {
        return followService.commonFollows(id);
    }
}
