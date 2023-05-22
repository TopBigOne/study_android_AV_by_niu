package com.ya.opengl.openglescoordinate.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.ya.opengl.openglescoordinate.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author : 周广亚
 * @version :
 * @Date :  2023/5/22 15:45
 * @Desc :
 */
public class ImageFilter {

    /**
     * opengl坐标系
     */
    static final float[] COORD_REVERSE = {
            -1.0f, 1.0f,
            1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, -1.0f,};

    /**
     * 纹理坐标系
     */
    static final float[] TEXTURE_COORD_REVERSE = {
            1.0f, 0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f};

    private FloatBuffer mPositionBuffer;
    private FloatBuffer mTextureCubeBuffer;


    private String mVertexShaderStr;

    private String mFragmentShaderStr;

    private int mProgramId;
    private int mPosition;
    private int mInputTexture;
    private int mInputTextureCoordinate;

    public ImageFilter(Context context) {
        this(OpenGLUtils.readRawTextFile(context, R.raw.base_vert), OpenGLUtils.readRawTextFile(context, R.raw.base_frag));
    }

    public ImageFilter(String vertexShader, String fragmentShader) {
        mVertexShaderStr = vertexShader;
        mFragmentShaderStr = fragmentShader;
    }


    public int initFilter(Bitmap bitmap) {
        loadVertex();
        initShader();
        // todo ???
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_CONSTANT_ALPHA);
        return initTexture(bitmap);
    }


    private void loadVertex() {
        float[] coord        = COORD_REVERSE;
        float[] textureCoord = TEXTURE_COORD_REVERSE;
        mPositionBuffer = ByteBuffer.allocateDirect(coord.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mPositionBuffer.put(coord).position(0);

        mTextureCubeBuffer = ByteBuffer.allocateDirect(textureCoord.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTextureCubeBuffer.put(textureCoord).position(0);
    }


    private void initShader() {
        mProgramId = OpenGLUtils.loadProgram(mVertexShaderStr, mFragmentShaderStr);

        mPosition = GLES20.glGetAttribLocation(mProgramId, "position");

        mInputTexture = GLES20.glGetUniformLocation(mProgramId, "inputImageTexture");
        mInputTextureCoordinate = GLES20.glGetAttribLocation(mProgramId, "inputTextureCoordinate");
    }

    private int initTexture(Bitmap bitmap) {
        // texture arrays.
        int[] textureArrays = new int[2];
        // GLES20 ，give me two textures, pls.
        GLES20.glGenTextures(textureArrays.length, textureArrays, 0);
        // bind texture which the index is 0;
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureArrays[0]);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        // S 方向贴图
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        // T 方向贴图
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        // texture sample.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        return textureArrays[0];
    }

    /**
     * 绘制
     *
     * @param glTextureId draw who ?
     */
    public void startDrawFrame(int glTextureId) {
        GLES20.glUseProgram(mProgramId);

        mPositionBuffer.position(0);
        // 用处：OpenGL该如何解析顶点数据（应用到逐个顶点属性上）
        GLES20.glVertexAttribPointer(mPosition, 2, GLES20.GL_FLOAT, false, 0, mPositionBuffer);
        GLES20.glEnableVertexAttribArray(mPosition);

        mTextureCubeBuffer.position(0);
        GLES20.glVertexAttribPointer(mInputTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0, mTextureCubeBuffer);
        GLES20.glEnableVertexAttribArray(mInputTextureCoordinate);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glTextureId);
        GLES20.glUniform1i(mInputTexture, 0);

        // draw
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(mPosition);
        GLES20.glDisableVertexAttribArray(mInputTextureCoordinate);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glDisable(GLES20.GL_BLEND);

    }


}
