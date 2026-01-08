package com.spring.springbootapplication.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.spring.springbootapplication.entity.UserInfo;
import com.spring.springbootapplication.service.FileStorageService;
import com.spring.springbootapplication.service.LearningDataService;
import com.spring.springbootapplication.service.UserInfoService;

@Controller
public class TopPageController {

    private final UserInfoService userInfoService;
    private final FileStorageService fileStorageService;
    private final LearningDataService learningDataService;

    //デフォルトの画像
    private static final String DEFAULT_IMAGE_PATH = "/images/default_profile_image.png";

    public TopPageController(
        UserInfoService userInfoService,
        FileStorageService fileStorageService,
        LearningDataService learningDataService) {
            this.userInfoService = userInfoService;
            this.fileStorageService = fileStorageService;
            this.learningDataService = learningDataService;
        }

    @GetMapping("/")
    public String index(Model model, @AuthenticationPrincipal UserInfo loggedInUser) {
        model.addAttribute("pageTitle", "学習情報詳細ページ");
        model.addAttribute("self_introduction", "自己紹介");
        model.addAttribute("learning_chart", "学習チャート");

        boolean isTopChart = true;
        model.addAttribute("top_learning_Chart", isTopChart);

        if (loggedInUser != null) {

            UserInfo latestUserInfo = userInfoService.findById(loggedInUser.getId());

            if (latestUserInfo != null) {

                // ログイン情報
                model.addAttribute("isLoggedIn", true);

                // ユーザー名を取得
                model.addAttribute("username", latestUserInfo.getName());

                // 画像パス
                model.addAttribute("profileDetail", latestUserInfo.getProfileDetail());
                model.addAttribute("userImage", latestUserInfo.getImage());

                // ユーザー画像の処理
                String userImage = latestUserInfo.getImage();
                if (userImage == null || userImage.isEmpty()) {

                    userImage = DEFAULT_IMAGE_PATH; // デフォルト画像をセット
                } else {

                    long lastModified = fileStorageService.getLastModifiedTime(userImage); // ファイルの最終更新日時を取得
                    userImage += "?v=" + lastModified; // 最終更新日時をパラメータとして付与
                }

                model.addAttribute("userImage", userImage); //画像URLをセット

                // --- チャートデータの生成ロジック ---

                // Serviceからカテゴリ, 学習時間を取得
                Map<String, Map<String, Integer>> aggregatedData = learningDataService.getChartData(loggedInUser.getId());

                // 常に直近3ヶ月のラベル(YYYY-MM)を生成する
                LocalDate today = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                
                List<String> labels = new ArrayList<>();
                for (int i = 2; i >= 0; i--) {
                    // [2ヶ月前, 1ヶ月前, 今月] の順で追加
                    labels.add(today.minusMonths(i).format(formatter));
                }

                // 生成したラベルに基づいてデータを抽出（データがなければ0を入れる）
                List<Integer> backendData = new ArrayList<>();
                List<Integer> frontendData = new ArrayList<>();
                List<Integer> infraData = new ArrayList<>();

                for (String monthKey : labels) {
                    Map<String, Integer> categoryMap = aggregatedData.get(monthKey);
                    if (categoryMap != null) {
                        backendData.add(categoryMap.getOrDefault("バックエンド", 0));
                        frontendData.add(categoryMap.getOrDefault("フロントエンド", 0));
                        infraData.add(categoryMap.getOrDefault("インフラ", 0));
                    } else {
                        backendData.add(0);
                        frontendData.add(0);
                        infraData.add(0);
                    }
                }

                        // Modelに追加
                        model.addAttribute("chartLabels", labels);
                        model.addAttribute("backendData", backendData);
                        model.addAttribute("frontendData", frontendData);
                        model.addAttribute("infraData", infraData);

                    } else {

                        // 情報が見つからない場合の処理
                        model.addAttribute("username", "ゲスト");
                        model.addAttribute("isLoggedIn", false);

                    }

        } else {

            // 未ログインユーザー
            model.addAttribute("username", "ゲスト");
            model.addAttribute("isLoggedIn", false);

        }

        return "index";
    }
    
}
