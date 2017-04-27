package cn.zheft.www.zheft.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.Constants;
import cn.zheft.www.zheft.camera.CameraHelper;
import cn.zheft.www.zheft.camera.MaskSurfaceView;
import cn.zheft.www.zheft.camera.OnCaptureCallback;

public class RectCameraActivity extends Activity implements OnCaptureCallback {

    private int flag = 0;

    private Context mContext;

    private int screenHeight;
    private int screenWidth;

    private MaskSurfaceView surfaceview;
    private ImageView imageView;

    private ImageView iv_back;

    //	拍照后得到的保存的文件路径
    private String filepath;

    private ImageView iv_sample;
    private TextView tv_title;

    private RelativeLayout rl_toolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.activity_rect_camera);

        mContext = this;

        Display display = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        screenHeight = display.getHeight();
        screenWidth = display.getWidth();

        initView();

    }

    private void initView(){
        tv_title = (TextView) findViewById(R.id.tv_title);
        iv_sample = (ImageView) findViewById(R.id.iv_sample);
        rl_toolBar = (RelativeLayout) findViewById(R.id.rl_toolBar);
        this.surfaceview = (MaskSurfaceView) findViewById(R.id.surface_view);
        this.imageView = (ImageView) findViewById(R.id.image_view);
        iv_back = (ImageView) findViewById(R.id.iv_back);

        //设置矩形区域大小
        this.surfaceview.setMaskSize(790, 510);

        flag = getIntent().getIntExtra("flag", 0);

        if (flag == 1){
            tv_title.setText("拍摄申请人身份证正面照");
        } else if (flag ==2){
            tv_title.setText("拍摄申请人身份证反面照");
        } else if (flag == 3){
            tv_title.setText("拍摄申请人银行卡正面照");
        } else if (flag == 4){
            tv_title.setText("拍摄申请人银行卡反面照");
        } else if (flag == 5){
            tv_title.setText("拍摄手持身份证和银行卡的半身照");
        } else if (flag == 6){
            tv_title.setText("拍摄营业执照照片");
        } else if (flag == 7){
            tv_title.setText("拍摄店铺外景照片");
        } else if (flag == 8){
            tv_title.setText("拍摄店铺内景照片");
        } else if (flag == 9){
            tv_title.setText("拍摄收银台照片");
        }

        if (flag == 5 ){
            iv_sample.setVisibility(View.VISIBLE);
        } else {
            iv_sample.setVisibility(View.GONE);
        }

        if (flag >4){
            this.surfaceview.setMaskSize(screenWidth,screenHeight);
        }

        //测试用
        iv_sample.setVisibility(View.VISIBLE);

        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void preview(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Log.e("flag===",flag+"");

        startActivityForResult(intent, Constants.RC_PICK_PICTURE_FROM_GALLERY_ORIGINAL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.RC_PICK_PICTURE_FROM_GALLERY_ORIGINAL:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this,"选择成功",Toast.LENGTH_LONG).show();

                    this.imageView.setVisibility(View.VISIBLE);
                    this.surfaceview.setVisibility(View.GONE);

                    Uri uri = data.getData();

                    Log.e("flag===data.getFlags()",flag+"");
                    Log.e("uri", uri.toString());
                    ContentResolver cr = this.getContentResolver();

                    Intent intent = new Intent(mContext, PreviewActivity.class);
                    intent.setData(uri);
                    intent.putExtra("flag", flag);
                    startActivity(intent);
                    finish();
                }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    public void takePhoto(View view) {
        CameraHelper.getInstance().tackPicture(RectCameraActivity.this);
    }

    public void sample(View view){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(getLayoutInflater().inflate(R.layout.content_rect_camera,null))
                .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .create();
        dialog.show();
        dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.blue));
        dialog.getButton(dialog.BUTTON_POSITIVE).setTextSize(20);
    }

    @Override
    public void onCapture(boolean success, String filepath) {
        this.filepath = filepath;
        String message = "拍照成功";
        if(!success){
            message = "拍照失败";
            CameraHelper.getInstance().startPreview();
            this.imageView.setVisibility(View.GONE);
            this.surfaceview.setVisibility(View.VISIBLE);
        }else{
            Intent intent = new Intent(mContext, PreviewActivity.class);
            intent.putExtra("path", filepath);
            intent.putExtra("flag", flag);
            startActivity(intent);
            finish();
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
