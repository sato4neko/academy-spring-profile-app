package com.spring.springbootapplication.entity;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * ユーザー情報 Entity
 */
@Data
public class UserInfo implements Serializable {

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

}