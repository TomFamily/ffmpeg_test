package com.example.base.utils;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

public class InterfaceTest {
    void testRunnable(Runnable runnable) {
        runnable.run();
    }

    <T> T testCallable(Callable<T> callable) throws Exception {
        return callable.call();
    }

    /**
     * [java.util.function] 家族
     */
    <T> T testFunction(Function<String, T> function) {
        return function.apply("test");
    }

    void testConsumer(Consumer<String> consumer) {
        consumer.accept("test");
    }

    /**
     * [kotlin.jvm.functions] 家族
     */
    <T> T testFunction(Function0<T> function0) {
        return function0.invoke();
    }

    <R> R testFunction(Function1<String, R> function1) {
        return function1.invoke("test function1");
    }
}
