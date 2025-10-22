package com.spring.springbootapplication.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;

@Controller
public class LoginController {

    @RequestMapping("/")
    public String index(Model model,Principal principal) {
        model.addAttribute("title", "Top page");
        model.addAttribute("msg", "仮のトップページ");

        //nullでなければログイン
        if (principal != null) {
            // ログインユーザーの名前を取得
            String username = principal.getName();
            
            // ログイン済みユーザー
            model.addAttribute("username", username);
            model.addAttribute("isLoggedIn", true);
        } else{
            // 未ログインユーザー
            model.addAttribute("username", "ゲスト");
            model.addAttribute("isLoggedIn", false);
        }
        return "index";
    }
  
    @RequestMapping("/login")
    public ModelAndView login(ModelAndView mav,
            @RequestParam(value = "error", required = false) String error) {
        
        //ログイン画面だけボタン非表示の設定
        mav.addObject("showHeaderButtons", false);
        //ロゴを中央に寄せるための設定
        mav.addObject("loginPage", true); 
        
        mav.setViewName("login");
        System.out.println(error);
        if (error != null) {
            mav.addObject("msg", "ログインできませんでした。");
        } else {
            mav.addObject("msg", "ユーザー名とパスワードを入力：");
        }
        return mav;
    }
}