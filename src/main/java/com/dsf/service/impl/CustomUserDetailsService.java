package com.dsf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dsf.entity.User;
import com.dsf.mapper.UserMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 自定义 UserDetailsService
 * Spring Security在登录认证时通过此服务从数据库加载用户信息
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserMapper userMapper;

    public CustomUserDetailsService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 根据用户名加载用户
     * @param username 登录时输入的用户名
     * @return UserDetails 对象（包含用户名、加密密码、角色权限）
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //等同于：SELECT * FROM user WHERE username = ?
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername,username));
        //用户不存在时抛出异常
        if(user==null){
            throw new UsernameNotFoundException("用户不存在");
        }
        // 将数据库中的角色转换为 Spring Security 的权限对象（格式：ROLE_XXX）
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            List.of(new SimpleGrantedAuthority("ROLE_"+user.getRole()))
        );
    }
}
