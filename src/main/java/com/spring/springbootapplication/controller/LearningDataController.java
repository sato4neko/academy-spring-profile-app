package com.spring.springbootapplication.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.spring.springbootapplication.entity.UserInfo;
import com.spring.springbootapplication.dto.LearningRecordAddRequest;
import com.spring.springbootapplication.entity.LearningRecord;
import com.spring.springbootapplication.service.LearningDataService;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.springframework.web.servlet.mvc.support.RedirectAttributes; 

@Controller
public class LearningDataController {
    
    //Service層の定義
    private final LearningDataService learningDataService;

    // カテゴリーの英語名と日本語名のマッピングを定義
    private static final Map<String, String> CATEGORY_NAMES_JA = Map.of(
        "Backend", "バックエンド",
        "Frontend", "フロントエンド",
        "Infrastructure", "インフラ"
    );

    //Serviceを注入
    public LearningDataController(LearningDataService learningDataService) {
        this.learningDataService = learningDataService;
    }

    // 学習情報編集ページの表示
    @GetMapping("/learning/list")
    public String displayLearningDataList(
            @RequestParam(name = "month", required = false) String monthParam, // 月パラメータを受け取る
            @AuthenticationPrincipal UserInfo loggedInUser, 
            Model model) {

        if (loggedInUser == null) {
            return "redirect:/login?error";
        }
        
        Long userId = loggedInUser.getId();

        // 全学習記録の月リストを取得（YYYY-MM-01形式のLocalDateリスト）
        List<LocalDate> allDistinctMonths = learningDataService.getDistinctMonthsByUserId(userId);

        // 月リストを降順にソート
        allDistinctMonths.sort(Comparator.reverseOrder());

        // 2. 表示対象の月 (LocalDate) を決定
        LocalDate targetMonth = null;
        String targetMonthKey = null; 
        
        // monthParamが指定されている場合
        if (monthParam != null && !monthParam.isBlank()) {
            try {
                // YYYYY-MM-01を作成
                LocalDate requestedMonth = LocalDate.parse(monthParam + "-01");
                
                // 月がデータリストに含まれているか確認
                if (allDistinctMonths.contains(requestedMonth)) {
                    targetMonth = requestedMonth;
                    targetMonthKey = monthParam;
                }
            } catch (Exception e) {
                
            }
        }
        
        // targetMonthが未設定の場合は最新の月をデフォルトにする
        if (targetMonth == null && !allDistinctMonths.isEmpty()) {
            targetMonth = allDistinctMonths.get(0); 
            targetMonthKey = targetMonth.toString().substring(0, 7);
        }
        
        // 3. 選択された月（targetMonth）の学習記録を取得し、Mapに格納
        Map<String, Map<String, List<LearningRecord>>> monthlyRecordsMap = new HashMap<>();

        if (targetMonth != null) {
            
            // Service層から特定の月で絞り込んだレコードを取得
            List<LearningRecord> records = learningDataService.findLearningRecordsByUserIdAndMonth(userId, targetMonth);
            
            // 選択された月のデータのみをカテゴリごとにグループ化
            Map<String, List<LearningRecord>> categorizedRecords = records.stream()
                .collect(Collectors.groupingBy(
                    LearningRecord::getCategoryName,
                    Collectors.collectingAndThen(
                        Collectors.toList(), 
                        list -> {
                            list.sort(Comparator.comparing(LearningRecord::getSubjectName));
                            return list;
                        }
                    )
                ));
            
            // targetMonthKeyに対応するデータを格納
            monthlyRecordsMap.put(targetMonthKey, categorizedRecords);
        }

        // Modelにデータを渡す
        model.addAttribute("monthlyRecordsMap", monthlyRecordsMap); 
        model.addAttribute("distinctMonths", allDistinctMonths);    
        model.addAttribute("selectedMonth", targetMonthKey);        

        model.addAttribute("learningDataArchive", true);
        model.addAttribute("pageTitle", "学習情報編集ページ");
        model.addAttribute("itemName", "項目名");
        model.addAttribute("learningTime", "学習時間");

        return "learning/list";
    }

    // 項目追加ページの表示
    @GetMapping(value = "/learning/new")
    public String displayNewLearningRecord(
        @RequestParam(name = "category", required = false) String categoryParam,
        @RequestParam(name = "month", required = false) String monthParam,
        @ModelAttribute("learningRecord") LearningRecordAddRequest request,
        @AuthenticationPrincipal UserInfo loggedInUser,
        Model model) {
                
        // ログインチェック
        if (loggedInUser == null) {
            return "redirect:/login?error";
        }

        // 初期値設定
        LocalDate currentRecordedDate = LocalDate.now().withDayOfMonth(1); // デフォルトは月1日
        String currentCategoryName = "Backend"; 
        
        
        // URLパラメータからの月設定
        if (monthParam != null && !monthParam.isBlank()) {
            try {
                // 'YYYY-MM'形式に'-01'を追加
                LocalDate month = LocalDate.parse(monthParam + "-01");
                currentRecordedDate = month;
            } catch (Exception e) {
                
            }
        }
        
        // URLパラメータからのカテゴリー設定
        if (categoryParam != null && !categoryParam.isBlank()) {
            currentCategoryName = categoryParam.trim();
        } else {
            // デフォルト設定をBackendで設定
            currentCategoryName = "Backend";
        }

        // DTOにセット
        request.setCategoryName(currentCategoryName);

        // LocalDateをStringにフォーマットして設定
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        request.setRecordedDate(currentRecordedDate.format(formatter)); 

        //ページタイトル
        String pageTitle = CATEGORY_NAMES_JA.getOrDefault(currentCategoryName, "未分類") + "学習記録 新規登録";
        model.addAttribute("pageTitle", pageTitle);

        model.addAttribute("currentCategoryName", currentCategoryName);
        model.addAttribute("currentRecordedDate", currentRecordedDate); 
        model.addAttribute("categories", List.of("Backend", "Frontend", "Infrastructure"));
        model.addAttribute("japaneseCategoriesMap", CATEGORY_NAMES_JA);

        return "learning/new";
    }

    // 項目の新規登録を保存
    @PostMapping(value = "/learning/save")
    public String saveLearningData(@Valid @ModelAttribute("learningRecord") LearningRecordAddRequest request,
            BindingResult result,
            @AuthenticationPrincipal UserInfo loggedInUser,
            RedirectAttributes redirectAttributes,
            Model model) {

        // ログインチェック
        if (loggedInUser == null) {
            return "redirect:/login?error";
        }

        // データベース側の重複チェック
        Long userId = loggedInUser.getId();
        String subjectName = request.getItemName();

        // StringをLocalDateに変換
        LocalDate month = null;
        if (request.getRecordedDate() != null) {
            try {
                month = LocalDate.parse(request.getRecordedDate());
            } catch (Exception e) {
                
            }
        }

        // validation(項目の重複チェック)
        if (!result.hasFieldErrors("itemName") && subjectName != null && !subjectName.isBlank() && month != null) {

            boolean isDuplicated = learningDataService.isRecordDuplicated(subjectName, month, userId);

            if (isDuplicated) {

                // 重複エラーを手動でBindingResultに追加
                result.addError(new FieldError(
                        "learningRecord", 
                        "itemName", 
                        request.getItemName(), 
                        false, 
                        null,  
                        null, 
                        "" + subjectName + "は既に登録されています" 
                ));
            }
        }
    
        // validation（form側の設定）
        if (result.hasErrors()) {
            
            List<String> errorList = new ArrayList<String>();
            for (ObjectError error : result.getAllErrors()) {
                errorList.add(error.getDefaultMessage());
            }
            model.addAttribute("validationError", errorList);

            String normalizedCategory = request.getCategoryName() != null ? request.getCategoryName().trim() : "";

            LocalDate recordedDate;

            try {
                recordedDate = request.getRecordedDate() != null ? LocalDate.parse(request.getRecordedDate()) : LocalDate.now().withDayOfMonth(1);
            } catch (Exception e) {
                 recordedDate = LocalDate.now().withDayOfMonth(1);
            }
            
            model.addAttribute("currentCategoryName", normalizedCategory);
            model.addAttribute("currentRecordedDate", recordedDate); 
            
            // ページタイトルを構築
            String errorPageTitle = CATEGORY_NAMES_JA.getOrDefault(normalizedCategory, "未分類") + "項目の追加 新規登録";
            
            // エラー時の再設定
            model.addAttribute("pageTitle", errorPageTitle); 
            model.addAttribute("categories", List.of("Backend", "Frontend", "Infrastructure"));
            model.addAttribute("japaneseCategoriesMap", CATEGORY_NAMES_JA);
            
            return "learning/new";
        }

        // Entityへの変換とデータの保存
        LearningRecord record = new LearningRecord();

        record.setUserId(loggedInUser.getId());
        record.setSubjectName(request.getItemName());
        record.setCategoryName(request.getCategoryName());
        record.setLearningTime(request.getLearningTime());
        record.setMonth(LocalDate.parse(request.getRecordedDate()));

        // データの保存
        try {

            // service層で実行
            learningDataService.saveLearningRecord(record);

            // 成功時のメッセージ
            String categoryJa = CATEGORY_NAMES_JA.getOrDefault(request.getCategoryName(), "未分類");
            String detailedMessage = String.format("%sに%sを%n%d分で追加しました！", 
            categoryJa, request.getItemName(), request.getLearningTime());
                                                
            redirectAttributes.addFlashAttribute("successMessage", detailedMessage);

            // 該当月の一覧ページにリダイレクト
            String recordedDateStr = request.getRecordedDate();
            String redirectMonthParam = "";

            // YYYY-MM形式にフォーマット
            if (recordedDateStr != null && recordedDateStr.length() >= 7) {

                redirectMonthParam = "?month=" + recordedDateStr.substring(0, 7);
            }
            
            // 登録後、月情報を使ってリダイレクト
            return "redirect:/learning/list" + redirectMonthParam;

        } catch (IllegalArgumentException e) {

            // 入力値に問題がある場合のエラー
            redirectAttributes.addFlashAttribute("error", "カテゴリーが見つかりませんでした");
            return "learning/new";
        } catch (Exception e) { 

            // エラーハンドリング
            redirectAttributes.addFlashAttribute("error", "保存中に予期せぬエラーが発生しました");
            return "learning/new";
        }
    }
    
    // 既存の学習記録を更新
    @PostMapping(value = "/learning/update")
    public String updateLearningData(
            @RequestParam("id") Long id,
            @RequestParam("learningTime") int learningTime,
            @AuthenticationPrincipal UserInfo loggedInUser,
            RedirectAttributes redirectAttributes) {

        // ログインチェック
        if (loggedInUser == null) {
            return "redirect:/login?error";
        }

        // 更新処理
        LearningRecord record = new LearningRecord();
        record.setId(id);
        record.setLearningTime(learningTime);

        // リダイレクト先の月を保持
        String redirectMonthParam = "";
        
        // データの更新
        try {
            // 更新対象のレコードの月情報を取得
            LearningRecord recordToUpdate = learningDataService.getLearningRecordById(id);

            // YYYY-MM形式にフォーマット
            if (recordToUpdate != null && recordToUpdate.getMonth() != null) {
                
                redirectMonthParam = "?month=" + recordToUpdate.getMonth().toString().substring(0, 7);
            }

            // service層で実行
            learningDataService.updateLearningRecord(record);

            // 成功時のメッセージ
            redirectAttributes.addFlashAttribute("successMessage", "項目の学習時間を保存しました！");


        } catch (IllegalArgumentException e) {

            // // 入力値に問題がある場合のエラー
            redirectAttributes.addFlashAttribute("error", "更新エラー: IDが不正です");
        } catch (RuntimeException e) {

            // Service層でのエラー
            redirectAttributes.addFlashAttribute("error", "削除エラーが発生しました: " + e.getMessage());
        } catch (Exception e) {

            // その他のエラー
            redirectAttributes.addFlashAttribute("error", "予期せぬエラーが発生しました");
        }
        // 登録後、月情報を使ってリダイレクト
        return "redirect:/learning/list"+ redirectMonthParam;
    }

    // 既存の学習記録を削除する
    @PostMapping(value = "/learning/delete")
    public String deleteLearningRecord(
            @RequestParam("id") Long id,
            @AuthenticationPrincipal UserInfo loggedInUser,
            RedirectAttributes redirectAttributes) {

        if (loggedInUser == null) {
            return "redirect:/login?error";
        }

        // 項目名を、tryブロックの外で初期化する
        String itemName = "";

        // リダイレクト先の月を保持する変数
        String redirectMonthParam = "";

        try {

            // 削除前にレコードから項目名を取得
            LearningRecord recordToDelete = learningDataService.getLearningRecordById(id);

            // レコードが見つからない場合はエラー
            if (recordToDelete == null) {

                throw new IllegalArgumentException("ID: " + id + " の学習記録が見つかりません。");
            }

            // 項目名を取得して格納
            itemName = recordToDelete.getSubjectName();

            // リダイレクト先の月情報を取得し、YYYY-MM形式にフォーマットする
            if (recordToDelete.getMonth() != null) {

                redirectMonthParam = "?month=" + recordToDelete.getMonth().toString().substring(0, 7);
            }

            // 記録を削除する
            learningDataService.deleteLearningRecord(id);

            // 成功メッセージ
            String successMessage = itemName + "を削除しました！";
            redirectAttributes.addFlashAttribute("successMessage", successMessage);

        } catch (IllegalArgumentException e) {

            // 入力値に問題がある場合のエラー
            redirectAttributes.addFlashAttribute("error", "削除エラー: IDが不正です");
        } catch (RuntimeException e) {

            // Service層でのエラー
            redirectAttributes.addFlashAttribute("error", "削除エラーが発生しました: " + e.getMessage());
        } catch (Exception e) {

            // その他のエラー
            redirectAttributes.addFlashAttribute("error", "予期せぬエラーが発生しました");
        }
         // 月情報を使ってリダイレクト
        return "redirect:/learning/list"+ redirectMonthParam;
    }
}
