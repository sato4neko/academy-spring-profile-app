package com.spring.springbootapplication.dto;

import java.io.Serializable;

import com.spring.springbootapplication.form.ValidGroup1;
import com.spring.springbootapplication.form.ValidGroup2;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class UserAddRequest implements Serializable{
    
    /* ユーザー登録機能の追加：名前 → 氏名 に変更 */
    //名前
    @NotEmpty(message="氏名は必ず入力してください",groups = ValidGroup1.class)
    @Size(max=255,message = "氏名は255文字以内で入力してください",groups = ValidGroup2.class)
    private String name;

    //メールアドレス
    @NotEmpty(message="メールアドレスは必ず入力してください",groups = ValidGroup1.class)
    @Pattern(regexp ="^[a-zA-Z0-9_+-]+(.[a-zA-Z0-9_+-]+)*@([a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9].)+[a-zA-Z]{2,}$",message="メールアドレスが正しい形式ではありません",groups = ValidGroup2.class)
    private String email;

    //パスワード
    @NotEmpty(message="パスワードは必ず入力してください",groups = ValidGroup1.class)
    @Pattern(regexp = "^(?=.*?[a-zA-Z])(?=.*?\\d)[a-zA-Z\\d]{8,}$",message = "英数字8文字以上で入力してください",groups = ValidGroup2.class)
    private String passwordDigest;
}
