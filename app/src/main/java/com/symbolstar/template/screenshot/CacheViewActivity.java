package com.symbolstar.template.screenshot;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.symbolstar.template.R;

public class CacheViewActivity extends AppCompatActivity {

    RelativeLayout mContainer;
    ImageView mExampleView;
    ImageView mExampleView2;
    ImageView mExampleView3;
    ImageView mExampleView4;
    ImageView mExampleView5;
    ImageView mExampleView6;
    ImageView mExampleView7;
    ImageView mExampleView8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache_view);
        mContainer = findViewById(R.id.rl_test_container);
        mContainer.setAnimationCacheEnabled(true);
        mExampleView = findViewById(R.id.iv_example);
        mExampleView2 = findViewById(R.id.iv_example2);
        mExampleView3 = findViewById(R.id.iv_example3);
        mExampleView4 = findViewById(R.id.iv_example4);
        mExampleView5 = findViewById(R.id.iv_example5);
        mExampleView6 = findViewById(R.id.iv_example6);
        mExampleView7 = findViewById(R.id.iv_example7);
        mExampleView8 = findViewById(R.id.iv_example8);
        mContainer.setOnClickListener(view -> {
            doAnimation();
        });
    }


    public void doAnimation() {
        AnimationSet animationSet = new AnimationSet(true);
        ScaleAnimation scaleAnimation = new ScaleAnimation(
                1, 0.1f, 1, 0.1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnimation.setDuration(2000);
        animationSet.addAnimation(scaleAnimation);
        mContainer.startAnimation(scaleAnimation);
        RotateAnimation rotateAnimation = new RotateAnimation(0f, 360, Animation.RELATIVE_TO_SELF,
                0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(2000);
        animationSet.addAnimation(rotateAnimation);
        mExampleView.startAnimation(rotateAnimation);
        mExampleView2.startAnimation(rotateAnimation);
        mExampleView3.startAnimation(rotateAnimation);
        mExampleView4.startAnimation(rotateAnimation);
        mExampleView5.startAnimation(rotateAnimation);
        mExampleView6.startAnimation(rotateAnimation);
        mExampleView7.startAnimation(rotateAnimation);
        mExampleView8.startAnimation(rotateAnimation);
    }
}