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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.project.busbogi.ble.service.BeaconScanService;

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
    private TextView statusText;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> busList;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        uiInit();

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        bleCheck(bluetoothAdapter);

    }

    private void uiInit() {
        busListView = findViewById(R.id.busList);
        statusText = findViewById(R.id.statusText);
        statusText.setText("정류장 스캔중...");
        busList = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, busList);
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
            }

            bluetoothLeScanner.startScan(scanCallback);
        }
    }

    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            ScanRecord scanRecord = result.getScanRecord();
            if( result.getDevice().getName() != null && result.getDevice().getName().equals("KangBeacon") ){
                Log.d("BLEDebug" , "Device name:" + result.getDevice().getName());
                Log.d("BLEDebug" , "device mac Address:"+ result.getDevice());

                StringBuilder sb = new StringBuilder();
                for(int i = 0 ; i < 16 ; i++){
                    sb.append( String.format("%02X", scanRecord.getBytes()[9+i]) );
                    sb.append("-");
                }
                Log.d("BLEDebug" , "data to String:" + sb.toString());

                //notificationBusStation(sb.toString());

                statusText.setText("정류소 찾음");
                bluetoothLeScanner.stopScan(scanCallback);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };






}
