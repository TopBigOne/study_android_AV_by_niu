package com.maniu.openglfilter;

import android.app.Application;
import android.content.Context;

/**
 * @author : 周广亚
 * @version :
 * @Date :  2023/5/22 19:19
 * @Desc :
 */
public class OpenGLApp extends Application {

    private static OpenGLApp openGLApp;

    @Override
    public void onCreate() {
        super.onCreate();
        openGLApp = this;
    }

    public static Application getOpenGLApp() {
        return openGLApp;
    }

    public static Context getContext() {
        return getOpenGLApp().getApplicationContext();
    }

}
