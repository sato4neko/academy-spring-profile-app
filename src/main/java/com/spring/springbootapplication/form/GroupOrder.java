package com.spring.springbootapplication.form;

import jakarta.validation.GroupSequence;

@GroupSequence({ ValidGroup1.class, ValidGroup2.class,UpdateValidationGroup.class})
public interface GroupOrder {
} 
