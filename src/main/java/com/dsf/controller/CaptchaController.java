 package com.dsf.controller;

 import com.dsf.util.CaptchaUtil;
 import com.dsf.util.Result;
 import org.springframework.data.redis.core.StringRedisTemplate;
 import org.springframework.web.bind.annotation.GetMapping;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RestController;

 import java.util.Map;
 import java.util.UUID;
 import java.util.concurrent.TimeUnit;

 @RestController
 @RequestMapping("/api/captcha")
 public class CaptchaController {
     private final StringRedisTemplate redis;

     public CaptchaController(StringRedisTemplate redis) {
         this.redis = redis;
     }

     @GetMapping
     public Result<Map<String, String>> get() {
         String code = CaptchaUtil.genCode();
         String uuid = UUID.randomUUID().toString();
         redis.opsForValue().set("captcha:" + uuid, code, 2, TimeUnit.MINUTES);
         return Result.success(Map.of("uuid", uuid, "image", CaptchaUtil.toBase64(code)));
     }
 }
