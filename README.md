# PhotoFactory

[![GitHub release](https://img.shields.io/github/release/AnliaLee/PhotoFactory.svg)](https://github.com/AnliaLee/PhotoFactory/releases)
[![GitHub license](https://img.shields.io/github/license/AnliaLee/PhotoFactory.svg)](https://github.com/AnliaLee/PhotoFactory/blob/master/LICENSE)

**PhotoFactory**封装了调用相机拍照，从相册选取照片，压缩选取的照片，图片搜索等功能
### 添加依赖
**Gradle** 
```
repositories {
	...
	maven { url 'https://jitpack.io' }
}

dependencies {
	compile 'com.github.AnliaLee:PhotoFactory:1.0.1'
}

```
### 配置权限
```
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```
***
### 如何使用

#### 从系统相册获取图片
**PhotoFactory**兼容了**Android 7.0 FileProvider**获取相片**uri**的问题，当然具体**Provider**的配置以及**Android 6.0的动态权限管理**需要大家在项目中自行完成。这里为了方便大家完成配置，我将完整的流程贴出来供大家参考

**1. 在res\xml目录中创建provider_paths.xml**
```
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-path name="external_files" path="."/>
</paths>
```
**2. 在AndroidManifest.xml中添加相应权限及配置Provider**
```
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<application
	android:icon="@mipmap/ic_launcher"
	...
	>
	<provider
		android:name="android.support.v4.content.FileProvider"
		android:authorities="${applicationId}.provider"
		android:exported="false"
		android:grantUriPermissions="true">
		<meta-data
			android:name="android.support.FILE_PROVIDER_PATHS"
			android:resource="@xml/provider_paths" />
	</provider>
	...
</application>
```
**3. 在Activity中进行动态权限管理以及使用PhotoFactory**

使用**PhotoFactory**分为三步，首先我们要实例化一个**photoFactory**
```java
PhotoFactory photoFactory = new PhotoFactory(this, this);//(Activity activity,Context context)
```
设置选择照片的方式
```java
//提供三种选择照片的方式
photoFactory.FactoryStart().SetStartType(PhotoFactory.TYPE_PHOTO_UNTREATED).Start();//调用相机拍照，照相后返回高清原图相片
/*photoFactory.FactoryStart().SetStartType(PhotoFactory.TYPE_PHOTO_AUTO_COMPRESS).Start();//调用相机拍照，照相后返回系统自动压缩过的相片
photoFactory.FactoryStart().SetStartType(PhotoFactory.TYPE_PHOTO_FROM_GALLERY).Start();//从本地相册中选取图片*/
```

获取相片**bitmap**或**uri**
```java
//在onActivityResult中调用此方法
photoFactory.FactoryFinish(requestCode,resultCode,data).GetBitmap();
//photoFactory.FactoryFinish(requestCode,resultCode,data).GetUri();

//也可以通过回调获取图片
photoFactory.FactoryFinish(requestCode,resultCode,data)
			.setOnResultListener(new PhotoFactory.OnResultListener() {
	@Override
	public void TakePhotoCancel() {
		Toast.makeText(PhotoTestActivity.this, "取消拍照", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void GalleryPhotoCancel() {
		Toast.makeText(PhotoTestActivity.this, "取消选择", Toast.LENGTH_SHORT).show();
	}

	@Override
	public void HasData(PhotoFactory.ResultData resultData) {//正确选取了相片
		Uri uri = resultData.GetUri();
	}
});
```
就这么简单，当然，你还可以对照片进行**压缩**处理，这里提供了三种压缩的方式
```java
addScaleCompress(int w, int h)//按目标宽高缩放
addScaleCompress(int scale)//等比例缩放，缩放比为 原图:新图 = scale:1
addQualityCompress(int targetSize)//质量压缩，targetSize为目标大小
```
你可以选择**其中一种或多种**压缩方式对相片进行压缩，例如
```java
//只选择一种压缩方式
photoFactory.FactoryFinish(requestCode,resultCode,data)
	    .addQualityCompress(128)
	    .GetBitmap();
	    
//按顺序逐步压缩
photoFactory.FactoryFinish(requestCode,resultCode,data)
	    .addQualityCompress(128)
	    .addScaleCompress(5)
	    .addScaleCompress(300,300)
	    .GetBitmap();
```

**完整示例**

```java
public class PhotoTestActivity extends AppCompatActivity {
    private Button btnPhoto;
    private ImageView imgPhoto;
    private PhotoFactory photoFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_test);

        photoFactory = new PhotoFactory(this,this);
        imgPhoto = (ImageView) findViewById(R.id.img_photo);
        btnPhoto = (Button) findViewById(R.id.btn_photo);
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(PhotoTestActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    //申请写入权限
                    ActivityCompat.requestPermissions(PhotoTestActivity.this, new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, 100);
                } else {
                    photoFactory.FactoryStart()
                                .SetStartType(PhotoFactory.TYPE_PHOTO_UNTREATED)
                                .Start();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case 100:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    photoFactory.FactoryStart()
                            .SetStartType(PhotoFactory.TYPE_PHOTO_UNTREATED)
                            .Start();
                }else{// 没有获取到权限，做特殊处理
                    Toast.makeText(this, "请授予权限！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //可以这样去获取相片，但需要你自己去写取消选择的处理
        Uri uri = photoFactory.FactoryFinish(requestCode,resultCode,data).GetUri();
        imgPhoto.setImageURI(uri);

        //也这样调用setOnResultListener一步到位了
        photoFactory.FactoryFinish(requestCode,resultCode,data)
                    .setOnResultListener(new PhotoFactory.OnResultListener() {
            @Override
            public void TakePhotoCancel() {
                Toast.makeText(PhotoTestActivity.this, "取消拍照", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void GalleryPhotoCancel() {
                Toast.makeText(PhotoTestActivity.this, "取消选择", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void HasData(PhotoFactory.FinishBuilder resultData) {//正确选取了相片
                Uri uri = resultData.GetUri();
                imgPhoto.setImageURI(uri);
            }
        });
        super.onActivityResult(requestCode, resultCode, data);
    }
}
```
***
#### 搜索图片

**PhotoFactory**封装了**搜索图片**的功能，你可以根据**图片的路径、名称、图片格式等条件**搜索图片，执行搜索后返回符合条件图片的**list**。这里以查询所有gif图片为例

```java
//加载数据的映射（MediaStore.Images.Media.DATA等）
String[] projection = new String[]{
	MediaStore.Images.Media.DATA,//图片路径
	MediaStore.Images.Media.DISPLAY_NAME,//图片文件名，包括后缀名
	MediaStore.Images.Media.TITLE//图片文件名，不包含后缀
};

photoFactory = new PhotoFactory(this,this);
photoFactory.FactorySearch(getSupportLoaderManager(),getApplicationContext(),projection)
			.setSelectionByFormat(new String[]{".gif"})//设置查询条件（通过图片格式查找，非必选）
			//.setSelection(new String[]{"图片收藏","weixin"}) （或模糊匹配搜索指定图片，非必选）
			.setLoadingEvent(new InterfaceManager.LoadingCallBack() {//设置异步加载时loading操作（非必选）
				@Override
				public void showLoading() {
					myProgressDialog.show();
				}

				@Override
				public void hideLoading() {
					if(myProgressDialog.isShowing()){
						myProgressDialog.dismiss();
					}
				}
			})
			.setErrorEvent(new InterfaceManager.ErrorCallBack() {//设置搜索出错时的操作（非必选）
				@Override
				public void dealError(String s) {
					Toast.makeText(SearchGifActivity.this, s, Toast.LENGTH_SHORT).show();
				}
			})
			.execute(new InterfaceManager.SearchDataCallBack() {//执行搜索并获取回调数据
				@Override
				public void onFinish(final List<Map<String, Object>> list) {
					searchGifAdapter = new SearchGifAdapter(SearchGifActivity.this,list);
					recyclerView.setAdapter(searchGifAdapter);
					
					//通过你之前设置的映射获取数据，例如：list.get(position).get(MediaStore.Images.Media.DATA)
				}
			});
```