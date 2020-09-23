package com.project.busbogi.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.project.busbogi.R;
import com.project.busbogi.main.ui.BusListAdapter;
import com.project.busbogi.splash.SplashActivity;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.invoke.MethodType;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {
    private static final String TAG = "TEST-TAG";

    //UI관련 View Widget
    private ListView busListView;
    private TextView statusText;
    private BusListAdapter adapter;
    private ArrayList<Integer> busList = new ArrayList<>();

    private BeaconManager beaconManager;
    private static final String IBEACON_LAYOUT="m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";

    private Region myRegion;

    private String pushToken;

    private boolean isRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate Called");
        setContentView(R.layout.activity_main);
        initUi();
        initBeaconManager();

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        pushToken = task.getResult().getToken();

                        Log.d(TAG, pushToken);
                    }
                });
    }

    //UI 초기화 메서드
    private void initUi() {
        busListView = findViewById(R.id.busList);
        //리스트 선택시 체크박스 체크 오류로 설정
        busListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        statusText = findViewById(R.id.statusText);

        adapter = new BusListAdapter();
        adapter.setBusNumberList(busList);
        busListView.setAdapter(adapter);
        //리스트 아이템 클릭시에 문제는 xml에서 focus옵션의 설정으로 해결.

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
                Log.d(TAG ,"Beacon 정보 읽는 중");
                if( !collection.isEmpty() ) {
                    int busStationId = 0 ;
                    for (Beacon beacon : collection) {
                        busStationId = beacon.getId2().toInt();
                        String msgText = "현재 " + busStationId + "번 정류소 입니니다.";
                        statusText.setText(msgText);
                        Toast.makeText(getApplicationContext(), msgText , Toast.LENGTH_SHORT).show();
                    }
                    if( busStationId != 0 ) {
                        requestBusList(busStationId );
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
    private void requestBusList(int busStationId){
        Log.d(TAG, "receive data :" + busStationId);

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        String url = getString(R.string.api_server)+"/api/station/bus/" + busStationId;

        Log.d(TAG, "request url :" + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url,null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray stopBusList = response.getJSONArray("bus_list");
                    busList.clear();
                    Log.d(TAG, response.toString());

                    for(int i = 0 ; i < stopBusList.length(); i++){
                         busList.add( stopBusList.getInt(i) );
                    }

                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.d(TAG, "Error:" + error.getLocalizedMessage() );
            }
        });

        requestQueue.add(jsonObjectRequest);

    }

    public void requestBusAlarm(View view){
        SparseBooleanArray checkedArray = busListView.getCheckedItemPositions();

        String url = getString(R.string.api_server)+"/api/station/bus/select";
        JSONArray jsonArray = new JSONArray();
        List<Integer> busNumList = new ArrayList<>();
        for(int i = 0 ; i < busListView.getAdapter().getCount() ; i ++){
            if(checkedArray.get(i)) jsonArray.put((Integer)adapter.getItem(i));
        }
        JSONObject bodyData = new JSONObject();

        try {
            bodyData.put("user_id" , pushToken);
            bodyData.put("bus_list" , jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, bodyData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                Toast.makeText(getApplicationContext(), "버스 알림 요청을 완료했습니다.", Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, error.getMessage() + "Error!!!!");
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }


}
