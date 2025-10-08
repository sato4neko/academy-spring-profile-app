package com.spring.springbootapplication.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.springbootapplication.dto.UserAddRequest;

import com.spring.springbootapplication.entity.UserInfo;
import com.spring.springbootapplication.repository.UserInfoMapper;

/**
 * ユーザー情報 Service
 */
@Service
public class UserInfoService {


    //ユーザー情報 Mapper
    @Autowired
    private UserInfoMapper userInfoMapper;

    //ユーザー情報全件検索
    public List<UserInfo> findAll() {
      return userInfoMapper.findAll();
    }

    //ユーザー情報主キー検索
    public UserInfo findById(Long id) {
      return userInfoMapper.findById(id);
    }
    
    //ユーザ情報登録
    public void save(UserAddRequest userAddRequest) {
      userInfoMapper.save(userAddRequest);
    }
    
}   
