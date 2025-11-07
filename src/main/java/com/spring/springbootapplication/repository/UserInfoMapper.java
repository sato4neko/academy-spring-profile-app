package com.spring.springbootapplication.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.spring.springbootapplication.dto.UserAddRequest;
import com.spring.springbootapplication.dto.UserUpdateRequest;
import com.spring.springbootapplication.entity.UserInfo;

/**
 * ユーザー情報 Mapper
 */
@Mapper
public interface UserInfoMapper {
    
    //ユーザー情報全件検索
    List<UserInfo> findAll();

    //ユーザー情報主キー検索
    UserInfo findById(Long id);

    //ユーザー情報登録
    void save(UserAddRequest userRequest);

    //ユーザー情報更新
    void update(UserUpdateRequest userUpdateRequest);
   
    //ユーザーネームで検索
    UserInfo findByName(String name);
    
    //spring Security認証用 
    UserInfo findByEmail(String email);
    
}
