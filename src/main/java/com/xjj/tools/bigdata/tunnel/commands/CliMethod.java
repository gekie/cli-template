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
    public String key() default "";
    public String description() default "";
    public boolean show() default true;
    public boolean calcRequestTime() default true;
    public boolean checkSession() default true;
    public String group() default "";
}
