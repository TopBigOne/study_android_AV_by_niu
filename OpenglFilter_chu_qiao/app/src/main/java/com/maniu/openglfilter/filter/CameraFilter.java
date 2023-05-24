package com.maniu.openglfilter.filter;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import androidx.annotation.LongDef;

import com.maniu.openglfilter.R;

import java.util.Arrays;

public class CameraFilter extends AbstractFboFilter {

    private static final String TAG = "CameraFilter : ";
    private       float[] mtx;
    private final int     vMatrix;
    public CameraFilter(Context context) {
        super(context, R.raw.camera_vert, R.raw.camera_frag);
        vMatrix = GLES20.glGetUniformLocation(program, "vMatrix");
    }

    @Override
    public void beforeDraw() {
        super.beforeDraw();
        Log.i(TAG, "beforeDraw:    mtx : "+ Arrays.toString(mtx)+"       step 2");
        //
        GLES20.glUniformMatrix4fv(vMatrix, 1, false, mtx, 0);
    }

    @Override
    public void setTransformMatrix(float[] mtx) {
        Log.i(TAG, "setTransformMatrix : mtx : "+ Arrays.toString(mtx)+" step 1");
        this.mtx = mtx;
    }


}
