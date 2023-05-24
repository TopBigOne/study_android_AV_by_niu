package com.maniu.openglfilter.filter;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.maniu.openglfilter.R;

/**
 * 1: 放大，
 * 2： 放大的同时，透明度变大
 */
public class SoulFilter extends AbstractFboFilter {
    private static final String TAG = "SoulFilter : ";

    public static final String SCALE_PERCENT   = "scalePercent";
    public static final String MIXTURE_PERCENT = "mixturePercent";

    /**
     * gpu 中，scalePercent 变量的句柄
     */
    private final int scalePercent;

    /**
     * gpu 中，mixturePercent 变量的句柄
     * 修改透明度
     */
    private final int mixturePercent;

    /**
     * 变化限制点
     */
    private static final float SOUL_LIMIT = 1.0f;

    /**
     * 默认起始点
     */
    private static final float SOUL_DEFAULT        = 0.0f;
    /**
     * 递增系数
     */
    private static final float INCREASE_BY_DEGREES = 0.08f;

    /**
     * 缩放，越大就放的越大
     */
    private float scale = SOUL_DEFAULT;

    /**
     * 透明度，越大越透明
     */
    private float mix = SOUL_DEFAULT;

    public SoulFilter(Context context) {
        super(context, R.raw.base_vert, R.raw.soul_frag);

        //从gpu 拿到 scalePercent 句柄
        scalePercent = GLES20.glGetUniformLocation(program, SCALE_PERCENT);
        //从gpu 拿到 mixturePercent 句柄
        mixturePercent = GLES20.glGetUniformLocation(program, MIXTURE_PERCENT);
    }


    /**
     * 摄像头   不断的调用 beforeDraw(),  scale值 会越来越大
     * 建议做
     */
    @Override
    public void beforeDraw() {
        super.beforeDraw();
        Log.d(TAG, "beforeDraw: ");

        // 给gpu 着色器中的 scalePercent 变量 赋值
        GLES20.glUniform1f(scalePercent, scale + SOUL_LIMIT);
        // 给gpu 着色器中的 mixturePercent 变量 赋值；
        // SOUL_LIMIT - mix 整体在不断地减小，一直到0；
        GLES20.glUniform1f(mixturePercent, SOUL_LIMIT - mix);

        // 放大系数，每次递增
        scale += INCREASE_BY_DEGREES;
        mix += INCREASE_BY_DEGREES;

        if (scale >= SOUL_LIMIT) {
            scale = SOUL_DEFAULT;
        }
        if (mix >= SOUL_LIMIT) {
            mix = SOUL_DEFAULT;
        }
    }

    @Override
    public int onDraw(int texture) {
        super.onDraw(texture);
        //  到两倍终止  片元程序
        // NOTE :  frameTextures 的值，是变化的。
        return frameTextures[0];
    }
}
