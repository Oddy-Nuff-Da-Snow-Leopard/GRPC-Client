package com.logicway.grpcclient.service.parallel;

import android.util.Log;

import com.google.protobuf.Empty;
import com.logicway.grpcclient.filedownload.FileDownloadGrpc;
import com.logicway.grpcclient.filedownload.RxFileDownloadGrpc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.grpc.Channel;
import io.michaelrocks.paranoid.Obfuscate;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

@Obfuscate
public class ParallelSumCalculator {

    private static final String TAG = ParallelSumCalculator.class.getName();

    private static final int MAX_THREAD_NUMBER = 10;

    private FileDownloadGrpc.FileDownloadBlockingStub blockingStub;

    private RxFileDownloadGrpc.RxFileDownloadStub stub;

    private AtomicInteger sum;

    private ParallelSumCalculator() {
    }

    private static class ParallelSumCalculatorHolder {

        private static final ParallelSumCalculator INSTANCE = new ParallelSumCalculator();
    }

    public static ParallelSumCalculator getInstance() {
        return ParallelSumCalculatorHolder.INSTANCE;
    }

    public void init(Channel channel) {
        blockingStub = FileDownloadGrpc.newBlockingStub(channel);
        stub = RxFileDownloadGrpc.newRxStub(channel);
        sum = new AtomicInteger();
    }

    public void setSumToZero() {
        sum.set(0);
    }

    public void resetCollectionIterator() {
        blockingStub.resetCollectionIterator(Empty.getDefaultInstance());
    }

    private int performCalculation() {
        int threadSum = 0;
        int value;
        do {
            value = blockingStub.getCollectionElement(Empty.newBuilder().build()).getValue();
            threadSum += value;
        } while (value != 0);
        Log.d(TAG, "Thread name: " + Thread.currentThread().getName()
                + ", calculated sum: " + threadSum);
        return threadSum;
    }

    private class SendRequestForCollectionElementRunnable implements Runnable {
        @Override
        public void run() {
            sum.addAndGet(performCalculation());
        }
    }

    private class SendRequestForCollectionElementCallable implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            return performCalculation();
        }
    }

    public int calculateSequentially() {
        return performCalculation();
    }

    public int calculateUsingSimpleThreads() {
        Thread[] threads = new Thread[MAX_THREAD_NUMBER];
        for (int i = 0; i < MAX_THREAD_NUMBER; i++) {
            threads[i] = new Thread(new SendRequestForCollectionElementRunnable());
            threads[i].start();
        }
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return sum.intValue();
    }

    public int calculateUsingExecutorServiceWithRunnable() {
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREAD_NUMBER);
        for (int i = 0; i < MAX_THREAD_NUMBER; i++) {
            executorService.execute(new SendRequestForCollectionElementRunnable());
        }
        shutdownAndAwaitTermination(executorService);
        return sum.intValue();
    }

    public int calculateUsingExecutorServiceWithCallable() {
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREAD_NUMBER);
        List<Callable<Integer>> callableTasks = new ArrayList<>();
        for (int i = 0; i < MAX_THREAD_NUMBER; i++) {
            callableTasks.add(new SendRequestForCollectionElementCallable());
        }
        try {
            List<Future<Integer>> futures = executorService.invokeAll(callableTasks);
            for (Future<Integer> f : futures) {
                sum.addAndGet(f.get().intValue());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        shutdownAndAwaitTermination(executorService);
        return sum.intValue();
    }

    public int calculateUsingRxJava() {
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREAD_NUMBER);
        Scheduler scheduler = Schedulers.from(executorService);
        Observable.range(1, MAX_THREAD_NUMBER)
                .flatMap(i -> Observable.just(i)
                        .subscribeOn(scheduler)
                        .map(v -> {
                            sum.addAndGet(performCalculation());
                            return Observable.just(sum);
                        })
                ).subscribe();
        scheduler.shutdown();
        shutdownAndAwaitTermination(executorService);
        return sum.intValue();
    }

    public int calculateUsingRxJava2() {
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREAD_NUMBER);
        Scheduler scheduler = Schedulers.from(executorService);
        Single.just(Empty.getDefaultInstance())
                .flatMap(stub::getCollectionElement)
                .repeat(MAX_THREAD_NUMBER)
                .takeUntil(response -> response.getValue() != 0)
                .doOnNext(response -> {
                    Log.d(TAG, Thread.currentThread().getName());
                    if (response.getValue() != 0) {
                        sum.addAndGet(response.getValue());
                    }
                })
                .subscribeOn(scheduler)
                .doOnComplete(() -> Log.d(TAG, sum.toString()))
                .subscribe();
        scheduler.shutdown();
        shutdownAndAwaitTermination(executorService);
        return sum.intValue();
    }

    public int calculateUsingCoroutine() {
        CoroutineLauncher parallelCoroutine = new CoroutineLauncher(blockingStub, MAX_THREAD_NUMBER);
        return parallelCoroutine.runCoroutines();
    }

    private void shutdownAndAwaitTermination(ExecutorService executorService) {
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
