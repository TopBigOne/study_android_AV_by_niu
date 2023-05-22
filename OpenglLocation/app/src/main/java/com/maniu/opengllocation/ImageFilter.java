package com.maniu.opengllocation;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class ImageFilter {


    private String mVertexShader;
    private String mFragmentShader;

    private FloatBuffer mPositionBuffer;
    private FloatBuffer mTextureCubeBuffer;

    protected int mProgId;
    protected int mPosition;
    protected int inputTextureBufferIndex;
    protected int mInputTexture;


    static final float[] COORD1      = {-1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f,};
    static final float[] BOOK_COORD1 = {0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f,};

    static final float[] TEXTURE_COORD1 = {0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,};

    //世界坐标系
    static final float[] COORD_REVERSE = {-1.0f, 1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f,};

    //    纹理坐标系
    static final float[] TEXTURE_COORD_REVERSE = {1.0f, 0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f

    };


    public ImageFilter(Context context) {
        this(OpenGLUtils.readRawTextFile(context, R.raw.base_vert), OpenGLUtils.readRawTextFile(context, R.raw.base_frag));
    }

    public ImageFilter(String vertexShader, String fragmentShader) {
        mVertexShader = vertexShader;
        mFragmentShader = fragmentShader;
    }

    public void loadVertex() {
        float[] coord         = COORD_REVERSE;
        float[] texture_coord = TEXTURE_COORD_REVERSE;


        mPositionBuffer = ByteBuffer.allocateDirect(coord.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mPositionBuffer.put(coord).position(0);

        mTextureCubeBuffer = ByteBuffer.allocateDirect(texture_coord.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mTextureCubeBuffer.put(texture_coord).position(0);
    }

    public void initShader() {
        mProgId = OpenGLUtils.loadProgram(mVertexShader, mFragmentShader);
        mPosition = GLES20.glGetAttribLocation(mProgId, "position");

        mInputTexture = GLES20.glGetUniformLocation(mProgId, "inputImageTexture");
        inputTextureBufferIndex = GLES20.glGetAttribLocation(mProgId, "inputTextureCoordinate");
    }

    /**
     * 纹理ID
     *
     * @param bitmap 图片
     * @return 纹理ID
     */
    public int init(Bitmap bitmap) {
        loadVertex();
        initShader();
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        return initTexture(bitmap);
    }

    private static final String TAG = "ImageFilter ： ";

    private int initTexture(Bitmap bitmap) {
        // 纹理数组
        int[] textures = new int[2];

        // 生成2个纹理，并将纹理的下标值放到 textures 数组中
        GLES20.glGenTextures(2, textures, 0);
        Log.d(TAG, "initTexture:  after gen textures ：" + Arrays.toString(textures));
        // 以下操作，都是针对index 为0 的纹理；
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        // 纹理过滤函数
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        // S 方向贴图
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        // T 方向贴图
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        // - 根据指定的参数，生成一个2D纹理（Texture）。相似的函数还有glTexImage1D、glTexImage3D
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        return textures[0];
    }

    public void drawFrame(int glTextureId) {
        Log.d(TAG, "drawFrame: glTextureId ：" + glTextureId);

        GLES20.glUseProgram(mProgId);
        mPositionBuffer.position(0);
        //
        GLES20.glVertexAttribPointer(mPosition, 2, GLES20.GL_FLOAT, false, 0, mPositionBuffer);
        GLES20.glEnableVertexAttribArray(mPosition);

        mTextureCubeBuffer.position(0);
        GLES20.glVertexAttribPointer(inputTextureBufferIndex, 2, GLES20.GL_FLOAT, false, 0, mTextureCubeBuffer);
        GLES20.glEnableVertexAttribArray(inputTextureBufferIndex);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glTextureId);
        GLES20.glUniform1i(mInputTexture, 0);

        //       这句话 来进行渲染
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(mPosition);
        GLES20.glDisableVertexAttribArray(inputTextureBufferIndex);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        GLES20.glDisable(GLES20.GL_BLEND);

    }

}
