package com.maniu.openglfilter;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.Log;

import androidx.camera.core.Preview;
import androidx.lifecycle.LifecycleOwner;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 *  运行在 gl thread
 */
public class CameraRender implements GLSurfaceView.Renderer, Preview.OnPreviewOutputUpdateListener, SurfaceTexture.OnFrameAvailableListener {
    private static final String         TAG = "CameraRender : ";
    private              CameraHelper   cameraHelper;
    private              CameraView     cameraView;
    private              SurfaceTexture mCameraTexture;

    //    int
    private ScreenFilter screenFilter;
    private int[]        textures;
    float[] mtx = new float[16];

    public CameraRender(CameraView cameraView) {
        Log.d(TAG, "CameraRender: ");
        this.cameraView = cameraView;
        LifecycleOwner lifecycleOwner = (LifecycleOwner) cameraView.getContext();
        //        打开摄像头
        cameraHelper = new CameraHelper(lifecycleOwner, this);

    }

    /**
     * textures
     *
     * @param gl
     * @param config
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated: ");
        //surface
        textures = new int[1];
        //        1
        //        让 SurfaceTexture   与 Gpu  共享一个数据源  0-31
        mCameraTexture.attachToGLContext(textures[0]);
        //监听摄像头数据回调，
        mCameraTexture.setOnFrameAvailableListener(this);
        screenFilter = new ScreenFilter(cameraView.getContext());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged: ");

        screenFilter.setSize(width, height);
    }

    /**
     * @param gl
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        Log.d(TAG, "onDrawFrame: the thread is : "+ Thread.currentThread().getName());
        //   摄像头的数据  ---》
        // 更新摄像头的数据  给了  gpu
        mCameraTexture.updateTexImage();
        //        不是数据
        mCameraTexture.getTransformMatrix(mtx);
        screenFilter.setTransformMatrix(mtx);
        //int   数据   byte[]
        screenFilter.onDraw(textures[0]);
    }

    //
    @Override
    public void onUpdated(Preview.PreviewOutput output) {
        //        摄像头预览到的数据 在这里
        mCameraTexture = output.getSurfaceTexture();
    }

    //当有数据 过来的时候
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        //一帧 一帧回调时
        cameraView.requestRender();
    }
}
