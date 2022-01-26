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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void ssh(View v){
        String[] infos = {((TextView)findViewById(R.id.username)).getText().toString(), ((TextView)findViewById(R.id.hostname)).getText().toString(), ((TextView)findViewById(R.id.port)).getText().toString(), ((TextView)findViewById(R.id.password)).getText().toString()};
        TextView output = (TextView) findViewById(R.id.output);
        new SSH().ssh(infos, output);
    }

}

class SSH extends Thread {
    private String[] infos = {};
    private TextView output;
    Channel channel = null;
    Thread thread = new Thread() {
        @Override
        public void run() {
            try{
                Session session = new JSch().getSession(infos[0], infos[1], Integer.parseInt(infos[2]));
                session.setPassword(infos[3]);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();
                output.setText("Connecté en ssh à " + infos[1]);
                Log.d("SSH", "SUCCESS");
                channel = (ChannelExec) session.openChannel("exec");
                ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                channel.setOutputStream(responseStream);
                channel.connect();

                while (channel.isConnected()) {
                    Thread.sleep(100);
                }

                String responseString = new String(responseStream.toByteArray());
                System.out.println(responseString);
            } catch(Exception e) {
                output.setText(e.toString());
                Log.d("SSH", e.toString());
                Log.d("SSH", "FAILURE");
            }
        }
    };

    public void ssh(String[] infos, TextView output){
        this.infos = infos;
        this.output = output;
        thread.start();
    }


}

