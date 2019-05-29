package com.example.bottomnavigation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.messaging.RemoteMessage;

public class MainActivity extends AppCompatActivity {
    private android.support.v4.app.Fragment frag1,frag2,frag3;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_url:
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame_fragmentholder, frag1)
                            .commit();

                    return true;
                case R.id.navigation_qrcode:
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame_fragmentholder, frag2)
                            .commit();

                    return true;
                case R.id.navigation_ocr:
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame_fragmentholder, frag3)
                            .commit();

                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 100);
        }
        frag1 = new urlfragment();
        frag2 = new qrcodefragment();
        frag3 = new ocrfragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_fragmentholder, frag1)
                .commit();
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }

    @Override
    protected void onPause(){
        super.onPause();

    }
    @Override
    protected void onResume(){
        super.onResume();

    }
    @Override
    protected void onDestroy(){
        super.onDestroy();

    }







}
