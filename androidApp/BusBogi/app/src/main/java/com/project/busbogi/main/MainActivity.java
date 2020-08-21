package com.project.busbogi.main;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.project.busbogi.R;
import com.project.busbogi.main.ui.BusListAdapter;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {
    private static final String TAG = "TEST-TAG";

    private String STATUS_TAG = "STATUS";

    //UI관련 View Widget
    private ListView busListView;
    private TextView statusText;
    private BusListAdapter adapter;
    private ArrayList<String> busList;

    private BeaconManager beaconManager;
    private static final String IBEACON_LAYOUT="m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";

    private Region myRegion;

    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(STATUS_TAG, "onCreate Called");
        setContentView(R.layout.activity_main);
        initUi();
        initBeaconManager();
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

    //비콘 매니저 초기화
    private void initBeaconManager(){
        beaconManager = BeaconManager.getInstanceForApplication(this);
        //IBeacon 규격만 수신하는 레이아웃 설정
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_LAYOUT));
        //버스 정류소 아이디만 수신
        Identifier busStopId = Identifier.fromUuid( UUID.fromString("abf13d60-0202-0106-01b4-a90800200c9a"));
        myRegion = new Region("com.project.busbogi", busStopId,null ,null);
        //Service 시작
        beaconManager.bind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        statusText.setText("버스 정류소를 찾고있습니다.");
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                Log.d(TAG , "region" + region.getUniqueId());
                Log.d(TAG ,"Beacon 정보 읽는 중");
                if( !collection.isEmpty() ) {
                    int busStopNumber = 0 ;
                    for (Beacon beacon : collection) {
                        busStopNumber = beacon.getId2().toInt();
                        String msgText = "현재 " + busStopNumber + "번 정류소 입니니다.";
                        statusText.setText(msgText);
                        Toast.makeText(getApplicationContext(), msgText , Toast.LENGTH_SHORT).show();
                    }
                    if( busStopNumber != 0 ) {
                        requestBusList(busStopNumber);
                    }
                    try {
                        beaconManager.stopRangingBeaconsInRegion(myRegion);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }else{
                    Log.d(TAG , "앵? 수신한게 비어있네?....?");
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(myRegion);
        }catch (RemoteException e){
            e.printStackTrace();
        }
    }



    //정류장 번호로 버스 리스트 요청
    private void requestBusList(int data){
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
    protected void onPause() {
        Log.d(STATUS_TAG , "PAUSE");
        isRunning = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(STATUS_TAG , "RESUME");
        isRunning = true;
        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.d(STATUS_TAG , "onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(STATUS_TAG,"onDestroy");
        beaconManager.unbind(this);
        super.onDestroy();
    }

//    private static String NOTIFICATION_CHANNEL_ID = "11010";
//
//    public void notificationBusStation(int busStationId) {
//        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
//
//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        notificationIntent.putExtra("busStationId" , busStationId);
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP) ;
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,  PendingIntent.FLAG_UPDATE_CURRENT);
//
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
//                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_bus_foreground)) //BitMap 이미지 요구
//                .setContentTitle("BusBogi")
//                .setContentText("버스정류장에 도착했습니다.")
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//                .setContentIntent(pendingIntent)
//                .setAutoCancel(true);
//
//        //OREO API 26 이상에서는 채널 필요
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//            builder.setSmallIcon(R.mipmap.ic_launcher_bus); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남
//            CharSequence channelName  = "BusBogiAlarm";
//            String description = "BusBogi Notification Channel";
//            int importance = NotificationManager.IMPORTANCE_HIGH;
//
//            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName , importance);
//            channel.setDescription(description);
//
//            // 노티피케이션 채널을 시스템에 등록
//            assert notificationManager != null;
//            notificationManager.createNotificationChannel(channel);
//
//        }else builder.setSmallIcon(R.mipmap.ic_launcher_bus); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남
//
//        assert notificationManager != null;
//        notificationManager.notify(1025, builder.build()); // 고유숫자로 노티피케이션 동작시킴
//
//    }

}
