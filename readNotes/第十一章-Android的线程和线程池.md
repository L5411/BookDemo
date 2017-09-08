Android 中的线程分为主线程和子线程，主线程主要处理和界面相关的事情，子线程往往用于耗时操作，除了 Thread，Android 中能够扮演线程的还有 AsyncTask、IntentService 和 HandlerThread。

操作系统中，线程是系统调度的最小单元，同时线程又是一种受限的系统资源，线程的创建和销毁都会有相应的开销。采用线程池能够避免因线程频繁的创建和销毁线程所带来的系统开销，Android 中的线程池来源于 Java，主要通过 Executor 来派生特定类型的线程池。

## 11.1 主线程和子线程
---
主线程指进程所拥有的线程，Android 沿用了 Java 的线程模型，默认一个进程只有一个线程，这个线程就是主线程，主线程也叫 UI 线程，作用是运行四大组件以及处理它们和用户的交互，不能执行耗时任务，否则会造成界面的卡顿或者程序的崩溃；子线程则用于网络请求、I/O 操作等。在 Android 3.0 开始系统要求网络访问必须在子线程中进行，否则网络访问会失败并且抛出 NetworkOnMainThreadException 这个异常，这样是为了避免由于耗时操作阻塞主线程从而出现 ANR 现象。

## 11.2 Android 中的线程形态
---
### 11.2.1 AsyncTask
AsyncTask 是一种轻量级的异步任务，能够在执行异步任务的同时将执行进度和最终结果传递给主线程并在主线程中更新 UI，但 AsyncTask 不适合进行特别耗时的后台任务。

AsyncTask 的声明：
```
public abstract class AsyncTask<Params, Progress, Result>
```
* Params：参数的类型
* Progress：后台任务的执行进度的类型
* Result：返回结果的类型  

如果不需要参数这三个泛型参数可以用 Void 代替。

AsyncTask 四个核心方法：
1. `onPreExecute()`：在主线程中执行，在异步任务执行前被调用，一般可以用来做一些准备工作

2. `doInBackground(Params...params)`：在线程池中执行，用来执行异步任务，params 表示异步任务的输入参数，在这个方法中可以通过 `publishProgress` 方法来更新任务的进度，`publishProgress` 方法会调用 `onProgressUpdate` 方法。此方法中还需要返回计算结果给 `onPostExecute` 方法

3. `onProgressUpdate(Progress...values)`：在主线程中执行，当后台任务的执行进度发生改变时此方法会被调用，即 `publishProgress` 被调用时此方法会被调用

4. `onPostExecute(Result result)`：在主线程中执行，当异步任务执行完成后，此方法会被调用，result 是 `doInBackground` 的返回值

AsyncTask 还提供了 `onCancelled()` 方法，当异步任务被取消时，onCancelled 会被调用，这个时候 onPostExecute 不会被调用。

示例：
```
private class DownloadFilesTask extends AsyncTask<URL, Integer, Long> {

    @Override
    protected Long doInBackground(URL... urls) {
        int count = urls.length;
        long totalSize = 0;
        for (int i = 0; i < count; i++) {
            totalSize += Downloader.downloadFile(urls[i]);
            publishProgress((int) ((i / (float) count) * 100));
            if (isCancelled()) {
                break;
            }
        }
        return totalSize;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        setProgressPercent(progress[0]);
    }
    
    @Override
    protected void onPostExecute(Long result) {
        showDialog("Downloaded " + result + " bytes");
    }
   
}
```

下载任务时，可以执行：
```
new DOwnloadFilesTask().execute(url1, url2, url3);
```

AsyncTask 的使用限制：
1. AsyncTask 的类加载必须在主线程中加载，所以第一次访问 AsyncTask 必须发生在主线程。Android 5.0 中 ActivityThread 的 main 方法中会调用 AsyncTask 的 init 方法

2. AsyncTask 的对象必须在 UI 线程中调用

3. execute 方法必须在 UI 线程中调用

4. 不要在程序中直接调用 onPreExecute、onPostExecute、doInBackground 和 onProgressUpdate 方法

5. 一个 AsyncTask 对象只能执行一次，只能调用一次 execute 方法

6. Android 1.6 之前，AsyncTask 是串行执行任务的，Android 1.6 AsyncTask 开始采用线程池里处理并行任务，Android 3.0 开始 AsyncTask 有采用一个线程来串行执行任务。Android 3.0 及以后的版本，仍然可以采用 AsyncTask 的 executeOnExecutor 方法来并行的执行任务

### 11.2.2 AsyncTask 的工作原理
AsyncTask::execute -> AsyncTask::executeOnExecutor -> Executor::execute

在执行 executeOnExecutor 时，传入了 sDefaultExecutor，是一个 SerialExecutor，在 executeOnExecutor 中，先执行了 onPreExecute，然后才执行 SerialExecutor 的 execute 方法。SeriaExecutor 的 execute 方法中会将 FutureTask 添加到任务队列 mTasks 中，如果没有处于活动状态的 task，就会调用 scheduleNext 方法执行任务，task 的真正执行是在 THREAD_POOL_EXECUTOR 这个线程池中执行。当 FutureTask 执行时，run 方法会调用 mWorker 的 call 方法，call 方法将当前任务设置为已经被调用过，然后执行 doInBackground 方法，将其返回值通过 postResult 发送给 sHandler 一个 MESSAGE_POST_RESULT 类型的消息，sHandler 是一个静态变量，sHandler 必须在 UI 线程中创建才能够在此时将任务切换到 UI 线程执行，这也就是 AsyncTask 为什么第一次创建必须在 UI 线程中，sHandler 收到 MESSAGE_POST_RESULT 后会调用 AsyncTask 的 finish 方法，在 finish 中会调用 `onCancelled` 方法或者 `onPostExecute` 方法。

在 Android 3.0 及以上可以通过如下代码使 AsyncTask 并行任务：
```
new MyAsnyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
```

### 11.2.3 HandlerThread
HandlerThread 继承自 Thread，是一种能够使用 Handler 的 Thread，它的实现是在 run 方法中通过 `Looper.prepare()` 来创建消息队列，并通过 `Looper.loop()` 来开启消息循环，这样在 HandlerThread 中就能够创建 Handler 了。HandlerThread 的 run 方法是一个无限循环，当明确不需要使用 HandlerThread 时，需要使用 quit 或者 quitSafely 方法来终止线程的执行。
```
public void run() {
    mTid = Process.myTid();
    Looper.prepare();
    synchronized (this) {
        mLooper = Looper.myLooper();
        notifyAll();
    }
    Process.setThreadPriority(mPriority);
    onLooperPrepared();
    Looper.loop();
    mTid = -1;
}
```

### 11.2.4 IntentService
IntentService 继承了 Service，是一个抽象类，能够执行后台耗时的任务，当任务执行完成后悔自动停止；由于 IntentService 是一个服务，所以优先级比较高，不容易被系统杀死。

在 IntentService 第一次启动时，会调用 onCreate 方法，会创建一个 HandlerThread，使用这个 HandlerThread 的 Looper 创建一个 Handler 对象 mServiceHandler，由于 mServiceHandler 是在 HandlerThread 的线程中，所以能够用来执行耗时操作：
```
public void onCreate() {
    // TODO: It would be nice to have an option to hold a partial wakelock
    // during processing, and to have a static startService(Context, Intent)
    // method that would launch the service & hand off a wakelock.

    super.onCreate();
    HandlerThread thread = new HandlerThread("IntentService[" + mName + "]");
    thread.start();

    mServiceLooper = thread.getLooper();
    mServiceHandler = new ServiceHandler(mServiceLooper);
}
```

IntentService 每次启动时都会调用 onStartCommand，onStartCommand 会调用 onStart 方法，onStart 方法中发送了一个 msg 给 mServiceHandler：
```
public void onStart(@Nullable Intent intent, int startId) {
    Message msg = mServiceHandler.obtainMessage();
    msg.arg1 = startId;
    msg.obj = intent;
    mServiceHandler.sendMessage(msg);
}
```

在 ServiceHandler 的 handleMessage 方法中会调用 `onHandleIntent` 方法来处理 Intent 中的参数：
```
@Override
public void handleMessage(Message msg) {
    onHandleIntent((Intent)msg.obj);
    stopSelf(msg.arg1);
}
```
处理完成后还会尝试停止 service，而 onHandleIntent 则是需要我们实现的抽象方法，在 onHandleIntent 中能够进行耗时操作。

## 11.3 Android 中的线程池
---
线程池的优点：
1. 重用线程池中的线程，避免因为线程的创建和销毁所带来的性能开销

2. 能有效控制线程池的最大并发数，避免大量的线程之间因互相抢占系统资源而导致阻塞现象

3. 能够对线程进行简单的管理，并提供定时执行以及指定间隔循环等功能

线程池的概念来源于 Java 中的 Executor 接口，真正的实现为 ThreadPoolExecutor，Android 中的线程池都是直接或者间接通过配置 ThreadPoolExecutor 来实现的。

### 11.3.1 ThreadPoolExecutor
ThreadPoolExecutor 比较常用的构造函数：
```
public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          ThreadFactory threadFactory)
```

* corePoolSize：线程池的核心线程数，默认情况下，线程核心会在线程池中一直存活，即使它们处于闲置状态。如果将ThreadPoolExecutor 的 `allowCoreThreadTimeOut` 属性设置为 true，那么闲置的核心线程在等待新任务到来时会有超时策略，这个时间由 keepAliveTime 指定，当等待时间超过 keepAliveTime 时核心线程将会被终止。

* maximumPoolSize：线程池所能容纳的最大线程数，当活动线程数达到这个数值后，后续的新任务将会被阻塞。

* keepAliveTime：非核心线程闲置时的超时时长，超过这个时长，非核心线程就会被回收。

* unit：用于指定 keepAiveTime 参数的时间单位，是一个枚举，常用的有 TimeUnit.MILLISECONDS（毫秒）、TimeUnit.SECONDS（秒）以及 TimeUnit.MINUTES（分钟）等。

* workQueue：线程池中的任务队列，通过线程池的 execute 方法提交的 Runnable 对象会存储在这个参数中。

* threadFactory：线程工厂，为线程池提供创建新线程的功能。ThreadFactory 是一个接口，它只有一个方法：`Thread newThread(Runnable r)`

ThreadPoolExecutor 还有一个不常用的参数 RejectedExecutionHandler handler。当线程无法执行新任务时，ThreadPoolExecuter 会调用 handler 的 rejectedExecution 方法抛出一个 RejectedExecutionException 来通知调用者。

ThreadPoolExecutor 执行任务时遵循的规则:
1. 如果线程池中的线程数量未达到核心线程的数量，那么会直接启动一个核心线程来执行任务

2. 如果线程池中的线程数量已经达到或者超过核心线程的数量，那么任务会被插入到任务队列中排队等待执行

3. 如果在步骤 2 中无法将任务插入到任务队列中，这往往是由于任务队列已满，这个时候如果线程数量未达到线程池的规定的最大值，那么会立刻启动一个非核心线程来执行任务

4. 如果步骤 3 中线程数量已经达到线程池规定的最大值，那么就拒绝执行此任务，ThreadPoolExecutor 会调用 RejectedExecutionHandler 的 rejectedExecution 方法来通知调用者

### 11.3.2 线程池的分类

Android 中最常见的四类具有不同功能特性的线程池，都直接或间接的通过配置 ThreadPoolExecutor 来实现自己的功能特性，分别是 FixedThreadPool、CachedThreadPool、ScheduledThreadPool 和 SingleThreadExecutor。

1. FixedThreadPool：通过 Executors 的 `newFixedThreadPool` 方法来创建，这种线程池的线程数量固定且全部为核心线程，不会被回收，任务队列大小没有限制，没有超时机制。
```
public static ExecutorService newFixedThreadPool(int nThreads) {
    return new ThreadPoolExecutor(nThreads, nThreads,
                                  0L, TimeUnit.MILLISECONDS,
                                  new LinkedBlockingQueue<Runnable>());
}
```

2. CachedThreadPool：通过 Executors 的 `newCachedThreadPool` 方法来创建，线程数量不定，只有非核心线程，且最大线程数为 Intengr.MAX_VALUE。空闲线程超为 60s，CachedThreadPool 的任务队列相当于一个空集合，是一个无法存储元素的队列，所以任何任务都会被立即执行，当线程池中的线程都处于闲置状态时，会因为超时被停止，此时 CachedThreadPool 几乎不占用系统资源。
```
public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                  60L, TimeUnit.SECONDS,
                                  new SynchronousQueue<Runnable>());
}
```

3. ScheduledThreadPool：通过 Executors 的 `newScheduledThreadPool` 方法来创建，它的核心线程数量固定，非核心线程数没有限制，并且非核心线程闲置时会立被立即回收。
```
    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize);
    }

    public ScheduledThreadPoolExecutor(int corePoolSize) {
        super(corePoolSize, Integer.MAX_VALUE,
              DEFAULT_KEEPALIVE_MILLIS, MILLISECONDS,
              new DelayedWorkQueue());
    }
```

4. SingleThreadExecutor：通过 Executors 的 newSingleThreadExecutor 方法来创建。这个线程池内部只有一个核心线程，确保所有的任务都在同一个线程中按顺序执行。SingleThreadExecutor 的意义在于统一所有的外界任务到一个线程中，使得这些任务不需要处理线程同步的问题。
```
public static ExecutorService newSingleThreadExecutor() {
    return new FinalizableDelegatedExecutorService
        (new ThreadPoolExecutor(1, 1,
                                0L, TimeUnit.MILLISECONDS,
                                new LinkedBlockingQueue<Runnable>()));
}
```

四种线程池的典型使用方法：
```
Runnable command = new Runnable() {
    @Override
    public void run() {
        SystemClock.sleep(2000);
    }   
}

ExecutorService fixedThreadPool = Executors.newFixedThreadPool(4);
fixedThreadPool.execute(command);

ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
cachedThreadPool.execute(command);

ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(4);
// 2000ms 后执行 command
scheduledThreadPool.schedule(command, 2000, TimeUnit.MILLISECONDS);
// 延迟 10ms 后，每 1000ms 执行一次 command
scheduledThreadPool.scheduleAtFixedRate(command, 10, 1000, TimeUnit.MILLISECONDS);

ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
singleThreadExecutor.execute(command);
```