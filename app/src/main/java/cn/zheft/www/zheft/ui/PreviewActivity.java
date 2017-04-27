package cn.zheft.www.zheft.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.FileNotFoundException;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.util.ToastUtil;

public class PreviewActivity extends BaseActivity {

    private Context mContext;

    private ImageView iv_preview;
    private ImageView iv_ok;
    private ImageView iv_back;

    private int flag = 0;
    private String path ="";

    private Bitmap bitmap = null;

    private Uri uri= null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        mContext = this;

        iv_ok = (ImageView) findViewById(R.id.iv_ok);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_preview = (ImageView) findViewById(R.id.iv_preview);

        initEvent();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        flag = getIntent().getIntExtra("flag",0);
        path = getIntent().getStringExtra("path");
        uri = getIntent().getData();

        if (!TextUtils.isEmpty(path)){
            iv_preview.setImageBitmap(BitmapFactory.decodeFile(path));
        } else if (uri != null){
            ContentResolver cr = this.getContentResolver();
            try {
                Log.e("urI===",uri.toString());
                bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                Log.e("BITMAP===", bitmap.toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            iv_preview.setImageBitmap(bitmap);
        }

    }

    private void initEvent() {
        iv_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtil.showShortMessage("flag="+flag );
                Intent intent = null;

                switch (flag){
                    case 0:
                        break;
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        intent = new Intent(mContext, UploadCardFirstActivity.class);
                        intent.putExtra("flag", flag);
                        intent.putExtra("path", path);
                        intent.setData(uri);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        finish();
                        break;
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                        intent = new Intent(mContext, ShopPhotoFirstActivity.class);
                        intent.putExtra("flag", flag);
                        intent.putExtra("path", path);
                        intent.setData(uri);
                        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(intent);
                        finish();
                        break;
                }
            }
        });


        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }





}
