package com.spring.springbootapplication.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.spring.springbootapplication.entity.UserInfo;

@Controller
public class TopPageController {
    @RequestMapping("/")
    public String index(Model model,@AuthenticationPrincipal UserInfo userInfo) {
        model.addAttribute("self_introduction", "自己紹介");
        model.addAttribute("learning_chart", "学習チャート");
            
        String defaultProfileDetail = "自分の自己紹介文章が入ります。";
        model.addAttribute("profileDetail", defaultProfileDetail);

        //nullでなければログイン
        if (userInfo != null) {
            // ログインユーザーの名前を取得
            String username = userInfo.getName();
            
            // ログイン済みユーザー
            model.addAttribute("username", username);
            model.addAttribute("isLoggedIn", true);

            // ログインユーザー・自己紹介文を取得
            String userProfileDetail = userInfo.getProfileDetail();
            if (userProfileDetail != null && !userProfileDetail.isBlank()) {
                model.addAttribute("profileDetail", userProfileDetail); // ⬅️ ここで上書きしているはず
            }
        } else{
            // 未ログインユーザー
            model.addAttribute("username", "ゲスト");
            model.addAttribute("isLoggedIn", false);
        }
        return "index";
    }
}
