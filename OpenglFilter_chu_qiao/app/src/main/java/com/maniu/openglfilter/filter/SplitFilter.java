package com.maniu.openglfilter.filter;

import android.content.Context;

import com.maniu.openglfilter.R;

public class SplitFilter extends AbstractFboFilter {
    public SplitFilter(Context context) {
        super(context, R.raw.base_vert, R.raw.split4_screen);
    }

    @Override
    public int onDraw(int texture  ) {
        // fbo-> new texture  美颜 ps    ps   ps ---》美颜  步骤   ps
        super.onDraw(texture );
        return frameTextures[0];
    }
}