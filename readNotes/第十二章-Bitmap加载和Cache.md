由于 Android 对单个应用所施加的内存限制，会导致加载 Bitmap 时很容易出现内存溢出：
```
java.lang.OutofMemoryError: bitmap size exceeds VM budget
```

实际的开发中经常需要使用 Bitmap 做缓存，通过缓存策略，不需要每次都从网络上请求图片或者从存储设备中加载图片，这样就大大提高了图片的加载效率以及产品的用户体验。常用的缓存策略有 LruCache 和 DisLruCache，LruCache 常被作为内存缓存，DiskLruCache 常被作为存储缓存。

Lru：Least Recently Used 的缩写，最近最少使用算法，当缓存快满时，会淘汰近期最少使用的缓存目标。

## 12.1 Bitmap 的高效加载
---
Bitmap 在 Android 中指的是一张图片，可以是 png 格式也可以是 jpg 等其他常见的图片格式。通过 BitmapFactory 的四类方法加载图片：decodeFile、decodeResource、decodeStream 和 decodeByteArray，分别用于支持从文件系统、资源、输入流以及字节数组中加载出一个 Bitmap 对象。decodeFile 和 decodeResource 又间接调用了 decodeStream 方法，这四类方法最终是在 Android 底层实现的，对应着 BitmapFactory 类的几个 native 方法。

高效加载 Bitmap 需要使用 BitmapFactory.Options 来加载所需尺寸的图片。通过 BitmapFactory.Options 可以按一定的采样率来加载缩小后的图片。使用 BitmapFactory.Options 的 inSampleSize 参数来确定采样率。当 inSampleSize 为 1 时，采样后的图片大小为图片的原始大小；当 inSampleSize 为 2 时，采样后的图片宽/高均为原图大小的 1/2，而像素数变为原图的 1/4，内存也为原图的 1/4。采样率必须是大于等于 1 的整数图片才会有缩小效果，并且采样率同时作用于宽/高，所以图片的缩放比例为 1/(2 的 n 次方)。官方文档指出，inSampleSize 应该总是 2 的指数，否则系统会向下取整并选择一个最接近的 2 的指数来替代，这个结论并非在所有 Android 版本上都成立。

获取采样率步骤：
1. 将 BitmapFactory.Options 的 inJustDecodeBounds 参数设为 true 并加载图片

2. 从 BitmapFactory.Options 中取出图片的原始宽高信息，它们对应于 outWidth 和 outHeight 参数

3. 根据采样率的规则并结合目标 View 的所需大小计算出采样率 inSampleSize

4. 将 BitmapFactory.Options 的 inJustDecodeBounds 设为 false，然后重新加载图片

inJustDecodeBounds 参数设置为 true 时，BitmapFactory 只会解析图片的原始宽高信息，并不会真正地加载图片，这个时候 BitmapFactory 获取的图片宽高信息和图片的位置以及程序运行的设备有关。

[示例代码](https://developer.android.com/topic/performance/graphics/load-bitmap.html)：
```
public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                     int reqWidth, int reqHeight) {

    // First decode with inJustDecodeBounds=true to check dimensions
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeResource(res, resId, options);

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false;
    return BitmapFactory.decodeResource(res, resId, options);
}

public static int calculateInSampleSize(
        BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) >= reqHeight
                && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2;
        }
    }

    return inSampleSize;
}

// 使用
mImageView.setImageBitmap(
    decodeSampledBitmapFromResource(getResources(), R.id.myimage, 100, 100));
```

## 12.2 Android 中的缓存策略
---
为了提高用户体验，很多时候在存储设备上缓存，同时还会在内存中缓存，当用户需要从网络上获取一份图片时，程序会首先从内存中获取，如果内存中没有那么从存储设备中获取，如果存储设备中也没有，那么才会从网络上下载这张图片。

缓存策略没有统一的标准，主要包含缓存的添加、获取和删除。由于缓存大小的限制，当缓存容量满了，需要删除旧的缓存并添加新的缓存，此时就需要缓存策略。

目前常用的缓存算法是 **LRU(Least Recently Used)**，最近最少使用算法，核心思想是当缓存满时，会优先淘汰那些近期最少使用的缓存对象。

### 12.2.1 LruCache
LruCache 是一个泛型类，内部采用一个 LinkedHashMap 以强引用的方式存储外界的缓存对象，提供了 get 和 put 方法来完成缓存的获取和添加操作，当缓存满时，LruCache 会移除较早使用的缓存对象，然后再添加新的缓存对象。

* 强引用：直接的对象引用

* 软引用：当一个对象只有软引用存在时，系统内存不足时此对象会被 gc 回收

* 弱引用：当一个对象只有弱引用存在时，此对象会随时被 gc 回收

LruCache 使用：
```
// 获取最大可用内存
int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
// 缓存容量为最大内存的 1/8
int cacheSize = maxMemory / 8;
LruCache<String, Bitmap> mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
    @Override
    protected int sizeOf(String key, Bitmap value) {
        // 计算 bitmap 的大小，单位与缓存容量的单位一致，为 KB
        return value.getRowBytes() * value.getHeight() / 1024;
    }
};

// 获取缓存对象
mMemoryCache.get(key);

// 添加缓存对象
mMemoryCache.put(key, bitmap);
```
LruCache 还支持删除操作，使用 remove 即可删除一个指定的缓存对象。

### 12.2.2 DiskLruCache
DiskLruCache 用于实现存储设备缓存，即磁盘缓存。DiskLruCache 不属于 Android 源码，[DiskLruCache 源码](https://github.com/JakeWharton/DiskLruCache)

DiskLruCache 使用方式：
1. DiskLruCache 创建  
    DiskLruChche 不能通过构造方法来创建，提供了 open 方法用于创建自身：
    ```
    public static DiskLruCache open(File directory, int appVersion, int valueCount, long maxSize)
    ```
    * directory：磁盘缓存在文件系统中的存储路径，可以选择 SD 卡上的缓存目录 `/sdcard/Android/data/package_name/cache`，当应用被卸载时，此目录会一并被删除，也可以指定 SD 卡上的其他目录。

    * appVersion：应用版本号，一般为 1，当版本号发生改变时会清空之前所有缓存文件，很多情况下即使版本更新缓存文件仍希望有效，所以设为 1 较好

    * valueCount：单个节点所对应的数据的个数，一般设为 1

    * maxSize：缓存的总大小，当缓存大小超出这个设定值后，DiskLruCache 会清除一些缓存从而保证大小不大于这个设定值

    DiskLruCache 创建过程：
    ```
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 50; // 50MB

    File diskCacheDir = getDiskCacheDir(mContext, "bitmap");
    if (!diskCacheDir.exists()) {
        diskCacheDir.mkdirs();
    }
    mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_CACHE_SIZE);
    ```
2. DiskLruCache 缓存添加  
DiskLruCache 缓存添加是通过 Editor 完成的，Editor 表示一个缓存对象的编辑对象。可以通过 `edit(key)` 方法来获取 Editor 对象，如果 Editor 正在被编辑，那么 `edit(key)` 会返回 null，DiskLruCache 不允许同时编辑一个缓存对象。这里将图片的 url 转成 md5 值作为 key，因为 url 中可能有特殊字符，会影响 url 的使用：
```
    private String hashKeyFormUrl(String url) {
        String cacheKey;

        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(url.getBytes());
            cacheKey = bytesToHexString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(url.hashCode());
            e.printStackTrace();
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xff & aByte);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
```
url 转为 key 之后就可以获取 Editor 对象，如果当前不存在 Editor 对象，那么 edit() 就会返回一个文件输出流，DiskLruCache 的 open 方法设置了一个节点只能有一个数据，此处 DISK_CACHE_INDEX 设为 0 即可：
```
String key = hashKeyFormUrl(url);
DiskLruCache.Editor editor = mDiskLruCache.edit(key);
if (edit != null) {
    OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
}
```
接下来直接将下载的图片通过文件输出流写入到文件系统上：
```
```
最后，还要通过 Editor 的 commit() 来提交写入操作，如果下载过程发生了异常，可以通过 abort() 方法来回退整个操作：
```
OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
if (downloadUrlToStream(url, outputStream)) {
    editor.commit();
} else {
    editor.abort();
}
mDiskLruCache.flush();
```
3. DiskLruCache 的缓存查找  
缓存查找过程也需要将 url 转换为 key，然后通过 DiskLruCache 的 get 方法得到 Snapshot 对象，通过 Snapshot 即可得到缓存的文件输入流，然后就可以得到 Bitmap 对象了。通过 BitmapFactory.Options 对象加载缩放后的图片，对于 FileInputStream 的缩放存在问题，FileInputStream 是一种有序的文件流，而两次 decodeStream 调用影响了文件流的位置属性，导致第二次 decodeStream 时得到的是 null。可以通过文件流来得到它所对应的文件描述符，再通过 BitmapFactory.decodeFileDescriptor 方法来加载一张缩放后的图片。

### 12.2.3 ImageLoader 的实现
一个优秀的 ImageLoader 应具备如下功能：

* 图片的同步加载

* 图片的异步加载

* 图片压缩

* 内存缓存

* 磁盘缓存

* 网络拉取

代码见 Chapter12

## 12.3 ImageLoader 的使用
---
代码见 Chapter12