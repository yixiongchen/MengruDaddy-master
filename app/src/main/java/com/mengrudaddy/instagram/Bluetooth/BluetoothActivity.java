package com.mengrudaddy.instagram.Bluetooth;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.mengrudaddy.instagram.R;

public class BluetoothActivity extends AppCompatActivity{
    private TextView btn_cancle,btn_ok;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        btn_cancle = (TextView) findViewById(R.id.cancle) ;
        btn_ok = (TextView) findViewById(R.id.ok) ;

        // on click buttons
        btn_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
