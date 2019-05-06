package com.example.mvvmframe.http;

import java.lang.annotation.*;

/**
 * Created by jingbin on 2015/6/24.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ParamNames {
    String value();
}
