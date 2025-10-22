package com.spring.springbootapplication;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

//ログイン機能の追加： カスタムエラーメッセージの設定
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        // エラーメッセージをリクエストに追加
        request.setAttribute("error", "メールアドレス、もしくはパスワードが間違っています。");

        // エラーメッセージ発生時のリダイレクト
        response.sendRedirect("/login?error=true");
    }
}
