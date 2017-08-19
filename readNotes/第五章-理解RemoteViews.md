RemoteViews 表示的是一个 View 结构，它可以在其他进程中显示，RemoteViews 提供了一组基础的操作用于跨进程更新它的界面。

## 5.1 RemoteViews 的应用
---
RemoteViews 在实际开发中，主要用在通知栏和桌面小部件的开发过程中。通知栏主要通过 NotificationManager 的 notify 方法来实现的。桌面小部件则是通过 AppWidgetProvider 来实现的。

### 5.1.1 RemoteViews 在通知栏上的应用
弹出一个默认通知:
```
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
```
弹出一个自定义通知
```
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
```

### 5.1.2  RemoteViews 在桌面小部件上的应用
AppWidgetProvider 是 Android 中提供的用于实现桌面小部件的类，本质是一个广播。小部件开发步骤：
1. **定义小部件界面**  
定义 layout 文件 `widget.xml`：
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:orientation="vertical" android:layout_width="match_parent"
    android:layout_height="match_parent">

    <ImageView
        android:id="@+id/widget_image"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:src="@mipmap/ic_launcher"/>
</LinearLayout>
```

2. **定义小部件配置信息**  
在 `res/xml` 下新建 `appwidget_provider_info.xml`：  
```
    <?xml version="1.0" encoding="utf-8"?>
    <appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
        android:initialLayout="@layout/layout_widget" 
        android:minHeight="84dp"
        android:minWidth="84dp"
        android:updatePeriodMillis="86400000">

    </appwidget-provider>
```

3. **定义小部件的实现类**
需继承 AppWidgetProvider。

4. **在 AndroidManifest.xml** 中声明小部件  
```  
<receiver android:name=".chapter_5.widget_provider.MyAppWidgetProvider">
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/appwidget_provider_info">
    </meta-data>

    <intent-filter>
        <action android:name="com.l_5411.bookdemo.chapter_5.widget_provider.action.CLICK"/>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
</receiver>
```

AppWidgetProvider 常用方法：
* onEnable：窗口小部件第一次添加到桌面时调用，可以添加多次但只在第一次调用
* onUpdate：小部件被添加时或者更新时都会调用一次，更新时机由 `updatePeriodMills` 来指定
* onDeleted：每删除一次小部件就调用一次
* onDisabled: 最后一个该类型的小部件被删除时调用
* onReceive：广播内置方法，用于分发具体时间给其它方法

### 5.1.3 PendingIntent 概述
PendingIntent 是在将来的某个不确定时刻发生，而 Intent 是立刻发生。PendingIntent 典型应用场景是给 RemoteViews 添加点击事件。PendingIntent 主要方法：
* `getActivity(Context context, int requestCode, Intent intent, int flags)`：启动 Activity

* `getService(Context context, int requestCode, Intent intent, int flags)`：启动 Service

* `getBroadcast(Context context, int requestCode, Intent intent, int flags)`：发送广播

PendingIntent 匹配规则：intent 相同，且 requestCode 相同  
Intent 匹配规则：ComponentName 和 intent-filter 相同  

flags 参数含义：
* `FLAG_ONE_SHOT`：PendingIntent 使用一次后就会被自动 cancel

* `FLAG_NO_CREATE`：PendingIntent 不会主动创建，如果 PendingIntent 之前不存在，那么 get 三个方法会返回 null

* `FLAG_CANCEL_CURRENT`：如果 PendingIntent 已经存在，那么它们都会被 cancel，然后创建一个新的 PendingIntent

* `FLAG_UPDATE_CURRENT`：如果 PendingIntent 已经存在，那么它们都会被更新，Intent 中的 Extras 会被替换为最新的

## 5.2 RemoteViews 的内部机制
---
* RemoteViews 不支持所有的 View 类型，不支持自定义 View 
* 无法直接访问 RemoteViews 里面的 View 元素，而必须通过 RemoteViews 所提供的一系列 set 方法来完成，set 方法底层是通过反射来完成的
* 系统首先将 View 操作封装成 Action 对象，并将这些对象跨进程传输操远程进程，接着在远程进程中执行 Action 对象中的具体操作
* RemoteViews 中的单击事件只支持发起 PendingIntent，不支持 onClickListener 的模式
* setOnClickPendingIntent 用于给普通 View 设置单机事件，但不能给 ListView 和 StackView 中的 View 设置单击事件，因为开销比较大
* 通过 setPendingIntentTemplate 和 setOnClickFillInIntent 组合使用才能够给 ListView 和 StackView 中的 Item 添加点击事件

## 5.3 RemoteViews 的意义
---
[模拟通知栏效果实现跨进程的 UI 更新](../app/src/main/java/com/l_5411/bookdemo/chapter_5/Chapter5Activity.java)