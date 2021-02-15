package com.sony.jiperf3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.system.ErrnoException;
import android.system.Os;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRvOutput;
    private EditText mPtCommand;
    private ExecutorService executorService;
    private CommandRecyclerViewAdapter adapter;
    private ArrayList<String> data;
    private MessageHandler messageHandler;
    static String TAG = "jiperf3.CommandTask";

    // iperf3 --help    <= OK
    // iperf3 -c 192.168.1.108 -p 7575 (server running on windows: iperf3 -s -p 7575)   <= OK (WiFi)
    // iperf3 -u -c xx.xxx.xxx.xx -p 5203 -w 416K -b 300M -i 1 -t 60 -V -R -P 4 (Maxims command)     <= OK (WiFi)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            Os.setenv("TMPDIR", "/data/data/com.sony.jiperf3/cache", true);
        } catch (ErrnoException e) {
            e.printStackTrace();
        }

        mRvOutput = findViewById(R.id.rv_output);
        mPtCommand = findViewById(R.id.pt_command);

        // Fix with the recycler view
        messageHandler = new MessageHandler(this);
        data = new ArrayList<String>();
        RecyclerView recyclerView = findViewById(R.id.rv_output);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommandRecyclerViewAdapter(this, data);
        // adapter.setClickListener(this); // skipping this for now
        recyclerView.setAdapter(adapter);

        findViewById(R.id.b_go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pingAdapter.clear();
                String command = mPtCommand.getText().toString().trim();
                if(!TextUtils.isEmpty(command)){
                    // String pingCmd = spellPing(ip);
                    executeCommand(command);
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mRvOutput.setAdapter(adapter);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }

    private void executeCommand(String command){
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new CommandTask(command, messageHandler));
    }

    class CommandTask implements Runnable {
        List<String> data;
        CommandRecyclerViewAdapter adapter;

        Process process;
        String command;

        public CommandTask(String command, MessageHandler handler){
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

                    // send messge
                    Message msg = messageHandler.obtainMessage();
                    msg.obj = readLine.trim();
                    msg.what = 10;
                    msg.sendToTarget();
                }
            } catch (IOException e) {
                process.destroy();
                process = null;
            }
        }
    }

    private static class MessageHandler extends Handler {
        private WeakReference<MainActivity> weakReference;

        public MessageHandler(MainActivity activity){
            this.weakReference = new WeakReference<>(activity);
        }

        public void handleMessage(@NonNull Message msg){
            switch (msg.what){
                case 10:
                    String strMessage = (String)msg.obj;
                    Log.d(TAG, strMessage);
                    weakReference.get().data.add(strMessage);
                    int lastIndex = weakReference.get().data.size() -1;
                    weakReference.get().adapter.notifyItemInserted(lastIndex);
                    weakReference.get().mRvOutput.scrollToPosition(lastIndex);

            }
        }
    }
}