package com.maniu.openglfilter.render;


import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.Log;

import androidx.camera.core.Preview;
import androidx.lifecycle.LifecycleOwner;

import com.maniu.openglfilter.camera.CameraHelper;
import com.maniu.openglfilter.camera.CameraView;
import com.maniu.openglfilter.filter.CameraFilter;
import com.maniu.openglfilter.filter.RecordFilter;
import com.maniu.openglfilter.filter.SplitFilter;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 分屏渲染
 */
public class DivideRender {
    private static final String TAG = "DivideRender : ";

    private final CameraView     cameraView;
    private       SurfaceTexture surfaceTexture;
    private       RecordFilter   recordFilter;

    private CameraFilter cameraFilter;
    private SplitFilter  splitFilter;

    private int[] textures;
    float[] mtx = new float[16];

    public DivideRender(CameraView cameraView) {
        this.cameraView = cameraView;
        LifecycleOwner lifecycleOwner = (LifecycleOwner) cameraView.getContext();
        //        打开摄像头
        new CameraHelper(lifecycleOwner, previewOutputUpdateListener);
    }

    public GLSurfaceView.Renderer getRenderer() {
        return renderer;
    }


    /**
     * 渲染回调
     */
    public GLSurfaceView.Renderer renderer = new GLSurfaceView.Renderer() {
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            //surface
            textures = new int[1];
            //        1
            //        让 SurfaceTexture   与 Gpu  共享一个数据源  0-31
            surfaceTexture.attachToGLContext(textures[0]);
            //监听摄像头数据回调，
            surfaceTexture.setOnFrameAvailableListener(frameAvailableListener);

            Context context = cameraView.getContext();

            cameraFilter = new CameraFilter(context);
            recordFilter = new RecordFilter(context);
            splitFilter = new SplitFilter(context);

        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            //
            recordFilter.setSize(width, height);
            cameraFilter.setSize(width, height);
            splitFilter.setSize(width, height);

        }

        @Override
        public void onDrawFrame(GL10 gl) {
            Log.i(TAG, "线程: " + Thread.currentThread().getName());
            //        摄像头的数据  ---》
            //        更新摄像头的数据  给了  gpu
            surfaceTexture.updateTexImage();
            //        不是数据
            surfaceTexture.getTransformMatrix(mtx);

            cameraFilter.setTransformMatrix(mtx);
            //int   数据   byte[]


            //id     FBO所在的图层   纹理  摄像头 有画面      有1  没有  画面       录屏
            int id = cameraFilter.onDraw(textures[0]);
            // 加载   新的顶点程序 和片元程序  显示屏幕  id  ----》fbo--》 像素详细
            //        显示到屏幕
            id = splitFilter.onDraw(id);
            //        是一样的
            id = recordFilter.onDraw(id);
        }
    };


    /**
     * frame 可用回调
     */
    private final SurfaceTexture.OnFrameAvailableListener frameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {
        //当有数据 过来的时候
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            // 一帧 一帧回调时
            cameraView.requestRender();
        }
    };


    /**
     * camera 预览输出回调
     */
    private Preview.OnPreviewOutputUpdateListener previewOutputUpdateListener = new Preview.OnPreviewOutputUpdateListener() {
        //
        @Override
        public void onUpdated(Preview.PreviewOutput output) {
            //        摄像头预览到的数据 在这里
            surfaceTexture = output.getSurfaceTexture();
        }
    };
}
