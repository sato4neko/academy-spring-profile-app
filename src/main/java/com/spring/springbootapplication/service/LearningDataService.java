package com.spring.springbootapplication.service;

import com.spring.springbootapplication.entity.LearningRecord;
import com.spring.springbootapplication.repository.LearningRecordMapper;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 学習情報 Service
 */
@Service
public class LearningDataService {

    private final LearningRecordMapper learningRecordMapper;

    // Service層の依存関係注入
    public LearningDataService(LearningRecordMapper learningRecordMapper) {
        this.learningRecordMapper = learningRecordMapper;
    }

    // 全ての学習記録を取得
    public List<LearningRecord> getAllRecordsByUserId(Long userId) {
        // MapperにfindAllByUserIdというメソッドがあると仮定して呼び出し
        return learningRecordMapper.findAllByUserId(userId);
    }

    // 学習記録の一覧を表示
    public List<LocalDate> getDistinctMonthsByUserId(Long userId) {

        return learningRecordMapper.findDistinctMonthsByUserId(userId);
    }

    public List<LearningRecord> findLearningRecordsByUserIdAndMonth(Long userId, LocalDate month) {

        return learningRecordMapper.findLearningRecordsByUserIdAndMonth(userId, month);
    }

    // 項目名の重複チェック
    public boolean isRecordDuplicated(String subjectName, LocalDate month, Long userId) {
        
        return learningRecordMapper.existsBySubjectNameAndMonthAndUserId(subjectName, month, userId);
    }

    public LearningRecord getLearningRecordById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Record ID must not be null for retrieval.");
        }
        return learningRecordMapper.getLearningRecordById(id);
    }
    
    // 学習記録を保存
    @Transactional
    public void saveLearningRecord(LearningRecord record) {
        
        Integer categoryId = learningRecordMapper.findCategoryIdByName(record.getCategoryName());

        if (categoryId == null) {
            
            throw new IllegalArgumentException("Category not found for name: " + record.getCategoryName());
        }

        record.setCategoryId(categoryId);

        learningRecordMapper.insertLearningRecord(record);
    }

    // 学習記録を更新
    @Transactional
    public void updateLearningRecord(LearningRecord record) {
        if (record.getId() == null) {
            throw new IllegalArgumentException("Record ID must not be null for update.");
        }

        int updatedRows = learningRecordMapper.updateLearningRecord(record);
        if (updatedRows == 0) {

            throw new RuntimeException(
                    "Failed to update learning record or record not found with ID: " + record.getId());
        }
    }

    // 学習記録を削除
    @Transactional
    public void deleteLearningRecord(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Record ID must not be null for delete.");
        }
        
        int deletedRows = learningRecordMapper.deleteLearningRecord(id);
        if (deletedRows == 0) {
           
            throw new RuntimeException("Failed to delete learning record or record not found with ID: " + id);
        }
    }
}
