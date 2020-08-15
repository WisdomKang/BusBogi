package com.project.busbogi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    //필요권한들
    private String[] PERMISSIONS = {
            permission.BLUETOOTH,
            permission.BLUETOOTH_ADMIN,
            permission.ACCESS_COARSE_LOCATION,
            permission.ACCESS_FINE_LOCATION
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }




    //권한 설정
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
}