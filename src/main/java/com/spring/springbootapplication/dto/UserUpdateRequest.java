package com.spring.springbootapplication.dto;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

import com.spring.springbootapplication.form.ValidGroup1;
import com.spring.springbootapplication.form.ValidGroup2;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class UserUpdateRequest implements Serializable {
    
    //更新対象のID
    @NotNull
    private Long id;

    //自己紹介文
    @NotEmpty(message="自己紹介文は必ず入力してください",groups = ValidGroup1.class)
    @Size(min=50,max=200,message = "自己紹介は50文字以上200文字以下で入力してください",groups = ValidGroup2.class)
    private String profileDetail;

    //画像
    private String image;

    //画像ファイルの格納
    private MultipartFile imageFile;
}
