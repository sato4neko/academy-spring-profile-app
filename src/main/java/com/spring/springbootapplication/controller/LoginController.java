package com.spring.springbootapplication.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LoginController {
  
    @RequestMapping("/login")
    public ModelAndView login(RedirectAttributes redirectAttributes,ModelAndView mav,
            @RequestParam(value = "error", required = false) String error) {
        
        //ログイン画面だけボタン非表示の設定
        mav.addObject("showHeaderButtons", false);
        //ロゴを中央に寄せるための設定
        mav.addObject("loginPage", true); 
        
        mav.setViewName("login");

        //エラーメッセージの表示
        if (error != null) {
            mav.addObject("errorMessage", "メールアドレス、もしくはパスワードが間違っています。");
        }
        return mav;
    }
}