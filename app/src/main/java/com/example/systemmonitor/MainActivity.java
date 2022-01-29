package com.example.systemmonitor;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TabWidget;
import android.widget.TextView;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.jcraft.jsch.*;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public SSH connection = new SSH();
    public static Language language = new Language();
    public TabLayout mainTabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainTabs = (TabLayout)findViewById(R.id.mainTab);
    }

    public void changeLang(String newLang){
        language.lang = newLang;
        ((TextView)findViewById(R.id.username)).setHint(language.translations.get(newLang)[0]);
        ((TextView)findViewById(R.id.hostname)).setHint(language.translations.get(newLang)[1]);
        ((TextView)findViewById(R.id.port)).setHint(language.translations.get(newLang)[2]);
        ((TextView)findViewById(R.id.password)).setHint(language.translations.get(newLang)[3]);
        if(connection.isConnected) {
            ((TextView)findViewById(R.id.connect_button)).setText(language.translations.get(newLang)[7]);
        } else {
            ((TextView)findViewById(R.id.connect_button)).setText(language.translations.get(newLang)[4]);
        }
        ((TextView)findViewById(R.id.input_text)).setHint(language.translations.get(newLang)[5]);
        ((TextView)findViewById(R.id.execute_button)).setText(language.translations.get(newLang)[6]);
        mainTabs.getTabAt(0).setText(language.translations.get(language.lang)[10]);
        mainTabs.getTabAt(1).setText(language.translations.get(language.lang)[11]);
        mainTabs.getTabAt(2).setText(language.translations.get(language.lang)[12]);
    }

    public void ssh(View v){
        connection.userInfo = new String[]{((TextView)findViewById(R.id.username)).getText().toString(), ((TextView)findViewById(R.id.hostname)).getText().toString(), ((TextView)findViewById(R.id.port)).getText().toString(), ((TextView)findViewById(R.id.password)).getText().toString()};
        connection.input = (TextView)findViewById(R.id.input_text);
        connection.output = (TextView)findViewById(R.id.text_output);
        connection.connectButton = (Button)findViewById(R.id.connect_button);
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
        translations.put("English", new String[]{"Username", "Hostname", "Port", "Password", "Connect", /*5*/"Command", "Execute", "Disconnect", "Could not send command, not connected", "Please fill all the fields", /*10*/"SSH", "Monitor", "Settings"});
        translations.put("Français", new String[]{"Nom d'utilisateur", "Hôte", "Port", "Mot de passe", "Connection", /*5*/"Commande", "Executer", "Déconnection", "Impossible d'envoyer la commande lorsque non connecté", "Il faut remplir tous les champs", /*10*/"SSH", "Moniteur", "Paramètres"});
    }
}

class SSH extends Thread{

    public String[] userInfo = null;
    public TextView output = null;
    public TextView input = null;
    public Button connectButton = null;
    public boolean isConnected = false;

    private ChannelExec channel = null;
    private Session session;
    private Thread thread;

    public void ssh(){
        if(!isConnected){
            thread = new Thread(){
                @Override
                public void run(){
                    try {
                        (session = new JSch().getSession(userInfo[0], userInfo[1], Integer.parseInt(userInfo[2]))).setPassword(userInfo[3]);
                        session.setConfig("StrictHostKeyChecking", "no");
                        session.connect();
                        Log.d("SSH", "CONNECTION SUCCESS");
                        isConnected = true;
                    } catch (Exception e) {
                        Log.d("SSH", "CONNECTION FAILURE: " + e.toString());
                        isConnected = false;
                        if(e.toString().contains("java.lang.NumberFormatException: For input string: \"\"")){
                            output.setText(MainActivity.language.translations.get(MainActivity.language.lang)[9]);
                        } else {
                            output.setText(e.toString());
                        }
                    }
                }
            };
            thread.start();
            if(isConnected){
                output.setText(userInfo[0] + " connecté en ssh à " + userInfo[1]);
                connectButton.setBackgroundColor(Color.rgb(30, 200, 30));
                connectButton.setText(MainActivity.language.translations.get(MainActivity.language.lang)[7]);
            }
        } else {
            if(!thread.isInterrupted()) thread.interrupt();
            isConnected = false;
            connectButton.setBackgroundColor(Color.rgb(200, 200, 200));
            connectButton.setText(MainActivity.language.translations.get(MainActivity.language.lang)[4]);
            Log.d("SSH", "DISCONNECT SUCCESS");
        }
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



