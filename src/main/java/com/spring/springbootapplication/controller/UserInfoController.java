package com.spring.springbootapplication.controller;

import java.io.IOException;
import com.spring.springbootapplication.service.FileStorageService;

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
import org.springframework.web.multipart.MultipartFile;

import com.spring.springbootapplication.dto.UserAddRequest;
import com.spring.springbootapplication.dto.UserUpdateRequest;
import com.spring.springbootapplication.entity.UserInfo;
import com.spring.springbootapplication.form.GroupOrder;
import com.spring.springbootapplication.service.DuplicateUserException;
import com.spring.springbootapplication.service.UserInfoService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;



@Controller
@SessionAttributes(types = UserAddRequest.class)
public class UserInfoController {

    //ユーザー情報 Service
    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private FileStorageService fileStorageService;

    //ユーザー新規登録画面を表示
    @GetMapping(value = "/user/add")
    public String displayAdd(Model model) {
        model.addAttribute("title", "新規登録");
        model.addAttribute("userAddRequest", new UserAddRequest());
        return "user/add";
    }

    //ユーザー情報の新規登録
    @RequestMapping(value = "/user/add", method = RequestMethod.POST)
    public String create(@Validated(GroupOrder.class) @ModelAttribute UserAddRequest userRequest, BindingResult result,
            Model model, HttpServletRequest request) {
        
        //Validation
        if (result.hasErrors()) {

            model.addAttribute("title", "新規登録");

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

            // ユーザー情報を取得
            UserDetails userDetails = userInfoService.loadUserByUsername(userRequest.getEmail());

            //認証トークンを作成
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());

            //セキュリティコンテキストを更新        
            SecurityContextHolder.getContext().setAuthentication(authentication);

            //セッションに認証情報を保存
            request.getSession(true).setAttribute(
                    org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());
                    
        //重複の処理
        } catch (DuplicateUserException e) {

            if (e.getMessage().contains("ユーザー名")) {
                result.rejectValue("name", "Duplicate.userAddRequest.name", e.getMessage());
            } else if (e.getMessage().contains("メールアドレス")) {
                result.rejectValue("email", "Duplicate.userAddRequest.email", e.getMessage());
            } else {
                result.reject("General.duplicateError", e.getMessage());
            }
            return "user/add";

        } catch (Exception e) {
            //ログイン失敗時
            return "redirect:/login?error";
        }
        return "redirect:/";
    }

    //ユーザー情報の更新ページを表示
    @GetMapping(value = "/user/edit")
    public String displayEdit(Model model, @AuthenticationPrincipal UserInfo loggedInUser) {
        
        if (loggedInUser == null) {
            // ログインしていない場合
            return "redirect:/login?error";
        }
        
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest();

        userUpdateRequest.setId(loggedInUser.getId());
        userUpdateRequest.setImage(loggedInUser.getImage());
        userUpdateRequest.setProfileDetail(loggedInUser.getProfileDetail());

        model.addAttribute("title", "自己紹介を編集する");
        model.addAttribute("userUpdateRequest", userUpdateRequest);
        return "user/edit";
    }

    //ユーザー情報の更新
    @RequestMapping(value = "/user/update", method = RequestMethod.POST)
    public String update(@Validated(GroupOrder.class)  @ModelAttribute UserUpdateRequest userUpdateRequest, BindingResult result,
            Model model,HttpServletRequest request) {

        //Validation
        if (result.hasErrors()) {

            model.addAttribute("title", "自己紹介を編集する");

            List<String> errorList = new ArrayList<String>();
            for (ObjectError error : result.getAllErrors()) {
                errorList.add(error.getDefaultMessage());
            }
            model.addAttribute("validationError", errorList);
            return "user/edit";
        }

        //画像ファイルの保存
        String newImagePath = userUpdateRequest.getImage();

        //画像ファイルを取得
        MultipartFile file = userUpdateRequest.getImageFile();
        
        //既存の画像パスを取得
        String oldImagePath = userInfoService.findById(userUpdateRequest.getId()).getImage();

        if (file != null && !file.isEmpty()) {
            try {

                if (oldImagePath != null && !oldImagePath.isEmpty()) {
                    fileStorageService.deleteFile(oldImagePath);

                }
            
            // ファイルを保存し、パスを取得
            newImagePath = fileStorageService.storeFile(file);
                
            } catch (IOException e) {

                // ファイル保存失敗時のエラー処理
                e.printStackTrace();
        
            } catch (RuntimeException e) {

                // ディレクトリ作成エラーなど、その他のファイル操作エラー
                e.printStackTrace();
            }
        }
        
        // 画像の更新
        userUpdateRequest.setImage(newImagePath);

        // ユーザー情報の更新
        userInfoService.update(userUpdateRequest);

        try {

            // 最新のユーザー情報を再取得
            UserDetails userDetails = userInfoService.loadUserByUsername(
                userInfoService.findById(userUpdateRequest.getId()).getEmail()
                
            );

            //認証トークンを作成
            Authentication newAuth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());
                    
            //セキュリティコンテキストを更新
            SecurityContextHolder.getContext().setAuthentication(newAuth);
            
            request.getSession().setAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
            SecurityContextHolder.getContext()
        );
    } catch (Exception e) {

        // 認証情報の再設定時の処理
        e.printStackTrace();

        }
        
        //情報を更新してリダイレクト
        return "redirect:/";
    }
}