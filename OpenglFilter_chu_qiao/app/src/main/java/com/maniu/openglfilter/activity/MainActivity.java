package com.maniu.openglfilter.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.maniu.openglfilter.R;
import com.maniu.openglfilter.camera.CameraView;
import com.maniu.openglfilter.camera.SoulCameraView;
import com.maniu.openglfilter.widget.RecordButton;


public class MainActivity extends AppCompatActivity implements RecordButton.OnRecordListener, RadioGroup.OnCheckedChangeListener {

    private CameraView     cameraView;
//    private SoulCameraView cameraView;
    private RecordButton   btnRecord;
    private Button       btnSoul;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        cameraView = findViewById(R.id.cameraView);
        initEvent();
    }


    private void initEvent() {

        //速度
        RadioGroup rgSpeed = findViewById(R.id.rg_speed);
        rgSpeed.setOnCheckedChangeListener(this);

        btnRecord = findViewById(R.id.btn_record);
        btnSoul = findViewById(R.id.btn_soul_view);
        btnRecord.setOnRecordListener(this);
        btnSoul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LinHunChuQiaoActivity.class);
                startActivity(intent);
            }
        });
    }

    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);

        }
        return false;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
//        switch (checkedId) {
//            case R.id.btn_extra_slow:
//                cameraView.setSpeed(CameraView.Speed.MODE_EXTRA_SLOW);
//                break;
//            case R.id.btn_slow:
//                cameraView.setSpeed(CameraView.Speed.MODE_SLOW);
//                break;
//            case R.id.btn_normal:
//                cameraView.setSpeed(CameraView.Speed.MODE_NORMAL);
//                break;
//            case R.id.btn_fast:
//                cameraView.setSpeed(CameraView.Speed.MODE_FAST);
//                break;
//            case R.id.btn_extra_fast:
//                cameraView.setSpeed(CameraView.Speed.MODE_EXTRA_FAST);
//                break;
//
//
//            default:
//        }
    }

    @Override
    public void onRecordStart() {
//        cameraView.startRecord();
    }

    @Override
    public void onRecordStop() {
        Log.i("tuch", "onRecordStop: ----------------->");
//        cameraView.stopRecord();
    }


}
