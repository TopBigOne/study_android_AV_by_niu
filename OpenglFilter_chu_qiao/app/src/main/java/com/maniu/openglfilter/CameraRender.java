package com.maniu.openglfilter;


import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.util.Log;

import androidx.camera.core.Preview;
import androidx.lifecycle.LifecycleOwner;

import java.io.File;
import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraRender {
    private static final String         TAG = "david";
    private              CameraHelper   cameraHelper;
    private              CameraView     cameraView;
    private              SurfaceTexture mCameraTexure;
    RecordFilter recordFilter;
    private MediaRecorder mRecorder;
    //    int
    private CameraFilter  cameraFilter;
    private SoulFilter    soulFilter;
    private SplitFilter   splitFilter;
    private int[]         textures;
    float[] mtx = new float[16];

    public CameraRender(CameraView cameraView) {
        this.cameraView = cameraView;
        LifecycleOwner lifecycleOwner = (LifecycleOwner) cameraView.getContext();
        //        打开摄像头
        cameraHelper = new CameraHelper(lifecycleOwner, previewOutputUpdateListener);

    }


    public void startRecord(float speed) {
        try {
            mRecorder.start(speed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecord() {
        mRecorder.stop();
    }

    public GLSurfaceView.Renderer getRenderer() {
        return renderer;
    }


    public GLSurfaceView.Renderer renderer = new GLSurfaceView.Renderer() {
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            //surface
            textures = new int[1];
            //        1
            //        让 SurfaceTexture   与 Gpu  共享一个数据源  0-31
            mCameraTexure.attachToGLContext(textures[0]);
            //监听摄像头数据回调，
            mCameraTexure.setOnFrameAvailableListener(frameAvailableListener);
            cameraFilter = new CameraFilter(cameraView.getContext());
            Context context = cameraView.getContext();
            recordFilter = new RecordFilter(context);
            soulFilter = new SoulFilter(context);
            //            splitFilter = new SplitFilter(context);
            File file = new File(context.getCacheDir() + "/input.mp4");
            if (file.exists()) {
                file.delete();
            }

            String path = file.getAbsolutePath();
            mRecorder = new MediaRecorder(cameraView.getContext(), path, EGL14.eglGetCurrentContext(), 480, 640);

        }


        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            //
            recordFilter.setSize(width, height);
            cameraFilter.setSize(width, height);


            soulFilter.setSize(width, height);
            // splitFilter.setSize(width, height);

        }

        @Override
        public void onDrawFrame(GL10 gl) {
            Log.i(TAG, "线程: " + Thread.currentThread().getName());
            //        摄像头的数据  ---》
            //        更新摄像头的数据  给了  gpu
            mCameraTexure.updateTexImage();
            //        不是数据
            mCameraTexure.getTransformMatrix(mtx);
            cameraFilter.setTransformMatrix(mtx);
            //int   数据   byte[]


            //id     FBO所在的图层   纹理  摄像头 有画面      有1  没有  画面       录屏
            int id = cameraFilter.onDraw(textures[0]);
            // 加载   新的顶点程序 和片元程序  显示屏幕  id  ----》fbo--》 像素详细
            //        显示到屏幕
                    id =  soulFilter.onDraw(id);
//            id = splitFilter.onDraw(id);
            //        是一样的
            id = recordFilter.onDraw(id);


            //        拿到了fbo的引用   ---》  编码视频      输出  直播推理
            //        起点
            //           起点数据  主动调用   opengl的函数  录制
            mRecorder.fireFrame(id, mCameraTexure.getTimestamp());

        }
    };


    private final SurfaceTexture.OnFrameAvailableListener frameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {
        //当有数据 过来的时候
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            //一帧 一帧回调时
            cameraView.requestRender();
        }
    };

    private Preview.OnPreviewOutputUpdateListener previewOutputUpdateListener = new Preview.OnPreviewOutputUpdateListener() {
        //
        @Override
        public void onUpdated(Preview.PreviewOutput output) {
            //        摄像头预览到的数据 在这里
            mCameraTexure = output.getSurfaceTexture();
        }
    };
}
