package com.l_5411.bookdemo.chapter_7;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.l_5411.bookdemo.R;
import com.l_5411.bookdemo.chapter_2.provider.ProviderActivity;
import com.l_5411.bookdemo.chapter_7.property_animation.PropertyActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Chapter7Activity extends AppCompatActivity {

    private static final String TAG = Chapter7Activity.class.getSimpleName();
    @BindView(R.id.chapter_7_view_animation_button)
    Button mViewAnimationButton;
    @BindView(R.id.chapter_7_frame_animation_button)
    Button mFrameAnimationButton;
    private Context mContext;

    public static Intent newIntent(Context context) {
        return new Intent(context, Chapter7Activity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter7);
        ButterKnife.bind(this);
        mContext = this;
    }

    @OnClick({R.id.chapter_7_view_animation_button,
            R.id.chapter_7_frame_animation_button,
            R.id.chapter_7_layout_animation_button,
            R.id.chapter_7_Activity_animation_button,
            R.id.chapter_7_property_animation_button})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chapter_7_view_animation_button:
                Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_view);
                mViewAnimationButton.startAnimation(animation);
                break;
            case R.id.chapter_7_frame_animation_button:
                mFrameAnimationButton.setBackgroundResource(R.drawable.animation_frame);
                AnimationDrawable drawable = (AnimationDrawable) mFrameAnimationButton.getBackground();
                drawable.start();
                break;
            case R.id.chapter_7_layout_animation_button:
                startActivity(LayoutAnimActivity.newIntent(mContext));
                break;
            case R.id.chapter_7_Activity_animation_button:
                startActivity(TestActivity.newIntent(mContext));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            case R.id.chapter_7_property_animation_button:
                startActivity(PropertyActivity.newIntent(mContext));
            default:
                break;
        }
    }
}
