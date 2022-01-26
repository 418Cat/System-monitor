package com.example.systemmonitor;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.jcraft.jsch.*;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void sshConnect(View v){
        Button btn = (Button)v;
        String[] infos = {"", "", "", ""};
        infos[0] = String.valueOf((TextView)findViewById(R.id.username));
        infos[1] = String.valueOf((TextView)findViewById(R.id.hostname));
        infos[2] = String.valueOf((TextView)findViewById(R.id.port));
        infos[3] = String.valueOf((TextView)findViewById(R.id.password));


    }
}