# PhotoFactory

### PhotoFactory简介
**PhotoFactory**封装了调用相机拍照，从相册选取照片，压缩选取的照片等功能。使用**PhotoFactory**分为三步，首先我们要实例化一个**photoFactory**
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

获取相片bitmap或uri
```java
/**
 * 在onActivityResult中调用此方法
 */
photoFactory.FactoryFinish(requestCode,resultCode,data).GetBitmap();
//photoFactory.FactoryFinish(requestCode,resultCode,data).GetUri();
```
就这么简单，当然，你还可以对照片进行压缩处理，这里提供了三种压缩的方式
```java
addScaleCompress(int w, int h)//按目标宽高缩放
addScaleCompress(int scale)//等比例缩放，缩放比为 原图:新图 = scale:1
addQualityCompress(int targetSize)//质量压缩，targetSize为目标大小
```
你可以选择其中一种或多种压缩方式对相片进行压缩，例如
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

***
### 完整示例
**PhotoFactory**兼容了**Android 7.0 FileProvider**获取相片**uri**的问题，当然具体**Provider**的配置以及**Android 6.0的动态权限管理**需要大家在项目中自行完成。这里为了方便大家完成配置，我将完整的流程贴出来供大家参考

**1.在Github下载photofactory library并导入到你的项目中**

地址：[AnliaLee/PhotoFactory](https://github.com/AnliaLee/PhotoFactory)

**2. 在res\xml目录中创建provider_paths.xml**
```
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-path name="external_files" path="."/>
</paths>
```
**3. 在AndroidManifest.xml中添加相应权限及配置Provider**
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
**4. 在AndroidManifest.xml中添加相应权限及配置Provider**
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
**5. 在Activity中进行动态权限管理以及使用PhotoFactory**
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
        if(requestCode == RESULT_CANCELED){
            Toast.makeText(this, "取消", Toast.LENGTH_SHORT).show();
        }else {
            Uri uri = photoFactory.FactoryFinish(requestCode,resultCode,data).GetUri();
            imgPhoto.setImageURI(uri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
```