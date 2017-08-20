package com.l_5411.bookdemo.chapter_6;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.l_5411.bookdemo.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Chapter6Activity extends AppCompatActivity {

    private static final String TAG = Chapter6Activity.class.getSimpleName();
    private Context mContext;

    public static Intent newIntent(Context context) {
        return new Intent(context, Chapter6Activity.class);
    }

    @BindView(R.id.level_list_button)
    Button levelListButton;

    @BindView(R.id.transition_button)
    Button transitionButton;

    @BindView(R.id.transition_text)
    TextView transitionText;

    @BindView(R.id.scale_image)
    ImageView scaleImage;

    @BindView(R.id.scale_add)
    Button scaleAddButton;

    @BindView(R.id.scale_reduce)
    Button scaleReduceButton;

    @BindView(R.id.clip_image)
    ImageView clipImage;

    @BindView(R.id.clip_add)
    Button clipAddButton;

    @BindView(R.id.clip_reduce)
    Button clipReduceButton;

    @BindView(R.id.custom_drawable_view)
    View customDrawableView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter6);
        mContext = this;
        ButterKnife.bind(this);

        levelListButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        levelListButton.getBackground().setLevel(1);
                        break;
                    case MotionEvent.ACTION_UP:
                        levelListButton.getBackground().setLevel(0);
                        break;
                }
                return false;
            }
        });

        final TransitionDrawable drawable = (TransitionDrawable) transitionText.getBackground();
        final boolean[] flag = {false};
        transitionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag[0]) {
                    drawable.startTransition(1000);
                    flag[0] = false;
                } else {
                    drawable.reverseTransition(1000);
                    flag[0] = true;
                }
            }
        });

        scaleImage.getBackground().setLevel(1);
        scaleAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScaleDrawable drawable1 = (ScaleDrawable) scaleImage.getBackground();
                drawable1.setLevel(drawable1.getLevel() + 1000);
            }
        });
        scaleReduceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScaleDrawable drawable1 = (ScaleDrawable) scaleImage.getBackground();
                drawable1.setLevel(drawable1.getLevel() - 1000);
            }
        });

        clipAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipDrawable drawable = (ClipDrawable) clipImage.getBackground();
                drawable.setLevel(drawable.getLevel() + 1000);
            }
        });

        clipReduceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipDrawable drawable = (ClipDrawable) clipImage.getBackground();
                drawable.setLevel(drawable.getLevel() - 1000);
            }
        });

        customDrawableView.setBackground(new CustomDrawable(Color.RED));
    }
}
