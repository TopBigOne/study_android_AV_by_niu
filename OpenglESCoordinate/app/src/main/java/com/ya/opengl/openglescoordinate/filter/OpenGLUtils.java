package com.ya.opengl.openglescoordinate.filter;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;

/**
 * @author : 周广亚
 * @version :
 * @Date :  2023/5/22 15:47
 * @Desc :
 */
public class OpenGLUtils {
    private static final String TAG = "OpenGLUtils : ";

    public static String readRawTextFile(Context context, int rawId) {
        Log.d(TAG, "readRawTextFile: ");
        InputStream    is = context.getResources().openRawResource(rawId);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String         line;
        StringBuilder  sb = new StringBuilder();
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public  static int loadProgram(String vertexStr, String fragmentStr) {
        int vShader = processVertexShader(vertexStr);
        int fShader = processFragmentShader(fragmentStr);
        // create the program
        int program = GLES20.glCreateProgram();
        // attach vertex shader and fragment shader
        GLES20.glAttachShader(program,vShader);
        GLES20.glAttachShader(program,fShader);
        // link program
        GLES20.glLinkProgram(program);

        int[] stats = new int[1];
        // check status
        GLES20.glGetProgramiv(program,GLES20.GL_LINK_STATUS,stats,0);
        if (stats[0] != GLES20.GL_TRUE) {
            //失败
            throw new IllegalStateException("link openGL program : " + GLES20.glGetShaderInfoLog(vShader));
        }
        GLES20.glDeleteShader(vShader);
        GLES20.glDeleteShader(fShader);

        return program;
    }


    /**
     * 加载顶点着色器
     *
     * @param vertexStr
     * @return
     */
    private  static int processVertexShader(String vertexStr) {
        int[] status = new int[1];
        Log.d(TAG, "processVertexShader: ");
        // process vertex Shader
        int vShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        // load
        GLES20.glShaderSource(vShader, vertexStr);
        // compile
        GLES20.glCompileShader(vShader);
        // check status
        GLES20.glGetShaderiv(vShader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            //失败
            throw new IllegalStateException("load vertex shader:" + GLES20.glGetShaderInfoLog(vShader));
        }
        return vShader;
    }


    /**
     * 加载片元着色器
     *
     * @param fragmentStr
     * @return
     */
    private  static int processFragmentShader(String fragmentStr) {
        int[] status         = new int[1];
        int   fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        //  step 1:load
        GLES20.glShaderSource(fragmentShader, fragmentStr);
        //  step 2:compile
        GLES20.glCompileShader(fragmentShader);
        // step 3:  check status
        GLES20.glGetShaderiv(fragmentShader, GLES20.GL_COMPILE_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            //失败
            throw new IllegalStateException("load fragment shader:" + GLES20.glGetShaderInfoLog(fragmentShader));
        }
        return fragmentShader;
    }
}
