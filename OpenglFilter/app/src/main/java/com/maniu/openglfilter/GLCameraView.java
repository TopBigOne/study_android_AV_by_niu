package com.maniu.openglfilter;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

/**
 * GLSurfaceView   glthread  线程     gl 传参  gpu       主线程
 *
 * 界面显示的view
 */
public class GLCameraView extends GLSurfaceView {
    private static final String TAG = "glCameraView : ";

    public GLCameraView(Context context) {
        super(context);
    }

    public GLCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //        2
        setEGLContextClientVersion(2);
        CameraRender renderer = new CameraRender(this);
        //        opengl  有讲究
        setRenderer(renderer);
        /*
         * 刷新方式：
         *     RENDERMODE_WHEN_DIRTY 手动刷新，調用requestRender();
         *     RENDERMODE_CONTINUOUSLY 自動刷新，大概16ms自動回調一次onDrawFrame方法
         */
        //注意必须在setRenderer 后面。
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);


    }
}
