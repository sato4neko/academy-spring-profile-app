package com.spring.springbootapplication.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.spring.springbootapplication.dto.UserRequest;

@Controller
public class UserInfoController {

    //ユーザー情報の一覧画面の表示
    @RequestMapping(value = "/user/add", method = RequestMethod.GET)
    public String displayAdd(Model model) {
        model.addAttribute("userRequest", new UserRequest());
        return "user/add";
    }


    //ユーザー情報の新規登録
    @RequestMapping(value="/user/create", method=RequestMethod.POST)
    public String create(@Validated @ModelAttribute UserRequest userRequest, BindingResult result, Model model) {
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
        return "user/add";
    }
    
}
