package com.example.blueconnect;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    Button blueOn, blueOff, showDeviceList;//, scanDevices;
    ListView listView;//, scanListView;
    BluetoothAdapter blueAdapter;
    //ArrayList<String> stringArrayList=new ArrayList<String>();
    //ArrayAdapter<String> arrayAdapter;
    Intent btEnable, btDisable;
    int requestEnableCode;
    private ActivityResultLauncher<Intent> enablebt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        blueOn = (Button) findViewById(R.id.btOn);
        blueOff = (Button) findViewById(R.id.btOff);
        showDeviceList = (Button) findViewById(R.id.ShowDevices);
//        scanDevices=(Button) findViewById(R.id.findDevices);
        blueAdapter = BluetoothAdapter.getDefaultAdapter();
        if (blueAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth not supported on this device", Toast.LENGTH_LONG).show();
        }
        btEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        requestEnableCode = 1;
        listView = (ListView) findViewById(R.id.listview);
//        scanListView=(ListView) findViewById(R.id.scannedListView);

        enablebt = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth Enabled", Toast.LENGTH_LONG).show();
            } else if (result.getResultCode() == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Bluetooth Enabling Cancelled", Toast.LENGTH_LONG).show();
            }
        });

/*        scanDevices.setOnClickListener(new View.OnClickListener() {
            //@SuppressLint("MissingPermission")
            @Override
            public void onClick (View v) {
                try {
                    blueAdapter.startDiscovery();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(myReceiver, intentFilter);

        arrayAdapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,stringArrayList);
        scanListView.setAdapter(arrayAdapter);
*/
        bluetoothOn();
        bluetoothOff();
        showListOfDevices();

    }

    private void bluetoothOn() {
        blueOn.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (blueAdapter == null) {
                    Toast.makeText(getApplicationContext(), "This device does not support Bluetooth", Toast.LENGTH_LONG).show();
                } else {
                    if (!blueAdapter.isEnabled()) {
                        enablebt.launch(btEnable);
                    }
                }
            }
        }));
    }

    /*   BroadcastReceiver myReceiver=new BroadcastReceiver() {
           //@SuppressLint("MissingPermission")
           @Override
           public void onReceive(Context context, Intent intent) {
               String action = intent.getAction();
                   if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                       BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                       stringArrayList.add(device.getName());
                       arrayAdapter.notifyDataSetChanged();
                   }
           }
       };
   */
    private void bluetoothOff() {
        blueOff.setOnClickListener((new View.OnClickListener() {
            //@SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (blueAdapter == null) {
                    Toast.makeText(getApplicationContext(), "Bluetooth not supported on this device", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    if (blueAdapter != null && blueAdapter.isEnabled()) {
                        Toast.makeText(getApplicationContext(), "Bluetooth Disabled", Toast.LENGTH_LONG).show();
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

    private void showListOfDevices() {
        showDeviceList.setOnClickListener((new View.OnClickListener() {
            //@SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if(blueAdapter == null)
                {
                    Toast.makeText(getApplicationContext(), "Bluetooth not supported on this device", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    //@SuppressLint("MissingPermission")
                    Set<BluetoothDevice> btset = blueAdapter.getBondedDevices();

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
