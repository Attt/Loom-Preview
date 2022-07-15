package io.github.attt.example;

import jdk.incubator.concurrent.StructuredTaskScope;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author atpexgo
 */
public class Examples {

    public static void main(String[] args) throws InterruptedException {
        // 创建builder
        Thread.Builder.OfVirtual ofVirtual = Thread.ofVirtual()
                // 名称，start表示种子1步进递增
                .name("virtual-thread-", 0)
                // 是否允许set threadLocal
                .allowSetThreadLocals(true)
                // 是否允许使用可继承的threadLocal
                .inheritInheritableThreadLocals(true)
                // 未捕获异常处理器
                .uncaughtExceptionHandler((t, e) -> {
                    System.out.println(t);
                    e.printStackTrace();
                });

        // 执行
        ofVirtual.start(() -> {
            // do something
            System.out.println(Thread.currentThread().getName());
            System.out.println(Thread.currentThread().isDaemon());
            System.out.println(Thread.currentThread().isVirtual());
        }).join();

        // 预设任务，返回Thread实例
        Thread unstarted = ofVirtual
                .unstarted(() -> {
                    System.out.println(Thread.currentThread().getName());
                    System.out.println("Wooooow!");
                });
        // 开启任务
        unstarted.start();

        // 基于Executors#newThreadPerTaskExecutor，使用VirtualThreadFactory作为线程工厂
        // 为每个提交的任务开启一个thread，无界
        ExecutorService virtualThreadPerTaskExecutor = Executors.newVirtualThreadPerTaskExecutor();
        virtualThreadPerTaskExecutor.submit(() -> {
            System.out.printf("%s-newVirtualThreadPerTaskExecutor %n", Thread.currentThread().getName());
        });

        // 使用builder的设置值创建的perTaskExecutor
        ExecutorService perTaskExecutor = Executors.newThreadPerTaskExecutor(ofVirtual.factory());
        perTaskExecutor.submit(() -> {
            System.out.printf("%s-newThreadPerTaskExecutor %n", Thread.currentThread().getName());
        });

        StructuredTaskScope.ShutdownOnFailure shutdownOnFailure = new StructuredTaskScope.ShutdownOnFailure();
        shutdownOnFailure.fork(() -> {
            System.out.println("taskA");
            TimeUnit.SECONDS.sleep(1);
            throw new RuntimeException("error occurred in taskA");
        });

        shutdownOnFailure.fork(() -> {
            System.out.println("taskB...");
            TimeUnit.SECONDS.sleep(5);
            System.out.println("taskB is finished.");
            return null;
        });

        try {
            shutdownOnFailure.joinUntil(LocalDateTime.now().plusSeconds(10).toInstant(ZoneOffset.UTC));
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
