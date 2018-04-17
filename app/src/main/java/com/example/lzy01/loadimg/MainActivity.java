package com.example.lzy01.loadimg;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    public  File  PICTURE_FILE;
    private static final int PHOTO_REQUEST=1;
    private static final int FILE_REQUEST=2;
    private ImageButton photo;
    private ImageButton file;
    private ImageButton back;
    private ImageView imageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        photo=findViewById(R.id.openPhoto);
        file=findViewById(R.id.openFile);
        back=findViewById(R.id.back);
        imageView=findViewById(R.id.showImg);
        imageView.setVisibility(View.INVISIBLE);
        back.setVisibility(View.INVISIBLE);
        photo.setOnClickListener(this);
        file.setOnClickListener(this);
        back.setOnClickListener(this);
    }


    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.openPhoto:
                String path = Environment.getExternalStorageDirectory() + File.separator+"Pictures"+ File.separator +"images"; //获取路径
                String fileName = new Date().getTime()+".jpg";//定义文件名
                PICTURE_FILE = new File(path,fileName);
                if(!PICTURE_FILE.getParentFile().exists()){//文件夹不存在
                    PICTURE_FILE.getParentFile().mkdirs();
                }
                Uri imageUri = Uri.fromFile(PICTURE_FILE);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, PHOTO_REQUEST);//takePhotoRequestCode是自己定义的一个请求码
                System.out.println("点击");
                break;
            case R.id.openFile:
                Intent intent2 = new Intent(Intent.ACTION_GET_CONTENT);
                //选择图片格式
                intent2.setType("image/*");
                intent2.putExtra("return-data",true);
                startActivityForResult(intent2,FILE_REQUEST);
                break;
            case R.id.back:
                photo.setVisibility(View.VISIBLE);
                file.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.INVISIBLE);
                back.setVisibility(View.INVISIBLE);
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap;
        if(resultCode == RESULT_OK) {
            photo.setVisibility(View.INVISIBLE);
            file.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.VISIBLE);
            back.setVisibility(View.VISIBLE);
            switch (requestCode) {
                case PHOTO_REQUEST:
                    //Toast.makeText(getApplicationContext(),"保存成功",Toast.LENGTH_SHORT).show();
                    try{
                        Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_LONG).show();
                    }catch (IOError error){
                        error.printStackTrace();
                    }
                    bitmap = loadBitmap(PICTURE_FILE.getPath());
                    imageView.setImageBitmap(bitmap);
                    break;
                case FILE_REQUEST:
                    Uri uri = data.getData();
                    //通过uri的方式返回，部分手机uri可能为空
                    if (uri != null) {
                        try {
                            //通过uri获取到bitmap对象
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            imageView.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else {
                        //部分手机可能直接存放在bundle中
                        Bundle bundleExtras = data.getExtras();
                        if (bundleExtras != null) {
                            Bitmap bitmaps = bundleExtras.getParcelable("data");
                            imageView.setImageBitmap(bitmaps);
                        }
                    }

                    break;
            }
        }else{
            photo.setVisibility(View.VISIBLE);
            file.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.INVISIBLE);
            back.setVisibility(View.INVISIBLE);
        }
    }


    public Bitmap loadBitmap(String imgpath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bm = null;

        // 减少内存使用量，有效防止OOM
        {
            options.inJustDecodeBounds = true;
            bm = BitmapFactory.decodeFile(imgpath, options);

            // 屏幕宽
            int Wight = getWindowManager().getDefaultDisplay().getWidth();

            // 缩放比
            int ratio = options.outWidth / Wight;
            Log.e("xiangji", "options.outWidth="+options.outWidth);
            Log.e("xiangji", "rWight="+Wight);
            if (ratio <= 0){
                ratio = 1;
            }

            //InSampleSize这个参数可以调节你在decode原图时所需要的内存，有点像采样率，会丢掉一些像素，值是大于1的数，为2的幂时更利于运算。
            //举个例子：当 inSampleSize == 4 时会返回一个尺寸(长和宽)是原始尺寸1/4，像素是原来1/16的图片，由此来减少内存使用

            //options.inSampleSize = ratio;由动态的生成此数值变为手动控制
            options.inSampleSize = 4;//此数值决定显示时照片的大小
            options.inJustDecodeBounds = false;
        }

        // 加载图片,并返回
        return BitmapFactory.decodeFile(imgpath, options);
    }
}
