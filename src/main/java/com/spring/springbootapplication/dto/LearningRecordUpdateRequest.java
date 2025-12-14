package com.spring.springbootapplication.dto;

import java.io.Serializable;

import com.spring.springbootapplication.form.UpdateValidationGroup;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class LearningRecordUpdateRequest implements Serializable{

    // ID
    @NotNull(message = "レコードIDが不正です",groups = UpdateValidationGroup.class)
    private Long id;

    // 学習時間
    @Min(value = 1, message = "学習時間は0以上の数字で入力してください",groups = UpdateValidationGroup.class)
    @NotNull(message = "学習時間は必ず入力してください",groups = UpdateValidationGroup.class)
    private Integer learningTime;

    // 学習時間記録月 (YYYY-MM-01形式）
    private String recordedDate;
    
    private String currentMonthKey;

}
