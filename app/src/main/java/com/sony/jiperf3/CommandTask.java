/*package com.sony.jiperf3;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

class CommandTask implements Runnable {

    final String TAG = "jiperf3.CommandTask";
    List<String> data;
    CommandRecyclerViewAdapter adapter;

    static Process process;
    String command;

    public CommandTask(String command, MessageH){
        this.command = command;
        this.data = data;
        this.adapter = adapter;
    }

    public void run(){
        if (TextUtils.isEmpty(command))
            return;

        try {
            // new StringBuilder("doInBackground to run: ").append(strArr[0]);
            process = new ProcessBuilder(new String[0]).command(command.split(" ")). redirectErrorStream(true).start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    break;
                }
                // publishProgress(readLine + "\n");
                Log.d(TAG, readLine);


            }
        } catch (IOException e) {
            process.destroy();
            process = null;
        }
    }
}*/
