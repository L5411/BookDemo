Drawable 表示的是一种可以在 Canvas 上进行绘制的抽象的概念，它的种类有很多，最常见的颜色和图片都可以是一个 Drawable。

## 6.1 Drawable 简介
---
Drawable 常被用作 View 的背景使用，一般通过 XML 来定义，也可以通过代码来创建具体的 Drawable 对象。Drawable 是一个抽象类，是所有 Drawable 对象的基类。

Drawable 可以通过 getIntrinsicWidth 和 getIntrinsicHeight 这两个方法获取宽高。并不是所有的 Drawable 都有宽高，图片所形成的 Drawable 的宽高就是图片的宽高，而颜色所形成的 Drawable 没有宽高的概念。Drawable 的内部宽高不等同于它的大小，Drawable 没有大小的概念。

## 6.2 Drawable 的分类
---
### 6.2.1 BitmapDrawable
BitmapDrawable 表示的是一张图片。可以直接用一张原始图片即可，通过 XML 的方式描述它可以设置更多的效果。

### 6.2.2 ShapeDrawable
通过颜色来构造图形，既可以是纯色图形，也可以是具有渐变效果的图形。以 `<shape></shape>` 包裹，实体类实际上是 GradientDrawable。

### 6.2.3 LayerDrawable
LayerDrawable 对应的 XML 标签是 `<layer-list>`，表示一种层次化的 Drawable 集合，通常将不同的 Drawable 放置在不同层次上达到一种叠加的效果。

### 6.2.4 StateListDrawable
StateListDrawable 对应于 `<selector>` 标签，也表示 Drawable 集合，每个 Drawable 对应着 View 的一种状态，系统会根据 View 的状态选择合适的 Drawable。主要用于设置可单机的 View 的背景，如 Button。  

系统会从上到下的顺序查找，直至查找到第一条匹配的 item，一般来说默认的 item 都应放在 selector 的最后一条且不附带任何状态。

### 6.2.5 LevelListDrawable
LevalListDrawable 对应于 `<level-list>` 标签，同样表示一个 Drawable 集合，每个 Drawable 都含有一个等级的概念，根据不同的等级切换对应的 Drawable，可以通过 Drawable 的 `setLevel` 方法来设置等级，等级的范围为 0 ~ 10000。

### 6.2.6 TransitionDrawable
TransitionDrawable 对应于 `<transition>` 标签，它用于实现两个 Drawable 之间的淡入淡出效果。

### 6.2.7 InsetDrawable
InsetDrawable 对应于 `<inset>` 标签，它可以将其他 Drawable 内嵌到自己当中，并可以在四周留出一定的间距。当一个 View 希望自己的背景比自己的实际区域小的时候，可以采用 InsetDrawable 来实现。

### 6.2.8 ScaleDrawable
ScaleDrawable 对应与 `<scale>` 标签，可以根据自己的等级将指定的 Drawable 缩放到一定的比例。

### 6.2.9 ClipDrawable
ClipDrawable 对应于 `<clip>` 标签，可以根据自己当前的等级来裁剪另一个 Drawable，等级越大裁剪度越高，通常用于实现进度栏之类的项目。

## 6.3 自定义 Drawable
---
[CustomDrawable](../app/src/main/java/com/l_5411/bookdemo/chapter_6/CustomDrawable.java)  

当自定义 Drawable 有固有大小时最好重写 `getIntrinsicWidth` 和 `getIntrinsicHeight` 这两个方法，它会影响到 wrap_content 布局，比如 Drawable 是绘制一张图片，那么 Drawable 内部大小就可以选用图片的大小。