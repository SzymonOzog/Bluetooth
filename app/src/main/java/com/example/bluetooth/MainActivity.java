package com.example.bluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;

    TextView mStatusBlueTv;
    ImageView mBlueIv;
    Button mOnOffBtn, mDiscoverableBtn, mDiscoverBtn, mShowBtn, mSetAlTempBtn, mGetAlTempBtn, mCheckTempBtn;
    ListView mDevicesLv;
    LinearLayout MainLayout;
    ArrayList listDevices = new ArrayList();
    ArrayAdapter adapter;
    String MACAddress;
    BluetoothAdapter mBlueAdapter;
    BluetoothSocket mBlueSocket;
    Boolean isBlueConnected=false;
    final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStatusBlueTv = findViewById(R.id.statusBluetoothTv);
        mBlueIv = findViewById(R.id.bluetoothIv);
        mOnOffBtn = findViewById(R.id.onOffBtn);
        mDiscoverableBtn = findViewById(R.id.discoverableBtn);
        mDiscoverBtn = findViewById(R.id.discoverBtn);
        mShowBtn = findViewById(R.id.showBtn);
        mSetAlTempBtn = findViewById(R.id.setAlTemBtn);
        mGetAlTempBtn = findViewById(R.id.getAlTemBtn);
        mCheckTempBtn = findViewById(R.id.checkTemBtn);

        mDevicesLv = findViewById(R.id.devicesLv);
        MainLayout = findViewById(R.id.table_main);
        //adapter
        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();


        //check if bluetooth is available
        if(mBlueAdapter==null)
        {
            mStatusBlueTv.setText("Bluetooth is not available");
        }
        else
        {
            mStatusBlueTv.setText("Bluetooth is available");
        }
        if(mBlueAdapter.isEnabled())
        {
            mBlueIv.setImageResource(R.drawable.ic_action_on);
        }
        else
        {
            mBlueIv.setImageResource(R.drawable.ic_action_off);
        }


        //on btn click
        mOnOffBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mBlueAdapter.isEnabled())
                {
                    showToast("Turning Bluetooth on...");
                    //intent to turn bluetooth on
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                }
                else if(mBlueAdapter.isEnabled())
                {
                    mBlueAdapter.disable();
                    showToast("Turning Bluetooth off...");
                    mBlueIv.setImageResource(R.drawable.ic_action_off);
                }
            }
        });

        //discover bluetooth btn click
        mDiscoverableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mBlueAdapter.isDiscovering())
                {
                    showToast("Making Your Device Discoverable");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(intent, REQUEST_DISCOVER_BT);
                }
            }
        });



        //get paired devices btn click
        mShowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listDevices.clear();
                if(mBlueAdapter.isEnabled())
                {
                    Set<BluetoothDevice> devices = mBlueAdapter.getBondedDevices();
                   if(devices.size()>0)
                   {
                       for (BluetoothDevice device : devices)
                       {
                           listDevices.add("\n Paired: " + device.getName() + ": " + device.getAddress());
                       }
                   }
                   else
                    {
                       showToast("There are no paired devices");
                    }

                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(reciever, filter);
                    mBlueAdapter.startDiscovery();

                    adapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, listDevices);
                    mDevicesLv.setAdapter(adapter);
                }
                else
                {
                    //Bluetooth is off
                    showToast("Please turn bluetooth on to get paired devices");
                }


            }
        });

        mDevicesLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //we no longer need to look for devices since
                // we found the one we want to connect with
                mBlueAdapter.cancelDiscovery();
                //get the devices MAC address
                String info = ((TextView) view).getText().toString();
                MACAddress = getMACAddress(info);
                BluetoothDevice device = mBlueAdapter.getRemoteDevice(MACAddress);
                if(device.getBondState()==BluetoothDevice.BOND_NONE)
                new BTPair().execute();
                if(device.getBondState()==BluetoothDevice.BOND_BONDED)
                new  BTConnect().execute();
            }
        });

        mGetAlTempBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {
                    mBlueSocket.getOutputStream().write("02".getBytes());
                }
                catch(IOException e)
                {
                    showToast("Error sending Get Alarm Temperature code");

                }

            }
        });
//


    }

    protected final BroadcastReceiver reciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // whenever a device is found make a checkbox with its name and MAC adress
            //then add it to a list of checkboxes
            if(BluetoothDevice.ACTION_FOUND.equals(action))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
              // mDiscoveredTv.append("\n" + device.getName() + "," + device );
//                CheckBox cb = new CheckBox(getApplicationContext());
//                cb.setText(device.getName() +": " + device.getAddress());
//                cb.setId(ListBoxes.size());
//                MainLayout.addView(cb);
//                ListBoxes.add(cb);
                listDevices.add("\n Visible: " + device.getName() + ": " + device.getAddress());
                adapter.notifyDataSetChanged();
            }

        }
    };

    private String getMACAddress(String hasAddress)
    {
        return hasAddress.substring(hasAddress.length() - 17);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_ENABLE_BT:
                if(resultCode==RESULT_OK)
                {
                    //bluetooth is on
                    mBlueIv.setImageResource(R.drawable.ic_action_on);
                    showToast("Bluetooth is on");
                }
                else
                {
                    //user denied to turn bluetooth on
                    showToast("inable to turn Bluetooth on");
                }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    //toast message function
    private void showToast(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }



    private class BTPair extends AsyncTask<Void, Void, Void>
    {


        @Override
        protected Void doInBackground(Void... devices)
        {
            BluetoothDevice device = mBlueAdapter.getRemoteDevice(MACAddress);
            device.createBond();
            return null;
        }

    }

    private class BTConnect extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
          //  progress = ProgressDialog.show(MainActivity.this, "Connecting...", "Please wait!");
        }

        @Override
        protected Void doInBackground(Void... devices)
        {
            try
            {
                if(mBlueSocket==null||!isBlueConnected)
                {
                    BluetoothDevice device = mBlueAdapter.getRemoteDevice(MACAddress);
                    mBlueSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                    mBlueSocket.connect();
                    isBlueConnected=true;
                }
            }
            catch(IOException e)
            {
               // showToast("Could not connect to device");
               // progress = ProgressDialog.show(MainActivity.this, "Connecting...", );
                Log.e("","Connect method failed", e);
            }
            return null;
        }

    }


}

