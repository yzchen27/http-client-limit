package com.thread0.demo.annotation;

import java.lang.annotation.*;

/**
 * 三方api访问限制
 *
 * @author : yzchen
 * since 2022-02-23,v1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ThirdAccessLimit {
    String value() default "";
}
