package com.ya.opengl.hellotriangle;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.ya.opengl.hellotriangle.utils.OpenGLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author : top
 * @version :
 * @Date :  2023/5/23 19:22
 * @Desc : 配置shader
 */
public class ConfigureShader {


    private static final String TAG = "ConfigureShader : ";

    public Context context;

    public float[] triangleCoordinate = {0.5f, 0.5f, 0.0f,// top
            -0.5f, -0.5f, 0.0f,// bottom left
            0.5f, -0.5f, 0.0f// bottom right
    };

    private int mVertexShader = -1;

    private int mFragmentShader = -1;

    private static final String V_POSITION = "vPosition";
    private static final String V_COLOR    = "vColor";


    private int         program         = -1;
    private FloatBuffer vertexBuffer;
    private int         vPositionHandle = -1;
    private int         vColorHandle    = -1;
    /**
     * 设置颜色，依次为红绿蓝和透明通道
     */
    float[] color = {1.0f, 0f, 0f, 1.0f};


    public ConfigureShader(Context context) {
        this.context = context;
    }

    public void initData() {
        Log.d(TAG, "initData: ");
        mVertexShader = loadShader(GLES20.GL_VERTEX_SHADER);
        mFragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(triangleCoordinate.length * 4);
        vertexBuffer = byteBuffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(triangleCoordinate);
        vertexBuffer.position(0);
    }


    private int loadShader(int shaderType) {
        // step 1:
        int shader = GLES20.glCreateShader(shaderType);
        // step 2:
        GLES20.glShaderSource(shader, getShaderStr(shaderType));
        // step 3: compile;
        GLES20.glCompileShader(shader);
        int[] status = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "loadShader: ERROR : " + shaderType + GLES20.glGetShaderInfoLog(shader));
        }
        return shader;
    }

    private String getShaderStr(int shaderType) {
        String result = "";
        if (shaderType == GLES20.GL_VERTEX_SHADER) {
            result = OpenGLUtils.readRawTextFile(context, R.raw.vertex_shader);

        }

        if (shaderType == GLES20.GL_FRAGMENT_SHADER) {
            result = OpenGLUtils.readRawTextFile(context, R.raw.fragment_shader);

        }
        return result;
    }


    public void startConfig() {
        Log.d(TAG, "startConfig: ");
        initData();
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, mVertexShader);
        GLES20.glAttachShader(program, mFragmentShader);
        GLES20.glLinkProgram(program);

        int[] status = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "startConfig: ERROR : " + GLES20.glGetProgramInfoLog(program));
        }
    }


    public void startDraw() {
        if (program == -1) {
            return;
        }
        Log.d(TAG, "startDraw: ");
        GLES20.glUseProgram(program);
        // 获取顶点坐标的句柄
        vPositionHandle = GLES20.glGetAttribLocation(program, V_POSITION);
        // 启动三角形顶点句柄
        GLES20.glEnableVertexAttribArray(vPositionHandle);
        // 准备三角形的坐标数据,告诉 ：OpenGL该如何解析顶点数据（应用到逐个顶点属性上）
        GLES20.glVertexAttribPointer(vPositionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer);

        // 获取片元着色器中的 变量
        vColorHandle = GLES20.glGetUniformLocation(program, V_COLOR);
        // 设置三角形颜色
        GLES20.glUniform4fv(vColorHandle, 1, color, 0);
        // 绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 3);
        // 禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(vPositionHandle);

    }

}
