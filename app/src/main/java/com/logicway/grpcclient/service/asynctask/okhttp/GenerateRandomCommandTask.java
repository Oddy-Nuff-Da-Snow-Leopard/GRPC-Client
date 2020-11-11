package com.logicway.grpcclient.service.asynctask.okhttp;

import android.os.AsyncTask;
import android.widget.Toast;

import com.logicway.grpcclient.App;
import com.logicway.grpcclient.filedownload.Command;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import io.michaelrocks.paranoid.Obfuscate;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Obfuscate
public class GenerateRandomCommandTask extends AsyncTask<Void, Void, Command> {

    @Override
    protected Command doInBackground(Void... nothing) {

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .protocols(Collections.singletonList(Protocol.H2_PRIOR_KNOWLEDGE)).build();

        Request request = new Request.Builder()
                .url("http://172.28.10.39:1337/filedownload.FileDownload/GenerateRandomCommand")
                .post(RequestBody.create(new byte[]{0, 0, 0, 0, 0}))
                .addHeader("TE", "trailers")
                .addHeader("Content-type", "application/grpc+proto")
                .addHeader("grpc-encoding", "gzip")
                .build();

        Command command = null;
        try {
            Response response = okHttpClient.newCall(request).execute();
            byte[] responseBytes = response.body().bytes();
            byte[] message = Arrays.copyOfRange(responseBytes, 5, responseBytes.length);
            command = Command.parseFrom(message);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return command;
    }

    @Override
    protected void onPostExecute(Command result) {
        Toast.makeText(App.getAppContext(),
                "Command: " + result.getCommandName(), Toast.LENGTH_SHORT).show();
    }
}
