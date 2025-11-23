package com.spring.springbootapplication.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * カテゴリー情報 Entity
 */
@Data
public class LearningRecord {
    //
    private Long id;

    // ユーザーID
    private Long userId;
    
    // カテゴリー名
    private String categoryName;

    // 項目名
    private String subjectName; 
    
    // 学習期間
    private LocalDate month;
      
    // 学習時間
    private Integer learningTime; 
    
    // 更新時間
    private LocalDateTime createdAt;

    // DBから取得した英語のカテゴリー名を日本語に変換して返します
    public String getJapaneseCategoryName() {
        switch (this.categoryName) {
            case "Backend":
                return "バックエンド";
            case "Frontend":
                return "フロントエンド";
            case "Infrastructure":
                return "インフラ";
            default:
                return this.categoryName;
        }
    }
}
