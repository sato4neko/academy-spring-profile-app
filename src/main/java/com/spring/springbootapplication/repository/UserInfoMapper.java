package com.spring.springbootapplication.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.spring.springbootapplication.dto.UserAddRequest;

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

    //pring Security認証用
    UserInfo findByName(@Param("name") String name);

}
