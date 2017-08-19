package com.l_5411.bookdemo.chapter_5;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.l_5411.bookdemo.R;
import com.l_5411.bookdemo.chapter_5.notification.NotificationActivity;
import com.l_5411.bookdemo.chapter_5.simulated_notification.LocalActivity;
import com.l_5411.bookdemo.chapter_5.simulated_notification.RemoteActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class Chapter5Activity extends AppCompatActivity {

    private static final String TAG = Chapter5Activity.class.getSimpleName();
    private Context mContext;

    public static Intent newIntent(Context context) {
        return new Intent(context, Chapter5Activity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter5);
        ButterKnife.bind(this);
        mContext = this;
    }

    @OnClick({R.id.chapter_5_notification, R.id.chapter_5_simulation_notification})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chapter_5_notification:
                startActivity(NotificationActivity.newIntent(mContext));
                break;
            case R.id.chapter_5_simulation_notification:
                startActivity(RemoteActivity.newIntent(mContext));
                break;
        }
    }
}
