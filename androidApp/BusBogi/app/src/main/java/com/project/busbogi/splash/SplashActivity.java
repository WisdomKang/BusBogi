package com.project.busbogi.splash;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.project.busbogi.R;
import com.project.busbogi.main.MainActivity;

public class SplashActivity extends AppCompatActivity {
    //필요권한들
    private String[] PERMISSIONS = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private BluetoothLeScanner bluetoothLeScanner;

    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        imageView = findViewById(R.id.imageView);

        //인트로 화면 애니메이션
        Animation ani = AnimationUtils.loadAnimation(this, R.anim.intro_bus_anim);
        imageView.setAnimation(ani);

        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        //Permission관련 로직 수행
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if( !hasPermissions( getApplicationContext(), PERMISSIONS) ){
                    getPermission();
                }else{
                    bleCheck(bluetoothAdapter);
                }
            }
        }, 3000);
    }

    //메인 액티비티 실행
    private void startMainActivity(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    //각 Permission에 대하여 권한을 가지고 있는지 체크
    public boolean hasPermissions(Context context, String[] permissions){
        if( context != null && permissions != null){
            for( String permission : permissions){
                if(ActivityCompat.checkSelfPermission(context , permission) != PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        return true;
    }

    //Permission 요청
    public void getPermission(){
        ActivityCompat.requestPermissions( this , PERMISSIONS ,1000 );
    }

    //Permission 요청후 Callback으로 권한 부여시에 다음 엑티비티로 전환
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean permissionCheck = true;
        for(int i = 0 ; i < permissions.length; i++){
            if(grantResults[i] != 0 ){
                permissionCheck = !permissionCheck;
                break;
            }
        }

        if( permissionCheck ){
            bleCheck(bluetoothAdapter);
        }else{
            Toast.makeText(this, "권할 설정을 해주세요." , Toast.LENGTH_SHORT).show();
            finish();
        }
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
                startActivityForResult(i, 1);
            }else{
                startMainActivity();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( resultCode == -1){
            startMainActivity();
        }else{
            Toast.makeText(this, "블루투스 장치를 켜주세요.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}