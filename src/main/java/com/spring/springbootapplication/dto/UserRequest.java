package com.spring.springbootapplication.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class UserRequest implements Serializable{
    
    //名前
    @NotEmpty(message="名前は必ず入力してください")
    @Size(max=255,message = "名前は255文字以内で入力してください")
    private String name;

    //メールアドレス
    @NotEmpty(message="メールアドレスは必ず入力してください")
    @Email(message="メールアドレスが正しい形式ではありません")
    private String email;

    //パスワード
    @NotEmpty(message="パスワードは必ず入力してください")
    @Pattern(regexp = "^(?=.*?[a-zA-Z])(?=.*?\\d)[a-zA-Z\\d]{8,}$",message = "英数字8文字以上で入力してください")
    private String password;
}
