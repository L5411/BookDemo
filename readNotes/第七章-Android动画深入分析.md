Android 动画分为三种：View 动画、帧动画和属性动画，帧动画也属于 View 动画的一种。

## 7.1 View 动画
---
View 动画的作用对象是 View，支持 4 种动画效果，分别是平移动画、缩放动画、旋转动画和透明度动画。

### 7.1.1 View 动画的种类
| 名称 | 标签 | 子类 | 效果 |
| --- | --- | --- | --- |
| 平移动画 | `<translate>` | TranslateAnimation | 移动 View |
| 缩放动画 | `<scale>` | ScaleAnimation | 放大或缩小 View |
| 旋转动画 | `<rotate>` | RotateAnimation | 旋转 View |
| 透明度动画 | `<alpha>` | AlphaAnimation | 改变 View 的透明度 |

View 动画一般使用 XML 文件定义，既可以是单个动画，以可以由一系列动画组成。`<set>` 标签表示动画集合，对应 AnimationSet 类，内部可以嵌套其他动画集合，两个属性含义如下：
* `android:interpolator`: 表示动画集合所采用的插值器，影响动画的速度，这个属性默认为 @android:anim/accelerate_decelerate_interpolator
* `android:shareInterpolator`: 表示集合中的动画是否和集合共享一个插值器

四种动画：
1. <translate> 表示平移动画，对应 TranslateAnimation 类，可以使一个 View 在水平和竖直方向完成平移效果
2. <scale> 表示缩放动画，对应 ScaleAnimation 类，可以使 View 放大或缩小
3. <rotate> 表示旋转动画，对应 RotateAnimation，可以使 View 具有旋转的动画效果
4. <alpha> 表示透明度动画，对应 AlphaAnimation，可以改变 View 的透明度

通过 Animation 的 setAnimationListener 方法给 View 动画添加过程监听：
```
public static interface AnimationListener {
    void onAnimationStart(Animation animation);
    void onAnimationEnd(Animation animation);
    void onAnimationRepeat(Animation animation);
}
```

实例：
声明动画 XML
```
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:fillAfter="true"
    android:zAdjustment="normal">

    <translate
        android:duration="100"
        android:fromXDelta="0"
        android:fromYDelta="0"
        android:interpolator="@android:anim/linear_interpolator"
        android:toXDelta="100"
        android:toYDelta="100"/>

    <rotate
        android:duration="400"
        android:fromDegrees="0"
        android:toDegrees="90" />

</set>
```
使用动画：
```
Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.animation_view);
mViewAnimationButton.startAnimation(animation);
```

### 7.1.2 自定义 View 动画
派生一种新动画只需要继承 Animation 这个抽象类，然后重写 initialize 和 applyTransformation 方法，在 applyTransformation 中进行相应的矩阵变换。自定义 View 动画的过程主要是矩阵变换的过程。

### 7.1.3 帧动画
帧动画是顺序播放一组预先定义好的图片，类似于电影播放。通过使用 AnimationDrawable 来使用帧动画，需要在 drawable 文件夹中通过 XML 来定义一个 AnimationDrawable：
```
<?xml version="1.0" encoding="utf-8"?>
<animation-list xmlns:android="http://schemas.android.com/apk/res/android"
    android:oneshot="false">

    <item android:drawable="@color/colorAccent" android:duration="500"/>
    <item android:drawable="@color/colorPrimary" android:duration="500"/>
    <item android:drawable="@drawable/icon1" android:duration="500"/>
</animation-list>
```
使用动画：
```
mFrameAnimationButton.setBackgroundResource(R.drawable.animation_frame);
AnimationDrawable drawable = (AnimationDrawable) mFrameAnimationButton.getBackground();
drawable.start();
```

## 7.2 View 动画的特殊使用场景
---
* 在 ViewGroup 中可以控制子元素的出场效果
* 在 Activity 中实现不同 Activity 之间的切换效果

### 7.2.1 LayoutAnimation
LayoutAnimation 作用于 ViewGroup，为 ViewGroup 指定一个动画，这样当子元素出场时都具有这种动画效果。步骤如下：
1. 定义 LayoutAnimation
```
<?xml version="1.0" encoding="utf-8"?>
<layoutAnimation xmlns:android="http://schemas.android.com/apk/res/android"
    android:delay="0.5"
    android:animationOrder="normal"
    android:animation="@anim/anim_item"/>
```
* `android:delay`：表示子元素开始动画的时间延迟，表示占子元素入场动画的时间周期的比例，如子元素入场动画时间周期为 300ms，则 0.5 表示每个子元素延迟 150ms

* `android:animationOrder`：表示子元素动画顺序，normal 表示顺序显示，reverse 表示逆向显示，random 随机显示

* `android:animation`：子元素具体入场动画

2. 为子元素指定具体入场动画
```
<?xml version="1.0" encoding="utf-8"?>
<set xmlns:android="http://schemas.android.com/apk/res/android"
    android:duration="300"
    android:interpolator="@android:anim/accelerate_decelerate_interpolator"
    android:shareInterpolator="true">

    <alpha
        android:fromAlpha="0.0"
        android:toAlpha="1.0"/>

    <translate
        android:fromXDelta="500"
        android:toXDelta="0"/>
</set>
```

3. 设置 `android:layoutAnimation` 属性
```
<ListView
    android:id="@+id/list"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layoutAnimation="@anim/anim_layout"
    android:background="#fff4f7f9"
    android:cacheColorHint="#00000000"
    android:divider="#dddbdb"
    android:dividerHeight="1dp"
    android:listSelector="@android:color/transparent" />
```
同时也可以在代码中通过 `LayoutAnimationContriller` 来实现：
```
Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_item);
LayoutAnimationController controller = new LayoutAnimationController(animation);
controller.setDelay(0.5f);
controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
listView.setLayoutAnimation(controller);
```
通过对 ListView 设置 LayoutAnimation 只能在初次显示时有动画，只有可见 item 有动画，滚动 ListView 时将没有动画。

### 7.2.2 Activity 的切换效果
使用 `overridePendingTransition(int enterAnim, int exitAnim)` 这个方法可以自定义 Activity 的切换效果，这个方法必须在 `startActivity(Intent)` 或者 `finish()` 之后被调用才能生效。

启动 Activity：
```
startActivity(TestActivity.newIntent(mContext));
overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
```
退出 Activity：
```
@Override
public void finish() {
    super.finish();
    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
}
```

## 7.3 属性动画
---
属性动画对作用对象进行了扩展，属性动画可以对任何对象做动画，甚至还可以没有对象。

### 7.3.1 使用属性动画
属性动画从 API 11 才有，需要采用开源动画库 nineoldandroids 对以前的版本进行兼容。比较常用的动画类：ValueAniator、ObjectAnimator 和 AnimatorSet。属性动画的使用：
1. 改变对象 translationY 属性，使其进行平移：
```
ObjectAnimator.ofFloat(myObject, "translationY", -myObject.getHeight()).start();
```
这里是设置对象的 translationY 属性，如果执行多次只有第一次起作用。

2. 改变对象的背景色属性
```
ValueAnimator colorAnim = ObjectAnimator.ofInt(imageView,
        "backgroundColor",
        /* holo_red_light */ 0xffff4444,
        /* holo_blue_light */ 0xff33b5e5);
colorAnim.setDuration(3000);
colorAnim.setEvaluator(new ArgbEvaluator());        // 设置颜色计算器，使颜色渐变
colorAnim.setRepeatCount(ValueAnimator.INFINITE);   // 设置无限循环
colorAnim.setRepeatMode(ValueAnimator.REVERSE);     // 设置反转效果
colorAnim.start();
```

3. 动画集合
```
AnimatorSet set = new AnimatorSet();
set.playTogether(
        ObjectAnimator.ofFloat(imageView, "rotationX", 0, 360),
        ObjectAnimator.ofFloat(imageView, "rotationY", 0, 180),
        ObjectAnimator.ofFloat(imageView, "rotation", 0, -90),
        ObjectAnimator.ofFloat(imageView, "translationX", 0, 90),
        ObjectAnimator.ofFloat(imageView, "translationY", 0, 90),
        ObjectAnimator.ofFloat(imageView, "scaleX", 1, 1.5f),
        ObjectAnimator.ofFloat(imageView, "scaleY", 1, 0.5f),
        ObjectAnimator.ofFloat(imageView, "alpha", 1, 0.25f, 1)
);
set.setDuration(5 * 1000).start();
```

属性动画不仅可以通过代码实现，还可以在 XML 中定义实现，属性动画需要定义在 `res/animator/` 目录下。实际开发中建议采用代码来实现属性动画。

### 7.3.2 理解插值器和估值器
插值器，根据时间流逝百分比计算出当前属性值改变的百分比：
* LinearInterpolator：线性插值器，匀速动画
* AccelerateDecelerateInterpolator：加速减速插值器，动画两头慢中间快
* DecelerateInterpolator：减速插值器，动画越来越慢

估值器，根据当前属性值改变的百分比计算出改变后的属性值：
* IntEvaluator：整型估值器
* FloatEvaluator：针对浮点型
* ArgbEvaluator：针对 Color 属性

自定义插值器需要实现 Interpolator 或者 TimeInterpolator，自定义估值器需要实现 TypeEvaluator。

### 7.3.3 属性动画的监听器
AnimatorListener：
```
public static interface AnimatorListener {

    void onAnimationStart(Animator animation);

    void onAnimationEnd(Animator animation);

    void onAnimationCancel(Animator animation);

    void onAnimationRepeat(Animator animation);
}
```
实现监听器时可以使用 AnimatorListenerAdapter 这个类，有选择的实现上面的四个方法。

AnimatorUpdateListener：
```
public static interface AnimatorUpdateListener {

    void onAnimationUpdate(ValueAnimator animation);

}
```
监听整个动画过程，每播放一帧，onAnimationUpdate 就被调用一次。

### 7.3.4 对任意属性做动画
属性动画原理：属性动画要求动画作用的对象提供该属性的 get 和 set 方法，属性动画根据外界传递的该属性的初始值和最终值，以动画的效果多次去调用 set 方法，每次传递给 set 方法的值都不一样。对象必须提供 set 方法，如果开始动画没有传递初始值，则要调用 get 方法获取初始值，否则程序会直接 Crash；set 方法对属性进行改变必须能够通过某种方式展现出来，否则将不会带来 UI 的改变。有三种方法使属性动画能作用于属性：
1. 给对象加上 get 和 set 方法，如果有权限的话，一般这些方法都是在 Android SDK 内部实现的，没有权限加 set 和 get 方法

2. 用一个类来包装原始对象，间接提供 get 和 set 方法：  
```
    private void performAnimate() {
        ViewWrapper wrapper = new ViewWrapper(mButton);
        ObjectAnimator.ofInt(wrapper, "width", 500).setDuration(5000).start();
    }

    @Override
    public void onClick(View v) {
        if (v == mButton) {
            performAnimate();
        }   
    }

    private static class ViewWrapper {
        private View mTarget;

        public ViewWrapper(View target) {
            mTarget = target;
        }

        public int getWidth() {
            return mTarget.getLayoutParams().width;
        }

        public void setWidth(int width) {
            mTarget.getLayoutParams().width = width;
            mTarget.requestLayout();
        }
    }
```

3. 采用 ValueAnimator，监听动画过程，自己实现属性的改变  
ValueAnimator 本身不作用于任何对象，通过它对一个值做动画，监听其动画过程，在动画过程中修改对象的属性值，这样就相当于对对象做了动画。
```
    private void performAnimate(final View target, final int start, final int end) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(1, 100);
        valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
            // 持有一个 IntEvaluator 对象，方便估值的时候使用
            private IntEvaluator mEvaluator = new IntEvaluator();

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                // 获得当前动画的进度值 1 - 100 之间
                int currentValue = (Integer) animator.getAnimatedValue();
                Log.d(TAG, "Current value: " + currentValue);

                // 获得当前进度占整个动画过程的比例，0 - 1 之间
                float fraction = animator.getAnimatedFraction();
                // 直接调用整型估值器，通过比例计算出宽度，然后再设置给 Button
                target.getLayoutParams().width = mEvaluator.evaluate(fraction, start, end);
                target.requestLayout();
            }
        });

        valueAnimator.setDuration(5000).start();
    }

    @Override
    public void onClick(View v) {
        if (v == mButton) {
            performAnimate(mButton, mButton.getWidth(), 500);
        }   
    }
```

### 7.3.5 属性动画的工作原理
通过调用对象提供的该属性的 set 方法，属性动画根据传递的该属性的初始值和最终值，以动画效果多次去调用 set 方法，每次传递给 set 方法的值都不一样，随着时间的推移，所传递的值越来越接近最终值，如果没有提供初始值，还要提供 get 方法获取属性的初始值。

## 7.4 使用动画注意事项
---
1. OOM 问题：帧动画中图片数量较多且图片较大时极易出现 OOM
2. 内存泄漏：属性动画中的无限循环动画在 Activity 退出时应该及时停止，否则会导致 Activity 无法释放从而造成内存泄漏，View 动画不存在此问题
3. 兼容性问题：3.0 以下系统上有兼容性问题
4. View 动画的问题：View 动画对 View 的影像做动画，并没有真正的改变 View 的状态，因此有时动画完成后 View 无法隐藏，即 `setVisibility(View.GONE)` 失效，这时需要调用 `view.clearAnimation()` 清除 View 动画即可解决
5. 不要使用 px：使用 px 在不同设备上会有不同效果，要尽量使用 dp
6. 动画元素的交互：Android 3.0 以前的系统中 View 动画和属性动画新位置无法触发单击事件，老位置仍然可以触发单击事件。从 3.0 以后属性动画不存在此问题，View 动画仍然存在
7. 使用动画的过程中建议开启硬件加速，这样会提高动画的流畅性
