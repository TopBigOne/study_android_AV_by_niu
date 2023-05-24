package com.maniu.openglfilter.filter;

import android.content.Context;

import com.maniu.openglfilter.R;

public class ScreenFilter extends AbstractFilter {
    public ScreenFilter(Context context) {
        super(context, R.raw.base_vert, R.raw.base_frag);
    }
}
