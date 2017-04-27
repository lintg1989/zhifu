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


import com.bumptech.glide.Glide;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.Request;
import com.yolanda.nohttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.Constants;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.model.BalanceInfo;
import cn.zheft.www.zheft.nohttp.CallServer;
import cn.zheft.www.zheft.nohttp.HttpListener;
import cn.zheft.www.zheft.nohttp.MyRequest;
import cn.zheft.www.zheft.nohttp.NoResponse;

/**
 * @author Lin
 * 证件上传第一页
 */
public class UploadCardFirstActivity extends BaseActivity {

    private int flag = 0;
    private String path ="";
    private String uriPath = "";

    private Context mContext;

    private MyApp myApplication;
    private ImageView iv_id_card_front;
    private ImageView iv_id_card_back;
    private ImageView iv_bank_card_front;

    private ImageView iv_bank_card_back;
    private ImageView iv_card_body;
    private Button btn_save;

    private String ic_front_path;//身份证正面
    private String ic_back_path;//身份证反面

    private String bank_front_path;//银行卡正面
    private String bank_back_path;//银行卡反面

    private String ic_body_path;//手持身份证照片


    private Bitmap bitmap = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_card_first);
        setToolbar(R.string.upload_card);
        mContext = this;

        myApplication = MyApp.getInstance();

        initView();

        initEvent();

        Log.e("OnCreate","ONCREATTEＳｔａｒｔｅｓｄ");
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
        ic_front_path = myApplication.getImagePathInfo().getIc_front_path();
        ic_back_path = myApplication.getImagePathInfo().getIc_back_path();
        bank_front_path = myApplication.getImagePathInfo().getBank_front_path();
        bank_back_path = myApplication.getImagePathInfo().getBank_back_path();
        ic_body_path = myApplication.getImagePathInfo().getIc_body_path();


        if (!TextUtils.isEmpty(ic_front_path)){
            iv_id_card_front.setImageBitmap(BitmapFactory.decodeFile(ic_front_path));
        }
        if (!TextUtils.isEmpty(ic_back_path)){
            iv_id_card_back.setImageBitmap(BitmapFactory.decodeFile(ic_back_path));
        }

        if (!TextUtils.isEmpty(bank_front_path)){
            iv_bank_card_front.setImageBitmap(BitmapFactory.decodeFile(bank_front_path));
        }
        if (!TextUtils.isEmpty(bank_back_path)){
            iv_bank_card_back.setImageBitmap(BitmapFactory.decodeFile(bank_back_path));
        }
        if (!TextUtils.isEmpty(ic_body_path)){
            iv_card_body.setImageBitmap(BitmapFactory.decodeFile(ic_body_path));
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
        if (flag == 1){
            ic_front_path = path;
            myApplication.getImagePathInfo().setIc_front_path(path);
            iv_id_card_front.setImageBitmap(BitmapFactory.decodeFile(ic_front_path));
            if (TextUtils.isEmpty(path)){
                iv_id_card_front.setImageBitmap(bitmap);
                Log.e("URIPath-===", uriPath);
                myApplication.getImagePathInfo().setIc_front_path(uriPath);
            }
        } else if (flag == 2){
            ic_back_path = path;
            myApplication.getImagePathInfo().setIc_back_path(path);
            iv_id_card_back.setImageBitmap(BitmapFactory.decodeFile(ic_back_path));
            if (TextUtils.isEmpty(path)){
                iv_id_card_back.setImageBitmap(bitmap);
                myApplication.getImagePathInfo().setIc_back_path(uriPath);
            }
        }else if (flag == 3){
            bank_front_path = path;
            myApplication.getImagePathInfo().setBank_front_path(path);
            iv_bank_card_front.setImageBitmap(BitmapFactory.decodeFile(bank_front_path));
            if (TextUtils.isEmpty(path)){
                iv_bank_card_front.setImageBitmap(bitmap);
                myApplication.getImagePathInfo().setBank_front_path(uriPath);
            }
        } else if (flag ==4){
            bank_back_path = path;
            myApplication.getImagePathInfo().setBank_back_path(path);
            iv_bank_card_back.setImageBitmap(BitmapFactory.decodeFile(bank_back_path));
            if (TextUtils.isEmpty(path)){
                iv_bank_card_back.setImageBitmap(bitmap);
                myApplication.getImagePathInfo().setBank_back_path(uriPath);
            }
        } else if (flag == 5){
            ic_body_path = path;
            myApplication.getImagePathInfo().setBank_back_path(path);
            iv_card_body.setImageBitmap(BitmapFactory.decodeFile(ic_body_path));
            if (TextUtils.isEmpty(path)){
                iv_card_body.setImageBitmap(bitmap);
                myApplication.getImagePathInfo().setIc_body_path(uriPath);
            }
        }
    }

    private void initView() {
        iv_id_card_front = (ImageView) findViewById(R.id.iv_id_card_front);
        iv_id_card_back = (ImageView) findViewById(R.id.iv_id_card_back);
        iv_bank_card_front = (ImageView) findViewById(R.id.iv_bank_card_front);

        iv_bank_card_back = (ImageView) findViewById(R.id.iv_bank_card_back);
        iv_card_body = (ImageView) findViewById(R.id.iv_card_body);
        btn_save = (Button) findViewById(R.id.btn_save);



    }


    private void initEvent() {

    }

    //保存
    public void save(View view){

    }

    //几张图片
    public void jump(View view){
        switch (view.getId()){
            case R.id.iv_id_card_front:
                flag = 1;
                break;
            case R.id.iv_id_card_back:
                flag = 2;
                break;
            case R.id.iv_bank_card_front:
                flag = 3;
                break;
            case R.id.iv_bank_card_back:
                flag = 4;
                break;
            case R.id.iv_card_body:
                flag = 5;
                break;
            default:
                flag = 0;
        }

        Intent intent = new Intent(mContext, RectCameraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra("flag", flag);
        startActivity(intent);
    }

    /**
     * 网络请求
     */
    private void request(){
        MyRequest request = new MyRequest(Constants.URL_IMAGE);
        CallServer.getRequestInstance().add(mContext, 0, request, objectListener, true, true);

    }

    private HttpListener<NoResponse> objectListener = new HttpListener<NoResponse>() {

        @Override
        public void onSucceed(int what, Response<NoResponse> response) {

            //加载图片
            Glide.with(mContext).load("").into(iv_bank_card_front);
        }

        @Override
        public void onFailed(int what, String url, Object tag, Exception exception, int responseCode, long networkMillis) {

        }
    };



}
