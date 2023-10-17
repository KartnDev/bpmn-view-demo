package com.company.bpmnviewdemo.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(value = ElementType.TYPE)
@Retention(RUNTIME)
public @interface CustomViewForm {
}
