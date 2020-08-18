package com.project.busbogi.ble.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.telecom.InCallService;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.project.busbogi.MainActivity;
import com.project.busbogi.R;

import java.util.List;


public class BeaconScanService extends Service {
    ICallback callback;
    public interface ICallback { public void remoteCall(); }

    public void registCallBack(ICallback inCallService){
        callback = inCallService;
    }


    @Override
    public IBinder onBind(Intent intent) {
        binder = new ScanBinder();
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    public ScanBinder binder;

    public class ScanBinder extends Binder{
        public BeaconScanService getService(){
            return BeaconScanService.this;
        }
    }

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;

    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        scanBusStation(bluetoothLeScanner);
    }

    //정류장 스캔
    private void scanBusStation(BluetoothLeScanner bluetoothLeScanner){
        bluetoothLeScanner.startScan(scanCallback);
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

                callback.remoteCall();
                //notificationBusStation(sb.toString());
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

    //Notification 함수 및 변수
    private static String NOTIFICATION_CHANNEL_ID = "11010";

    public void notificationBusStation(String busStationId) {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("busStationId" , busStationId);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK) ;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,  PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_bus_foreground)) //BitMap 이미지 요구
                .setContentTitle("BusBogi")
                .setContentText("버스정류장에 도착했습니다.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        //OREO API 26 이상에서는 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            builder.setSmallIcon(R.mipmap.ic_launcher_bus); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남
            CharSequence channelName  = "BusBogiAlarm";
            String description = "BusBogi Notification Channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName , importance);
            channel.setDescription(description);

            // 노티피케이션 채널을 시스템에 등록
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);

        }else builder.setSmallIcon(R.mipmap.ic_launcher_bus); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남

        assert notificationManager != null;
        notificationManager.notify(1234, builder.build()); // 고유숫자로 노티피케이션 동작시킴

    }
}
