package com.mengrudaddy.instagram.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mengrudaddy.instagram.R;

import java.util.ArrayList;
import java.util.Set;


public class BluetoothActivity extends AppCompatActivity{
    private TextView btn_cancle,btn_ok;
    BluetoothAdapter BA;
    private IntentFilter filter;
    private Button buttonOff = findViewById(R.id.buttonOff);
    private Button buttonDiscoverDevices = findViewById(R.id.findDiscoverableDevices);
    private Button buttonPairedDevices = findViewById(R.id.viewPairedDevices);

    private final BroadcastReceiver receiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                System.out.println("@ discovered devices: " + device.getName());
            }
        }
    };

    /*
    public void turnBluetoothOff () {

        BA.disable();

        if (BA.isEnabled()) {

            Toast.makeText(getApplicationContext(), "Bluetooth could not be disabled", Toast.LENGTH_LONG).show();

        } else {

            Toast.makeText(getApplicationContext(), "Bluetooth turned off", Toast.LENGTH_LONG).show();
        }
    }
    */
    /*
    public void findDiscoverableDevices () {

        Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivity(i);

    }
    */

    public void viewPairedDevices () {

        BA = BluetoothAdapter.getDefaultAdapter();
        if(BA.isEnabled()==false){
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
        }

        BA.startDiscovery();
        filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        Set<BluetoothDevice> pairedDevices = BA.getBondedDevices();

        ListView pairedDevicesListView = (ListView) findViewById(R.id.pairedDevicesListView);

        ArrayList pairedDevicesArrayList = new ArrayList();

        for (BluetoothDevice bluetoothDevice : pairedDevices) {

            pairedDevicesArrayList.add(bluetoothDevice.getName());

        }

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, pairedDevicesArrayList);

        pairedDevicesListView.setAdapter(arrayAdapter);

    }


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

        if (BA.isEnabled()) {

            Toast.makeText(getApplicationContext(), "Bluetooth is on", Toast.LENGTH_LONG).show();

        } else {

            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(i);

            if (BA.isEnabled()) {

                Toast.makeText(getApplicationContext(), "Bluetooth turned on", Toast.LENGTH_LONG).show();
            }

        }


        buttonOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BA.disable();

                if (BA.isEnabled()) {

                    Toast.makeText(getApplicationContext(), "Bluetooth could not be disabled", Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(getApplicationContext(), "Bluetooth turned off", Toast.LENGTH_LONG).show();
                }
            }
        });

        buttonDiscoverDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                startActivity(i);
            }
        });

        buttonPairedDevices.setOnClickListener((view)->{viewPairedDevices();});
    }
}
