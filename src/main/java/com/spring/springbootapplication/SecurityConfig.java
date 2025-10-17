package com.spring.springbootapplication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    
   @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {
        //CSRFの設定
        http.csrf(CsrfConfigurer::disable);
        //*権限の設定
        http.authorizeHttpRequests(authorize -> {

            //静的リソース
            authorize.requestMatchers(
    "/css/**",
                "/images/**"
            ).permitAll();

            //ユーザー登録・ログイン画面を許可
            authorize.requestMatchers(
    "/user/add", // ユーザー登録処理
                "/login", // ログイン画面
                "/error" //エラー対応
            ).permitAll();
            authorize.anyRequest().authenticated();
            
        });
        http.formLogin(form -> {
            form.defaultSuccessUrl("/")
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .usernameParameter("email")
                    .failureUrl("/login?error") 
                    .permitAll();
        });
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoderをBeanとして登録
        return new BCryptPasswordEncoder();
    }
}
