package com.spring.springbootapplication.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import com.spring.springbootapplication.dto.UserAddRequest;

import com.spring.springbootapplication.entity.UserInfo;
import com.spring.springbootapplication.repository.UserInfoMapper;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Objects;
import java.util.Collections;

/**
 * ユーザー情報 Service
 */
@Service
public class UserInfoService implements UserDetailsService{
@Autowired
    private UserInfoMapper userInfoMapper;
    
    //パスワードのハッシュ化
    @Autowired
    private PasswordEncoder passwordEncoder; 

    //ユーザ情報登録
    public void save(UserAddRequest userAddRequest) {
      String rawPassword = userAddRequest.getPasswordDigest(); 
      String hashedPassword = passwordEncoder.encode(rawPassword);
      userAddRequest.setPasswordDigest(hashedPassword);
      userInfoMapper.save(userAddRequest);
    }

    //Spring Security認証メソッド
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo userInfo = userInfoMapper.findByName(username); 
        
        if (Objects.isNull(userInfo)) {
            throw new UsernameNotFoundException("ユーザーが見つかりません: " + username);
        }

        return new org.springframework.security.core.userdetails.User(
                userInfo.getName(),                  
                userInfo.getPasswordDigest(),
                Collections.emptyList()              
        );
    }
}   
