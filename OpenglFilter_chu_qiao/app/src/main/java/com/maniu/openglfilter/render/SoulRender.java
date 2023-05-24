package com.maniu.openglfilter.render;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.Log;

import androidx.camera.core.Preview;
import androidx.lifecycle.LifecycleOwner;

import com.maniu.openglfilter.camera.CameraHelper;
import com.maniu.openglfilter.camera.SoulCameraView;
import com.maniu.openglfilter.filter.CameraFilter;
import com.maniu.openglfilter.filter.RecordFilter;
import com.maniu.openglfilter.filter.SoulFilter;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author : 周广亚
 * @version :
 * @Date :  2023/5/23 14:37
 * @Desc :
 */
public class SoulRender {
    private static final String TAG = "SoulRender : ";

    private SoulCameraView cameraView;
    private SurfaceTexture surfaceTexture;

    private SoulFilter   soulFilter;
    private RecordFilter recordFilter;

    private int[] textures = new int[1];

    private CameraFilter cameraFilter;

    // 矩阵
    private float[] mtx = new float[16];

    public SoulRender(SoulCameraView cameraView) {
        this.cameraView = cameraView;
        LifecycleOwner lifecycleOwner = (LifecycleOwner) cameraView.getContext();
        new CameraHelper(lifecycleOwner, previewOutputUpdateListener);

    }

    public GLSurfaceView.Renderer getRender() {
        return renderer;

    }


    private final GLSurfaceView.Renderer renderer = new GLSurfaceView.Renderer() {
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            surfaceTexture.attachToGLContext(textures[0]);
            Log.d(TAG, "onSurfaceCreated: textures ： " + textures[0]);
            surfaceTexture.setOnFrameAvailableListener(frameAvailableListener);

            Context context = cameraView.getContext();
            cameraFilter = new CameraFilter(context);
            soulFilter = new SoulFilter(context);
            recordFilter = new RecordFilter(context);

        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.d(TAG, "onSurfaceChanged: width: " + width + "，height: " + height);
            cameraFilter.setSize(width, height);
            soulFilter.setSize(width, height);
            recordFilter.setSize(width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            Log.d(TAG, "onDrawFrame: ");
            surfaceTexture.updateTexImage();
            surfaceTexture.getTransformMatrix(mtx);
            cameraFilter.setTransformMatrix(mtx);

            // step 1:
            int id = cameraFilter.onDraw(textures[0]);
            Log.d(TAG, "onDrawFrame: id : "+id);
            // step 2:
            id = soulFilter.onDraw(id);
            Log.d(TAG, "onDrawFrame: id : "+id);
            // step 3:
            id = recordFilter.onDraw(id);
            Log.d(TAG, "onDrawFrame: id : "+id);
        }
    };

    private final SurfaceTexture.OnFrameAvailableListener frameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            Log.d(TAG, "onFrameAvailable: ");
            // 请求绘制啊。
            cameraView.requestRender();

        }
    };


    private final Preview.OnPreviewOutputUpdateListener previewOutputUpdateListener = new Preview.OnPreviewOutputUpdateListener() {
        @Override
        public void onUpdated(Preview.PreviewOutput output) {
            Log.d(TAG, "onUpdated: ");
            surfaceTexture = output.getSurfaceTexture();
        }
    };

}
