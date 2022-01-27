package com.example.systemmonitor;
import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaCas;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.jcraft.jsch.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    public SSH connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void ssh(View v){
        connection = new SSH();
        String[] infos = {((TextView)findViewById(R.id.username)).getText().toString(), ((TextView)findViewById(R.id.hostname)).getText().toString(), ((TextView)findViewById(R.id.port)).getText().toString(), ((TextView)findViewById(R.id.password)).getText().toString()};
        TextView output = (TextView) findViewById(R.id.output);
        connection.infos = infos;
        connection.input = (TextView)findViewById(R.id.input);
        connection.output = (TextView)findViewById(R.id.output);
        connection.ssh();
    }

    public void execute(View v){
        connection.sendCommand();
    }

}

class SSH extends Thread{

    public String[] infos;
    public TextView output;
    public TextView input;

    private ChannelExec channel;
    private Session session;
    private Thread thread = new Thread() {
        @Override
        public void run(){
            try {

                (session = new JSch().getSession(infos[0], infos[1], Integer.parseInt(infos[2]))).setPassword(infos[3]);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();

                Log.d("SSH", "CONNECTION SUCCESS");
                output.setText(infos[0] + " connecté en ssh à " + infos[1]);

            } catch (Exception e) {
                Log.d("SSH", "CONNECTION FAILURE: " + e.toString());
                output.setText(e.toString());
            }
        }
    };

    public void ssh(){
        thread.start();
    }
    public void sendCommand(){
        Thread t = new Thread(){
            @Override
            public void run(){
                try {
                    channel = (ChannelExec)session.openChannel("exec");
                    channel.setCommand(input.getText().toString());
                    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                    channel.setOutputStream(responseStream);
                    channel.connect();

                    while (channel.isConnected()) {
                        Thread.sleep(100);
                    }

                    Log.d("SSH", new String(responseStream.toByteArray()));
                    output.setText(new String(responseStream.toByteArray()));
                    Log.d("SSH", "COMMAND SUCCESS");
                } catch(Exception e){
                    Log.d("SSH", "COMMAND FAILURE: " + e.toString());
                    output.setText(e.toString());
                }
            }
        };
        t.start();
    }
}

