package com.example.blueconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    Button blueOn, blueOff, showDeviceList;
    ListView listView;
    BluetoothAdapter blueAdapter;

    Intent btEnable;
    int requestEnableCode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        blueOn=(Button) findViewById(R.id.btOn);
        blueOff=(Button) findViewById(R.id.btOff);
        showDeviceList=(Button) findViewById(R.id.ShowDevices);
        blueAdapter=BluetoothAdapter.getDefaultAdapter();
        if(blueAdapter == null)
        {
            Toast.makeText(getApplicationContext(), "Bluetooth not supported on this device", Toast.LENGTH_LONG).show();
            return;
        }
        btEnable=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        requestEnableCode=1;
        listView=(ListView) findViewById(R.id.listview);

        bluetoothOn();
        bluetoothOff();
        showListOfDevices();
    }

        private void bluetoothOff() {
        blueOff.setOnClickListener((new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if(blueAdapter == null)
                {
                    Toast.makeText(getApplicationContext(), "Bluetooth not supported on this device", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    if (blueAdapter != null && blueAdapter.isEnabled()) {
                        blueAdapter.disable();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == requestEnableCode) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth Enabled", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Bluetooth Enabling Cancelled", Toast.LENGTH_LONG).show();
            }
        }
    }
    private void bluetoothOn() {
        blueOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(blueAdapter==null)
                {
                    Toast.makeText(getApplicationContext(), "This device does not support Bluetooth", Toast.LENGTH_LONG).show();
                    return;
                }
                else
                {
                    if(!blueAdapter.isEnabled())
                    {
                        startActivityForResult(btEnable, requestEnableCode);
                    }
                }
            }
        });
    }

    private void showListOfDevices() {
        showDeviceList.setOnClickListener((new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if(blueAdapter == null)
                {
                    Toast.makeText(getApplicationContext(), "Bluetooth not supported on this device", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    @SuppressLint("MissingPermission") Set<BluetoothDevice> btset = blueAdapter.getBondedDevices();

                    String[] deviceList = new String[btset.size()];
                    int i = 0;

                    if (btset.size() > 0) {
                        for (BluetoothDevice device : btset) {
                            deviceList[i] = device.getName();
                            i++;
                        }
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceList);
                        listView.setAdapter(arrayAdapter);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }));
    }
}