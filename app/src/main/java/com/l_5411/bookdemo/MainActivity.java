package com.l_5411.bookdemo;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.l_5411.bookdemo.chapter_2.Chapter2Activity;
import com.l_5411.bookdemo.chapter_4.Chapter4Activity;
import com.l_5411.bookdemo.chapter_5.Chapter5Activity;
import com.l_5411.bookdemo.chapter_6.Chapter6Activity;
import com.l_5411.bookdemo.chapter_7.Chapter7Activity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mContext = this;
    }

    @OnClick({R.id.chapter_2, R.id.chapter_4, R.id.chapter_5, R.id.chapter_6, R.id.chapter_7})
    void onClick(View v) {
        switch(v.getId()) {
            case R.id.chapter_2:
                startActivity(Chapter2Activity.newIntent(mContext));
                break;
            case R.id.chapter_4:
                startActivity(Chapter4Activity.newIntent(mContext));
                break;
            case R.id.chapter_5:
                startActivity(Chapter5Activity.newIntent(mContext));
                break;
            case R.id.chapter_6:
                startActivity(Chapter6Activity.newIntent(mContext));
                break;
            case R.id.chapter_7:
                startActivity(Chapter7Activity.newIntent(mContext));
                break;
            default:
                break;
        }
    }
}
