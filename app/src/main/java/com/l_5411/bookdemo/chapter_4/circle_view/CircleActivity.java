package com.l_5411.bookdemo.chapter_4.circle_view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.l_5411.bookdemo.R;

import butterknife.ButterKnife;

public class CircleActivity extends AppCompatActivity {

    private static final String TAG = CircleActivity.class.getSimpleName();
    private Context mContext;

    public static Intent newIntent(Context context) {
        return new Intent(context, CircleActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circle);
        ButterKnife.bind(this);
        mContext = this;
    }
}
