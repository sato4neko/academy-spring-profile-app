package com.spring.springbootapplication.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.spring.springbootapplication.entity.UserInfo;
import com.spring.springbootapplication.service.FileStorageService;
import com.spring.springbootapplication.service.UserInfoService;

@Controller
public class TopPageController {

    private final UserInfoService userInfoService;
    private final FileStorageService fileStorageService;

    //デフォルトの画像
    private static final String DEFAULT_IMAGE_PATH = "/images/default_profile_image.png";

    public TopPageController(UserInfoService userInfoService, FileStorageService fileStorageService) {
        this.userInfoService = userInfoService;
        this.fileStorageService = fileStorageService;
    }

    @RequestMapping("/")
    public String index(Model model, @AuthenticationPrincipal UserInfo loggedInUser) {
        model.addAttribute("pageTitle", "学習情報詳細ページ");
        model.addAttribute("self_introduction", "自己紹介");
        model.addAttribute("learning_chart", "学習チャート");

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

            String userImage = latestUserInfo.getImage();

            if (userImage == null || userImage.isEmpty()) {

                // デフォルト画像をセット
                userImage = DEFAULT_IMAGE_PATH;
                    
                } else {

                // ファイルの最終更新日時を取得
                long lastModified = fileStorageService.getLastModifiedTime(userImage);

                // 最終更新日時をパラメータとして付与
                userImage += "?v=" + lastModified;
            }
                
           //画像URLをセット
           model.addAttribute("userImage", userImage);     

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
