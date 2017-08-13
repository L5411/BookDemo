package com.l_5411.bookdemo;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.l_5411.bookdemo.chapter_2.Chapter2Activity;
import com.l_5411.bookdemo.chapter_4.Chapter4Activity;

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

    @OnClick({R.id.chapter_2, R.id.chapter_4})
    void onClick(View v) {
        switch(v.getId()) {
            case R.id.chapter_2:
                Log.d(TAG, "2");
                startActivity(Chapter2Activity.newIntent(mContext));
                break;
            case R.id.chapter_4:
                Log.d(TAG, "4");
                startActivity(Chapter4Activity.newIntent(mContext));
                break;
            default:
                break;
        }
    }
}
