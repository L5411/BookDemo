package com.l_5411.bookdemo.chapter_5.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;

import com.l_5411.bookdemo.R;
import com.l_5411.bookdemo.chapter_5.Chapter5Activity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class NotificationActivity extends AppCompatActivity {

    private static final String TAG = NotificationActivity.class.getSimpleName();
    private Context mContext;

    public static Intent newIntent(Context context) {
        return new Intent(context, NotificationActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        ButterKnife.bind(this);
        mContext = this;
    }

    @OnClick({R.id.default_notification, R.id.custom_notification})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.default_notification:
                showDefaultNotification();
                break;
            case R.id.custom_notification:
                showCustomNotification();
                break;
        }
    }

    private void showDefaultNotification() {
        Intent intent = new Intent(this, NotificationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(mContext)
                .setAutoCancel(true)
                .setContentTitle("title")
                .setContentText("describe")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .build();

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, notification);
    }

    private void showCustomNotification() {
        // 点击通知跳转 NotificationActivity
        Intent intent = new Intent(this, NotificationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext,
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_notification);
        remoteViews.setTextViewText(R.id.msg, "Custom Notification");
        remoteViews.setImageViewResource(R.id.icon, R.mipmap.ic_launcher_round);
        PendingIntent pendingIntent2 = PendingIntent.getActivity(mContext,
                0, new Intent(this, Chapter5Activity.class), PendingIntent.FLAG_UPDATE_CURRENT);
        // 点击图标跳转 Chapter5Activity
        remoteViews.setOnClickPendingIntent(R.id.icon, pendingIntent2);

        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            notification = new Notification.Builder(mContext)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker("Hello world")
                    .setWhen(System.currentTimeMillis())
                    .setCustomContentView(remoteViews)
                    .setContentIntent(pendingIntent)
                    .build();
        }

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, notification);
    }
}
