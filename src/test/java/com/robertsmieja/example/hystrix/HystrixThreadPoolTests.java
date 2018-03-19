package com.robertsmieja.example.hystrix;

import com.netflix.hystrix.*;
import com.netflix.hystrix.strategy.HystrixPlugins;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static org.junit.Assert.*;

public class HystrixThreadPoolTests {
    public static final String className = HystrixThreadPoolTests.class.getName();

    public static final HystrixCommandGroupKey GROUP_KEY = HystrixCommandGroupKey.Factory.asKey(className);
    public static final HystrixThreadPoolKey THREAD_POOL_KEY = HystrixThreadPoolKey.Factory.asKey(className);
    public static final String FALLBACK = "fallback";

    @Test
    public void testDefaultHystrixBehavior() throws ExecutionException, InterruptedException {
        ThreadCommand threadCommand1 = new ThreadCommand("1");
        ThreadCommand threadCommand2 = new ThreadCommand("2");
        ThreadCommand threadCommand3 = new ThreadCommand("3");

        //Should be same thread pool
        HystrixThreadPool.HystrixThreadPoolDefault threadPool = threadCommand1.getThreadPool();
        assertSame(threadPool, threadCommand2.getThreadPool());
        assertSame(threadPool, threadCommand3.getThreadPool());

        ThreadPoolExecutor executor = threadPool.getExecutor();
        assertEquals(0, executor.getPoolSize());
        assertEquals(0, executor.getActiveCount());
        assertEquals(2, executor.getMaximumPoolSize());

        Future<String> result1 = threadCommand1.queue();
        Future<String> result2 = threadCommand2.queue();

        assertFalse(result1.isDone());
        assertFalse(result2.isDone());
        assertFalse(result1.isCancelled());
        assertFalse(result2.isCancelled());

        assertEquals(2, executor.getPoolSize());
        assertEquals(2, executor.getActiveCount());

        Future<String> result3 = threadCommand3.queue();
        assertFalse(result1.isDone());
        assertFalse(result2.isDone());
        assertFalse(result1.isCancelled());
        assertFalse(result2.isCancelled());
        assertTrue(result3.isDone());
        assertFalse(result3.isCancelled());

        assertEquals("fallback3", result3.get());
    }

    @Test
    public void testCustomHystrixConcurrencyStrategy() throws ExecutionException, InterruptedException {
        HystrixPlugins.getInstance().registerConcurrencyStrategy(new FifoHystrixConcurrencyStrategy());

        ThreadCommand threadCommand1 = new ThreadCommand("1");
        ThreadCommand threadCommand2 = new ThreadCommand("2");
        ThreadCommand threadCommand3 = new ThreadCommand("3");

        //Should be same thread pool
        HystrixThreadPool.HystrixThreadPoolDefault threadPool = threadCommand1.getThreadPool();
        assertSame(threadPool, threadCommand2.getThreadPool());
        assertSame(threadPool, threadCommand3.getThreadPool());

        ThreadPoolExecutor executor = threadPool.getExecutor();
        assertEquals(0, executor.getPoolSize());
        assertEquals(0, executor.getActiveCount());
        assertEquals(2, executor.getMaximumPoolSize());

        Future<String> result1 = threadCommand1.queue();
        Future<String> result2 = threadCommand2.queue();

        assertFalse(result1.isDone());
        assertFalse(result2.isDone());
        assertFalse(result1.isCancelled());
        assertFalse(result2.isCancelled());

        assertEquals(2, executor.getPoolSize());
        assertEquals(2, executor.getActiveCount());

        Future<String> result3 = threadCommand3.queue();
        assertFalse(result1.isDone());
        assertFalse(result2.isDone());
        assertFalse(result1.isCancelled());
        assertFalse(result2.isCancelled());
        assertTrue(result3.isDone());
        assertFalse(result3.isCancelled());

        assertEquals("fallback3", result3.get());
    }

    public static class ThreadCommand extends HystrixCommand<String> {
        public final String returnValue;

        public ThreadCommand(String returnValue) {
            super(Setter
                    .withGroupKey(GROUP_KEY)
                    .andThreadPoolKey(THREAD_POOL_KEY)
                    .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                            .withExecutionTimeoutEnabled(false)
                            .withCircuitBreakerEnabled(false)
                            .withRequestCacheEnabled(false)
                            .withFallbackEnabled(true)
                    )
                    .andThreadPoolPropertiesDefaults(HystrixThreadPoolProperties.Setter()
                            .withCoreSize(2)
                            .withAllowMaximumSizeToDivergeFromCoreSize(false)
                            .withMaxQueueSize(-1)
                    )
            );
            this.returnValue = returnValue;
        }

        @Override
        protected String getFallback() {
            return FALLBACK + returnValue;
        }

        @Override
        protected String run() throws Exception {
            //Block forever
            try {
                while (true) {
                    Thread.sleep(0);
                }
            } catch (InterruptedException e) {
                throw e;
            }
        }

        public HystrixThreadPool.HystrixThreadPoolDefault getThreadPool() {
            return (HystrixThreadPool.HystrixThreadPoolDefault) this.threadPool;
        }
    }
}
