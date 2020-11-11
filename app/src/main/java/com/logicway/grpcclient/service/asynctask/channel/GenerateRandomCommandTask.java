package com.logicway.grpcclient.service.asynctask.channel;

import android.os.AsyncTask;
import android.widget.Toast;

import com.google.protobuf.Empty;
import com.logicway.grpcclient.App;
import com.logicway.grpcclient.filedownload.Command;
import com.logicway.grpcclient.filedownload.FileDownloadGrpc;

import io.grpc.ManagedChannel;
import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class GenerateRandomCommandTask extends AsyncTask<Void, Void, Command> {

    private final ManagedChannel channel;

    public GenerateRandomCommandTask(ManagedChannel channel) {
        this.channel = channel;
    }

    @Override
    protected Command doInBackground(Void... nothing) {
        return FileDownloadGrpc.newBlockingStub(channel)
                .generateRandomCommand(Empty.newBuilder().build());
    }

    @Override
    protected void onPostExecute(Command result) {
        Toast.makeText(App.getAppContext(),
                "Command: " + result.getCommandName(), Toast.LENGTH_SHORT).show();
    }
}
