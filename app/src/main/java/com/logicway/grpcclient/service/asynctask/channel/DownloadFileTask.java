package com.logicway.grpcclient.service.asynctask.channel;

import android.os.AsyncTask;
import android.util.Log;

import com.logicway.grpcclient.App;
import com.logicway.grpcclient.filedownload.DataChunk;
import com.logicway.grpcclient.filedownload.FileDownloadGrpc;
import com.logicway.grpcclient.filedownload.FileDownloadRequest;
import com.logicway.grpcclient.util.Constant;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class DownloadFileTask extends AsyncTask<String, Void, Void> {

    private static final String TAG = DownloadFileTask.class.getName();

    private final ManagedChannel channel;

    public DownloadFileTask(ManagedChannel channel) {
        this.channel = channel;
    }

    @Override
    protected Void doInBackground(String... urls) {
        for (String url : urls) {
            downloadFile(FileDownloadGrpc.newStub(channel), url);
        }
        return null;
    }

    private void downloadFile(FileDownloadGrpc.FileDownloadStub asyncStub, String url)
            throws StatusRuntimeException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CountDownLatch finishLatch = new CountDownLatch(1);
        AtomicBoolean isCompleted = new AtomicBoolean(false);

        StreamObserver<DataChunk> streamObserver = new StreamObserver<DataChunk>() {
            @Override
            public void onNext(DataChunk dataChunk) {
                try {
                    baos.write(dataChunk.getData().toByteArray());
                } catch (IOException e) {
                    Log.d(TAG, "Error on write to byte array stream", e);
                    onError(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                Log.d(TAG, "Error in downloadFile method", t);
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                Log.d(TAG, "downloadFile method has been completed!");
                isCompleted.compareAndSet(false, true);
                finishLatch.countDown();
            }
        };
        try {
            FileDownloadRequest fileDownloadRequest = FileDownloadRequest
                    .newBuilder().setUrl(url).build();
            asyncStub.downloadFile(fileDownloadRequest, streamObserver);
            finishLatch.await(Constant.TIMEOUT, TimeUnit.SECONDS);

            if (!isCompleted.get()) {
                throw new Exception("downloadFile method did not complete");
            }

        } catch (Exception e) {
            Log.d(TAG, "downloadFile method did not complete", e);
        }

        try (OutputStream os = new FileOutputStream(
                App.getAppContext().getCacheDir() + File.separator + url)) {
            baos.writeTo(os);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
