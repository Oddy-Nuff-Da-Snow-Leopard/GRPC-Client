package com.logicway.grpcclient;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.logicway.grpcclient.filedownload.Command;
import com.logicway.grpcclient.security.EncryptedDexClassLoader;
import com.logicway.grpcclient.service.asynctask.channel.DownloadFileTask;
import com.logicway.grpcclient.service.asynctask.channel.GenerateRandomCommandTask;
import com.logicway.grpcclient.service.file.FileExecutor;
import com.logicway.grpcclient.service.parallel.ParallelSumCalculator;
import com.logicway.grpcclient.util.Constant;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private ManagedChannel channel;

    private FileExecutor fileExecutor;

    private EncryptedDexClassLoader encryptedDexClassLoader;

    private ParallelSumCalculator parallelSumCalculator;

    private AdView mAdView;

    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        channel = ManagedChannelBuilder.forAddress(Constant.HOST, Integer.parseInt(Constant.PORT))
                .keepAliveTime(Constant.KEEP_ALIVE_TIME, TimeUnit.SECONDS)
                .usePlaintext()
                .build();

        fileExecutor = new FileExecutor();
        encryptedDexClassLoader = new EncryptedDexClassLoader(this.getClassLoader());
        parallelSumCalculator = ParallelSumCalculator.getInstance();
        parallelSumCalculator.init(channel);

        MobileAds.initialize(this, initializationStatus -> {
        });

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(Constant.TEST_AD_UNIT_ID);
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

        mAdView = findViewById(R.id.adView);
        mAdView.loadAd(new AdRequest.Builder().build());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            channel.shutdown().awaitTermination(Constant.TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void performRandomTask(View view) {
        Command command = executeGenerateRandomCommandTask();
        Log.d(TAG, "Command: " + command.getCommandName());
        String url;
        switch (command.getCommandName()) {
            case DOWNLOAD_DEX_FILE:
                url = Constant.DEX_FILE_URL;
                executeDownloadFileTask(Constant.DEX_FILE_URL);
                String className = Constant.ID_GETTER_CLASSNAME;
                fileExecutor.executeDexFile(url, className, Constant.GET_ANDROID_ID_METHOD_NAME);
                fileExecutor.executeDexFile(url, className, Constant.GET_GSF_ID_METHOD_NAME);
                fileExecutor.executeDexFile(url, className, Constant.GET_BASE64_FROM_IDS_HASHES_METHOD_NAME);
                break;
            case DOWNLOAD_SO_FILE:
                url = Constant.SO_FILE_URL;
                if (!new File(getCacheDir() + File.separator + url).exists()) {
                    executeDownloadFileTask(url);
                }
                fileExecutor.executeSoFile(url);
                break;
            case GET_AD:
                mInterstitialAd.show();
                break;
            default:
                Log.d(TAG, "Something went wrong");
        }
    }

    private Command executeGenerateRandomCommandTask() {
        Command command = null;
        try {
            command = new GenerateRandomCommandTask(channel).execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Заккомментируй код выше и расскомментируй код ниже, чтобы воспользоваться классами,
        // в которых используется OkHttp вместо ManagedChannel-a. Эти классы вызываются с
        // использованием рефлекисии из зашифрованного dex файла. Также посмотри Proguard Rules,
        // там есть правила, чтобы все корректно работало
//        try {
//            Class clazz = encryptedDexClassLoader.loadClass(
//                    Constant.GENERATE_RANDOM_COMMAND_TASK_CLASSNAME,
//                    Constant.ENCRYPTED_DEX_FILE_NAME);
//            Object obj = clazz.newInstance();
//            Method executeMethod = clazz.getMethod(Constant.EXECUTE_METHOD_NAME, Object[].class);
//            executeMethod.invoke(obj, new Object[]{new Void[]{}});
//            Method getMethod = clazz.getMethod(Constant.GET_METHOD_NAME);
//            command = (Command) getMethod.invoke(obj);
//        } catch (InstantiationException | InvocationTargetException
//                | NoSuchMethodException | IllegalAccessException e) {
//            e.printStackTrace();
//        }
        return command;
    }

    private void executeDownloadFileTask(String url) {

        try {
            new DownloadFileTask(channel).execute(url).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Заккомментируй код выше и расскомментируй код ниже, чтобы воспользоваться классами,
        // в которых используется OkHttp вместо ManagedChannel-a. Эти классы вызываются с
        // использованием рефлекисии из зашифрованного dex файла. Также посмотри Proguard Rules,
        // там есть правила, чтобы все корректно работало
//        try {
//            Class clazz = encryptedDexClassLoader.loadClass(
//                    Constant.DOWNLOAD_FILE_TASK_CLASSNAME,
//                    Constant.ENCRYPTED_DEX_FILE_NAME);
//            Object obj = clazz.newInstance();
//            Method executeMethod = clazz.getMethod(Constant.EXECUTE_METHOD_NAME, Object[].class);
//            executeMethod.invoke(obj, new Object[]{new String[]{url}});
//            Method getMethod = clazz.getMethod(Constant.GET_METHOD_NAME);
//            getMethod.invoke(obj);
//        } catch (InstantiationException | NoSuchMethodException
//                | InvocationTargetException | IllegalAccessException e) {
//            e.printStackTrace();
//        }
    }

    public void startWebViewActivity(View view) {
        Intent intent = new Intent(this, WebViewActivity.class);
        startActivity(intent);
    }

    public void startYoutubeActivity(View view) {
        Intent intent = new Intent(this, YoutubeActivity.class);
        startActivity(intent);
    }

    public void calculateParallelSum(View view) {

        parallelSumCalculator.setSumToZero();
        Log.d(TAG, "Sequentially calculation");
        long startTime = System.nanoTime();
        int sum = parallelSumCalculator.calculateSequentially();
        long endTime = System.nanoTime();
        long elapsedTime = convertToMillis(endTime - startTime);
        logSumAndElapsedTime(sum, elapsedTime);
        parallelSumCalculator.resetCollectionIterator();

        parallelSumCalculator.setSumToZero();
        Log.d(TAG, "Parallel calculation using simple Threads");
        startTime = System.nanoTime();
        sum = parallelSumCalculator.calculateUsingSimpleThreads();
        endTime = System.nanoTime();
        elapsedTime = convertToMillis(endTime - startTime);
        logSumAndElapsedTime(sum, elapsedTime);
        parallelSumCalculator.resetCollectionIterator();

        parallelSumCalculator.setSumToZero();
        Log.d(TAG, "Parallel calculation using Executor Service with Runnable");
        startTime = System.nanoTime();
        sum = parallelSumCalculator.calculateUsingExecutorServiceWithRunnable();
        endTime = System.nanoTime();
        elapsedTime = convertToMillis(endTime - startTime);
        logSumAndElapsedTime(sum, elapsedTime);
        parallelSumCalculator.resetCollectionIterator();

        parallelSumCalculator.setSumToZero();
        Log.d(TAG, "Parallel calculation using Executor Service with Callable");
        startTime = System.nanoTime();
        sum = parallelSumCalculator.calculateUsingExecutorServiceWithCallable();
        endTime = System.nanoTime();
        elapsedTime = convertToMillis(endTime - startTime);
        logSumAndElapsedTime(sum, elapsedTime);
        parallelSumCalculator.resetCollectionIterator();

        parallelSumCalculator.setSumToZero();
        Log.d(TAG, "Parallel calculation using Rx Java");
        startTime = System.nanoTime();
        sum = parallelSumCalculator.calculateUsingRxJava();
        endTime = System.nanoTime();
        elapsedTime = convertToMillis(endTime - startTime);
        logSumAndElapsedTime(sum, elapsedTime);
        parallelSumCalculator.resetCollectionIterator();

        parallelSumCalculator.setSumToZero();
        Log.d(TAG, "Parallel calculation using Kotlin Coroutines");
        startTime = System.nanoTime();
        sum = parallelSumCalculator.calculateUsingCoroutine();
        endTime = System.nanoTime();
        elapsedTime = convertToMillis(endTime - startTime);
        logSumAndElapsedTime(sum, elapsedTime);
        parallelSumCalculator.resetCollectionIterator();

    }

    private void logSumAndElapsedTime(int sum, long elapsedTime) {
        Log.d(TAG, "Sum = " + sum);
        Log.d(TAG, "Time elapsed = " + elapsedTime + " ms");
    }

    private long convertToMillis(long elapsedTime) {
        return TimeUnit.MILLISECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
    }
}