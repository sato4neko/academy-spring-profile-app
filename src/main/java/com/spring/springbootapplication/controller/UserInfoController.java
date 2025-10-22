package com.spring.springbootapplication.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.spring.springbootapplication.dto.UserAddRequest;
import com.spring.springbootapplication.service.UserInfoService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;


@Controller
public class UserInfoController {

    //ユーザー情報 Service
    @Autowired
    private UserInfoService userInfoService;

    //ユーザー新規登録画面を表示
    @GetMapping(value = "/user/add")
    public String displayAdd(Model model) {
        model.addAttribute("userAddRequest", new UserAddRequest());
        return "user/add";
    }

    //ユーザー情報の新規登録
    @RequestMapping(value="/user/create", method=RequestMethod.POST)
    public String create(@Validated @ModelAttribute UserAddRequest userRequest, BindingResult result, Model model,HttpServletRequest request) {
        //エラーチェック
        if (result.hasErrors()) {
            List<String> errorList = new ArrayList<String>();
            for (ObjectError error : result.getAllErrors()) {
                errorList.add(error.getDefaultMessage());
            }
            model.addAttribute("validationError", errorList);
            return "user/add";
        }
        //ユーザー登録
        userInfoService.save(userRequest);
        try {
            UserDetails userDetails = userInfoService.loadUserByUsername(userRequest.getEmail());
            Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, 
            null, // 認証後はnullでOK
            userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // ★ セッションに認証情報を保存 ★
        request.getSession(true).setAttribute(
            org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
            SecurityContextHolder.getContext()
        );
        }catch(Exception e){
            System.err.println("自動ログイン失敗: " + e.getMessage());
            return "redirect:/login?error"; 
        }
        return "redirect:/";
    }
}
