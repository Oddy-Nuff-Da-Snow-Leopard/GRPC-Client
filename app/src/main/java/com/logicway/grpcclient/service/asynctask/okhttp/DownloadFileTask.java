package com.logicway.grpcclient.service.asynctask.okhttp;

import android.os.AsyncTask;

import com.logicway.grpcclient.App;
import com.logicway.grpcclient.filedownload.DataChunk;
import com.logicway.grpcclient.filedownload.FileDownloadRequest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

import io.grpc.StatusRuntimeException;
import io.michaelrocks.paranoid.Obfuscate;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSource;

@Obfuscate
public class DownloadFileTask extends AsyncTask<String, Void, Void> {

    private static final int COMPRESSED_FLAG_OFFSET = 1;

    private static final int MESSAGE_LENGTH_OFFSET = 4;

    @Override
    protected Void doInBackground(String... urls) {
        for (String url : urls) {
            downloadFile(url);
        }
        return null;
    }

    private void downloadFile(String url) throws StatusRuntimeException {

        try {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .protocols(Collections.singletonList(Protocol.H2_PRIOR_KNOWLEDGE)).build();

            FileDownloadRequest fileDownloadRequest = FileDownloadRequest.newBuilder().setUrl(url).build();

            byte[] toRequestBody = {};
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                baos.write(new byte[]{0, 0, 0, 0});
                baos.write((byte) fileDownloadRequest.toByteArray().length);
                baos.write(fileDownloadRequest.toByteArray());
                toRequestBody = baos.toByteArray();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Request request = new Request.Builder()
                    .url("http://172.28.10.39:1337/filedownload.FileDownload/DownloadFile")
                    .post(RequestBody.create(toRequestBody))
                    .addHeader("TE", "trailers")
                    .addHeader("Content-type", "application/grpc+proto")
                    .addHeader("grpc-encoding", "gzip")
                    .build();

            Response response = okHttpClient.newCall(request).execute();
            BufferedSource bs = response.body().source();

            byte[] messageBytes;
            byte[] messageLengthBytes;
            try (OutputStream os = new FileOutputStream(
                    App.getAppContext().getCacheDir() + File.separator + url)) {
                while (!bs.exhausted()) {
                    bs.readByteArray(COMPRESSED_FLAG_OFFSET);
                    messageLengthBytes = bs.readByteArray(MESSAGE_LENGTH_OFFSET);
                    messageBytes = bs.readByteArray(Byte.toUnsignedInt(messageLengthBytes[3]));
                    os.write(DataChunk.parseFrom(messageBytes).getData().toByteArray());
                }
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
