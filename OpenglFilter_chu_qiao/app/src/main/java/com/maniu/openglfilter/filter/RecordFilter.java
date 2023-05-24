package com.maniu.openglfilter.filter;

import android.content.Context;

import com.maniu.openglfilter.R;

/**
 * 输出到屏幕
 */
public class RecordFilter extends AbstractFilter{

    public RecordFilter(Context context){
        super(context, R.raw.base_vert, R.raw.base_frag);
    }



}
