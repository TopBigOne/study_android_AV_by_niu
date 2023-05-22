package com.ya.opengl.openglescoordinate;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.ViewGroup;

import com.ya.opengl.openglescoordinate.filter.ImageFilter;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class MainActivity extends AppCompatActivity {

    GLSurfaceView glSurfaceView;
    int           mTextureId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }


    private void initView() {

        glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(2);
        ViewGroup container = (ViewGroup) findViewById(R.id.main);
        container.addView(glSurfaceView);


        Bitmap      bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.hu_shu_lan_road);
        ImageFilter filter = new ImageFilter(this);

        glSurfaceView.setRenderer(new GLSurfaceView.Renderer() {
            @Override
            public void onSurfaceCreated(GL10 gl, EGLConfig config) {
                mTextureId = filter.initFilter(bitmap);

            }

            @Override
            public void onSurfaceChanged(GL10 gl, int width, int height) {
                GLES20.glViewport(0, 0, width, height);
            }

            @Override
            public void onDrawFrame(GL10 gl) {
                filter.startDrawFrame(mTextureId);

            }
        });


    }
}