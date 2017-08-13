## 2.1 Android IPC 简介
---
IPC 是 Inter-Process Communication 的缩写，含义为**进程间通信**或者**跨进程通信**。
* 线程是 CPU 调度的最小单元，同时线程是一种有限的系统资源。  
* 进程一般指一个执行单元，在 PC 和移动设备上指一个程序或者一个应用。  
* 一个进程可以包含多个线程，因此进程和线程是包含于被包含的关系。
* ANR: Application Not Responding 应用无响应

## 2.2 Android 中的多进程模式
---
### 2.2.1 开启多进程模式
在 Android 中使用多进程只有一种方法，那就是给四大组件在 AndroidMenifest 中指定 `android:process`属性，除此以外没有其他办法（通过 JNI 在 ntive 层去 fork 一个新的进程不予考虑）。  

进程名以 “:” 开头的进程属于当前应用的私有进程，其他应用的组件不可以和它跑在同一个进程中，而进程名不以 “:” 开头的进程属于全局进程，其他应用通过 ShareUID 方式可以和它跑在同一个进程中。

### 2.2.2 多进程模式的运行机制
所有运行在不同进程中的四大组件，只要它们之间需要通过内存来共享数据，都会共享失败，这也是多进程所带来的主要影响。

多进程造成的问题：
1. 静态成员和单例模式完全失效。
2. 线程同步机制完全失效。
3. SharedPreferences 的可靠性下降，SharedPreferences 不支持两个进程同时去执行写操作，否则会导致一定几率的数据丢失。
4. Application 会多次创建，当一个组件跑在一个新的进程中的时候，由于系统要在创建新的进程同时分配独立的虚拟机，所以这个过程其实就是启动一个应用的过程，那么自然会创建新的 Application。

实现跨进程通信的方式：
1. Intent 传递数据
2. 共享文件和 SharedPreferences
3. 基于 Binder 的Messenger 和 AIDL 以及 Socket 等

## 2.3 IPC 基础概念介绍
---
主要介绍 Serializable 接口、 Parcelable 接口以及 Binder

### 2.3.1 Serializable 接口
实现一个对象的序列化只需要实现 Serializable 接口并声明一个 serialVersionUID：
```
public class User implements Serializable {
  private static final long serialVersionUID = 519067123721295774L;

  public int userId;
  public String userName;
  public boolean isMale;
  ...
}
```
进行对象的序列化和反序列化也非常简单，只需要采用 ObjectOutputStream 和 ObjectInputStream 即可：
```
// 序列化
User user = new User(0, "jack", true);
ObjectOutputStream out = new ObjectOutputStream(
  new FileOutputStream("cache.txt")
  );
out.writeObject(user);
out.close;

// 反序列化
ObjectInputStream input = new ObjectInputStream(
  new FileInputStream("cache.txt")
  );
User newUser = (User) input.readObject();
input.close;
```
恢复后的对象和原本对象内容完全一样，但两者并不是同一个对象。原则上序列化后的数据中的 serialVersionUID 只有和当前类的 serialVersionUID 相同才能够正常地被反序列化。
### 2.3.2 Parcelable 接口
实现了 Parcelable 接口，一个类的对象就可以实现序列化并可以通过 Intent 和 Binder 传递：
```
public class User implements Parcelable {

    public int userId;
    public String userName;
    public boolean isMale;

    public Book book;

    public User(int userId, String useName, boolean isMale) {
        this.userId = userId;
        this.userName = useName;
        this.isMale = isMale;
    }

    /**
     * 返回当前对象的内容描述
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * 进行序列化功能
     */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(userId);
        out.writeString(userName);
        out.writeInt(isMale ? 0 : 1);
        out.writeParcelable(book, 0);
    }

    private User(Parcel in) {
        userId = in.readInt();
        userName = in.readString();
        isMale = in.readInt() == 1;
        // 由于 book 是另一个可序列化对象，所以它的反序列化过程需要传递当前线程的上下文类加载器
        book = in.readParcelable(Thread.currentThread().getContextClassLoader());
    }

    /**
     * 进行反序列化功能
     */
    public static final Creator<User> CREATOR = new Creator<User>() {
        /**
         * 从序列化后的对象中创建原始对象
         */
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        /**
         * 创建指定长度的原始对象数组
         */
        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

}
```
Parcelable 是 Android 中的序列化方式，使用起来麻烦，但使用效率很高，首选 Parcelable。将对象序列化到存储设备中或者将对象序列化后通过网络传输时推荐使用 Serializable。
### 2.3.3 Binder
Binder 实现了 IBinder 接口， 从 Android 应用层来说， Binder 是客户端和服务器进行通信的媒介。  
所有可以在 Binder 中传输的接口都需要继承 IInterface 接口。

* **DESCRIPTOR**  
Binder 的唯一标识，一般用当前 Binder 的类名表示。
* **asInterface(android.os.IBinder obj)**  
用于将服务端的 Binder 对象转换成客户端所需的 AIDL 接口类型的对象，这种转换过程是区分进程的，如果客户端和服务端位于同一进程，那么此方法返回服务端的 Stub 对象本身，否则返回的是系统封装后的 Stub.proxy 对象。
* **asBinder**  
此方法用于返回当前 Binder 对象
* **onTransact**  
运行于服务端中的 Binder 线程池中，当客户端发起跨进程请求时，远程请求会通过系统底层封装后交由此方法来处理。

## 2.4 Android 中的 IPC 方式
---
### 2.4.1 使用 Bundle
当我们在一个进程中启动了另一个进程的 Activity、Service 和 Receiver，我们就可以在 Bundle 中附加我们需要传输给远程进程的信息并通过 Intent 发送出去。  
特殊场景使用：A 进程正在进行一个计算，计算完成后要启动 B 进程的一个组件并把计算结果传递给 B 进程，可计算结果不支持放入 Bundle 中，因此无法通过 Intent 传输，这是使用其他 IPC 方式就会略显复杂。  
解决方案：通过 Intent 启动进程 B 的一个 Service 组件，让 Service 在后台进行计算，计算完成后再启动 B 进程中真正要启动的目标组件，由于 Service 也运行在 B 进程中，所以目标组建就可以直接获取计算结果。
### 2.4.2 使用文件共享
我们可以序列化一个对象到文件系统的同时从另一个进程中恢复这个对象，恢复的对象虽然内容相同，但本质上还是两个对象。SharedPreferences 也属于文件的一种，但是由于系统对它的读/写有一定的缓存策略，即在内存中会有一份 SharedPreferences 文件的缓存，因此在多进程下系统对他的读/写就变得不可靠，当面对高并发的读/写访问 Sharedprefernences 有很大几率会丢失数据。
### 2.4.3 使用 Messenger
Messenger 可以翻译为信使，是一种轻量级的 IPC 方案，其底层实现是 AIDL，对 AIDL 做了封装。
1. 服务端进程  
服务端创建一个 Service 来处理客户端的连接请求，同时创建一个 Handler 并通过它来创建一个 Messenger 对象，在 Service 的 onBind 中返回这个 Messenger 对象底层的 Binder 即可。
```
  public class MessengerService extends Service {

      private static final String TAG = MessengerService.class.getSimpleName();

      public static final int MSG_FROM_CLIENT = 0;
      public static final int MSG_FROM_SERVICE = 1;

      private static class MessengerHandler extends Handler {
          @Override
          public void handleMessage(Message msg) {
              switch (msg.what) {
                  case MSG_FROM_CLIENT:
                      Log.i(TAG, "receive msg from Client:" + msg.getData().getString("msg"));
                      // 接收 replyTo，用于回复信息
                      Messenger client = msg.replyTo;
                      Message replyMessage = Message.obtain(null, MSG_FROM_SERVICE);
                      Bundle args = new Bundle();
                      args.putString("reply", "已收到信息");
                      replyMessage.setData(args);
                      try {
                          client.send(replyMessage);
                      } catch (RemoteException e) {
                          e.printStackTrace();
                      }
                      break;
                  default:
                      super.handleMessage(msg);
              }
              super.handleMessage(msg);
          }
      }

      private final Messenger mMessenger = new Messenger(new MessengerHandler());

      @Nullable
      @Override
      public IBinder onBind(Intent intent) {
          return mMessenger.getBinder();
      }
  }
```
2. 客户端程序  
客户端进程中，首先绑定服务端 Service， 绑定成功后用服务端返回的 IBinder 对象创建一个 Messenger，通过这个 Messenger 就可以向服务端发送消息，消息类型为 Message 对象。  
如果需要服务端能够回应客户端，那么客户端也要创建一个 Handler 并创建一个新的 Messenger，通过 Message 的 replyTo 参数传递给服务端，服务端通过这个 replyTo 参数就能回应客户端。
```
  public class MessengerActivity extends AppCompatActivity {

      private static final String TAG = MessengerActivity.class.getSimpleName();

      public static Intent newIntent(Context context) {
          return new Intent(context, MessengerActivity.class);
      }

      private Messenger mService;

      private Messenger mGetReplyMessenger = new Messenger(new MessengerHandler());

      private static class MessengerHandler extends Handler {
          @Override
          public void handleMessage(Message msg) {
              switch (msg.what) {
                  case MessengerService.MSG_FROM_SERVICE:
                      Log.d(TAG, "receive msg from Service:" + msg.getData().getString("reply"));
                      break;
              }
              super.handleMessage(msg);
          }
      }

      private ServiceConnection mConnection = new ServiceConnection() {
          @Override
          public void onServiceConnected(ComponentName name, IBinder service) {
              mService = new Messenger(service);
              Message msg = Message.obtain(null, MessengerService.MSG_FROM_CLIENT);
              Bundle data = new Bundle();
              data.putString("msg", "Hello, this is client.");
              msg.setData(data);
              // 添加 replyTo 用于接收返回参数
              msg.replyTo = mGetReplyMessenger;
              try {
                  mService.send(msg);
              } catch (RemoteException e) {
                  e.printStackTrace();
              }
          }

          @Override
          public void onServiceDisconnected(ComponentName name) {
          }
      };

      @Override
      protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_messenger);
          Intent intent = new Intent(this, MessengerService.class);
          bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
      }

      @Override
      protected void onDestroy() {
          unbindService(mConnection);
          super.onDestroy();
      }
  }
```

### 2.4.4 使用 AIDL
AIDL 能够处理大量的并发请求，能够实现跨进程的方法调用。
1. 服务端  
服务端要创建一个 Service 来监听客户端的连接请求，然后创建一个 AIDL 文件，将暴露给客户端的接口在这个 AIDL 文件中声明，最后在 Service 中实现这个 AIDL接口。  
2. 客户端  
首先绑定服务端的 Service，绑定成功后，将服务端返回的 Binder 对象转成 AIDL 接口所属的类型，接着就可以调用 AIDL 中的方法。  
3. AIDL 接口的创建  
```  
  // IBookManager.aidl
  package com.l_5411.bookdemo.chapter_2.aidl;

  import com.l_5411.bookdemo.chapter_2.aidl.Book;

  interface IBookManager {
      List<Book> getBookList();
      void addBook(in Book book);
  }  
```  
在 AIDL 中，仅支持以下数据类型：
  * Java 编程语言中的使所有原语类型（如 int、long、char、boolean 等等）；
  * String；
  * CharSequence；
  * List：只支持 ArrayList，里面的每个元素都必须能够被 AIDL 支持；
  * Map：只支持 HashMap，里面的每个元素都必须被 AIDL 支持，包括 key 和 value；
  * Parcelable：所有实现了 Parcelable 接口的对象；
  * AIDL：所有的 AIDL 接口本身也可以在 AIDL 文件中使用。  

  自定义的 Parcelable 对象和 AIDL 对象必须要显式 import 进来。同时用到的自定义 Parcelable 对象，必须为其新建一个和它同名的 AIDL 文件，并在其中声明它为 Parcelable 类型：
  ```
  // Book.aidl
  package com.l_5411.bookdemo.chapter_2.aidl;

  parcelable Book;
  ```  
  AIDL 中除了基本数据类型，其他类型的参数必须标上方向：in、out 或者 inout。AIDL 中只支持方法，不支持静态常量。
4. 远程服务端 Service 的实现
```
  /**
   * 远程服务端 Service
   * Created by L_5411 on 2017/8/1.
   */

  public class BookManagerService extends Service {

      private static final String TAG = "BMS";

      private AtomicBoolean mIsServiceDestoryed = new AtomicBoolean(false);

      // CopyOnWriteArrayList 支持并发读写
      private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<>();

      private RemoteCallbackList<IOnNewBookArrivedListener> mListenerList
              = new RemoteCallbackList<>();

      private Binder mBinder = new IBookManager.Stub() {
          @Override
          public List<Book> getBookList() throws RemoteException {
              return mBookList;
          }

          @Override
          public void addBook(Book book) throws RemoteException {
              mBookList.add(book);
          }

          @Override
          public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
              mListenerList.register(listener);
          }

          @Override
          public void unRegisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
              mListenerList.unregister(listener);
          }
      };

      @Override
      public void onCreate() {
          super.onCreate();
          // 初始化图书信息
          mBookList.add(new Book(1, "Android"));
          mBookList.add(new Book(2, "IOS"));
          new Thread(new ServiceWorker()).start();
      }

      @Nullable
      @Override
      public IBinder onBind(Intent intent) {
          // 返回 Binder
          return mBinder;
      }

      @Override
      public void onDestroy() {
          mIsServiceDestoryed.set(true);
          super.onDestroy();
      }

      private void onNewBookArrived(Book book) throws RemoteException {
          mBookList.add(book);
          // 遍历 RemoteCallbackList
          final int N = mListenerList.beginBroadcast();
          for(int i = 0 ; i < N; i++) {
              IOnNewBookArrivedListener listener = mListenerList.getBroadcastItem(i);
              if(listener != null) {
                  listener.onNewBookArrived(book);
              }
          }
          mListenerList.finishBroadcast();
      }

      private class ServiceWorker implements Runnable {
          @Override
          public void run() {
              while (!mIsServiceDestoryed.get()) {
                  try {
                      Thread.sleep(5000);
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                  }
                  int bookId = mBookList.size() + 1;
                  Book book = new Book(bookId, "new book#" + bookId);
                  try {
                      onNewBookArrived(book);
                  } catch (RemoteException e) {
                      e.printStackTrace();
                  }
              }
          }
      }
  }
```
RemoteCallbackList 是系统专门提供的用于删除跨进程 listener 的接口，当客户端进程终止后，它能够自动移除客户端所注册的 listener。

5. 客户端的实现
```
  public class BookManagerActivity extends AppCompatActivity {

      private static final String TAG = BookManagerActivity.class.getSimpleName();

      private static final int MESSAGE_NEW_BOOK_ARRIVED = 1;

      private IBookManager mRemoteBookManager;

      private Handler mHandler = new Handler() {
          @Override
          public void handleMessage(Message msg) {
              switch (msg.what) {
                  case MESSAGE_NEW_BOOK_ARRIVED:
                      Log.d(TAG, "receive new book :" + msg.obj);
                      break;
                  default:
                      break;
              }
          }
      };

      private IOnNewBookArrivedListener mOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub() {
          @Override
          public void onNewBookArrived(Book newBook) throws RemoteException {、
              // 运行在客户端的 Binder 线程池中，不能访问 UI 相关的内容
              mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED, newBook).sendToTarget();
          }
      };

      private ServiceConnection mConnection = new ServiceConnection() {
          @Override
          public void onServiceConnected(ComponentName name, IBinder service) {
              IBookManager bookManager = IBookManager.Stub.asInterface(service);
              try {
                  mRemoteBookManager = bookManager;
                  List<Book> list = bookManager.getBookList();
                  Log.d(TAG, "query book list:" + list.toString());
                  Book newBook = new Book(3, "Android 开发艺术探索");
                  bookManager.addBook(newBook);
                  List<Book> newList = bookManager.getBookList();
                  Log.d(TAG, "query new book list:" + newList.toString());
                  bookManager.registerListener(mOnNewBookArrivedListener);
              } catch (RemoteException e) {
                  e.printStackTrace();
              }
          }

          @Override
          public void onServiceDisconnected(ComponentName name) {
              mRemoteBookManager = null;
              Log.d(TAG, "binder died");
          }
      };

      public static Intent newIntent(Context context) {
          return new Intent(context, BookManagerActivity.class);
      }

      @Override
      protected void onCreate(Bundle savedInstanceState) {
          super.onCreate(savedInstanceState);
          setContentView(R.layout.activity_book_manager);
          Intent intent = new Intent(this, BookManagerService.class);
          bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
      }

      @Override
      protected void onDestroy() {
          if (mRemoteBookManager != null && mRemoteBookManager.asBinder().isBinderAlive()) {
              try {
                  Log.d(TAG, "unregister listener:" + mOnNewBookArrivedListener);
                  mRemoteBookManager.unRegisterListener(mOnNewBookArrivedListener);
              } catch (RemoteException e) {
                  e.printStackTrace();
              }
          }
          unbindService(mConnection);
          super.onDestroy();
      }
  }
```
客户端调用服务端方法时，如果服务端方法执行起来比较耗时，就会导致客户端线程长时间阻塞在这里，我们应避免在 UI 线程中去访问远程方法。服务端方法本身就运行在服务端的 Binder 线程池中，能够执行大量耗时操作，不用在服务端方法中开线程进行异步任务。  
6. 当 Binder 意外死亡时，往往由于服务端进程意外停止，需要重新连接服务。
  * 给 Binder 设置 DeathRecipient 建通，当 Binder 死亡时，我们会收到 binderDied 方法的回调，在这个方法中重连远程服务。在客户端的 Binder 线程池中被回调。
  * 在 onServiceDisconnected 中重连远程服务。在客户端 UI 线程中被回调。  
7. AIDL 权限验证  
  * 在 onBind 中进行验证，验证不通过直接返回 null，首先在 AndroidMenifest 中声明所需权限，比如：
  ```
  <permission android:name="com.l_5411.bookdemo.chapter_2.aidl.permission.ACCESS_BOOK_SERVICE"
          android:protectionLevel="normal" />
  ```  
  然后在 BookManagerService 的 onBind 方法中做权限验证。
  ```
  public IBinder onBind(Intent intent) {
        // 返回 Binder
        int check = checkCallingOrSelfPermission(
                "com.l_5411.bookdemo.chapter_2.aidl.permission.ACCESS_BOOK_SERVICE");
        if (check == PackageManager.PERMISSION_DENIED) {
            return null;
        }
        return mBinder;
    }
  ```  
  若需要绑定服务，只需要在 AndroidMenifest 中申请权限：
  ```
  <uses-permission android:name="com.l_5411.bookdemo.chapter_2.aidl.permission.ACCESS_BOOK_SERVICE" />
  ```  
  * 在服务端的 onTransact 方法中进行权限验证，可以使用 premission 验证，也可以采用 Uid 和 Pid 进行验证。验证失败直接返回 false 即可。  

### 2.4.5 使用 ContentProvider
系统预置了许多 ContentProvider，如通讯录信息、日程表信息等，要访问这些信息只需通过 ContentResolver 的 query、update、insert 和 delete方法即可。创建自定义 ContentProvider 需要实现六个抽象方法：
* onCreate：代表 ContentProvider 的创建，一般需要做好一些初始化工作，只有这个方法由系统回调并运行在主线程中，其它五个均由外界回掉并运行在 Binder 线程池中；
* getType：返回一个 Uri 请求所对应的 MIME 类型（媒体类型），比如图片、视频等，如果不关注这个选项，可以直接返回 null 或者“*/*”；
* insert、delete、update、query：对应增删改查操作。

ContentProvider 需要在 AndroidManifest 中进行注册：
```
<provider
    android:authorities="com.l_5411.bookdemo.chapter_2.provider"
    android:name=".chapter_2.provider.BookProvider"
    android:permission="com.l_5411.bookdemo.PROVIDER"
    android:process=":provider" />
``` 
`android:authorities` 是 ContentProvider 的唯一标识，外部应用通过这个属性访问此 provider，因此 `android:authorities` 必须是唯一的，建议加上包名前缀；`android:permission` 声明权限，外界程序必须声明权限才可访问此 provider。

ContentProvider 通过 Uri 来区分外界要访问的数据集合，通过将 Uri 和 Uri_Code 关联到一起从而得到所要访问的表。当执行增删改查导致数据源发生改变时，需要通过 ContentResolver 的 `notifyChange` 来通知外界当前 ContentProvider 中的数据发生改变。要观察一个 ContentProvider 可以通过 ContentResolver 的 `registerContentObserver` 方法来注册观察者，通过 `unregisterContentObserver` 方法来解除观察者。

***query、update、insert、delet 四大方法存在多线程并发访问，需要在方法内部做好线程同步。***

### 2.4.6 使用 Socket
Socket 也称为“套接字”，分为**流式套接字**和**用户数据报套接字**，分别对应于网络的传输控制层中的 TCP 和 UDP 协议。
* TCP：面向连接的协议，提供稳定的双向通信功能，TCP 连接的建立需经过“三次握手”才能完成，其本身提供了超时重传机制，基友很高的稳定性
* UDP：无连接的协议，提供了不稳定的单向通信功能，性能上 UDP 拥有更好的效率，但不能够保证数据一定能正确传输。

使用 Socket 需要注意两点：
1. 声明权限：
```
  <uses-permission android:name="android.permission.INTERNET" />
  <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```
2. **不能在主线程中访问网络，在 Android 4.0及以上的设备中会抛出异常：`android.os.NetworkOnMainThreadException` 而且进行网络操作很可能是耗时的，放在主线程中会影响效率**

## 2.5 Binder 连接池

AIDL 使用流程：首先创建一个 Service 和一个 AIDL 接口，接着创建一个类继承自 AIDL 接口中的 Stub 类并实现 Stub 中的抽象方法，在 Service 的 onBind 方法中返回这个类的对象，然后客户端就可以绑定服务端 Service，建立连接后就可以访问远程服务端的方法了。

当 AIDL 数量过多时，需要减少 Service 的数量，将所有的 AIDL 放在同一个 Service 中去管理。每个业务模块创建自己的 AIDL 接口并实现此接口，服务端只需一个 Service 提供一个 queryBinder 接口，根据 binderCode 返回不同的 Binder 对象给客户端。

---

## 2.6 选用合适的 IPC 方式
---
ICP 方式的优缺点和适用场景  

| 名称 | 优点 | 缺点 | 适用场景 |
| ------| ------ | ------ | ------ |
| Bundle | 简单易用 | 只能传输 Bundle 支持的数据 | 四大组件的进程间通信 |
| 文件共享 | 简单易用 | 不适合高并发场景，并且无法做到进程间的即使通信 | 无并发访问情形，交换简单的数据实时性不高的场景 |
| AIDL | 功能强大，支持一对多并发通信，支持实时通信 | 使用稍微复杂，需要处理好线程同步 | 一对多通信且有 RPC 需求 |
| Messenger | 功能一般，支持一对多串行通信，支持实时通信 | 不能很好处理高并发情形，不支持 RPC，数据通过 Message 进行传输，因此只能传输 Bundle 支持的数据类型 | 低并发的一对多即时通信，无 RPC 需求，或者无须要返回结果的 RPC 需求 |
| ContentProvider | 在数据源访问方面功能强大，支持一对多并发数据共享，可通过 call 方法扩展其他操作 | 可以理解为受约束的 AIDL，主要提供数据源的 CRUD 操作 | 一对多的进程间的数据共享 |
| Socket | 功能强大，可用过网络传输字节流，支持一对多并发实时同行 | 实现细节稍微有点繁琐，不支持直接的 RPC | 网络数据交换 |
