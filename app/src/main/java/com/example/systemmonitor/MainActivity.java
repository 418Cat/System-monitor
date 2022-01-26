package com.example.systemmonitor;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.jcraft.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    public SSH connection = new SSH();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void ssh(View v){
        String[] infos = {((TextView)findViewById(R.id.username)).getText().toString(), ((TextView)findViewById(R.id.hostname)).getText().toString(), ((TextView)findViewById(R.id.port)).getText().toString(), ((TextView)findViewById(R.id.password)).getText().toString()};
        TextView output = (TextView) findViewById(R.id.output);
        connection.ssh(infos, output);
    }

    public void execute(View v){
        connection.sendCommand(((TextView)findViewById(R.id.command)).toString());
    }

}

class SSH extends Thread {

    private String[] infos = {};
    private TextView output;
    Channel channel = null;
    private Session session;
    private String response;
    String command = null;
    
    private Thread thread = new Thread() {
        @Override
        public void run() {
            try{
                session = new JSch().getSession(infos[0], infos[1], Integer.parseInt(infos[2]));
                session.setPassword(infos[3]);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();
                channel = (ChannelExec) session.openChannel("exec");
                output.setText("Connecté en ssh à " + infos[1]);
                Log.d("SSH", "SUCCESS");
                while(channel.isConnected()){
                    if(command != null) {
                        channel.setInputStream(new ByteArrayInputStream(command.getBytes()));
                        command = null;
                        output.setText(channel.getOutputStream().toString());
                    }
                    thread.sleep(100);
                }
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
    public void sendCommand(String input){
        if(!channel.isConnected()) return;
        if(command.toLowerCase() == "exit"){channel.disconnect();return;}
        command = input;
    }
}

