package com.l_5411.bookdemo.chapter_5.simulated_notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import com.l_5411.bookdemo.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class LocalActivity extends AppCompatActivity {

    public static final String REMOTE_ACTION =
            "com.l_5411.bookdemo.chapter_5.simulated_notification.action_REMOTE";
    public static final String EXTRA_REMOTE_VIEWS = "extra_remoteViews";

    private static final String TAG = LocalActivity.class.getSimpleName();
    private Context mContext;

    public static Intent newIntent(Context context) {
        return new Intent(context, LocalActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local);
        ButterKnife.bind(this);
        mContext = this;
    }

    @OnClick({R.id.send_notification})
    void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_notification:
                sendNotification();
                break;
            default:
                break;
        }
    }

    private void sendNotification() {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_simulated_notification);
        remoteViews.setTextViewText(R.id.title, "Notification");
        remoteViews.setTextViewText(R.id.container, "msg from process: " + Process.myPid());
        remoteViews.setImageViewResource(R.id.icon, R.drawable.icon1);

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,
                LocalActivity.newIntent(mContext), PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent openRemotePendingIntent = PendingIntent.getActivity(mContext, 0,
                RemoteActivity.newIntent(mContext), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.item_holder, pendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.open_remoteActivity, openRemotePendingIntent);

        Intent intent = new Intent(REMOTE_ACTION);
        intent.putExtra(EXTRA_REMOTE_VIEWS, remoteViews);
        sendBroadcast(intent);
    }
}
