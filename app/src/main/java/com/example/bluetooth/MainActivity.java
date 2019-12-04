package com.example.bluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Set;
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;

    TextView mStatusBlueTv, mPairedTv, mDiscoveredTv;
    ImageView mBlueIv;
    Button mOnOffBtn, mDiscoverableBtn, mDiscoverBtn, mPairedBtn;

    BluetoothAdapter mBlueAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStatusBlueTv = findViewById(R.id.statusBluetoothTv);
        mPairedTv = findViewById(R.id.pairedTv);
        mDiscoveredTv = findViewById(R.id.discoveredTv);
        mBlueIv = findViewById(R.id.bluetoothIv);
        mOnOffBtn = findViewById(R.id.onOffBtn);
        mDiscoverableBtn = findViewById(R.id.discoverableBtn);
        mDiscoverBtn = findViewById(R.id.discoverBtn);
        mPairedBtn = findViewById(R.id.pairedBtn);

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

        //discover devices
        mDiscoverBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
              if(mBlueAdapter.isEnabled())
              {
                  IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                  registerReceiver(reciever, filter);
                  mDiscoveredTv.setText("Discovered devices:");
                  mBlueAdapter.startDiscovery();
              }
              else
              {
                  showToast("Please turn Bluetooth on");
              }
            }
        });

        //get paired devices btn click
        mPairedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPairedTv.setText("Paired Devices:");
                if(mBlueAdapter.isEnabled())
                {
                    Set<BluetoothDevice> devices = mBlueAdapter.getBondedDevices();
                    for(BluetoothDevice device: devices)
                    {
                        mPairedTv.append("\n" + device.getName() + "," + device );
                    }
                }
                else
                {
                    //Bluetooth is off
                    showToast("Please turn bluetooth on to get paired devices");
                }
            }
        });


    }

    private final BroadcastReceiver reciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mDiscoveredTv.append("\n" + device.getName() + "," + device );
            }
        }
    };


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
}