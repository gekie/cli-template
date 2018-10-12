package com.xjj.tools.bigdata.tunnel.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by admin on 2018/9/19.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CliMethod {
    String key() default "";
    String description() default "";
    boolean show() default true;
    boolean calcRequestTime() default true;
    boolean checkSession() default true;
    String group() default "";
}
