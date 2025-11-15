package com.spring.springbootapplication.repository;

import com.spring.springbootapplication.entity.LearningRecord;

import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LearningRecordMapper {
    
    // ユーザーIDから学習記録を取得します
    List<LearningRecord> findByUserIdAndMonth(
        @Param("userId") Long userId,
                
        // 絞り込み用の月パラメータを追加     
        @Param("targetMonth") LocalDate targetMonth
    );

    // 学習記録がある月を取得 
    List<LocalDate> findDistinctMonthsByUserId(@Param("userId") Long userId);
}
