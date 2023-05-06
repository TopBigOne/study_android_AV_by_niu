package com.maniu.musicclip;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.DialogTitle;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {
    MusicProcess musicProcess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        musicProcess = new MusicProcess(MainActivity.this);
    }

    public boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        }
        return false;
    }

    private static final String TAG = "MainActivity ； ";

    public void clip(View view) {
        Log.d(TAG, "clip: ");

        int a = 10;
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "music process run: ");


                try {
                    File         filesDir    = getFilesDir();
                    final String aacPath     = new File(filesDir, "music.mp3").getAbsolutePath();
                    final String videoAAPath = new File(filesDir, "input2.mp4").getAbsolutePath();
                    copyAssets("music.mp3", aacPath);
                    copyAssets("input2.mp4", videoAAPath);
                    final String videoPath  = new File(filesDir, "input2.mp4").getAbsolutePath();
                    final String outPathPcm = new File(filesDir, "outPut.mp3").getAbsolutePath();

                    musicProcess.mixAudioTrack( videoPath, aacPath, outPathPcm, 60 * 1000 * 1000, 70 * 1000 * 1000, 30,//0 - 100
                            90);//
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }).start();


    }

    private void copyAssets(String assetsName, String path) {
        Log.i(TAG, "copyAssets: path       : " + path);
        Log.i(TAG, "copyAssets: assetsName : " + assetsName);


        try {
            AssetFileDescriptor assetFileDescriptor = getAssets().openFd(assetsName);
            FileChannel         from                = new FileInputStream(assetFileDescriptor.getFileDescriptor()).getChannel();
            FileChannel         to                  = new FileOutputStream(path).getChannel();
            from.transferTo(assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength(), to);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}