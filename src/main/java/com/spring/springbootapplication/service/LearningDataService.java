package com.spring.springbootapplication.service;

import com.spring.springbootapplication.entity.LearningRecord;
import com.spring.springbootapplication.repository.LearningRecordMapper;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

/**
 * 学習情報 Service
 */
@Service
public class LearningDataService {

    private final LearningRecordMapper learningRecordMapper;
    
    public LearningDataService(LearningRecordMapper learningRecordMapper) {
        this.learningRecordMapper = learningRecordMapper;
    }
    
    public List<LocalDate> getDistinctMonthsByUserId(Long userId) {

        return learningRecordMapper.findDistinctMonthsByUserId(userId);
    }

    public List<LearningRecord> getLearningRecordsByUserIdAndMonth(Long userId, LocalDate targetMonth) {
        
        return learningRecordMapper.findByUserIdAndMonth(userId, targetMonth);
    }
}
