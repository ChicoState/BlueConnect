package com.example.blueconnect;

        import static androidx.constraintlayout.widget.StateSet.TAG;

        import android.Manifest;
        import android.annotation.SuppressLint;
        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;
        import android.bluetooth.BluetoothServerSocket;
        import android.bluetooth.BluetoothSocket;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;
        import android.content.pm.PackageManager;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.Message;
        import android.util.Log;
        import android.view.View;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ListView;
        import android.widget.TextView;
        import android.widget.Toast;

        import androidx.annotation.NonNull;
        import androidx.annotation.Nullable;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.core.app.ActivityCompat;
        import androidx.core.content.ContextCompat;


        import androidx.activity.result.ActivityResultLauncher;
        import androidx.activity.result.contract.ActivityResultContracts;

        import java.io.IOException;
        import java.io.InputStream;
        import java.io.OutputStream;
        import java.util.ArrayList;
        import java.util.Set;
        import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button listen, send, listDevices;
    ListView listView;
    TextView msg_box, status;
    EditText writeMsg;
    BluetoothAdapter blueAdapter;
    BluetoothDevice[] btArray;

    SendReceive[] sendIt = {null, null, null, null, null};
    SendReceive[] receiveIt = {null, null, null, null, null};
    ClientClass[] clients = {null, null, null, null, null};

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;

    private static final String APP_NAME = "BTChat";
    private static final UUID MY_UUID1 = UUID.fromString("ec01d2c3-52ef-4495-be73-112d2e2ce787");
    private static final UUID MY_UUID2 = UUID.fromString("ef790c77-fd18-4e86-9e62-08aeba104465");
    private static final UUID MY_UUID3 = UUID.fromString("2238fe5f-b5cc-4226-9416-3d2385146075");
    private static final UUID MY_UUID4 = UUID.fromString("a16ecdf3-74c1-40db-94e4-6f29be306617");
    private static final UUID MY_UUID5 = UUID.fromString("89143ef2-bf5e-434b-a740-43c05dc4697a");
    Intent btEnable;
    private ActivityResultLauncher<Intent> enableBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        blueAdapter = BluetoothAdapter.getDefaultAdapter();
        btEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        enableBt = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth Enabled", Toast.LENGTH_LONG).show();
            } else if (result.getResultCode() == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Bluetooth Enabling Cancelled", Toast.LENGTH_LONG).show();
            }
        });
        findViewByIds();
        implementListeners();
        bluetoothOn();
    }//OnCreate End

    private void implementListeners() {
        listDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(blueAdapter == null)
                {
                    Toast.makeText(getApplicationContext(), "Bluetooth not supported on this device", Toast.LENGTH_LONG).show();
                    return;
                }
                Set<BluetoothDevice> bt = blueAdapter.getBondedDevices();
                btArray=new BluetoothDevice[bt.size()];
                String[] strings = new String[bt.size()];
                int index = 0;

                if (bt.size() > 0) {
                    for (BluetoothDevice device : bt) {
                        btArray[index]=device;
                        strings[index] = device.getName();
                        index++;
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, strings);
                    listView.setAdapter(arrayAdapter);
                }
            }
        });

        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerClass1 serverClass1 = new ServerClass1();
                serverClass1.start();
                ServerClass2 serverClass2 = new ServerClass2();
                serverClass2.start();
                ServerClass3 serverClass3 = new ServerClass3();
                serverClass3.start();
                ServerClass4 serverClass4 = new ServerClass4();
                serverClass4.start();
                ServerClass5 serverClass5 = new ServerClass5();
                serverClass5.start();
            }
        });

        listView.setOnItemClickListener((new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for(int i = 0; i < 5; i++)
                {
                    if(clients[i] == null)
                    {
                        clients[i] = new ClientClass(btArray[position]);
                        clients[i].start();
                        status.setText("Connecting");
                        break;
                    }
                }
            }
        }));

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string=String.valueOf(writeMsg.getText());
                for(int i = 0; i < 5; i++) {
                    if (sendIt[i] != null) {
                        sendIt[i].write(string.getBytes());
                    }
                }
            }
        });
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch(msg.what)
            {
                case STATE_LISTENING:
                    status.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    status.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    status.setText("Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText("connection Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff= (byte[]) msg.obj;
                    String tempMsg=new String(readBuff,0,msg.arg1);
                    msg_box.setText(tempMsg);
                    break;
            }
            return true;
        }
    });

    private void findViewByIds() {
        //Buttons
        listen=(Button) findViewById(R.id.listen);
        send=(Button) findViewById(R.id.send);
        listView = (ListView) findViewById(R.id.listview);
        msg_box = (TextView) findViewById(R.id.msg);
        status= (TextView) findViewById(R.id.status);
        writeMsg=(EditText) findViewById(R.id.writemsg);
        listDevices = (Button) findViewById(R.id.listdevices);
    }

    private void bluetoothOn() {
        if (blueAdapter == null) {
            Toast.makeText(getApplicationContext(), "This device does not support Bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (!blueAdapter.isEnabled()) {
                enableBt.launch(btEnable);
            }
        }
    }

    int serve = 0;
    private class ServerClass1 extends Thread
    {
        private BluetoothServerSocket serverSocket = null;
        public ServerClass1() {
            try {
                serverSocket = blueAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID1);
            } catch (IOException a) {
                a.printStackTrace();
            }
        }
        public void run()
        {
            BluetoothSocket socket = null;
            while (socket == null)
            {
                try {
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTING;
                    handler.sendMessage(message);
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }
                if(socket != null)
                {
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTED;
                    handler.sendMessage(message);
                    receiveIt[serve]=new SendReceive(socket);
                    receiveIt[serve].start();
                    serve += 1;
                    socket = null;
                }
            }
        }
    }

    private class ServerClass2 extends Thread
    {
        private BluetoothServerSocket serverSocket = null;
        public ServerClass2() {
            try {
                serverSocket = blueAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID2);
            } catch (IOException a) {
                a.printStackTrace();
            }
        }
        public void run()
        {
            BluetoothSocket socket = null;
            while (socket == null)
            {
                try {
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTING;
                    handler.sendMessage(message);
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }
                if(socket != null)
                {
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTED;
                    handler.sendMessage(message);
                    receiveIt[serve]=new SendReceive(socket);
                    receiveIt[serve].start();
                    serve += 1;
                    socket = null;
                }
            }
        }
    }

    private class ServerClass3 extends Thread
    {
        private BluetoothServerSocket serverSocket = null;
        public ServerClass3() {
            try {
                serverSocket = blueAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID3);
            } catch (IOException a) {
                a.printStackTrace();
            }
        }
        public void run()
        {
            BluetoothSocket socket = null;
            while (socket == null)
            {
                try {
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTING;
                    handler.sendMessage(message);
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }
                if(socket != null)
                {
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTED;
                    handler.sendMessage(message);
                    receiveIt[serve]=new SendReceive(socket);
                    receiveIt[serve].start();
                    serve += 1;
                    socket = null;
                }
            }
        }
    }

    private class ServerClass4 extends Thread
    {
        private BluetoothServerSocket serverSocket = null;
        public ServerClass4() {
            try {
                serverSocket = blueAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID4);
            } catch (IOException a) {
                a.printStackTrace();
            }
        }
        public void run()
        {
            BluetoothSocket socket = null;
            while (socket == null)
            {
                try {
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTING;
                    handler.sendMessage(message);
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }
                if(socket != null)
                {
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTED;
                    handler.sendMessage(message);
                    receiveIt[serve]=new SendReceive(socket);
                    receiveIt[serve].start();
                    serve += 1;
                    socket = null;
                }
            }
        }
    }

    private class ServerClass5 extends Thread
    {
        private BluetoothServerSocket serverSocket = null;
        public ServerClass5() {
            try {
                serverSocket = blueAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID5);
            } catch (IOException a) {
                a.printStackTrace();
            }
        }
        public void run()
        {
            BluetoothSocket socket = null;
            while (socket == null)
            {
                try {
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTING;
                    handler.sendMessage(message);
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }
                if(socket != null)
                {
                    Message message=Message.obtain();
                    message.what=STATE_CONNECTED;
                    handler.sendMessage(message);
                    receiveIt[serve]=new SendReceive(socket);
                    receiveIt[serve].start();
                    serve += 1;
                    socket = null;
                }
            }
        }
    }
    int connections = 0;
    int conn = 0;
    private class ClientClass extends Thread
    {
        private final BluetoothDevice device;
        private final BluetoothSocket socket;
        public ClientClass (BluetoothDevice device1)
        {
            BluetoothSocket tmp = null;
            device = device1;

            if(connections == 0) {
                try {
                    tmp = device.createRfcommSocketToServiceRecord(MY_UUID1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(connections == 1) {
                try {
                    tmp = device.createRfcommSocketToServiceRecord(MY_UUID2);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(connections == 2) {
                try {
                    tmp = device.createRfcommSocketToServiceRecord(MY_UUID3);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (connections == 3) {
                try {
                    tmp = device.createRfcommSocketToServiceRecord(MY_UUID4);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(connections == 4) {
                try {
                    tmp = device.createRfcommSocketToServiceRecord(MY_UUID5);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            connections++;
            socket = tmp;
        }
        public void run()
        {
            //blueAdapter.cancelDiscovery(); Leaving commented out on purpose as a reminder that this needs to be here if discovery of new devices gets implemented.
            try {
                socket.connect();
                Message message = Message.obtain();
                message.what=STATE_CONNECTED;
                handler.sendMessage(message);
                sendIt[conn] =new SendReceive(socket);
                sendIt[conn].start();
                conn += 1;
            } catch(IOException connectException) {
                Message message = Message.obtain();
                message.what=STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
                try {
                    socket.close();
                }
                catch(IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }
        }
    }
    private class SendReceive extends Thread
    {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        public SendReceive (BluetoothSocket socket)
        {
            bluetoothSocket=socket;
            InputStream tempIn=null;
            OutputStream tempOut=null;
            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            inputStream=tempIn;
            outputStream=tempOut;
        }
        public void run()
        {
            byte[] buffer=new byte[1024];
            int bytes;
            while(true)
            {
                try {
                    bytes=inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED,bytes,-1,buffer).sendToTarget();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}