* Window 表示一个窗口，是一个抽象类，具体实现是 PhoneWindow
* WindowManager 是外界访问 Window 的入口，通过 WindowManager 可以创建一个 Window
* Window 的具体实现位于 WindowManagerService 中，WindowManager 和 WindowManagerService 的交互是一个 IPC 的过程
* Android 中所有视图都是 Window 来呈现的，Activity、Dialog、Toast 的视图都是附加在 Window 上的，Window 是 View 的直接管理者

## 8.1 Window 和 WindowManager
---
通过 WindowManager 添加一个 Window：
```
Button mFloatingButton = new Button(mContext);
mFloatingButton.setText("Button");
LayoutParams mLayoutParams = new WindowManager.LayoutParams(
        LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT,
        0,
        0,
        PixelFormat.TRANSPARENT
);
mLayoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
        | LayoutParams.FLAG_SHOW_WHEN_LOCKED
        | LayoutParams.FLAG_NOT_FOCUSABLE;
mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
// 必须要指定 WindowManager.LayoutParams 的 type
mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
mLayoutParams.x = 100;
mLayoutParams.y = 300;
getWindowManager().addView(mFloatingButton, mLayoutParams);
```
上述代码可以将一个 Button 添加到屏幕 (100, 300) 的位置上，需要注意的是：
* **需要指定 WindowManager.LayoutParams 的 type**
* **开启悬浮窗权限：`<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />` **

WindowManager.LayoutParams 的 flags 参数：
* FLAG_NOT_FOCUSABLE  
表示 Window 不需要获取焦点，也不需要接受各种输入事件，此标记会同时启用 FLAG_NOT_TOUCH_MODAL，最终事件会之间传递给下层的具有焦点的 Window

* FLAG_NOT_TOUCH_MODAL  
此模式下系统会将当前 Window 区域以外的单击事件传递给底层的 Window，当前 Window 区域以内的单击事件则自己处理。一般都需要开启此标记，否则其它 Window 将无法收到单击事件

* FLAG_SHOW_WHEN_LOCKED  
开启此模式可以让 Window 显示在锁屏的界面上

WindowManager.LayoutParams 的 Type 参数表示 Window 的类型：
* `应用 Window`：对应着一个 Activity，层级范围 1 ~ 99

* `子 Window`：不能单独存在，需要附属在特定的父 Window 之中，比如 Dialog 就是一个 子 Window，层级范围 1000 ~ 1999

* `系统 Window`：需要声明权限才能创建 Window，层及范围 2000 ~ 2999，系统层级一般选用 `TYPE_SYSTEM_OVERLAY` 或者 `TYPE_SYSTEM_ERROR`，如果采用 `TYPE_SYSTEM_ERROR` 只需要指定 `WindowManager.LayoutParams.TYPE_SYSTEM_ERROR` 并且开启权限：`<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />`

Window 是分层的，每个 Window 都有对应的 z-ordered，层级大的会覆盖在层级小的 Window 之上。

WindowManager 提供的常用功能有添加 View、更新 View 和删除 View，这三个方法来自接口 ViewManager，WindowManager 实现了 ViewManager：
```
public interface ViewManager
{
    /**
     * Assign the passed LayoutParams to the passed View and add the view to the window.
     * <p>Throws {@link android.view.WindowManager.BadTokenException} for certain programming
     * errors, such as adding a second view to a window without removing the first view.
     * <p>Throws {@link android.view.WindowManager.InvalidDisplayException} if the window is on a
     * secondary {@link Display} and the specified display can't be found
     * (see {@link android.app.Presentation}).
     * @param view The view to be added to this window.
     * @param params The LayoutParams to assign to view.
     */
    public void addView(View view, ViewGroup.LayoutParams params);
    public void updateViewLayout(View view, ViewGroup.LayoutParams params);
    public void removeView(View view);
}
```

## 8.2 Window 的内部机制
---
每个 Window 都对应着一个 View 和一个 ViewRootImpl，Window 和 View 通过 ViewRootImpl 建立联系，Window 并不是实际存在的，它是以 View 的形式存在的。

### 8.2.1 Window 的添加过程
Window 的添加过程通过 WindowManager 的 addView 来实现，WindowManagerImpl 是 WindowManager 的实现类，在 WindowManagerImpl 的 addView 中调用了 WindowManagerGlobal 的 addView 最终来实现 View 的添加，添加步骤如下：
1. 检查参数是否合法，如果是子 Window 那么还需要调整一些布局参数

2. 创建 ViewRootImpl 并将 View 添加到列表中
WindowManagerGlobal 中如下几个列表：
```
private final ArrayList<View> mViews = new ArrayList<View>();
private final ArrayList<ViewRootImpl> mRoots = new ArrayList<ViewRootImpl>();
private final ArrayList<WindowManager.LayoutParams> mParams =
        new ArrayList<WindowManager.LayoutParams>();
private final ArraySet<View> mDyingViews = new ArraySet<View>();
```
mViews 存储的是所有 Window 所对应的 View，mRoots 存储的是所有 Window 所对应的 ViewRootImpl，mParams 存储的是所有 Window 对应的布局参数，mDyingViews 存储了正在被删除还没删除完成的 View 的对象，addView 过程：  
```
    root = new ViewRootImpl(view.getContext(), display);

    view.setLayoutParams(wparams);

    mViews.add(view);
    mRoots.add(root);
    mParams.add(wparams);
```

3. 通过 ViewRootImpl 来更行界面并完成 Window 的添加过程  
通过 ViewRootImpl 的 setView 方法，在 setView 内部调用 requestLayout 完成异步刷新请求：
```
public void requestLayout() {
    if (!mHandlingLayoutInLayoutRequest) {
        checkThread();
        mLayoutRequested = true;
        scheduleTraversals();
    }
}
```
scheduleTraversals 实际是 View 绘制的入口，接着通过 WindowSession 最终来完成 Window 的添加过程，Session 内部会通过 WindowManagerService 来实现 Window 的添加，这是一次 IPC 的调用。最后 Window 的添加请求会交给 WindowManagerService 进行处理。

### 8.2.2 Window 的删除过程
通过 WindowManagerImpl 后，再进一步通过 WindowManagerGlobal 来实现，WindowManagerGlobal 的 `removeView` 方法中会调用 `removeViewLocked` 来做进一步删除，`removeViewLocked` 中通过 ViewRootImpl 来完成删除操作，`removeView` 是异步删除，通过 ViewRootImpl 的 `die` 方法发送一个删除请求消息后将 View 添加到 mDyingViews 中，在 ViewRootImpl 的 Hanlder 中会处理这个删除请求，调用 `doDie` 方法，`doDie` 中调用 `dispatchDetachedFromWindow` 方法，进行 View 的真正的删除，`dispatchDetachedFromWindow` 中主要完成的事情有：
1. 垃圾回收的相关工作，比如清除数据和消息、溢出回调

2. 通过 Session 的 remove 方法删除 Window：`mWindowSession.remove(mWindow)`，这是一个 IPC 过程，最终会调用 WindowManagerService 的 `removeWindow` 方法

3. 调用 View 的 `dispatchDetachedFromWindow` 方法，在内部调用 View 的 `onDetachedFromWindow()` 以及 `onDetachedFromWindowInternal()`方法，可以在 `onDetachedFromWindow()` 方法中进行资源回收工作，比如终止动画、停止线程等

4. 调用 WindowManagerGlobal 的 doRemoveView 方法刷新数据，包括 mRoots、mParams 以及 mDyingViews

### 8.2.3 Window 的更新过程
通过 WindowManagerGlobal 的 `updateViewLayout` 方法，首先更新 View 的 LayoutParams 并替换掉老的 LayoutParams，再接着更行 ViewRootImpl 中的 LayoutParams，ViewRootImpl 中会通过 `scheduleTraversals` 方法对 View 进行重新布局、测量、重绘。ViewRootImpl 还会通过 WindowSession 来更新 Window 的视图，这个过程最终由 WindowManagerService 的 `relayoutWindow()` 来具体是心啊，同样是一个 IPC 过程。

## 8.3 Window 的创建过程
---
### 8.3.1 Activity 的 Window 创建过程
Activity 的 Window 是通过 PolicyManager 的 makeNewWindow 方法创建一个 PhoneWindow，PhoneWindow 的 setContentView 创建了一个 View，创建步骤如下：
1. 如果没有 DecorView，那么就创建它  
DecorView 是一个 FrameLayout，是 Activity 中的顶级 View，内部包含一个标题栏和一个 contentView，标题栏会根据主题的设定是否存在，而 contentView 一定会存在。

2. 将 View 添加到 DecorView 的 mContentParent 中

3. 回调 Activity 的 onContentChanged 方法通知 Activity 视图已经发生改变

经过上面三个步骤，DecorView 已经被创建并且初始化完成，Activity 的布局文件也成功添加到 DecorView 的 mContentParent 中，但 DecorView 还没有被 WindowManager 正式添加到 Window 中。在 ActivityThread 的 handleResumeActivity 方法中，会调用 Activity 的 onResume 方法，接着调用 `makeVisible()` ，此时 DecorView 才完成了添加和显示的两个过程。

### 8.3.2 Dialog 的 Window 创建过程
1. 创建 Window  
通过 PolicyManager 的 makeNewWindow 方法来创建一个 PhoneWindow

2. 初始化 DecorView 并将 Dialog 的视图添加到 DecorView 中  
此过程也是通过 Window 的 setContentView 来设置

3. 将 DecorView 添加到 Window 中并显示  
在 Dialog 的 show 方法中通过 WindowManager 将 DecorView 添加到 Window 中。
```
mWindowManager.addView(mDecor, l);
mShowing = true;
```

Dialog 的 Window 和 Activity 的 Window 的创建过程类似。普通 Dialog 必须采用 Activity 的 Context，如果采用 Application 的 Context，那么就会报错。或者指定对话框的 Window 为系统类型并申请悬浮窗权限：
```
dialog.getWindow().setType(LayoutParams.TYPE_SYSTEM_ERROR);

<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
```

### 8.3.3 Toast 的 Window 创建过程
Toast 也是基于 Window 来实现的，由于 Toast 具有取消功能，系统采用了 Handle。Toast 内部有两类 IPC 过程，第一类是 Toast 访问 NotificationManagerService，第二类是 NotificationManagerService 回掉 Toast 的 TN 接口。

Toast 中的 View 可以是系统默认样式，也可以通过 setView 指定一个自定义 View，都对应于 Toast 的内部成员 mNextView。
```
public void show() {
    if (mNextView == null) {
        throw new RuntimeException("setView must have been called");
    }

    INotificationManager service = getService();
    String pkg = mContext.getOpPackageName();
    TN tn = mTN;
    tn.mNextView = mNextView;

    try {
        service.enqueueToast(pkg, tn, mDuration);
    } catch (RemoteException e) {
        // Empty
    }
}
```
Toast 的显示和隐藏都需要通过 NMS 来显示，NMS 运行于系统的进程中，所以只能通过远程调用的方式来显示和隐藏 Toast。TN 是一个 Binder 类，当 NMS 处理 Toast 时会跨进程回调 TN 中的方法，TN 运行于 Binder 线程池中，需要用 Handler 将其切换回当前线程中，所以当一个线程没有 Looper 时，Toast 就无法弹出。

需要显示或隐藏 Toast 时，NMS 会回调 TN 的 show 和 hide 方法，在 TN 的 show 和 hide 方法中，需要使用 Handler 将执行环境切换到 Toast 请求所在的线程：
```
@Override
public void show(IBinder windowToken) {
    if (localLOGV) Log.v(TAG, "SHOW: " + this);
    mHandler.obtainMessage(0, windowToken).sendToTarget();
}

@Override
public void hide() {
    if (localLOGV) Log.v(TAG, "HIDE: " + this);
    mHandler.post(mHide);
}
```
mHandler 处理 show 和 hide 时，分别调用 handleShow 和 handleHide 方法，在 handleShow 中会添加 View，handleHide 中会将 View 移除：
```
// handleShow
mWM = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
mWM.addView(mView, mParams);

// handleHide
if (mView != null) {
    // note: checking parent() just to make sure the view has
    // been added...  i have seen cases where we get here when
    // the view isn't yet added, so let's try not to crash.
    if (mView.getParent() != null) {
        if (localLOGV) Log.v(TAG, "REMOVE! " + mView + " in " + this);
        mWM.removeViewImmediate(mView);
    }

    mView = null;
}
```