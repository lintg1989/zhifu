package cn.zheft.www.zheft.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.FileNotFoundException;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;

public class ShopPhotoFirstActivity extends BaseActivity {

    private int flag = 0;
    private String path ="";
    private String uriPath = "";

    private Bitmap bitmap = null;

    private Context mContext;
    private MyApp myApplication;

    private String shop_outside_path;//店铺外照
    private String shop_inside_path;//店铺内照
    private String shop_sail_path;//营业执照
    private String shop_desk_path;//收银台照


    private ImageView iv_business;
    private ImageView iv_shop_outside;
    private ImageView iv_shop_inside;

    private ImageView iv_desk;
    private Button btn_save;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_photo_first);

        setToolbar(R.string.shop_photo);
        mContext = this;

        myApplication = MyApp.getInstance();

        initView();

        initEvent();
    }

    private void initEvent() {
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e("onResume","onResume");
        shop_sail_path = myApplication.getImagePathInfo().getShop_sail_path();
        shop_outside_path = myApplication.getImagePathInfo().getShop_outside_path();
        shop_inside_path = myApplication.getImagePathInfo().getShop_inside_path();
        shop_desk_path = myApplication.getImagePathInfo().getShop_desk_path();


        if (!TextUtils.isEmpty(shop_outside_path)){
            iv_shop_outside.setImageBitmap(BitmapFactory.decodeFile(shop_outside_path));
        }
        if (!TextUtils.isEmpty(shop_inside_path)){
            iv_shop_inside.setImageBitmap(BitmapFactory.decodeFile(shop_inside_path));
        }

        if (!TextUtils.isEmpty(shop_sail_path)){
            iv_business.setImageBitmap(BitmapFactory.decodeFile(shop_sail_path));
        }
        if (!TextUtils.isEmpty(shop_desk_path)){
            iv_desk.setImageBitmap(BitmapFactory.decodeFile(shop_desk_path));
        }

        Uri uri = getIntent().getData();
        ContentResolver cr = this.getContentResolver();
        try {
            if (uri != null){
                Log.e("urI===",uri.toString());
                bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));

                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor cursor = cr.query(uri, proj, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                uriPath = cursor.getString(column_index);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        path = getIntent().getStringExtra("path");
        flag = getIntent().getIntExtra("flag", 0);
        if (flag == 6){
            shop_sail_path = path;
            myApplication.getImagePathInfo().setShop_sail_path(path);
            iv_business.setImageBitmap(BitmapFactory.decodeFile(shop_sail_path));
            if (TextUtils.isEmpty(path)){
                iv_business.setImageBitmap(bitmap);
                Log.e("URIPath-===", uriPath);
                myApplication.getImagePathInfo().setShop_sail_path(uriPath);
            }
        } else if (flag == 7){
            shop_outside_path = path;
            myApplication.getImagePathInfo().setShop_outside_path(path);
            iv_shop_outside.setImageBitmap(BitmapFactory.decodeFile(shop_outside_path));
            if (TextUtils.isEmpty(path)){
                iv_shop_outside.setImageBitmap(bitmap);
                myApplication.getImagePathInfo().setShop_outside_path(uriPath);
            }
        }else if (flag == 8){
            shop_inside_path = path;
            myApplication.getImagePathInfo().setShop_inside_path(path);
            iv_shop_inside.setImageBitmap(BitmapFactory.decodeFile(shop_inside_path));
            if (TextUtils.isEmpty(path)){
                iv_shop_inside.setImageBitmap(bitmap);
                myApplication.getImagePathInfo().setShop_inside_path(uriPath);
            }
        } else if (flag ==9){
            shop_desk_path = path;
            myApplication.getImagePathInfo().setShop_desk_path(path);
            iv_desk.setImageBitmap(BitmapFactory.decodeFile(shop_desk_path));
            if (TextUtils.isEmpty(path)){
                iv_desk.setImageBitmap(bitmap);
                myApplication.getImagePathInfo().setShop_desk_path(uriPath);
            }
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("onRestart","onRestart");
    }


    private void initView() {
        iv_business = (ImageView) findViewById(R.id.iv_business);
        iv_shop_outside = (ImageView) findViewById(R.id.iv_shop_outside);
        iv_shop_inside = (ImageView) findViewById(R.id.iv_shop_inside);

        iv_desk = (ImageView) findViewById(R.id.iv_desk);
        btn_save = (Button) findViewById(R.id.btn_save);
        
    }

    public void jump(View view){
        switch (view.getId()){
            case R.id.iv_business:
                flag = 6;
                break;
            case R.id.iv_shop_outside:
                flag = 7;
                break;
            case R.id.iv_shop_inside:
                flag = 8;
                break;
            case R.id.iv_desk:
                flag = 9;
                break;
            default:
                flag = 0;
        }

        Intent intent = new Intent(mContext, RectCameraActivity.class);
        intent.putExtra("flag", flag);
        startActivity(intent);
    }


    //保存
    public void save(View view){

    }
}
