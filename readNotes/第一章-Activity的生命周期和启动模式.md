## 1.1 Activity 的生命周期全面分析
---
### 1.1.1 典型情况下的生命周期分析
* onCreate -> onStart -> onResume ->onPause -> onStop -> onDestroy
* onPause 中经常做一些存储数据、停止动画等工作，不能太耗时，否则会影响到新的 Activity的显示，**onPause 执行完，新的 Activity 的 onResume 才会执行。**onStop中可以做一些稍微重量级的回收工作，同样不能太耗时。
* 第一次启动一个 Activity : onCreate -> onStart -> onResume
* 切换到新的 Activity 或切换到桌面，即当前 Activity 处于不可见状态时：onPause -> onStop。当启动一个透明新 Activity 即当前 Activity 仍可见时只会调用 onPause 而不会调用 onStop。
* 回到原 Activity 时：onRestart -> onStart -> onResume, 原 Activity 可见时只会调用 onResume。
* 当用户按 back 键回退时，会完全退出 Activity：onPause -> onStop -> onDestroy，此时onSavcInstanceState 不会被调用。

### 1.1.2 异常情况下的生命周期分析
1. 资源相关的系统配置发生改变导致 Activity 被杀死并重新创建  
  * 比如当突然旋转屏幕导致系统配置发生变化时，Activity 就会被杀死重建，当系统配置发生变化后，Activity 会在 onStop 之前调用 onSaveInstanceState 保存 Bundle 作为对象的数据，正常情况下不会调用此方法，onSaveInstanceState 可能在 onPause 之前调用也可能在 onPause 之后调用。在重建 Activity 时，会在 onStart 之后调用 onRestoreInstanceState 来进行状态的恢复。  

  * 状态的恢复可以在 onCreate 中或者 onRestoreInstanceState 中，onCreate 中的参数 Bundle savedInstanceState 在正常启动时为空，需要进行判断，而 onRestoreInstanceState 一旦被调用，其参数 Bundle savedInstanceState 一定不为空。

2. 资源内存不足导致优先级低的 Activity 被杀死
  * Activity 优先级：前台 Activity > 可见但非前台 Activity > 后台 Activity

  * 资源内存不足时会按优先级从低到高的顺序去杀死 Activity 所在的进程，并在后续通过 onSaveInstanceState 和 onRestoreInstanceState 来存储和恢复数据。

3. 通过制定 Activity 的 configChanges 属性可以不让系统重建 Activity。如不想让 Activity 在屏幕旋转后进行重建，则设置：
`android:codfigChanges="orientation"`

## 1.2 Activity 的启动模式
---
### 1.2.1 Activity 的 LaunchMode
* standard：标准模式，系统的默认模式。每次启动启动一个 Activity 都会重新创建一个新的实例，不管这个实例是否已经存在。

* singleTop：栈顶复用模式。如果新 Activity 已经位于任务栈的栈顶，那么此 Activity 不会被重新创建，同时它的 onNewIntent 方法会被回掉，通过此方法的参数我们可以取出当前请求的信息，但如果新 Activity 的实例已存在但不是位于栈顶，那么新的 Activity仍然会重新重建。

* singleTask：栈内复用模式。这是一种单例模式，只要 Activity 在一个栈中存在，那么多次启动此 Activity 都不会重新创建实例，和 singleTop 一样，系统也会调用其 onNewIntent。如果 Activity 处于栈内但没有处于栈顶，那么将会将 Activity 切换到栈顶并调用其 onNewIntent 方法，因为 singleTask 默认具有 clearTop 的效果，Activity 之上的 Activity 都会出栈。可以通过对 MainActivity 设置 singleTask 然后退出 MainActivity 即可达到任务栈清空的目的。

* singleInstance：单实例模式，这是一种加强的 singleTask 模式，具有此种模式的 Activity 只能单独的位于一个任务栈中。

### 1.2.2 Activity 的 Flags
* FLAG_ACTIVITY_TASKL：singleTask 模式

* FLAG_ACTIVITY_SINGLE_TOP：singleTop 模式

* FLAG_ACTIVITY_CLEAR_TOP：具有此标记位的 Activity， 当它启动时，在同一个任务栈中所有位于它上面的 Activity 都要出栈。通常和 singleTask 启动模式一起出现。如果被启动的 Activity 采用 standard 模式启动，那么它连同它之上的 Activity 都要出栈，系统会创建新的 Activity 实例并放入栈顶。

* FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS：具有这个标记的 Activity 不会出现在历史 Activity 的列表中，当某些情况下我们不希望用户通过历史列表回到我们的 Activity 的时候这个标记比较有用，等同于在 XML 中设置 Activity 的属性为`android:excludeFromRecents="true"`

## 1.3 IntentFilter 的匹配规则
---
IntentFilter 过滤信息有 action、category、data，需要同时匹配过滤列表中的 action、category、data 信息，否则匹配失败。一个 Activity 中可以有多个 intent-filter，一个 Intent 只要能匹配任何一组 intent-filter 即可成功启动对应的 Activity。

  1. action 的匹配规则  
  一个过滤规则中可以有多个 action，只要 Intent 中的 action 能够和过滤规则中的任何一个 action 相同即可匹配成功。action 的匹配要求 Intent 中的 action 存在且必须和过滤规则中的其中一个 action 相同；action 区分大小写。

  2. category 的匹配规则  
  与 action 的匹配规则不同，action 要求 Intent 中必须有一个 action 且必须能够和过滤规则中的某个 action 相同，而 category 要求 Intent 中可以没有 category，但一旦有 category，不管有几个，每个都必须能够和过滤规则中的任何一个 category 相同。不设置 category 也能够匹配成功是因为 startActivity 或 startActivityForResult 的时候会默认为 Intent 加上 `android.intent.category.DEFAULT` 这个 category。若一个 Activity 能够接收隐式调用，则必须在 intent-filter 中指定 `android.intent.category.DEFAULT` 这个 category。**不含 `android.intent.category.DEFAULT` 这个 category 的 Activity 是无法接收隐式 Intent 的。**

  3. data 的匹配规则  
  如果过滤规则中定义了 data，那么 Intent 中必须也要定义可匹配的 data。如果要为 Intetn 指定完整的 data，必须调用 `setDataAndType` 方法，不能先调用 `setData` 再调用 `setType`，这两个方法会彼此清除对方的值。  

当我们通过隐式方式启动一个 Activity 的时候，可以使用 PackageManager 的 `resolverActivity` 方法或者 Intent 的 `resolverActivity` 方法进行判断是否有 Activity 能够匹配我们的隐式 Intent。PackageManager 还提供了 `queryIntentActivities` 方法，使用这个方法可以返回所有成功匹配的 Activity 信息。
