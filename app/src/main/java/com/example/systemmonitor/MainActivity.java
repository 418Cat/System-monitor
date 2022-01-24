package com.example.systemmonitor;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void enableDisable(View v){
        Button btn = (Button)v;
        Button btn2;
        if(btn.getId() == R.id.button){
            btn2 = (Button)findViewById(R.id.button2);
        } else {
            btn2 = (Button)findViewById(R.id.button);
        }
        btn.setEnabled(false);
        btn.setText("désactivé");
        btn2.setEnabled(true);
        btn2.setText("activé");
    }
}