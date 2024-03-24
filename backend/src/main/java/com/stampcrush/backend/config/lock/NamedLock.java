package com.stampcrush.backend.config.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NamedLock {

    String lockKey();

    String lockType();

    int timeout() default 3000;
}
