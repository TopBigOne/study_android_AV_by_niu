package com.maniu.openglfilter.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.maniu.openglfilter.R;
import com.maniu.openglfilter.camera.SoulCameraView;

/**
 * 灵魂出窍 特效；
 */
public class LinHunChuQiaoActivity extends AppCompatActivity {

    private SoulCameraView mCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lin_hun_chu_qiao);
        mCameraView = findViewById(R.id.main_soul_camera_view);
    }
}