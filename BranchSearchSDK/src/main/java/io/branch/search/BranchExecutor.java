package io.branch.search;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * A thread pool executor that creates, caches and reuses at most a given number of threads.
 * When a thread is done, it is kept alive for {@link #KEEP_ALIVE_SECONDS} seconds and then either
 * reused if there's work to do, or killed.
 *
 * When all threads are busy and a new action is requested, there are various behaviors that
 * the Executor API allows. What we do here is simply enqueue requests, so they'll wait
 * for busy threads to complete their ongoing work.
 *
 * This is the "Unbounded queues" policy that is described in the official {@link ThreadPoolExecutor}
 * class javadocs. In our case it is tuned around the {@link URLConnectionNetworkHandler} class
 * and what it does with this Executor. Specifically, it will often cancel requests and free up
 * threads, which means we will never have the "work queue growth" problem described in
 * {@link ThreadPoolExecutor}.
 */
class BranchExecutor implements Executor {

    private final static long KEEP_ALIVE_SECONDS = 5L;
    private final static ThreadFactory FACTORY = new ThreadFactory() {
        private final AtomicInteger count = new AtomicInteger(1);

        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "BranchThread #" + count.getAndIncrement());
        }
    };

    private final ThreadPoolExecutor executor;

    BranchExecutor(int poolSize) {
        executor = new ThreadPoolExecutor(
                poolSize,
                poolSize,
                KEEP_ALIVE_SECONDS,
                TimeUnit.SECONDS,
                // requests exceeding the pool size will go into this queue.
                new LinkedBlockingQueue<Runnable>(),
                FACTORY
        );
        executor.allowCoreThreadTimeOut(true);
    }

    @Override
    public void execute(@NonNull Runnable command) {
        executor.execute(command);
    }
}
