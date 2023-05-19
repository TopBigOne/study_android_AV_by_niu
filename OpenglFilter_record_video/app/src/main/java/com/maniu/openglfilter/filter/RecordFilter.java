package com.maniu.openglfilter.filter;

import android.content.Context;

import com.maniu.openglfilter.R;
import com.maniu.openglfilter.filter.AbstractFilter;

public class RecordFilter extends AbstractFilter {
    public RecordFilter(Context context){
        super(context, R.raw.base_vert, R.raw.base_frag);
    }

}
