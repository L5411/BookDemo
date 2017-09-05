Android 的消息机制主要是指 Handler 的运行机制， Handler 的运行需要底层的 MessageQueue 和 Looper 的支撑。MessageQueue 即消息队列，采用单链表的数据结构来存储消息列表，Looper 即消息循环，MessageQueue 只是一个消息的存储单元，不能够去处理消息，而 Looper 就填补了这个功能，Looper 会以无限循环的形式去查找是否有新的消息，如果有就去处理，否则就一直等待。Looper 中还存在一个 ThreadLocal，ThreadLocal 用于在每个线程存储数据，在 Handler 的内部通过 ThreadLocal 获取当前线程的 Looper，ThreadLocal 可以在不同的线程中互不干扰的存储并提供数据。

## 10.1 Android 的消息机制概述
---
Handler 的主要作用是将一个任务切换到某个指定的线程中执行。Handler 创建完毕以后，可以通过 Handler 的 post 方法将一个 Runnable 投递到 Handler 的内部的 Looper 中去处理，也可以通过 send 方法发送一个消息，这个消息同样会在 Looper 中进行处理。post 方法最终也是通过 send 方法来完成的。当 Lopper 发现有新消息到来时，会处理这个消息，Looper 运行在 Handler 所在的线程中，这样 Handler 中的业务逻辑就被切换到 Handler 所在的线程中去执行了。

## 10.2 Android 的消息机制分析
---
### 10.2.1 ThreadLocal 的工作原理
ThreadLocal 是一个线程内部的数据存储类，通过它可以在指定的线程中存储数据，数据存储后，只有在指定线程中才可以获取到存储的数据，其他线程则无法获取到指定线程的数据。

ThreadLocal 实例：
```
private ThreadLocal<Boolean> mBooleanThreadLocal = new ThreadLocal<>();

mBooleanThreadLocal.set(true);
Log.d(TAG, "[Thread#main]mBooleanThreadLocal = " + mBooleanThreadLocal.get());

new Thread("Thread #1") {
    @Override
    public void run() {
        mBooleanThreadLocal.set(false);
        Log.d(TAG, "[Thread#1]mBooleanThreadLocal = " + mBooleanThreadLocal.get());
    }   
}

new Thread("Thread #2") {
    @Override
    public void run() {
        Log.d(TAG, "[Thread#2]mBooleanThreadLocal = " + mBooleanThreadLocal.get());
    }   
}
```
最终输出结果为：
```
[Thread#main]mBooleanThreadLocal = true
[Thread#1]mBooleanThreadLocal = false
[Thread#2]mBooleanThreadLocal = null
```
只要弄清楚 ThreadLocal 的 set 和 get 方法就能弄清楚 ThreadLocal 的工作原理。

ThreadLocal 的 set 方法：
```
public void set(T value) {
    Thread currentThread = Thread.currentThread();
    Values values = values(currentThread);
    if (values == null) {
        values = initializeValues(currentThread);
    }
    values.put(this, value);
}
```
通过 values 方法获取当前线程的 ThreadLocal 数据，ThreadLocal 中有一个 localValues 成员专门用于存储线程的 ThreadLocal 的数据，Values 类中有一个 `private Object[] table`，ThreadLoacl 的值就存在这个 table 中，通过 Values 的 put 方法将 ThreadLocal 值放入 table 中。table 中的存储规则为 `table[index] = key.reference; table[index + 1] = value;` 其中 key 为 ThreadLocal，所以值总是存在 reference 的下一个位置。

get 方法获取到 values.table，接着获取到 ThreadLocal 的 reference 对象在 table 中的位置，返回 `talbe[index + 1]`

### 10.2.2 消息队列的工作原理
MessageQueue 主要包含两个操作：enqueueMessage 和 next，enqueueMessage 用于往消息队列中插入一条消息，插入的位置按 when 进行排列；而 next 的作用是从消息队列中取出一条消息并将其从消息队列中移除，next 是一个无限循环的方法，如果消息队列中没有消息，那么 next 会阻塞在这里直到新的消息到来。

### 10.2.3 Looper 的工作原理
Looper 在构造方法中创建了一个 MessageQueue，并将当前线程保存起来：
```
private Looper(boolean quitAllowed) {
    mQueue = new MessageQueue(quitAllowed);
    mThread = Thread.currentThread();
}
```

创建一个 Lopper：
```
new Thread("Thread#2") {
    @Override
    public void run() {
        Looper.prepare();
        Handler handler = new Handler();
        Looper.loop();
    }
}
```

Looper 提供了 prepareMainLooper 方法来给主线程 ActivityThread 创建 Looper，可以通过 getMainLooper 在任何地方获取到主线程的 Looper。

Looper 也是可以退出的，有 quit 和 quitSafely 两个方法， quit 会直接退出 Looper，quitSafely 会设定一个退出标记，在消息队列中的已有消息处理完毕后会安全退出，Looper 退出后，Handler 的 send 方法会返回 false，子线程中如果手动创建了 Looper，那么在所有消息处理完成后应调用 quit 方法终止消息循环，否则子线程会一直处于等待状态。

Looper 最重要的方法是 loop 方法，只有调用了 loop 方法，Looper 才会真正的起作用。loop 中是一个死循环，只有 MessageQueue 的 next 方法返回 null 才会退出循环，只有当 Looper 的 quit 方法被调用时，Looper 会通知 MessageQueue 的 quit 或者 quitSafely 方法来通知消息队列退出，当消息队列被标记为退出状态时，next 方法才会返回 null。当 MessageQueue 没有被标记为退出状态而又没有消息时，next 会阻塞在那里，不会返回 null。当 next 返回了新消息时，通过 `msg.target.dispatchMessage(msg)` 来处理消息，msg.target 是发送 msg 的 Handler，所以最终 msg 的处理回到了 Handler 所在的线程中去执行。

### 10.2.4 Handler 的工作原理
Handler 的消息发送的过程可以通过 post 的一系列方法和 send 的一系列方法来实现，post 系列方法最终也是通过 send 来实现的。send 的过程是向 MessageQueue 中插入一条消息，MessageQueue 的 next 方法会取出消息交由 Looper 处理，最终 Looper 再将消息交由 Handler 处理，此时 Handler 的 dispatchMessage 方法会被调用：
```
public void dispatchMessage(Message msg) {
    if (msg.callback != null) {
        handleCallback(msg);
    } else {
        if (mCallback != null) {
            if (mCallback.handleMessage(msg)) {
                return;
            }
        }
        handleMessage(msg);
    
}
```
首先检查 Message 的 callback 是否为 null，不为 null 就通过 `handleCallback` 处理，在 `handleCallback` 中会调用 `mssage.callback.run()`，接下来检查 mCallback 是否为 null，不为空就调用 mCallback 的 handleMessage 方法，如果 mCallback 为 null，则调用 Handler 的 handleMessage 方法，这个方法默认实现为：
```
public void handleMessage(Message msg) {
}
```

## 10.3 主线程的消息循环
---
Android 的主线程是 ActivityThread，主线程的入口为 main 方法，在 main 方法中通关过 Looper.prepareMainLooper() 创建主线程 Looper 以及 MessageQueue，并通过 Looper.loop() 来开启主线程的消息循环。主线程还需要一个 Handler 来和消息队列进行交互，这个 Handler 就是 ActivityThread.H，内部定义了一组消息类型，主要为四大组件的启动和停止等过程。

ActivityThread 通过 ApplicationThread 和 AMS 进行进程间通信，AMS 以进程间通信的方式完成 ActivityThread 的请求后回调 ApplicationThread 中的 Binder 方法，然后 ApplicationThread 会向 H 发送消息， H 收到消息后会将 ApplicationThread 中的逻辑切换到 ActivityThread 中去执行，即切换到主线程中去执行。