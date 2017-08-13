主要介绍 View 的工作原理和自定义 View 的实现方式。

## 4.1 初识 ViewRoot 和 DecorView
---
ViewRoot 对应于 ViewRootImpl 类，是连接 WindowManager 和 DecorView 的纽带。View 的绘制流程是从 ViewRoot 的 performTraversals 方法开始的，经过如下三个过程将一个 View 绘制出来：
* measure：用来测量 View 的宽和高；
* layout：用来确定 View 在父容器中放置位置；
* draw：负责将 View 绘制在屏幕上。

在 performTravesals 方法中会依次调用 performMeasure、performLayout 和 performDraw 三个方法，performMeasure 会调用 measure 方法测量当前 View 的宽高，measure 中会调用 onMeasure，onMeasure 会对子元素进行 measure 过程，其余两个方法类似。

DecorView 中包含一个 竖直方向的 LinearLayout，这个 LinearLayout 的上半部分为标题栏，下半部分为内容栏。Activity 中的 setContentView 就是将布局文件加载到内容栏中，内容栏 id 为 content。如何获取 content：
```
ViewGroup content = (ViewGroup) findViewById(android.R.id.content);

// 获取加载的 View
content.getChildAt(0);
```

## 4.2 理解 MeasureSpec
---
在测量过程中，系统会将 View 的 LayoutParams 根据父容器所施加的规则转换成对应的 MeasureSpec，然后再根据这个 measureSpec 来测量出 View 的宽和高。

### 4.2.1 MeasureSpec
MeasureSpec 代表一个 32 位 int 值，高 2 位代表 SpecMode，低 30 位代表 SpecSize，SpecMode 指测量模式，SpecSize 指某种测量模式下的规格大小。  
SpecMode 类型：
* UNSPECIFIED：译为不明，父容器不对 View 有任何限制，要多大给多大，这种情况一般用于系统内部，表示一种测量的状态；

* EXACTLY：译为准确，父容器已经检测出 View 所需要的精确大小，这个时候 View 的最终大小就是 SpecSize 所指定的值。它对应于 LayoutParams 中 match_parent 和具体的数值这两种模式；

* AT_MOST：译为最大，父容器指定了一个可用大小即 SpecSize，View 的大小不能大于这个值，具体什么值要看不同 View 的具体实现。它对应于 LayoutParams 中的 wrap_content。

### 4.2.2 MeasureSpec 和 LayoutParams 的对应关系
在 View 测量的时候，系统会将 LayoutParams 在父容器的约束下转换成对应的 MeasureSpec，然后再根据这个 MeasureSpec 来确定 View 的测量后的宽和高。MeasureSpec 不是唯一由 LayoutParams 决定的，LayoutParams 需要和父容器一起才能决定 View 的 MeasureSpec，从而进一步确定 View 的宽和高。对于 DecorView，其 MeasureSpec 由窗口的尺寸和其自身的 LayoutParams 来共同确定；对于普通 View，其 MeasureSpec 由父容器的 MeasureSpec 和自身的 LayoutParams 来共同决定。

* View 采用固定宽高：不管父容器的 MeasureSpec 是什么，View 的 MeasureSpec 都是 EXACTLY，并且大小为 LayoutParams 中的大小；
* View 采用 match_parent：父容器的 MeasureSpec 为 EXACTLY 时，View 的 MeasureSpec 为 EXACTLY，大小为父容器剩余的空间；父容器的 MeasureSpec 为 AT_MOST 时，View 的 MeasureSpec 为 AT_MOST，大小不会超过父容器的剩余空间；
* View 采用 wrap_content：不管父容器的 MeasureSpec 为什么，View 的 MeasureSpec 都为 AT_MOST，并且大小不能超过父容器剩余空间。

## 4.3 View 的工作流程
---
具体的工作流程参看[Android View 的绘制流程](http://www.jianshu.com/p/5a71014e7b1b)  
measure 确定 View 的测量宽高，layout 确定 View 的最终宽高和四个定点位置，draw 将 View 绘制到屏幕上。

### 4.3.1 measure 过程
measure 过程分为 View 的 measure 和 ViewGroup 的 measure。View 的 measure 进行自身的测量过程；ViewGroup 的 measure 除了完成自己的测量过程外，还会遍历去调用所有子元素的 measure 方法，各个子元素递归的去执行这个流程直到全部测量完成。  
直接继承 View 的自定义控件需要重写 onMeasure 方法并设置 wrap_content 时的自身大小，否则在布局中使用 wrap_content 就相当于使用 match_parent。
```
protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
    int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
    int heightSpecMode = MeasureSpec.getMode(widthMeasureSpec);
    int heightSpecSize = MeasureSpec.getSize(widthMeasureSpec);

    if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
        setMeasuredDimension(mWidth, mHeight);
    } else if (widthSpecMode == MeasureSpec.AT_MOST) {
        setMeasuredDimension(mWidth, heightSpecSize);
    } else if (heightSpecMode == MeasureSpec.AT_MOST) {
        setMeasuredDimension(widthSpecSize, mHeight);
    }
}
```
上述代码中 mWidth 和 mHeight 为 wrap_content 时的宽高，根据需要而定。

View 的 measure 过程和 Activity 的生命周期方法不是同步执行，因此无法保证 Activity 执行了 onCreate、onStart、onResume 时某个 View 已经测量完毕了，如果 View 未测量完毕，那么获得的宽高就是 0。获取 View 的宽高四种方法：
1. Activity/View#onWindowFocusChanged  
    onWidowFocusChanged 会在 View 初始化完毕时调用，会在 Activity 窗口得到焦点失去焦点时调用，在 onResume、onPause 时均会被调用。  
    ```
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            int width = view.getMeasuredWidth();
            int height = view.getMeasureHeight();
        }
    }
    ```
2. view.post(runnable)  
    通过 post 可以将一个 runnable 投递到消息队列的尾部，然后等待 Lopper 调用此 runnable 的时候，View 也已经初始化好了。   
    ```
    protected void onStart() {
        super.onStart();
        view.post(new Runnable() {
            @Override
            public void run() {
                int width = view.getMeasuredWidth();
                int height = view.getMeasureHeight();
            }
        })
    }
    ```
3. ViewTreeObserver  
    使用 ViewTreeObserver 的众多回调可以完成这个功能，如使用 OnGlobalLayoutListener 这个接口，当 View 树的状态发生改变或者 View 树内部 View 的可见性发生改变时，onGlobalLayout 方法将被回调，但随着 View 树状态的改变等，onGlobalLayout 会被调用多次。
    ```
    protected void onStart() {
        super.onStart();
        ViewTreeObserver observer = view.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = view.getMeasuredWidth();
                int height = view.getMeasureHeight();
            }
        })
    }    
    ```  
4. view.measure(int widthMeasureSpec, int heightMeasureSpec)  
    通过手动对 View 进行 measure 来得到 View 的宽和高。需要根据 LayoutParams 来分情况处理：
    * match_parent:  
    此时无法 measure 出具体的宽和高。构造 match_parent 类型的 MeasureSpec 需要知道 parentSize，而此时无法知道 parentSize 的大小，所以不能测量出 View 的大小。

    * 具体数值（dp/px）
    ```
    int widthMeasureSpec = MeasureSpec.makeMeasureSpec(100, MeasureSpec,EXACTLY);
    int heightMeasureSpec = MeasureSpec.makeMeasureSpec(100, MeasureSpec.EXACTLY);
    view.measure(widthMeasureSpec, heightMeasureSpec);
    ```  

    * wrap_content  
    ```
    int widthMeasureSpec = MeasureSpec.makeMeasureSpec((1 << 30) - 1, MeasureSpec,AT_MOST);
    int heightMeasureSpec = MeasureSpec.makeMeasureSpec((1 << 30) - 1, MeasureSpec.AT_MOST);
    view.measure(widthMeasureSpec, heightMeasureSpec);
    ```  

### 4.3.2 layout 过程
Layout 的作用是 ViewGroup 用来确定子元素的位置，当 ViewGroup 的位置被确定后，它在 onLayout 中会遍历所有的子元素并调用其 layout 方法，在 layout 方法中会调用 onLayout 方法。Layout 用于确定 View 本身的位置，onLayout 方法则会确定所有子元素的位置。

### 4.3.3 draw 过程
draw 绘制步骤：
1. 绘制背景 background.draw(canvas)
2. 绘制自己 onDraw
3. 绘制 children dispatchDraw
4. 绘制装饰 onDrawScrollBars

View 有 setWillNotDraw 方法，默认情况下 View 没有启用这个标记位，而 ViewGroup 默认启用这个优化标记位。当一个 ViewGroup 需要通过 onDraw 来绘制内容时，需要显示的关闭 WILL_NOT_DRAW 这个标记位。

## 4.4 自定义 View
---
### 4.4.1 自定义 View 的分类
1. 继承 View 重写 onDraw 方法  
    这种方法用于实现不规则的效果，采用这种方法需要自己支持 wrap_content，并且 padding 也需要自己处理。
2. 继承 ViewGroup 派生特殊的 Layout  
    主要用于实现自定义的布局，需要合适的处理 ViewGroup 的测量、布局这两个过程，并同时处理子元素的测量和布局过程。
3. 继承特定的 View（比如 TextView）  
    比较常见，不需要自己支持 wrap_content 和 padding 等。
4. 继承特定的 ViewGroup（比如 LinearLayout）
    采用这种方法不需要自己处理 ViewGroup 的测量和布局这两个过程，一般来说方法 2 能实现的效果，方法 4 也能实现，而方法 2 更加接近 View 的底层。

### 4.4.2 自定义 View 须知
1. 让 View 支持 wrap_content
2. 如果有必要，让你的 View 支持 padding  
    直接继承 View 的控件，需要在 draw 方法中处理 padding；直接继承 ViewGroup 的空间需要在 onMeasure 和 onLayout 中考虑 padding 和子元素的 margin 对其造成的影响，不然将导致 padding 和 子元素的 margin 失效。
3. 尽量不要在 View 中使用 Handler  
    View 内部本身提供了 post 系列的方法，完全可以替代 Handler 的作用，除非你很明确的要使用 Handler 来发送消息。
4. View 中如果有线程和动画，需即使停止，参考 View#onDetachedFromWindow  
    当 View 变得不可见时需要停止线程和动画，如果不及时处理，可能会造成内存泄漏。
5. View 带有滑动嵌套情形时，需要处理好滑动冲突。

### 4.4.3 自定义 View 示例
