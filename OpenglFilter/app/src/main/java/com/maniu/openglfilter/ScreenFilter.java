package com.maniu.openglfilter;

import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;
import android.view.VelocityTracker;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * 屏幕滤镜
 */
public class ScreenFilter {
    private static final String TAG = "ScreenFilter : ";

    //    顶点着色器
    //    片元着色器
    private int program;
    //句柄  gpu中  vPosition
    private int vPosition;
    FloatBuffer textureBuffer; // 纹理坐标
    private int     vCoord;
    private int     vTexture;
    private int     vMatrix;
    private int     mWidth;
    private int     mHeight;
    private float[] mtx;
    //gpu顶点缓冲区
    FloatBuffer vertexBuffer; //顶点坐标缓存区

    float[] VERTEX = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f};

    /**
     * 纹理 坐标系
     */
    float[] TEXTURE = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f};

    public ScreenFilter(Context context) {
        Log.d(TAG, "ScreenFilter: ");
        vertexBuffer = ByteBuffer.allocateDirect(4 * 4 * 2).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.clear();
        vertexBuffer.put(VERTEX);

        textureBuffer = ByteBuffer.allocateDirect(4 * 4 * 2).order(ByteOrder.nativeOrder()).asFloatBuffer();
        textureBuffer.clear();
        textureBuffer.put(TEXTURE);

        //
        String vertexSharder = OpenGLUtils.readRawTextFile(context, R.raw.camera_vert);
        //  先编译    再链接   再运行  程序
        String fragSharder = OpenGLUtils.readRawTextFile(context, R.raw.camera_frag4);
        //cpu 1   没有用  索引     program gpu
        program = OpenGLUtils.loadProgram2(vertexSharder, fragSharder);

        // 0
        vPosition = GLES20.glGetAttribLocation(program, "vPosition");
        //接收纹理坐标，接收采样器采样图片的坐标
        // 1
        vCoord = GLES20.glGetAttribLocation(program, "vCoord");
        //采样点的坐标
        vTexture = GLES20.glGetUniformLocation(program, "vTexture");

        //变换矩阵， 需要将原本的vCoord（01,11,00,10） 与矩阵相乘
        vMatrix = GLES20.glGetUniformLocation(program, "vMatrix");
        //   构造 的时候 给 数据  vPosition gpu 是1  不是 2
    }

    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public void setTransformMatrix(float[] mtx) {
        this.mtx = mtx;
    }

    /**
     * 摄像头数据  渲染   摄像  开始渲染
     * @param texture
     */
    public void startDraw(int texture) {
        //  opengl
        // View 的大小
        GLES20.glViewport(0, 0, mWidth, mHeight);
        // 使用程序
        GLES20.glUseProgram(program);



        // 从索引位0的地方读
        vertexBuffer.position(0);
        // index   指定要修改的通用顶点属性的索引。
        // size    指定每个通用顶点属性的组件数。
        // type    指定数组中每个组件的数据类型。
        //  接受符号常量GL_FLOAT  GL_BYTE，GL_UNSIGNED_BYTE，GL_SHORT，GL_UNSIGNED_SHORT或GL_FIXED。 初始值为GL_FLOAT。
        //  normalized    指定在访问定点数据值时是应将其标准化（GL_TRUE）还是直接转换为定点值（GL_FALSE）。
        // cpu 和 GPU
        // 反人类的操作
        GLES20.glVertexAttribPointer(vPosition, 2, GL_FLOAT, false, 0, vertexBuffer);
        // 生效
        GLES20.glEnableVertexAttribArray(vPosition);





        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);
        //CPU传数据到GPU，默认情况下着色器无法读取到这个数据。 需要我们启用一下才可以读取
        GLES20.glEnableVertexAttribArray(vCoord);





        //  形状就确定了
        //  32  数据
        // 激活图层：gpu    获取读取
        GLES20.glActiveTexture(GL_TEXTURE0);

        //生成一个采样
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        // 对这几个纹理采样器变量进行设置,0 : 对应纹理第一层
        GLES20.glUniform1i(vTexture, 0);
        //  将 mtx 传递给 vMatrix
        GLES20.glUniformMatrix4fv(vMatrix, 1, false, mtx, 0);
        //通知画画，
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    public  void startDraw2(int textureIndex){
        GLES20.glViewport(0,0,mWidth,mHeight);
        GLES20.glUseProgram(program);


        // process vertex
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition,2,GL_FLOAT,false,0,vertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        // process texture
        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord,2, GL_FLOAT,false,0,textureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        // 激活图层 0
        GLES20.glActiveTexture(GL_TEXTURE0);

        // 生成一个采样
        GLES20.glBindTexture(GL_TEXTURE_2D,textureIndex);
        GLES20.glUniform1i(vTexture,0);

        GLES20.glUniformMatrix4fv(vMatrix,1,false,mtx,0);
        // 通知画画
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN,0,4);







    }


}
