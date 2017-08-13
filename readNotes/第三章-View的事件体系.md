## 3.1 View 基础知识
---
主要介绍：View 的位置参数、MotionEvent 和 TouchSlop 对象、VelocityTracker、GestureDetector 和 Scroller 对象。

### 3.1.1 什么是 View
View 是 Android 中所有控件的基类，是一种界面层的控件的一种抽象，它代表了一个控件。ViewGroup 被译为控件组，ViewGroup 内部包含了许多个控件，即一组 View，ViewGroup 也是一个 View。

### 3.1.2 View 的位置参数
View 中有四个属性：top、left、right、bottom，这些坐标是相对于父容器的，是**相对坐标**
```
width = right - left
weight = bottom - top
```
从 Android 3.0 开始，View 增加了额外几个参数：x、y、translationX 和 translationY，x 和 y 是 View 的左上角坐标，而 translationX 和 translationY 是 View相对于父容器的偏移量，这几个参数都是相对于父容器的。
```
x = left + translationX
y = top + translationY
```
translationX、translationY 默认值为 0，在 View 的平移过程中，top 和 left 表示的是原始左上角的信息，其值不会发生改变，发生改变的是 x、y、translationX 和 translationY。

### 3.1.3 MotionEvent 和 TouchSlop
#### 1. MotionEvent
* ACTION_DOWN：手指刚接触到屏幕;
* ACTION_MOVE：手指在屏幕上移动;
* ACTION_UP：手指从屏幕上松开的一瞬间;
* 点击屏幕后离开松手：DOWN -> UP;
* 点击屏幕滑动一会再松开：DOWN -> MOVE -> ... -> MOVE -> UP。  

使用 getX 和 getY 可以获取相对于当前 View 左上角的 x 和 y 坐标，使用 getRawX 和 getRawY 返回相对于手机屏幕左上角的 x 和 y 坐标。

#### 2. TouchSlop
TouchSlop 是系统所能识别出的被认为是滑动的最小距离，如果两次滑动距离小于这个常量，系统就不认为在进行滑动操作。可以通过以下方式获取：
```
ViewConfiguration.get(getContext()).getScaledTouchSlop()
```

### 3.1.4 VelocityTracker、GestureDetector 和 Scroller
#### 1. VelocityTracker
速度追踪，用于追踪手指在滑动过程中的速度。使用方法：
* 在 View 的 onTouchEvent 方法中追踪当前点击事件的速度：
```
VelocityTracker velocityTracker = VelocityTracker.obtain();
velocityTracker.addMovement(event);
```
* 获取滑动速度：
```
velocityTracker.computeCurrentVelocity(1000);
int xVelocity = (int) velocityTracker.getXVelocity();
int yVelocity = (int) velocityTracker.getYVelocity();
```
在获取滑动速度之前需要调用 `computeCurrentVelocity` 进行速度计算，参数代表时间段，单位 ms，速度值可以为负数，上边代码获取的速度为 x像素/1000ms
* 当不需要使用的时候，需要调用 clear 方法来重置并回收内存：
```
velocityTracker.clear();
velocityTracker.recycle();
```

#### 2. GestureDetector
手势检测，用于辅助检测用户的单击、滑动、长按、双击等行为。
* 创建一个 GestureDetector 对象并实现 OnGestureListener 接口：
```
GestureDetector mGestureDetector = new GestureDetector(this);
// 解决长按屏幕后无法拖动的现象
mGestureDetector.setIsLongpressEnabled(false);
```
* 接管目标 View 的 onTouchEvent 方法，在待监听 View 的 onTouchEvent 方法中添加如下实现：
```
boolean consume = mGestureDetector.onTouchEvent(event);
return consume;
```

#### 3. Sroller
弹性滑动对象，用于实现 View 的弹性滑动，需要和 View 的 computeScroll 方法配合使用才能完成 View 的弹性滑动。

## 3.2 View 的滑动
---
实现 Veiw 的滑动有三种方式：
1. 通过 View 本身提供的 scrollTo/scrollBy 方法来实现滑动；
2. 通过动画给 View 施加平移的效果来实现滑动；
3. 通过改变 View 的 LayoutParams 使得 View 重新布局从而实现滑动。

### 3.2.1 使用 scrollTo/scrollBy
scrollBy 实际上也是调用 scrollTo 方法，从左向右滑动时，mScrollX 的值为负值，反之为正值；从上往下滑动时，mScrollY 为负值，反之为正值。  

***使用这两个方法只能将 View 的内容进行移动，不能将 View 本身进行移动。***

### 3.2.2 使用动画
使用动画来移动 View 主要是操作 View 的 translationX 和 translationY 属性，可以使用传统的 View 动画，也可以使用属性动画。
* View 动画是对 View 的影像做操作，并不能真正的改变 View 的位置参数，包括宽高，若要动画后的状态得以保留必须将 fillAfter 的属性设置为 true，否则在动画完成的一刹那 View 会瞬间恢复到原来的状态，但仍然没有真正的改变 View 的位置。
* 属性动画将 View 在 100ms 内向右平移 100 个像素：
```
ObjectAnimator.ofFloat(targetView, "translationX", 0, 100).setDuration(100).start();
```
使用属性动画则真正的改变了 View 的位置。在 Android 3.0 以下无法使用属性动画，需要使用动画兼容库 nineoldandroids 来实现属性动画。

### 3.2.3 改变布局参数
改变布局参数即改变 LayoutParams。
1. 将一个 Button 向右平移 100px，只需要将 Button 的 LayoutParams 里的 marginLeft 参数值增加 100px；
2. 可以在 Button 左侧放置一个空 View，当需要右移 Button 时，只需重新设置空 View 的宽度。 

重新设置 View 的 LayoutParams：
```
MarginLayoutParams params = (MarginLayoutParams) mButton.getLayoutParams();
params.width += 100;
params.leftMargin += 100;
mButton.requestLayout();
// 或者 mButton.setLayoutParams(params);
```

### 3.2.4 各种滑动方式的对比
* scrollTo/scrollBy：操作简单，适合对 View 内容的滑动，只能滑动 View 的内容，不能滑动 View本身；
* 动画：操作简单，主要适用于没有交互的 View 和实现复杂的动画效果；
* 改变布局参数：操作稍微复杂，适用于有交互的 View。

## 3.3 弹性滑动
---
### 3.3.1 使用 Scroller
```\
Scroller scroller = new Scroller(mContext);

// 缓慢滚动到指定位置
private void smoothScrollTo(int destX, int destY) {
    int scrollX = getScrollX();
    int deltaX = destX - scrollX;
    // 1000ms 内滑向 destX，效果就是慢慢滑动
    mScroller.startScroll(scrollX, 0, deltaX, 0, 1000);
    invalidate();
}

@Override
public void computeScroll() {
    if (mScroller.computeScrollOffset()) {
        scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
        postInvalidate();
    }
}
```
`invalidate` 方法会导致 View 的重绘，在 View 的 draw 方法中又会去调用 `computeScroll` 方法，此时会从 Scroller 获取当前的 scrollX 和 scrollY，然后通过 `scrollTo` 方法进行滑动，滑动完成后又调用 `postInvalidate` 方法进行重绘，从而又调用 `computeScroll` 方法，最终实现 View 的弹性滑动。  
Scroller 的 `computeScrollOffset` 方法会根据时间流逝的百分比计算 scrollX 和 scrollY 当前的值，如果滑动完成此方法会返回 false，当返回 true 时需要进行对 View 的滑动。

### 3.3.2 通过动画
动画本身是一种渐进的过程，通过动画实现滑动天然具有弹性效果：
```
ObjectAnimator.ofFloat(targetView, "translationX", 0, 100).setDuration(100).start();
```
通过动画仿 Scroller 实现滑动：
```
final int startX = 0;
final int deltaX = 100;

ValueAnimator animator = ValueAnimator.ofInt(0, 1).setDuration(1000);
animator.addUpdateListener(new AnimatorUpdateListener() {
    @Override
    public void onAnimationUpdate(ValueAnimator animator) {
        float fraction = animator.getAnimatedFranction();
        mButton.scrollTo(startX + (int) (deltaX * fraction), 0);
    }   
});
animatro.start();
```
通过上述代码，可以实现一些其他的效果，完全可以在 onAnimationUpdate 方法中加上想要的其它操作。

### 3.3.3 使用延时策略
通过发送一系列延时消息从而达到一种渐进式的效果，可以使用 Handler 或 View 的 postDelayed 方法，也可以使用线程的 sleep 方法。
通过 Handler 在约 1000ms 中将 View 向左滑动 100 像素：
```
private static final int MESSAGE_SCROLL_TO = 1;
private static final int FRAME_COUNT = 30;
private static final int DELAYED_TIME = 33;

private int mCount = 0;

@SuppressLint("HandlerLeak")
private Handler mHandler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what){
            case MESSAGE_SCROLL_TO: {
                mCount++;
                if (mCount <= FRAME_COUNT) {
                    float fraction = mCount / (float) FRAME_COUNT;
                    int scrollX = (int) (fraction * 100);
                    mButton.scrollTo(scrollX, 0);
                    mHandler.sendEmptyMessageDelayed(MESSAGE_SCROLL_TO, DELAYED_TIME);
                }
            }
            break;
            default:
                break;
        }
    }
};
```
## 3.4 View 的事件分发机制
---
[图解 Android 事件分发机制](http://www.jianshu.com/p/e99b5e8bd67b)

### 3.4.1 点击事件的传递规则
点击事件：MotionEvent  
所谓的点击事件的事件分发，其实就是对 MotionEvent 事件的分发过程，当一个 MotionEvent 产生以后，系统需要把这个事件传递给一个具体的 View。主要方法有三个：
* `public boolean dispatchTouchEvent(MotionEvent ev)`  
    用来进行事件的分发，如果事件能够传递给当前 View，那么此方法一定会被调用，返回值表示点击事件是否被当前 View 消耗。

* `public boolean onInterceptTouchEvent(MotionEvent ev)`  
    在 `dispatchTouchEvent` 内部调用，表示是否要拦截点击事件，如果拦截了点击事件，那么在同一个事件序列中，此方法将不会被再次调用。

* `public boolean onTouchEvent(MotionEvent ev)`  
    在 `dispatchTouchEvent` 内部调用，用来处理点击事件，返回结果表示是否消耗当前点击事件。

上述三个方法关系：
```
public boolean dispatchTouchEvent(MotionEvent ev) {
    boolean consume = false;
    // 如果拦截了时间 将由当前 View 处理点击事件 不会再传递给子 View
    if (onInterceptTouchEvent(ev)) {
        consume = onTouchEvent(ev);
    } else {
        consume = child.dispatchTouchEvent(ev);
    }   

    return consume;
}
```

**onTouch > onTouchEvent > onClick**  

OnTouchListener 中的 onTouch 方法如果返回 false，则当前 View 的 onTouchEvent 方法会被调用；如果返回 true，则 onTouchEvent 不会被调用；OnClickListener 的 onClick 会在 onTouchEvent 方法中被调用，所以三者的优先级如上所示。

事件传递机制结论：
1. 同一个事件序列从手指接触屏幕一刻开始，到离开屏幕一刻结束。由 down 事件开始，中间含有数量不定的 move 事件，最终以 up 事件结束。

2. 正常情况下一个事件序列只能被一个 View 拦截且消耗。

3. 某个 View 一旦决定拦截，那么这一个事件序列都只能交给它来处理，并且 onInterceptTouchEvent 不会再被调用。

4. 当一个 View 一旦开始处理事件，如果它不消耗 ACTION_DOWN 事件（onTouchEvent 返回了 false），那么同一事件序列中的其他事件都不会再交给它处理，并将事件重新交给它的父元素去处理。

5. 如果 View 不消耗除 ACTION_DOWN 以外的其他事件，那么这个点击事件将会消失，此时父元素的 onTouchEvent 并不会被调用，当前 View 可以持续收到后续的事件，最终消失的点击事件会传递给 Activity 处理。

6. ViewGroup 默认不拦截任何事件。

7. View 没有 onInterceptTouchEvent 方法，一旦有点击事件传递给它，那么它的 onTouchEvent 方法就会被调用。

8. View 的 onTouchEvent 默认都会消耗事件，除非它为不可点击的（clickable 和 longClickable 同时为 false）。

9. View 的 enable 属性不影响 onTouchEvent 的默认返回值。哪怕一个 View 是 disable 状态，只有 clickable 或 longClickable 有一个为 true，那么 onTouchEvent 就返回 true。

10. onClick 会发生的前提是当前 View 是可点击的，并且收到了 down 和 up 事件。

11. 事件传递过程是由外向内的，即事件总是先传递给父元素，然后再由父元素分发给子 View，通过 `requestDisallowInterceptTouchEvent` 方法可以在子元素中干扰父元素的事件分发过程，但是 ACTION_DOWN 事件除外。

## 3.5 View 的滑动冲突
---
### 3.5.1 常见的滑动冲突场景
* 场景1：外部滑动方向和内部滑动方向不一致，如外部纵向 ScrollView 和内部横向的 ListView；

* 场景2：外部滑动方向和内部滑动方向一致，这种情况下系统不知道用户到底想让哪一层进行滑动，这种场景主要是指内外两层能同时上下滑动或者内外两层能同时左右滑动。

* 场景3：上面两种情况的嵌套。

### 3.5.2 滑动冲突的处理规则
* 场景1：根据滑动是水平还是竖直来判断到底是谁来拦截事件。判断滑动方向可以根据滑动路径和水平方向所形成的夹角，某些特殊的时候还可以根据水平和竖直方向的速度差来判断，一般根据水平方向和竖直方向的距离差来判断，如果竖直方向距离大于水平方向距离则是竖直滑动，否则则是水平滑动。

* 场景2：它的滑动无法根据角度、距离差以及速度差来判断，一般是根据业务上的逻辑来判断需要哪一个进行滑动。

* 场景3：同样需要根据业务上的需求来制定相应的处理规则。

### 3.5.3 滑动冲突的解决方式
1. 外部拦截法  
外部拦截法指点击事件需要先经过父容器的拦截处理，如果父容器需要点击事件则拦截，不需要则进行分发。进行外部拦截需要重写父容器的 `onInterceptTouchEvent` 方法：
```
public boolean onInterceptTouchEvent(MotionEvent event) {
    boolean intercepted = false;
    int x = (int) event.getX();
    int y = (int) event.getY();
    switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            // 此处不能拦截，一旦拦截，后续事件都无法再传递给子元素了
            intercepted = false;
            break;
        case MotionEvent.ACTION_MOVE:
            if (父容器需要当前点击事件) {
                intercepted = true;
            } else {
                intercepted = false;
            }
            break;
        case MotionEvent.ACTION_UP:
            // 此处也必须返回 false，否则子元素的 onClick 就无法触发，一旦父容器拦截了拦截任何一个事件，后续事件都将由父容器处理，所以此处也会由父容器处理，即便返回了 false
            intercepted = false;
            break;
        default:
            break;
    }
    mLastXIntercept = x;
    mLastYIntercept = y;
    return intercepted;
}
```
2. 内部拦截法
父容器不拦截任何事件，所有的事件都传递给子元素，如果子元素需要此事件就直接消耗，否则交由父容器处理，需要配合 `requestDisallowInterceptTouchEvent` 方法才能正常工作。需要重写子元素的 `dispatchTouchEvent` 方法：
```
public boolean dispatchTouchEvent(MotionEvent event) {
    int x = (int) event.getX();
    int y = (int) event.getY();

    switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            parent.requestDisallowInterceptTouchEvent(true);
            break;
        case MotionEvent.ACTION_MOVE:
            int deltaX = x - mLastX;
            int deltaY = y - mLastY;
            if (父容器需要当前点击事件) {
                parent.requestDisallowInterceptTouchEvent(false);
            }
            break;
        case MotionEvent.ACTION_UP:
            break;
        default:
            break;
    }

    mLastX = x;
    mLastY = y;
    return super.dispatchTouchEvent(event);
}
```
父元素修改如下：
```
public boolean onInterceptTouchEvent(MotionEvent event) {
    int action = event.getAction();
    if (action == MotionEvent.ACTION_DOWN) {
        return false;
    } else {
        // 此处参考结论3
        return true;
    }
}
```