package io.github.attt.benchmark;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.openjdk.jmh.annotations.Scope.Benchmark;

/**
 * @author atpexgo
 */
@Warmup(iterations = 1)
@Measurement(iterations = 5)
@Fork(1)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Benchmark)
public class ThreadThroughputBenchmark {

    private final Thread.Builder.OfVirtual ofVirtual = Thread.ofVirtual()
            // 名称，start表示种子1步进递增
            .name("virtual-thread-", 0)
            // 是否允许set threadLocal
            .allowSetThreadLocals(false)
            // 是否允许使用可继承的threadLocal
            .inheritInheritableThreadLocals(false)
            // 未捕获异常处理器
            .uncaughtExceptionHandler((t, e) -> {
                System.out.println(t);
                e.printStackTrace();
            });

    private final Thread.Builder.OfPlatform ofPlatform = Thread.ofPlatform()
            // 名称，start表示种子1步进递增
            .name("virtual-thread-", 0)
            // 线程栈大小
            .stackSize(10)
            // 是否允许set threadLocal
            .allowSetThreadLocals(false)
            // 是否允许使用可继承的threadLocal
            .inheritInheritableThreadLocals(false)
            // 未捕获异常处理器
            .uncaughtExceptionHandler((t, e) -> {
                System.out.println(t);
                e.printStackTrace();
            });

    private final ExecutorService executorService = new ThreadPoolExecutor(100, 100,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(),
            ofPlatform.factory());

    @Benchmark
    public void virtualThread() throws InterruptedException {
        ofVirtual.start(() -> {
        });
    }


    @Benchmark
    public void platformThread() throws InterruptedException {
        ofPlatform.start(() -> {
        });
    }


//    @Benchmark
//    public void pooledPlatformThread() {
//        executorService.submit(() -> {
//            try {
//                TimeUnit.MILLISECONDS.sleep(50);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//                throw new RuntimeException(e);
//            }
//        });
//    }
}
