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
import org.springframework.web.bind.annotation.SessionAttributes;

import com.spring.springbootapplication.dto.UserAddRequest;
import com.spring.springbootapplication.form.GroupOrder;
import com.spring.springbootapplication.service.DuplicateUserException;
import com.spring.springbootapplication.service.UserInfoService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Controller
@SessionAttributes(types = UserAddRequest.class)
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
    @RequestMapping(value="/user/add", method=RequestMethod.POST)
    public String create(@Validated(GroupOrder.class) @ModelAttribute UserAddRequest userRequest, BindingResult result, Model model,HttpServletRequest request) {
        if (result.hasErrors()) {
        List<String> errorList = new ArrayList<String>();
        for (ObjectError error : result.getAllErrors()) {
            errorList.add(error.getDefaultMessage());
        }
        model.addAttribute("validationError", errorList);
        return "user/add";
    }

    try {
        //ユーザー登録
        userInfoService.save(userRequest);
        UserDetails userDetails = userInfoService.loadUserByUsername(userRequest.getEmail());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //セッションに認証情報を保存
        request.getSession(true).setAttribute(
            org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            SecurityContextHolder.getContext());

        } catch (DuplicateUserException e) {

            //重複の処理
            if (e.getMessage().contains("ユーザー名"))  {
                result.rejectValue("name", "Duplicate.userAddRequest.name", e.getMessage());
            } else if (e.getMessage().contains("メールアドレス")){
                result.rejectValue("email", "Duplicate.userAddRequest.email", e.getMessage());
            } else {
                result.reject("General.duplicateError", e.getMessage());
            }
            return "user/add";
            
        } catch (Exception e) {
            //ログイン失敗時
            return "redirect:/login?error";
        }
            //ログイン成功時
            return "redirect:/";
     }
}
