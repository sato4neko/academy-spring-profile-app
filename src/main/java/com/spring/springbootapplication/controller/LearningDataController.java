package com.spring.springbootapplication.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.spring.springbootapplication.entity.UserInfo;
import com.spring.springbootapplication.entity.LearningRecord;
import com.spring.springbootapplication.service.LearningDataService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;

@Controller
public class LearningDataController {
    
    //Service層の定義
    private final LearningDataService learningDataService;

    //Serviceを注入
    public LearningDataController(LearningDataService learningDataService) {
        this.learningDataService = learningDataService;
    }

    // 学習情報編集ページの表示
    @GetMapping(value = "/learning/list")
    public String displayLearningData(Model model, @AuthenticationPrincipal UserInfo loggedInUser) {

        // 未ログイン時の処理
        if (loggedInUser == null) {
            return "redirect:/login?error";
        }

        // ログインユーザーIDを取得
        Long userId = loggedInUser.getId();

        // 月リストを取得
        List<LocalDate> allDistinctMonths = learningDataService.getDistinctMonthsByUserId(userId);

        // 月リストを降順（新しい順）
        allDistinctMonths.sort(Comparator.reverseOrder());

        List<LocalDate> limitedMonths;
        if (allDistinctMonths.size() > 3) {
            // 直近の3ヶ月を取得
            limitedMonths = allDistinctMonths.subList(0, 3);
        } else {
            // 3ヶ月未満の場合
            limitedMonths = allDistinctMonths;
        }

        // 月リストを格納
        model.addAttribute("distinctMonths", limitedMonths);

        // 月ごとの学習記録を格納するMap 
        Map<String, Map<String, List<LearningRecord>>> monthlyRecordsMap = new HashMap<>();

        for (LocalDate month : limitedMonths) {
            // Mapperを呼び出してデータを取得 
            List<LearningRecord> records = learningDataService.getLearningRecordsByUserIdAndMonth(userId, month);
            
            // "yyyy-MM" 形式の文字列
            String key = month.toString().substring(0, 7); 
            
            // レコードをカテゴリー名でグループ化
            // monthlyRecordsMapに格納
            monthlyRecordsMap.put(
                key, 
                records.stream()
                       .collect(Collectors.groupingBy(LearningRecord::getCategoryName))
            ); 
        }

        model.addAttribute("monthlyRecordsMap", monthlyRecordsMap);

        // ヘッダー・フッターのページ固有のCSSの追加
        model.addAttribute("learningDataArchive", true);

        // 各項目の表示
        model.addAttribute("pageTitle", "学習情報編集ページ");
        model.addAttribute("itemName", "項目名");
        model.addAttribute("learningTime", "学習時間");

        return "learning/list";
    }
}
