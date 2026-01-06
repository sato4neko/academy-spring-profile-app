package com.spring.springbootapplication.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.spring.springbootapplication.entity.UserInfo;
import com.spring.springbootapplication.form.UpdateValidationGroup;
import com.spring.springbootapplication.dto.LearningRecordAddRequest;
import com.spring.springbootapplication.dto.LearningRecordUpdateRequest;
import com.spring.springbootapplication.entity.LearningRecord;
import com.spring.springbootapplication.service.LearningDataService;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    private static final Map<String, String> CATEGORY_NAMES_JA = new LinkedHashMap<>();
    static {
        CATEGORY_NAMES_JA.put("Backend", "バックエンド");
        CATEGORY_NAMES_JA.put("Frontend", "フロントエンド");
        CATEGORY_NAMES_JA.put("Infrastructure", "インフラ");
    }

    //Serviceを注入
    public LearningDataController(LearningDataService learningDataService) {
        this.learningDataService = learningDataService;
    }

    // 学習情報編集ページの表示
    @GetMapping("/learning/list")
    public String displayLearningDataList(
            @RequestParam(name = "month", required = false) String monthParam, 
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
    
        // 表示対象の月 (LocalDate) を決定
        LocalDate targetMonth = null;
        String targetMonthKey = null; 
        
        // monthParamが指定されている場合
        if (monthParam != null && !monthParam.isBlank()) {
            try {
                // YYYYY-MM-01を作成
                targetMonth = LocalDate.parse(monthParam + "-01");
                targetMonthKey = monthParam;
            } catch (Exception e) {
                
            }
        }
        
        // targetMonthが未設定の場合は最新の月をデフォルトにする
        if (targetMonth == null && !allDistinctMonths.isEmpty()) {
            targetMonth = allDistinctMonths.get(0); 
            targetMonthKey = targetMonth.toString().substring(0, 7);
        }
        
        // 選択された月（targetMonth）の学習記録を取得し、Mapに格納
        Map<String, Map<String, List<LearningRecord>>> monthlyRecordsMap = new HashMap<>();
    
        if (targetMonth != null) {
            
            List<LearningRecord> records = learningDataService.findLearningRecordsByUserIdAndMonth(userId, targetMonth);
            
            // カテゴリーごとにグループ化
            Map<String, List<LearningRecord>> grouped = records.stream()
                .collect(Collectors.groupingBy(LearningRecord::getCategoryName));

            // CATEGORY_NAMES_JA のキー順に並び替え
            Map<String, List<LearningRecord>> orderedCategorizedRecords = new LinkedHashMap<>();
            
            for (String categoryKey : CATEGORY_NAMES_JA.keySet()) {
                // 該当カテゴリーのリスト取得（なければ空リスト）
                List<LearningRecord> list = grouped.getOrDefault(categoryKey, new ArrayList<>());
                
                // 項目名でソート
                list.sort(Comparator.comparing(LearningRecord::getSubjectName));
                
                // LinkedHashMapに追加することで表示順序を確定
                orderedCategorizedRecords.put(categoryKey, list);
            }
            
            // targetMonthKeyに対応するデータを格納
            monthlyRecordsMap.put(targetMonthKey, orderedCategorizedRecords);
        }
    
        // Modelにデータを渡す
        model.addAttribute("monthlyRecordsMap", monthlyRecordsMap); 
        model.addAttribute("distinctMonths", allDistinctMonths);    
        model.addAttribute("selectedMonth", targetMonthKey);
        
        // 追加: 日本語カテゴリー名マップを渡す
        model.addAttribute("japaneseCategoriesMap", CATEGORY_NAMES_JA);
    
        boolean isArchive = true; 
        model.addAttribute("learningDataArchive", isArchive);
        
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

        // JavaScript連携のために、YYYY-MM形式の月情報をModelにセットする
        String currentMonthKey = currentRecordedDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        model.addAttribute("currentMonthKey", currentMonthKey);

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
            // RedirectAttributes redirectAttributes,
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

        LocalDate targetDate = null;

        // データの保存
        try {

            // targetDateの代入
            targetDate = LocalDate.parse(request.getRecordedDate()); 
            record.setMonth(targetDate);

            // service層で実行
            learningDataService.saveLearningRecord(record);

            String redirectMonthKey = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

            // 成功時のメッセージ
            String categoryJa = CATEGORY_NAMES_JA.getOrDefault(request.getCategoryName(), "未分類");
            String detailedMessage = String.format("%sに%sを%n%d分で追加しました！",
                    categoryJa, request.getItemName(), request.getLearningTime());
        
            model.addAttribute("currentMonthKey", redirectMonthKey);
            model.addAttribute("successMessage", detailedMessage);

            //  成功時のModelデータ再設定 
            model.addAttribute("currentCategoryName", request.getCategoryName());
            model.addAttribute("currentRecordedDate", LocalDate.parse(request.getRecordedDate())); 
            model.addAttribute("categories", List.of("Backend", "Frontend", "Infrastructure"));
            model.addAttribute("japaneseCategoriesMap", CATEGORY_NAMES_JA); 
            model.addAttribute("pageTitle", "学習記録 新規登録"); 
            
            // 登録後、月情報を使ってリダイレクト
            return "learning/new";

        } catch (IllegalArgumentException e) {

            try {
                targetDate = request.getRecordedDate() != null ? LocalDate.parse(request.getRecordedDate()) : LocalDate.now().withDayOfMonth(1);
            } catch (Exception dateEx) {
                targetDate = LocalDate.now().withDayOfMonth(1);
            }

            // 入力値に問題がある場合のエラー
            model.addAttribute("error", "カテゴリーが見つかりませんでした");
            
            String currentMonthKey = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

            model.addAttribute("currentMonthKey", currentMonthKey);
            model.addAttribute("currentCategoryName", request.getCategoryName());
            model.addAttribute("currentRecordedDate", targetDate); 
            model.addAttribute("categories", List.of("Backend", "Frontend", "Infrastructure"));
            model.addAttribute("japaneseCategoriesMap", CATEGORY_NAMES_JA); 
            model.addAttribute("pageTitle", "エラー"); 

            return "learning/new";

        } catch (Exception e) {
            
            try {
            targetDate = request.getRecordedDate() != null ? LocalDate.parse(request.getRecordedDate()) : LocalDate.now().withDayOfMonth(1);
            } catch (Exception dateEx) {
                targetDate = LocalDate.now().withDayOfMonth(1);
            }
    
            String currentMonthKey = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM"));

            model.addAttribute("currentMonthKey", currentMonthKey);

            model.addAttribute("currentCategoryName", request.getCategoryName());
            model.addAttribute("currentRecordedDate", targetDate); 
            model.addAttribute("categories", List.of("Backend", "Frontend", "Infrastructure"));
            model.addAttribute("japaneseCategoriesMap", CATEGORY_NAMES_JA); 
            model.addAttribute("pageTitle", "エラー"); 

            // エラーハンドリング
            model.addAttribute("error", "保存中に予期せぬエラーが発生しました");

            return "learning/new";
        }
    }
    
    // 既存の学習記録を更新
    @PostMapping(value = "/learning/update")
    public String updateLearningData(
        @Validated (UpdateValidationGroup.class)
        @ModelAttribute LearningRecordUpdateRequest request,
        BindingResult result,
        @AuthenticationPrincipal UserInfo loggedInUser,
        RedirectAttributes redirectAttributes,
        Model model) {

        // ログインチェック
        if (loggedInUser == null) {
            return "redirect:/login?error";
        }

        if (result.hasErrors()) {

            Long userId = loggedInUser.getId();
            String targetMonthKey = request.getCurrentMonthKey();

            // 学習記録の月リストを取得
            List<LocalDate> allDistinctMonths = learningDataService.getDistinctMonthsByUserId(userId);
            allDistinctMonths.sort(Comparator.reverseOrder());

            // 表示対象の月 (LocalDate) を決定
            LocalDate targetMonth = null;
            if (targetMonthKey != null && !targetMonthKey.isBlank()) {
                try {
                    targetMonth = LocalDate.parse(targetMonthKey + "-01");
                } catch (Exception e) {
                    // パース失敗時は何もしない
                }

                // targetMonthが未設定の場合は最新の月をデフォルトにする 
                if (targetMonth == null && !allDistinctMonths.isEmpty()) {
                    targetMonth = allDistinctMonths.get(0);
                    targetMonthKey = targetMonth.toString().substring(0, 7);
                }
            }

            // 選択された月（targetMonth）の学習記録を取得し、Mapに格納
            Map<String, Map<String, List<LearningRecord>>> monthlyRecordsMap = new HashMap<>();

            if (targetMonth != null) {

                List<LearningRecord> records = learningDataService.findLearningRecordsByUserIdAndMonth(userId,
                        targetMonth);

                Map<String, List<LearningRecord>> categorizedRecords = records.stream()
                        .collect(Collectors.groupingBy(
                                LearningRecord::getCategoryName,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> {
                                            list.sort(Comparator.comparing(LearningRecord::getSubjectName));
                                            return list;
                                        })));

                monthlyRecordsMap.put(targetMonthKey, categorizedRecords);
            }

            // エラーメッセージ
            Map<Long, String> recordErrors = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                if ("learningTime".equals(error.getField())) {
                    recordErrors.put(request.getId(), error.getDefaultMessage());
                    break; 
                }
            }
            model.addAttribute("recordErrors", recordErrors);

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

        System.out.println("DEBUG: Validation PASSED. Proceeding to UPDATE and REDIRECT.");

        String itemName = "";
        String redirectMonthParam = "";

        // 更新処理
        LearningRecord record = new LearningRecord();
        record.setId(request.getId());
        record.setLearningTime(request.getLearningTime());

        // データの更新
        try {
            // 更新対象のレコードの月情報を取得
            LearningRecord recordToUpdate = learningDataService.getLearningRecordById(request.getId());

            // IDに対応するレコードが存在しない場合は、不正なリクエストとして処理
            if (recordToUpdate == null) {
                
                throw new IllegalArgumentException("更新対象のレコードIDがデータベースに見つかりません: " + request.getId());
            }
        
            // YYYY-MM形式にフォーマット
            if (recordToUpdate != null && recordToUpdate.getMonth() != null) {

                redirectMonthParam = "?month=" + recordToUpdate.getMonth().toString().substring(0, 7);
            }

            itemName = recordToUpdate.getSubjectName();

            // service層で実行
            learningDataService.updateLearningRecord(record);

            // 成功時のメッセージ
            redirectAttributes.addFlashAttribute("successMessage",itemName + "の学習時間を保存しました！");

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
       
        Long userId = loggedInUser.getId();
        String redirectMonthParam = "";

        try {
            //  削除前にレコードを取得
            LearningRecord recordToDelete = learningDataService.getLearningRecordById(id);

            if (recordToDelete == null) {
                throw new IllegalArgumentException("ID: " + id + " の学習記録が見つかりません。");
            }

            String itemName = recordToDelete.getSubjectName();
            LocalDate targetDate = recordToDelete.getMonth();
            String currentMonthKey = targetDate.toString().substring(0, 7);

            // 記録を削除
            learningDataService.deleteLearningRecord(id);

            // 削除後の該当月の残件数を確認
            List<LearningRecord> remaining = learningDataService.findLearningRecordsByUserIdAndMonth(userId, targetDate);
            
            String finalRedirectMonth = currentMonthKey;

            // 該当月のデータが0件になった場合
            if (remaining.isEmpty()) {
                // DBにデータが存在する「最新の月」を取得
                List<LocalDate> availableMonths = learningDataService.getDistinctMonthsByUserId(userId);
                if (!availableMonths.isEmpty()) {
                    // 新しいリダイレクト先（最新の月）を設定
                    finalRedirectMonth = availableMonths.get(0).toString().substring(0, 7);
                }
            }

            // リダイレクトURLの構築
            redirectMonthParam = "?month=" + finalRedirectMonth;

            // 成功メッセージ
            String successMessage = itemName + "を削除しました！";
            redirectAttributes.addFlashAttribute("successMessage", successMessage);

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "削除エラー: " + e.getMessage());
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "削除エラーが発生しました: " + e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "予期せぬエラーが発生しました");
        }

        return "redirect:/learning/list" + redirectMonthParam;
    }
}
