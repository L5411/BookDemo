package com.l_5411.bookdemo.chapter_5.simulated_notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

import com.l_5411.bookdemo.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RemoteActivity extends AppCompatActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, RemoteActivity.class);
    }

    private static final String TAG = RemoteActivity.class.getSimpleName();
    private Context mContext;

    @BindView(R.id.remote_view_content)
    public LinearLayout mRemoteViewContent;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            RemoteViews remoteViews = intent.getParcelableExtra(LocalActivity.EXTRA_REMOTE_VIEWS);
            if (remoteViews != null) {
                updateUI(remoteViews);
            }
        }
    };

    private void updateUI(RemoteViews remoteViews) {
        int layoutId = getResources().getIdentifier("layout_simulated_notification", "layout", getPackageName());
        View view = getLayoutInflater().inflate(layoutId, mRemoteViewContent, false);
        remoteViews.reapply(this, view);
        mRemoteViewContent.addView(view);
        Log.i(TAG, "updateUI: " + mRemoteViewContent.getChildCount()) ;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);
        mContext = this;
        ButterKnife.bind(this);

        IntentFilter filter = new IntentFilter(LocalActivity.REMOTE_ACTION);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @OnClick(R.id.send)
    void onClick(View v) {
        switch (v.getId()) {
            case R.id.send:
                startActivity(LocalActivity.newIntent(mContext));
                break;
            default:
                break;
        }
    }
}
