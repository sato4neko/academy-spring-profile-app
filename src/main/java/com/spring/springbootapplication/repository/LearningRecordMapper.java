package com.spring.springbootapplication.repository;

import com.spring.springbootapplication.entity.LearningRecord;

import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LearningRecordMapper {
    
    // 学習記録がある月を取得 
    List<LocalDate> findDistinctMonthsByUserId(@Param("userId") Long userId);

    // 全ての学習記録を取得
    List<LearningRecord> findAllByUserId(Long userId);

    // 学習記録を取得
    LearningRecord getLearningRecordById(Long id); 
    
    // ユーザーIDと指定された月で学習記録を絞り込んで取得
    List<LearningRecord> findByUserIdAndMonth(@Param("userId") Long userId, @Param("targetMonth") LocalDate targetMonth);

    // ユーザーIDと月の学習記録をカテゴリー名と結合して取得
    List<LearningRecord> findLearningRecordsByUserIdAndMonth(
        @Param("userId") Long userId,
        @Param("month") LocalDate month 
    );

    // カテゴリー名（英語）からカテゴリーIDを取得
    Integer findCategoryIdByName(@Param("categoryName") String categoryName);

    // データベースに挿入
    void insertLearningRecord(LearningRecord record);

    // 学習記録の項目名と学習時間を更新
    int updateLearningRecord(LearningRecord record);

    // 学習時間を更新
    int updateLearningTime(@Param("id") Long id, @Param("userId") Long userId, @Param("learningTime") int learningTime);
    
    // 学習記録を削除
    int deleteLearningRecord(Long id);

    // 項目名の重複チェック
    boolean existsBySubjectNameAndMonthAndUserId(
        @Param("subjectName") String subjectName, 
        @Param("month") LocalDate month, 
        @Param("userId") Long userId
    );
    
}
