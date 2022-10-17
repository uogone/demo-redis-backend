package com.hmdp.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.service.IBlogService;
import com.hmdp.utils.UserHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private IBlogService blogService;

    @PostMapping
    public ResponseEntity<Long> saveBlog(@RequestBody Blog blog) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        // 保存探店博文
        blogService.save(blog);
        // 返回id
        return ResponseEntity.ok(blog.getId());
    }

    @PutMapping("/like/{id}")
    public ResponseEntity<Boolean> likeBlog(@PathVariable("id") Long id) {
        return ResponseEntity.ok(blogService.like(id));
    }

    @GetMapping("/of/me")
    public ResponseEntity<List<Blog>> queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 获取登录用户
        Long userId = UserHolder.getUser().getId();
        // 根据用户查询
        Page<Blog> page = blogService.findBlogOfUser(userId, current);
        return ResponseEntity.ok(page.getRecords());
    }

    @GetMapping("/hot")
    public ResponseEntity<List<Blog>> queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        Page<Blog> page = blogService.findHotBlog(current);
        return ResponseEntity.ok(page.getRecords());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Blog> getBlog(@PathVariable Long id) {
        return ResponseEntity.of(blogService.findById(id));
    }

    @GetMapping("/of/user")
    public ResponseEntity<List<Blog>> queryBlogByUserId(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam("id") Long id) {
        return ResponseEntity.ok(blogService.findBlogOfUser(id, current).getRecords());
    }

    @GetMapping("/of/follow")
    public ResponseEntity<List<Blog>> getBlogOfFollowee(@RequestParam Long lastId) {
        return ResponseEntity.ok(blogService.readNewBlogOfFollowee(lastId));
    }
}
