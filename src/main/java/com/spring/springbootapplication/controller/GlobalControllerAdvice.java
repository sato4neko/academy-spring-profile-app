package com.spring.springbootapplication.controller;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;


@ControllerAdvice
public class GlobalControllerAdvice {
    
    @ModelAttribute
    public void addCommonAttributes(Model model) {
       
        model.addAttribute("learningDataArchive", false);
        model.addAttribute("top_learning_Chart", false);
    }
}
