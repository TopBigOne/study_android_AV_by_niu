package com.maniu.openglfilter.camera;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.maniu.openglfilter.render.NormalRender;

/**
 * GLSurfaceView   glthread  线程     gl 传参  gpu       主线程
 */
public class CameraView extends GLSurfaceView {
    private NormalRender renderer;

    public CameraView(Context context) {
        super(context);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //        2
        setEGLContextClientVersion(2);
        renderer = new NormalRender(this);
        //        opengl  有讲究
        setRenderer(renderer.getRenderer());
        /*
         * 刷新方式：
         *     RENDERMODE_WHEN_DIRTY 手动刷新，調用requestRender();
         *     RENDERMODE_CONTINUOUSLY 自動刷新，大概16ms自動回調一次onDrawFrame方法
         */
        //注意必须在setRenderer 后面。
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);


    }



}
