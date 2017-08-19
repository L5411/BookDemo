package com.l_5411.bookdemo.chapter_5.widget_provider;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.l_5411.bookdemo.R;

/**
 * 自定义小部件
 * Created by L_5411 on 2017/8/19.
 */

public class MyAppWidgetProvider extends AppWidgetProvider {

    public static final String TAG = MyAppWidgetProvider.class.getSimpleName();
    public static final String CLICK_ACTION =
            "com.l_5411.bookdemo.chapter_5.widget_provider.action.CLICK";

    public MyAppWidgetProvider() {
        super();
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        super.onReceive(context, intent);
        Log.i(TAG, "onReceive : action = " + intent.getAction());

        if (CLICK_ACTION.equals(intent.getAction())) {
            Toast.makeText(context, "Clicked it", Toast.LENGTH_SHORT).show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap srcBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon1);
                    AppWidgetManager manager = AppWidgetManager.getInstance(context);

                    for (int i = 0; i < 37; i++) {
                        float degree = (i * 10) % 360;
                        Log.i(TAG, "run: rotate " + degree);
                        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.layout_widget);
                        remoteViews.setImageViewBitmap(R.id.widget_image, rotateBitmap(srcBitmap, degree));

                        Intent intentClick = new Intent();
                        intentClick.setAction(CLICK_ACTION);

                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentClick, 0);
                        remoteViews.setOnClickPendingIntent(R.id.widget_image, pendingIntent);
                        manager.updateAppWidget(new ComponentName(context, MyAppWidgetProvider.class), remoteViews);

//                        SystemClock.sleep(30);
                    }
                }
            }).start();
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        final int counter = appWidgetIds.length;
        Log.i(TAG, "onUpdate: counter = " + counter);
        for (int appWidgetId : appWidgetIds) {
            onWidgetUpdate(context, appWidgetManager, appWidgetId);
        }
    }

    private void onWidgetUpdate(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Log.i(TAG, "onWidgetUpdate: appWidgetId = " + appWidgetId);
        RemoteViews remoteViews =  new RemoteViews(context.getPackageName(), R.layout.layout_widget);

        Intent intentClick = new Intent();
        intentClick.setAction(CLICK_ACTION);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentClick, 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_image, pendingIntent);
        appWidgetManager.updateAppWidget(new ComponentName(context, MyAppWidgetProvider.class), remoteViews);
    }

    private Bitmap rotateBitmap(Bitmap srcBitmap, float degree) {
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.setRotate(degree);

        return Bitmap.createBitmap(srcBitmap,
                0, 0, srcBitmap.getWidth(), srcBitmap.getHeight(), matrix, true);
    }

}
