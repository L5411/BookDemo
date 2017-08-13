package com.l_5411.bookdemo.chapter_4;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.l_5411.bookdemo.R;
import com.l_5411.bookdemo.chapter_2.Chapter2Activity;
import com.l_5411.bookdemo.chapter_4.circle_view.CircleActivity;
import com.l_5411.bookdemo.chapter_4.horizontal_scroll_view.HorizontalScrollActivity;
import com.l_5411.bookdemo.chapter_4.horizontal_scroll_view.HorizontalScrollViewEx;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class Chapter4Activity extends AppCompatActivity {

    private static final String TAG = Chapter4Activity.class.getSimpleName();
    private Context mContext;

    public static Intent newIntent(Context context) {
        return new Intent(context, Chapter4Activity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter4);
        ButterKnife.bind(this);
        mContext = this;
    }

    @OnClick({R.id.chapter_4_circle_view, R.id.chapter_4_horizontal_scroll_view})
    void onClick(View v) {
        switch (v.getId()) {
            case R.id.chapter_4_circle_view:
                startActivity(CircleActivity.newIntent(mContext));
                break;
            case R.id.chapter_4_horizontal_scroll_view:
                startActivity(HorizontalScrollActivity.newIntent(mContext));
            default:
                break;
        }
    }
}
