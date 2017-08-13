package com.l_5411.bookdemo.chapter_2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.l_5411.bookdemo.R;
import com.l_5411.bookdemo.chapter_2.aidl.BookManagerActivity;
import com.l_5411.bookdemo.chapter_2.binder_pool.BinderPoolActivity;
import com.l_5411.bookdemo.chapter_2.messenger.MessengerActivity;
import com.l_5411.bookdemo.chapter_2.provider.ProviderActivity;
import com.l_5411.bookdemo.chapter_2.socket.TCPClientActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class Chapter2Activity extends AppCompatActivity {

    private static final String TAG = Chapter2Activity.class.getSimpleName();
    private Context mContext;

    public static Intent newIntent(Context context) {
        return new Intent(context, Chapter2Activity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter2);
        ButterKnife.bind(this);
        mContext = this;
    }

    @OnClick({R.id.chapter_2_messenger, R.id.chapter_2_aidl,
            R.id.chapter_2_provider, R.id.chapter_2_socket,
            R.id.chapter_2_binder_pool})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chapter_2_messenger:
                startActivity(MessengerActivity.newIntent(mContext));
                break;
            case R.id.chapter_2_aidl:
                startActivity(BookManagerActivity.newIntent(mContext));
                break;
            case R.id.chapter_2_provider:
                startActivity(ProviderActivity.newIntent(mContext));
                break;
            case R.id.chapter_2_socket:
                startActivity(TCPClientActivity.newIntent(mContext));
                break;
            case R.id.chapter_2_binder_pool:
                startActivity(BinderPoolActivity.newIntent(mContext));

            default:
                break;
        }
    }

}
