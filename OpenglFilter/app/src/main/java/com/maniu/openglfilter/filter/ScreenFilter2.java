package com.maniu.openglfilter.filter;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.maniu.openglfilter.OpenGLUtils;
import com.maniu.openglfilter.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author :
 * @version :
 * @Date :  2023/5/18 14:23
 * @Desc :
 */
public class ScreenFilter2 {
    private static final String TAG = "ScreenFilter2";

    FloatBuffer textureBuffer;
    private float[] mtx;

    private int program;
    private int vPosition;
    private int vCoord;
    private int vMatrix;
    private int vTexture;
    private int mWidth;
    private int mHeight;


    FloatBuffer vertexBuffer;
    float[]     VERTEX = {-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f};

    /**
     * 纹理 坐标系
     */
    float[] TEXTURE = {0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f};

    private Context context;

    public ScreenFilter2(Context context) {
        this.context = context;
        vertexBuffer = ByteBuffer.allocateDirect(4 * 4 * 2).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.clear();
        vertexBuffer.put(VERTEX);

        textureBuffer = ByteBuffer.allocateDirect(4 * 4 * 2).order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureBuffer.clear();
        textureBuffer.put(TEXTURE);

        initOpengl();

    }


    private void initOpengl() {
        Log.d(TAG, "initOpengl: ");
        String vertexShader = OpenGLUtils.readRawTextFile(context, R.raw.camera_vert);
        //  先编译    再链接   再运行  程序
        String fragShader = OpenGLUtils.readRawTextFile(context, R.raw.camera_frag4);

        program = OpenGLUtils.loadProgram2(vertexShader, fragShader);
        // 获取着色器里的变量
        vPosition = GLES20.glGetAttribLocation(program, "vPosition");
        vCoord = GLES20.glGetAttribLocation(program, "vCoord");

        // 采样点坐标
        vTexture = GLES20.glGetUniformLocation(program, "vTexture");

        // 变换矩阵
        vMatrix = GLES20.glGetUniformLocation(program, "vMatrix");
    }

    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public void setTransformMatrix(float[] mtx) {
        this.mtx = mtx;
    }

    public void startDraw(int texture) {
        Log.d(TAG, "startDraw: ");
        // view 的大小
        GLES20.glViewport(0, 0, mWidth, mHeight);
        GLES20.glUseProgram(program);
        vertexBuffer.position(0);

        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        // cpu 将数据传递给GPU
        GLES20.glEnableVertexAttribArray(vPosition);

        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord,2,GLES20.GL_FLOAT,false,0,textureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);
        // 激活纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // sample
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GLES20.glUniform1i(vTexture, 0);
        GLES20.glUniformMatrix4fv(vMatrix, 1, false, mtx, 0);
        // 通知画画
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }


}
