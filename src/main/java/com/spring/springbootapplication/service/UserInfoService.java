package com.spring.springbootapplication.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class UserInfoService implements UserDetailsService {
    
@Autowired
private UserInfoMapper userInfoMapper;
    
    //パスワードのハッシュ化
    @Autowired
    private PasswordEncoder passwordEncoder; 

    //ユーザ情報登録
    @Transactional
    public UserAddRequest save(UserAddRequest userAddRequest) {
        
        //メールアドレス・ユーザー名の取得
        String name = userAddRequest.getName();
        String email = userAddRequest.getEmail();
        
        //重複チェック
        UserInfo existingUserByName = userInfoMapper.findByName(name);
        if (Objects.nonNull(existingUserByName)) {
            throw new DuplicateUserException("このユーザー名は既に使用されています。");
        }
        UserInfo existingUser = userInfoMapper.findByEmail(email);
        if (Objects.nonNull(existingUser)) {
            throw new DuplicateUserException("このメールアドレスは既に登録されています。"); 
        }

        //登録処理
        String rawPassword = userAddRequest.getPasswordDigest();
        
        // パスワードのハッシュ化
        String hashedPassword = passwordEncoder.encode(rawPassword);
        userAddRequest.setPasswordDigest(hashedPassword);
        
        //データベースに保存
        userInfoMapper.save(userAddRequest);
        
        //ハッシュ化する前のパスワードをセット
        userAddRequest.setPasswordDigest(rawPassword);
        
        return userAddRequest;
    }

    //Spring Security認証メソッド
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserInfo userInfo = userInfoMapper.findByEmail(email); 
        
        if (Objects.isNull(userInfo)) {
            throw new UsernameNotFoundException("ユーザーが見つかりません");
        }

        return new org.springframework.security.core.userdetails.User(
                userInfo.getEmail(),                  
                userInfo.getPasswordDigest(),
                Collections.emptyList()              
        );
    }
}   
