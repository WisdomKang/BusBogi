package com.project.busbogi;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest.permission;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    //필요권한들
    private String[] PERMISSIONS = {
            permission.BLUETOOTH,
            permission.BLUETOOTH_ADMIN,
            permission.ACCESS_COARSE_LOCATION,
            permission.ACCESS_FINE_LOCATION
    };

    private ListView busListView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> busList;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        busListInit();

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        bleCheck(bluetoothAdapter);

    }

    private void busListInit() {
        busListView = findViewById(R.id.busList);
        busList = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, busList);
        busList.add("110");
        busList.add("304");
        busList.add("664");
        busListView.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();
    }

    private void bleCheck(BluetoothAdapter bluetoothAdapter) {
        if (bluetoothAdapter == null) {
            //블루투스를 지원하지 않으면 장치를 끈다
            Toast.makeText(this, "블루투스를 지원하지 않는 장치입니다.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            //연결 안되었을 때
            if (!bluetoothAdapter.isEnabled()) {
                Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(i);
            }else{
                bleScanStart(bluetoothLeScanner);
            }
        }
    }

    private void bleScanStart(final BluetoothLeScanner bluetoothLeScanner){
        bluetoothLeScanner.startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                ScanRecord scanRecord = result.getScanRecord();
                if( scanRecord.getDeviceName() != null ){
                    Log.d("BLEDebug" , "-------------ScanRecordData------------------");
                    Log.d("BLEDebug" , "getDevice:" + scanRecord.getDeviceName());
                    Log.d("BLEDebug" , "getAdvertiseFlags:"+ scanRecord.getAdvertiseFlags());
                    Log.d("BLEDebug" , "getTxPowerLevel:"+ scanRecord.getTxPowerLevel());
                    Log.d("BLEDebug" , "getServiceUuids:"+ scanRecord.getServiceUuids());
                }

            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                Log.d("BLEDebug" , "onBatchScanResults is called");
                Log.d("BLEDebug" , "List size :" + results.size());
            }

            @Override
            public void onScanFailed(int errorCode) {
                Log.d("BLEDebug" , "onScanFailed is called");
            }
        });
    }

}