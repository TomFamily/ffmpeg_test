/*
 * Copyright (C) 2018 xuexiangjys(xuexiangjys@163.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.aop_aspect;

import android.util.Log;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;


/**
 * 配置 AspectJ 依赖：https://blog.csdn.net/zengsidou/article/details/129922204
 * 或者在 apple 账户的备忘录中（个人）
 */
@Aspect
public class InterceptAspectJ {
    private static final String TAG = "InterceptAspectJ";


    @Pointcut("execution(* android.app.Activity+.onCreate(..))")
    public void test() {
    }
    
    @Pointcut("execution(* com.example.aop_test.AOPTest.clickTest(..))")
    public void clickTest() {}
    @After("clickTest()")
    public void afterClickTest(JoinPoint joinPoint) {
        Log.d(TAG, "afterClickTest: ");
    }
    
    @Before("test()")
    public void before(JoinPoint joinPoint) {
        Log.d(TAG, "拦截结果: before Activity+.onCreate");
    }
    @After("test()")
    public void after(JoinPoint joinPoint) {
        Log.d(TAG, "拦截结果: after Activity+.onCreate");
    }

    @Pointcut("within(@com.xuexiang.xaop.annotation.Intercept *)")
    public void withinAnnotatedClass() {
    }

    @Pointcut("execution(!synthetic * *(..)) && withinAnnotatedClass()")
    public void methodInsideAnnotatedType() {
    }

    @Pointcut("execution(!synthetic *.new(..)) && withinAnnotatedClass()")
    public void constructorInsideAnnotatedType() {
    }

    @Pointcut("execution(@com.xuexiang.xaop.annotation.Intercept * *(..)) || methodInsideAnnotatedType()")
    public void method() {
    } //方法切入点

    @Pointcut("execution(@com.xuexiang.xaop.annotation.Intercept *.new(..)) || constructorInsideAnnotatedType()")
    public void constructor() {
    } //构造器切入点

}
