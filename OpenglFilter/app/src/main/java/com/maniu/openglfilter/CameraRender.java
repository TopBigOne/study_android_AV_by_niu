package com.maniu.openglfilter;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.Log;

import androidx.camera.core.Preview;
import androidx.lifecycle.LifecycleOwner;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 运行在 gl thread
 */
public class CameraRender implements GLSurfaceView.Renderer {
    private static final String TAG = "CameraRender : ";

    private final GLCameraView   glCameraView;
    private       SurfaceTexture mCameraSurfaceTexture;

    //  int
    private ScreenFilter screenFilter;

    /**
     *
     */
    private int[] textures;

    /**
     * 用于匹配屏幕的矩阵
     */
    float[] mtx = new float[16];

    public CameraRender(GLCameraView glCameraView) {
        Log.d(TAG, "CameraRender: ");
        this.glCameraView = glCameraView;
        LifecycleOwner lifecycleOwner = (LifecycleOwner) glCameraView.getContext();
        /*
         *
         *  A bundle containing a {@link SurfaceTexture} and properties needed to display a Preview.
         * 摄像头输出回调。
         * @param output
         */

        CameraHelper cameraHelper = new CameraHelper(lifecycleOwner, previewOutputUpdateListener);

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
        // 让 SurfaceTexture 与 Gpu  共享一个数据源  0-31
        mCameraSurfaceTexture.attachToGLContext(textures[0]);
        // 监听摄像头数据回调，
        mCameraSurfaceTexture.setOnFrameAvailableListener(frameAvailableListener);
        screenFilter = new ScreenFilter(glCameraView.getContext());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged: width  :" + width);
        Log.d(TAG, "onSurfaceChanged: height :" + height);

        // 设置屏幕区域大小
        screenFilter.setSize(width, height);
    }

    /**
     * @param gl
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        // Log.d(TAG, "onDrawFrame: the thread is : " + Thread.currentThread().getName());
        //   摄像头的数据  ---》
        // 更新摄像头的数据给gpu ,更细纹理图像
        mCameraSurfaceTexture.updateTexImage();
        //        不是数据
        mCameraSurfaceTexture.getTransformMatrix(mtx);
        screenFilter.setTransformMatrix(mtx);
        Log.d(TAG, "onDrawFrame: mtx : "+ Arrays.toString(mtx));
        //int   数据   byte[]
        screenFilter.startDraw2(textures[0]);
    }


    private final SurfaceTexture.OnFrameAvailableListener frameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {

        /**
         * 当有数据 过来的时候
         * @param surfaceTexture
         */
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            Log.d(TAG, "onFrameAvailable: ");
            //一帧 一帧回调时
            glCameraView.requestRender();

        }
    };

    // 摄像头预览到的数据 在这里
    private final Preview.OnPreviewOutputUpdateListener previewOutputUpdateListener = new Preview.OnPreviewOutputUpdateListener() {
        /**
         *
         *  A bundle containing a {@link SurfaceTexture} and properties needed to display a Preview.
         * 摄像头输出回调。
         * @param output
         */
        @Override
        public void onUpdated(Preview.PreviewOutput output) {
            Log.d(TAG, "OnPreviewOutputUpdateListener # onUpdated: ");
            // 摄像头预览到的数据 在这里
            mCameraSurfaceTexture = output.getSurfaceTexture();

        }
    };
}
