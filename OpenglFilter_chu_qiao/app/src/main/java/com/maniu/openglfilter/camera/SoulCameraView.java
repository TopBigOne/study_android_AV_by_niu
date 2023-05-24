package com.maniu.openglfilter.camera;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import com.maniu.openglfilter.render.SoulRender;

/**
 * @author : 周广亚
 * @version :
 * @Date :  2023/5/23 15:42
 * @Desc :
 */
public class SoulCameraView extends GLSurfaceView {

    private static final String     TAG = "SoulCameraView ： ";
    private              SoulRender soulRender;
    

    public SoulCameraView(Context context) {
        super(context);
        initView();
    }


    public SoulCameraView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initView();
    }

    private void initView() {
        Log.d(TAG, "initView: ");
        
        setEGLContextClientVersion(2);
        soulRender = new SoulRender(this);
        setRenderer(soulRender.getRender());
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }


}
