package com.l_5411.bookdemo.chapter_7.property_animation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.l_5411.bookdemo.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PropertyActivity extends AppCompatActivity {

    private static final String TAG = PropertyActivity.class.getSimpleName();
    @BindView(R.id.image)
    View imageView;
    private Context mContext;

    public static Intent newIntent(Context context) {
        return new Intent(context, PropertyActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property);
        mContext = this;
        ButterKnife.bind(this);
    }

    @OnClick({R.id.image, R.id.transition_x_add, R.id.transition_x_reduce, R.id.change_background, R.id.animator_set})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image:
                Toast.makeText(mContext, "Image Clicked!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.transition_x_add:
                ObjectAnimator.ofFloat(imageView, "translationX", 500).start();
                break;
            case R.id.transition_x_reduce:
                ObjectAnimator.ofFloat(imageView, "translationX", -500).start();
                break;
            case R.id.change_background:
                ValueAnimator colorAnim = ObjectAnimator.ofInt(imageView,
                        "backgroundColor",
                        /* holo_red_light */ 0xffff4444,
                        /* holo_blue_light */ 0xff33b5e5);
                colorAnim.setDuration(3000);
                colorAnim.setEvaluator(new ArgbEvaluator());        // 设置颜色计算器，使颜色渐变
                colorAnim.setRepeatCount(ValueAnimator.INFINITE);   // 设置无限循环
                colorAnim.setRepeatMode(ValueAnimator.REVERSE);     // 设置反转效果
                colorAnim.start();
                break;
            case R.id.animator_set:
                AnimatorSet set = new AnimatorSet();
                set.playTogether(
                        ObjectAnimator.ofFloat(imageView, "rotationX", 0, 360),
                        ObjectAnimator.ofFloat(imageView, "rotationY", 0, 180),
                        ObjectAnimator.ofFloat(imageView, "rotation", 0, -90),
                        ObjectAnimator.ofFloat(imageView, "translationX", 0, 90),
                        ObjectAnimator.ofFloat(imageView, "translationY", 0, 90),
                        ObjectAnimator.ofFloat(imageView, "scaleX", 1, 1.5f),
                        ObjectAnimator.ofFloat(imageView, "scaleY", 1, 0.5f),
                        ObjectAnimator.ofFloat(imageView, "alpha", 1, 0.25f, 1)
                );
                set.setDuration(5 * 1000).start();
                break;
            default:
                break;
        }
    }
}
