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
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.ParcelUuid;
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.TarsosDSPAudioInputStream;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Obuffer;

public class MainActivity extends AppCompatActivity {

    Button listen, send, listDevices;
    ListView listView;
    TextView msg_box, status;
    EditText writeMsg;
    BluetoothAdapter blueAdapter;
    BluetoothDevice[] btArray;
    MediaPlayer restorePlayer;
    AudioTrack audioTrack;
    HandlerThread audioThread;
    TarsosDSPAudioInputStream tDSPAudioStream;
    TarsosDSPAudioFormat dspFormat;
    AudioDispatcher dispatcher;
    AudioDispatcherFactory dispatcherFactory;
    InputStream restoreStream;
    Bitstream restoreBitStream;

    SendReceive[] sendReceive = {null, null, null, null, null, null};
    ClientClass[] clients = {null, null, null, null, null, null};

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;

    private static final String APP_NAME = "BTChat";

    Intent btEnable;
    private ActivityResultLauncher<Intent> enableBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        blueAdapter = BluetoothAdapter.getDefaultAdapter();
        btEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        // Temporary code for testing audio playback
        restoreStream = getResources().openRawResource(R.raw.restore);
        restoreBitStream = new Bitstream(restoreStream);


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
                        ServerClass serverClass = new ServerClass();
                        serverClass.start();
                }
            });

        listView.setOnItemClickListener((new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                for(int i = 0; i < 6; i++)
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

                Decoder restoreDecoder = new Decoder();
                try {
                    Obuffer outBuf = restoreDecoder.decodeFrame(restoreBitStream.readFrame(), restoreBitStream);

                } catch (DecoderException e) {
                    throw new RuntimeException(e);
                } catch (BitstreamException e) {
                    throw new RuntimeException(e);
                }

                /*
                byte audioData[] = new byte[1024];
                try {
                    while (restoreStream.read(audioData) > 0) {
                        sendReceive[i].write(SBCPacketBuilder(audioData));
                        msg_box.setText(Arrays.toString(audioData));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                */

                /*
                for(int i = 0; i < 6; i++) {
                    if (sendReceive[i] != null) {

                        try {
                            while (restoreStream.read(audioData) > 0) {
                                //sendReceive[i].write(SBCPacketBuilder(audioData));
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        sendReceive[i].write(string.getBytes());
                    }
                }*/
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
 
    /*
        Attemping to reverse engineer kernel's A2DP functionality to avoid
        android's connection limits
        A2DP Spec: http://www.themadhowes.org.uk/g0etp/a2dp_spec_v12.pdf
        Packet Breakdown starts at Page 18

        Find some way to guarantee packet-by-packet sending
        Find out what AVDTP Multiplexing is cause apparently it's important?

        Octet 0: 0010 | 0010 - 0x22 - Freq 44100 | Stereo
        Octet 1: 0100 | 01 | 01 - 0x45 - Block Length 8 | 8 Sub Bands | Loudness Allocation Method
        Octet 2: 00000010 - Min Bitpool Value 2
        Octet 3: 01111111 - Max Bitpool Value 127
        Octet 4: 0 | 0 | 0 | 0 | ???? - Don't worry about it just case num frames to byte (;
        NEVERMIND, SCREW ALL THAT, AUDIOTRACK AND AUDIOFORMAT DO THAT FOR US!!!
        nvm they don't its so over
     */
    private byte[] SBCPacketBuilder(byte[] audioData) {
        // Connection index is iterator i from setOnClickListener

        // Was using these values in a library earlier but now I'm
        // Just keeping them in case they're useful later
        int frequency = 44100;
        int channels = 2;
        int blockLength = 16;
        int subbands = 8;
        int numFrames = audioData.length / (channels * blockLength);
        int sbcPacketSize = 4 + (channels * numFrames * subbands);

        byte[] sbcPacket = new byte[sbcPacketSize];
        sbcPacket[0] = 0x22;
        sbcPacket[1] = 0x45;
        sbcPacket[2] = 0x02;
        sbcPacket[3] = 0x7F;
        sbcPacket[4] = (byte) numFrames;

        // I still don't even know if this is working. The earbuds just close the connection when I send
        System.arraycopy(audioData, 0, sbcPacket, 4, audioData.length);

        return sbcPacket;

        /*
        // Start audio if this is the first time it is being streamed
        if (audioTrack == null) {
            audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                44100,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT),
                AudioTrack.MODE_STREAM
            );

            audioThread = new HandlerThread("AudioThread");
            audioThread.start();


        }*/
    }

    private class ServerClass extends Thread
    {
        private BluetoothServerSocket serverSocket = null;
        public ServerClass() {
            try {
                Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
                ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(blueAdapter, null);

                serverSocket = blueAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, uuids[0].getUuid());
            } catch (IOException | NoSuchMethodException a) {
                a.printStackTrace();
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
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
                    sendReceive[0]=new SendReceive(socket);
                    sendReceive[0].start();
                    break;
                }
            }
        }
    }

    int connections = 0;
    Set<UUID> inUse = new HashSet<>();

    private class ClientClass extends Thread
    {
        private final BluetoothDevice device;

        private final BluetoothSocket socket;
        public ClientClass (BluetoothDevice device1)
        {
            UUID connecting_uuid = null;
            ParcelUuid[] uuids = device1.getUuids();

            String uuidstring = "";
            for (ParcelUuid id : uuids) {
                uuidstring += id.toString() + '\n';
            }

            for(int i = 0; i < uuids.length; i++)
            {
                if(!inUse.contains(uuids[i].getUuid()))
                {
                    connecting_uuid = uuids[i].getUuid();
                    break;
                }
            }
                BluetoothSocket tmp = null;
                device = device1;
            try {
                tmp = device.createRfcommSocketToServiceRecord(connecting_uuid);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                sendReceive[connections]=new SendReceive(socket);
                sendReceive[connections].start();
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