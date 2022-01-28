package com.example.systemmonitor;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import com.jcraft.jsch.*;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public SSH connection = new SSH();
    public static Language language = new Language();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void changeLang(String newLang){
        language.lang = newLang;
        ((TextView)findViewById(R.id.username)).setHint(language.translations.get(newLang)[0]);
        ((TextView)findViewById(R.id.hostname)).setHint(language.translations.get(newLang)[1]);
        ((TextView)findViewById(R.id.port)).setHint(language.translations.get(newLang)[2]);
        ((TextView)findViewById(R.id.password)).setHint(language.translations.get(newLang)[3]);
        ((TextView)findViewById(R.id.connect_button)).setText(language.translations.get(newLang)[4]);
        ((TextView)findViewById(R.id.input_text)).setHint(language.translations.get(newLang)[5]);
        ((TextView)findViewById(R.id.execute_button)).setText(language.translations.get(newLang)[6]);
    }

    public void ssh(View v){
        connection = new SSH();
        connection.userInfo = new String[]{((TextView)findViewById(R.id.username)).getText().toString(), ((TextView)findViewById(R.id.hostname)).getText().toString(), ((TextView)findViewById(R.id.port)).getText().toString(), ((TextView)findViewById(R.id.password)).getText().toString()};
        connection.input = (TextView)findViewById(R.id.input_text);
        connection.output = (TextView)findViewById(R.id.text_output);
        connection.connect = (Button)findViewById(R.id.connect_button);
        connection.ssh();
    }

    public void execute(View v){
        connection.sendCommand();
    }

    public void onSwitch(View v){
        if(v == (Switch)findViewById(R.id.language_switch)){
            if (((Switch)findViewById(R.id.language_switch)).isChecked()){
                changeLang("Français");
            } else {
                changeLang("English");
            }
        }
    }

}

class Language {
    public static Map<String, String[]> translations = new HashMap<String, String[]>();
    public static String lang = "English";
    public Language(){
        translations.put("English", new String[]{"Username", "Hostname", "Port", "Password", "Connect", "Command", "Execute", "Disconnect", "Could not send command, not connected"});
        translations.put("Français", new String[]{"Nom d'utilisateur", "Hôte", "Port", "Mot de passe", "Connection", "Commande", "Executer", "Déconnection", "Impossible d'envoyer la commande lorsque non connecté"});
    }
}

class SSH extends Thread{

    public String[] userInfo = null;
    public TextView output = null;
    public TextView input = null;
    public Button connect = null;
    private boolean isConnected = false;

    private ChannelExec channel = null;
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
                isConnected = true;
            } catch (Exception e) {
                Log.d("SSH", "CONNECTION FAILURE: " + e.toString());
                output.setText(e.toString());
                isConnected = false;
            }
        }
    };

    public void ssh(){
        thread.start();
    }
    public void sendCommand(){
        if((session == null || !isConnected) && output!=null) {
            Log.d("SSH", "COMMAND FAILURE: NOT CONNECTED");
            output.setText(MainActivity.language.translations.get(MainActivity.language.lang)[8]);
            return;
        } else if(isConnected){
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
                        isConnected = true;
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
        } else {
            Log.d("SSH", "COMMAND FAILURE: OBJECT NOT INITIALIZED");
            return;
        }
    }
}



