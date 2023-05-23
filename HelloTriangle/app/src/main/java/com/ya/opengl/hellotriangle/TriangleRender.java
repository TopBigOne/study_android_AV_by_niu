package com.ya.opengl.hellotriangle;

import android.content.Context;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author :
 * @version :
 * @Date :  2023/5/23 19:00
 * @Desc :
 */
public class TriangleRender implements GLSurfaceView.Renderer {

    public  Context         context;
    private ConfigureShader configureShader;


    public TriangleRender(Context context) {
        this.context = context;
        configureShader = new ConfigureShader(context);
    }
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        configureShader.startConfig();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        configureShader.startDraw();
    }
}
