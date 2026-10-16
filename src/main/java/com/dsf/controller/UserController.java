package com.dsf.controller;

import com.dsf.service.UserService;
import com.dsf.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final StringRedisTemplate redis;

    public UserController(UserService userService, StringRedisTemplate redis){
        this.userService = userService;
        this.redis = redis;
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestParam String username, @RequestParam String password){
        if (username.length() < 3 || username.length() > 50){
            return Result.error(400, "Invalid username length (3-50)");
        }
        if (password.length() < 6){
            return Result.error(400, "Password must be at least 6 characters");
        }
        userService.register(username, password);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestParam String username, @RequestParam String password,
                                              @RequestParam String captchaUuid, @RequestParam String captchaCode,
                                              HttpServletRequest request) {
        // verify captcha
        String key = "captcha:" + captchaUuid;
        String stored = redis.opsForValue().get(key);
        if (stored == null) {
            return Result.error(400, "Verification code expired, please refresh");
        }
        if (!stored.equalsIgnoreCase(captchaCode)) {
            return Result.error(400, "Verification code is incorrect");
        }
        redis.delete(key);

        // check login attempts
        String attemptKey = "login:attempts:" + username;
        String cooldownKey = "login:cooldown:" + username;
        String cooldown = redis.opsForValue().get(cooldownKey);
        if (cooldown != null) {
            long ttl = redis.getExpire(cooldownKey);
            return Result.error(429, "Too many failed attempts, please wait " + ttl + " seconds");
        }

        try {
            userService.login(username, password, request);
            redis.delete(attemptKey);
            return Result.success(Map.of("message", "Login successful"));
        } catch (Exception e) {
            Long count = redis.opsForValue().increment(attemptKey);
            if (count == null) count = 1L;
            if (count >= 5) {
                redis.opsForValue().set(cooldownKey, "1", 60, TimeUnit.SECONDS);
                redis.delete(attemptKey);
                return Result.error(429, "Too many failed attempts, please wait 60 seconds");
            }
            return Result.error(401, "Invalid username or password, " + (5 - count) + " attempts remaining");
        }
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request){
        userService.logout(request);
        return Result.success();
    }
}