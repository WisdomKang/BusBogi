package com.project.busbogi.main;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest.permission;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.project.busbogi.R;
import com.project.busbogi.ble.service.BeaconScanService;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{

    //UI관련 View Widget
    private ListView busListView;
    private TextView statusText;
    private BusListAdapter adapter;
    private ArrayList<String> busList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUi();
        beaconScanServiceStart();
    }

    //UI 초기화 메서드
    private void initUi() {
        busListView = findViewById(R.id.busList);
        statusText = findViewById(R.id.statusText);
        busList = new ArrayList<>();

        adapter = new BusListAdapter();
        adapter.setBusNumberList(busList);

        busListView.setAdapter(adapter);

        //리스트 아이템 클릭시에 문제는 xml에서 focus옵션의 설정으로 해결.
        //리스트 선택시 체크박스 체크 오류로 설정
        busListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    //
    private void beaconScanServiceStart(){
    }



    //정류장 번호로 버스 리스트 요청
    private void requestBusList(String data){
        Log.d("Test", "receive data :" + data);

        busList.add("305");
        busList.add("578");
        busList.add("708");
        busList.add("103");
        busList.add("1006");
        busList.add("79");
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onStop() {
        Intent intent = new Intent(this.getApplicationContext(), BeaconScanService.class);
        stopService(intent);
        super.onStop();
    }

}
