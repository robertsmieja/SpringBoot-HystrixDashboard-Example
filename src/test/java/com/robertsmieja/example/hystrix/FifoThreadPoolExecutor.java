package com.robertsmieja.example.hystrix;

import java.util.*;
import java.util.concurrent.*;

public class FifoThreadPoolExecutor extends ThreadPoolExecutor {
    final /*package*/ Map<Runnable, Long> runnableNanosMap = new HashMap<>();
    final /*package*/ NavigableMap<Runnable, FifoThreadPoolExecutor.ThreadInfo> threadInfos = new TreeMap<>(new RunnableNanoComparator(this));

    public FifoThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        this.setRejectedExecutionHandler(new FifoThreadPoolRejectedExecutionHandler(this));
    }

    public FifoThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        this.setRejectedExecutionHandler(new FifoThreadPoolRejectedExecutionHandler(this));
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        //Store ThreadInfo so we can kill it later if we need it
        long threadNano = System.nanoTime();
        ThreadInfo threadInfo = new ThreadInfo(t, r, threadNano);
        threadInfos.put(r, threadInfo);
        runnableNanosMap.put(r, threadNano);

        super.beforeExecute(t, r);
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        //Remove the correspond ThreadInfo as it's finished, and we will never need to interrupt it
        threadInfos.remove(r);
        runnableNanosMap.remove(r);

        super.afterExecute(r, t);
    }

    private static class FifoThreadPoolRejectedExecutionHandler implements RejectedExecutionHandler {
        private final FifoThreadPoolExecutor fifoThreadPoolExecutor;

        FifoThreadPoolRejectedExecutionHandler(FifoThreadPoolExecutor fifoThreadPoolExecutor) {
            this.fifoThreadPoolExecutor = fifoThreadPoolExecutor;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            //Get oldest
            Map.Entry<Runnable, ThreadInfo> threadInfo = fifoThreadPoolExecutor.threadInfos.pollLastEntry();

            //Interrupt oldest
            ThreadInfo value = threadInfo.getValue();
            value.thread.interrupt();

            //Attempt to add ourselves again if the ThreadPool is still valid
            if (!executor.isShutdown()) {
                executor.submit(r);
            }
        }
    }

    private static class ThreadInfo implements Comparable<ThreadInfo> {
        //nanoTime() only works in the same JVM, so be careful if this is ever serialized!
        private final long nanoTime; // = System.nanoTime();
        private final Thread thread;
        private final Runnable runnable;

        public ThreadInfo(Thread thread, Runnable runnable, long nanoTime) {
            this.thread = thread;
            this.runnable = runnable;
            this.nanoTime = nanoTime;
        }

        public long getNanoTime() {
            return nanoTime;
        }

        public Thread getThread() {
            return thread;
        }

        public Runnable getRunnable() {
            return runnable;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ThreadInfo that = (ThreadInfo) o;
            return nanoTime == that.nanoTime &&
                    Objects.equals(thread, that.thread) &&
                    Objects.equals(runnable, that.runnable);
        }

        @Override
        public int hashCode() {

            return Objects.hash(nanoTime, thread, runnable);
        }

        @Override
        public int compareTo(ThreadInfo o) {
            return (int) (this.nanoTime - o.nanoTime);
        }
    }

    private static class RunnableNanoComparator implements Comparator<Runnable> {
        private final FifoThreadPoolExecutor fifoThreadPoolExecutor;

        RunnableNanoComparator(FifoThreadPoolExecutor fifoThreadPoolExecutor) {
            this.fifoThreadPoolExecutor = fifoThreadPoolExecutor;
        }

        @Override
        public int compare(Runnable o1, Runnable o2) {
            Long nano1 = fifoThreadPoolExecutor.runnableNanosMap.get(o1);
            Long nano2 = fifoThreadPoolExecutor.runnableNanosMap.get(o2);

            //These should never happpen...
            if (nano1 == null) {
                nano1 = 0L;
            }
            if (nano2 == null) {
                nano2 = 0L;
            }

            return (int) (nano1 - nano2);
        }
    }
}
