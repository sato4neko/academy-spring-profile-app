package com.spring.springbootapplication.dto;

import java.io.Serializable;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class LearningRecordAddRequest implements Serializable{
    
    // 項目名
    @NotBlank(message = "項目名は必ず入力してください")
    @Size(max = 50, message = "項目名は50文字以内で入力してください")
    private String itemName;

    // カテゴリー名
    @NotBlank(message = "カテゴリーが正しく設定されていません。")
    private String categoryName;

    // 学習時間
    @NotNull(message = "学習時間は必ず入力してください")
    @Min(value = 1, message = "学習時間は0以上の数字で入力してください")
    private Integer learningTime;

    // 学習時間記録月 (YYYY-MM-01形式）
    @NotBlank(message = "記録月が正しく設定されていません。")
    private String recordedDate; 
}
