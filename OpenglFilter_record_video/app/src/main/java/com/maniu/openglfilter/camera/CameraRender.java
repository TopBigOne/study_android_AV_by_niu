package com.maniu.openglfilter.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.util.Log;

import androidx.camera.core.Preview;
import androidx.lifecycle.LifecycleOwner;

import com.maniu.openglfilter.MediaRecorder;
import com.maniu.openglfilter.filter.RecordFilter;
import com.maniu.openglfilter.filter.CameraFilter;

import java.io.File;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraRender implements GLSurfaceView.Renderer, Preview.OnPreviewOutputUpdateListener, SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "CameraRender : ";
    private CameraHelper cameraHelper;
    private CameraView cameraView;
    private SurfaceTexture mCameraTexure;
    RecordFilter recordFilter;
    private MediaRecorder mRecorder;
//    int
    private CameraFilter  cameraFilter;
    private int[]         textures;
    float[] mtx = new float[16];
    public CameraRender(CameraView cameraView) {
        this.cameraView = cameraView;
        LifecycleOwner lifecycleOwner = (LifecycleOwner) cameraView.getContext();
//        打开摄像头
        cameraHelper = new CameraHelper(lifecycleOwner, this);

    }
//textures 主线程    1   EGL线程
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//surface
        textures = new int[1];
//        1
//        让 SurfaceTexture   与 Gpu  共享一个数据源  0-31
        mCameraTexure.attachToGLContext(textures[0]);
//监听摄像头数据回调，
        mCameraTexure.setOnFrameAvailableListener(this);
        cameraFilter = new CameraFilter(cameraView.getContext());
        Context context = cameraView.getContext();
        recordFilter = new RecordFilter(context);
        String path = new File(Environment.getExternalStorageDirectory(),
                "input.mp4").getAbsolutePath();

        mRecorder = new MediaRecorder(cameraView.getContext(), path,
                EGL14.eglGetCurrentContext(),
                480, 640);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
//
        recordFilter.setSize(width,height);
        cameraFilter.setSize(width,height);
    }
//  有数据的时候给
    @Override
    public void onDrawFrame(GL10 gl) {
//        摄像头的数据  ---》
//        更新摄像头的数据  给了  gpu
        mCameraTexure.updateTexImage();
//        不是数据
        mCameraTexure.getTransformMatrix(mtx);
        cameraFilter.setTransformMatrix(mtx);
//int   数据   byte[]

//id     FBO所在的图层   纹理  摄像头 有画面      有1  没有  画面       录屏
        int id =  cameraFilter.onDraw(textures[0]);
// 加载   新的顶点程序 和片元程序  显示屏幕  id  ----》fbo--》 像素详细
//        显示到屏幕
        id = recordFilter.onDraw(id);
//        拿到了fbo的引用   ---》  编码视频      输出  直播推理
//        起点
//           起点数据  主动调用   opengl的函数
        mRecorder.fireFrame(id,mCameraTexure.getTimestamp());
    }
//
    @Override
    public void onUpdated(Preview.PreviewOutput output) {
//        摄像头预览到的数据 在这里
        mCameraTexure=output.getSurfaceTexture();
    }

    /**
     *
     * @param surfaceTexture
     */
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
       // 手动请求刷新，内部调用：notifyAll()一帧 一帧回调时
        cameraView.requestRender();
    }

    public void startRecord(float speed) {
        Log.i(TAG, "startRecord: ");
        try {
            mRecorder.start(speed);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void stopRecord() {
        mRecorder.stop();
    }
}
