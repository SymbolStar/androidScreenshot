package com.symbolstar.template.screenshot;


import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.symbolstar.template.BuildConfig;

public class TestImageView extends ImageView {

    static int count  = 0 ;

    public TestImageView(Context context) {
        super(context);
    }

    public TestImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (BuildConfig.DEBUG) Log.e("TestImageView","onMeasure"+System.currentTimeMillis());
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        count++ ;
        if (BuildConfig.DEBUG) Log.e("TestImageView", "onDraw"+System.currentTimeMillis()
                +"---"+count);
    }

}
