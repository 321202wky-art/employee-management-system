package com.dsf.service;

import jakarta.servlet.http.HttpServletRequest;

//关于User的业务层接口：注册功能
public interface UserService {
    void register(String username,String password);
    void login(String username, String password, HttpServletRequest request);
    void logout(HttpServletRequest request);


}
