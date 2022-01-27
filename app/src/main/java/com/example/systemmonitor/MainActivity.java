package com.example.systemmonitor;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.jcraft.jsch.*;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    public SSH connection;
    public String[][] translations = {{"English", "Username", "Hostname", "Port", "Password", "Connect", "Command", "Execute"}, {"Français", "Nom d'utilisateur", "Hôte", "Port", "Mot de passe", "Connection", "Commande", "Executer"}};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setLanguages("Français");
    }

    public void setLanguages(String language){
        String[] translate = {};
        for(String[] i:translations){
            if(i[0] == language){
                translate = i;
                break;
            }
        }
        ((TextView)findViewById(R.id.username)).setHint(translate[1]);
        ((TextView)findViewById(R.id.hostname)).setHint(translate[2]);
        ((TextView)findViewById(R.id.port)).setHint(translate[3]);
        ((TextView)findViewById(R.id.password)).setHint(translate[4]);
        ((TextView)findViewById(R.id.connect_button)).setText(translate[5]);
        ((TextView)findViewById(R.id.input_text)).setHint(translate[6]);
        ((TextView)findViewById(R.id.execute_button)).setText(translate[7]);
    }

    public void ssh(View v){
        connection = new SSH();
        String[] userInfo = {((TextView)findViewById(R.id.username)).getText().toString(), ((TextView)findViewById(R.id.hostname)).getText().toString(), ((TextView)findViewById(R.id.port)).getText().toString(), ((TextView)findViewById(R.id.password)).getText().toString()};
        connection.userInfo = userInfo;
        connection.input = (TextView)findViewById(R.id.input_text);
        connection.output = (TextView)findViewById(R.id.text_output);
        connection.ssh();
    }

    public void execute(View v){
        connection.sendCommand();
    }

}

class SSH extends Thread{

    public String[] userInfo;
    public TextView output;
    public TextView input;

    private ChannelExec channel;
    private Session session;
    private Thread thread = new Thread() {
        @Override
        public void run(){
            try {

                (session = new JSch().getSession(userInfo[0], userInfo[1], Integer.parseInt(userInfo[2]))).setPassword(userInfo[3]);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();

                Log.d("SSH", "CONNECTION SUCCESS");
                output.setText(userInfo[0] + " connecté en ssh à " + userInfo[1]);

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

