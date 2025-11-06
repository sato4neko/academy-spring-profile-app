package com.spring.springbootapplication.entity;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Data;

/**
 * ユーザー情報 Entity
 */
@Data
public class UserInfo implements UserDetails{

    //ID
    private Long id;

    //名前
    private String name;

    //メールアドレス
    private String email;

    // パスワードダイジェスト
    private String passwordDigest;

    //画像ファイルのパスやURL
    private String image;

    //自己紹介などの詳細情報
    private String profileDetail;

    //更新日時
    private Date updatedAt;

    //登録日時
    private Date createdAt;

    //Spring Security認証：ユーザー名(メールアドレス)
    @Override
    public String getUsername() {
        return this.email;
    }

    //Spring Security認証：パスワード
    @Override
    public String getPassword() {
        return this.passwordDigest;
    }
    
    //Spring Security認証：権限
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }
    
    //UserDetails メソッド
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}