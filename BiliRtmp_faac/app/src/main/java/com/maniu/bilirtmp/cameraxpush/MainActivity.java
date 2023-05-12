package com.maniu.bilirtmp.cameraxpush;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.maniu.bilirtmp.R;
import com.maniu.bilirtmp.live.LivePusher;


public class MainActivity extends AppCompatActivity {

//H264码流
    private LivePusher livePusher;
    private TextureView textureView;
    private String url = "rtmp://live-push.bilivideo.com/live-bvc/?streamname=live_345162489_81809986&key=03693092c85bd15a1d3fbbc227da0ad1&schedule=rtmp";
    VideoChanel videoChanel;
    AudioChannel audioChannel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camerax);
        checkPermission();
        textureView = findViewById(R.id.textureView);
        checkPermission();
        livePusher = new LivePusher(this, 800, 480, 800_000, 10, Camera.CameraInfo.CAMERA_FACING_BACK);
        //        链接B站
        livePusher.startLive(url);
        videoChanel = new VideoChanel(this, textureView, livePusher);
        audioChannel = new AudioChannel(44100, 2, livePusher);


    }
    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            }, 1);

        }
        return false;
    }

    public void switchCamera(View view) {
        livePusher.switchCamera();
    }

    public void startLive(View view) {
        videoChanel.startLive();
        audioChannel.start();
    }

    public void stopLive(View view) {
        livePusher.stopLive();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        livePusher.native_release();
    }
}
