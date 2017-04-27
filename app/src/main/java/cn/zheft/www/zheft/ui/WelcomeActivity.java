package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.WelcomePicInfo;
import cn.zheft.www.zheft.model.WelcomePicOldInfo;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.RetrofitAPI;
import cn.zheft.www.zheft.util.CountDownTimerUtil;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.StringUtil;
import cn.zheft.www.zheft.util.VersionUtil;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WelcomeActivity extends BaseActivity {
    private Context context;
    private static final String TAG = "WelcomeActivity";
    private static final String WELCOME_PIC_NAME = "WelcomePic";// 文件名

    private WelcomePicInfo picInfo;
    private Bitmap showBmp;// 用于展示的图片
    private TextView tvTime;// 显示倒计时
    private ImageView ivShow;
    private RelativeLayout rlShow;// 展示部分

    private boolean pictureOk = false;// 下载图片准备状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTransScreen();
        setContentView(R.layout.activity_welcome);
        disablePatternLock();
        context = this;

        picInfo = Config.getWelcomePic();
        if (picInfo != null) {
            initView();
        } else {
            toNextPage(2);
        }
    }

    private void initView() {
        tvTime = (TextView) findViewById(R.id.tv_welcome_time);
        ivShow = (ImageView) findViewById(R.id.iv_welcome_show);
        rlShow = (RelativeLayout) findViewById(R.id.rl_welcome_show);
        LinearLayout llSkip = (LinearLayout) findViewById(R.id.ll_welcome_skip);
        llSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timer != null) {
                    timer.cancel();
                }
                toNextPage(2);
            }
        });

        // 判断如果图片路径不为空说明无需下载新图，否则显示默认并下载
        WelcomePicOldInfo oldPic = Config.getWelcomePicOld();
        String getPicVer = picInfo.getVersionNum();
        if (oldPic != null && oldPic.getVersionNum() != null && oldPic.getFilePath() != null
                && StringUtil.strToInt(getPicVer) <= StringUtil.strToInt(oldPic.getVersionNum())
                && !StringUtil.nullOrEmpty(picInfo.getFilePath())) {
            showPicture(0);
        } else {
            downloadPic(picInfo.getFilePath());
        }
        timer.start();
    }

    private CountDownTimerUtil timer = new CountDownTimerUtil(3000,300) {
        @Override
        public void onTick(long millisUntilFinished) {
            long num = (millisUntilFinished/1000 + 1);
            tvTime.setText(num + "");
        }

        @Override
        public void onFinish() {
            tvTime.setText("0");
            toNextPage(2);
        }
    };

    // 显示图片(参数为动画时间)
    private void showPicture(int duration) {
        if (isFinishing()) {
            return;
        }
        WelcomePicOldInfo oldPicInfo = Config.getWelcomePicOld();
        if (oldPicInfo != null && oldPicInfo.getFilePath() != null) {
            showBmp = BitmapFactory.decodeFile(oldPicInfo.getFilePath());
            ivShow.setImageBitmap(showBmp);
            AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(duration);
            rlShow.startAnimation(anim);
            rlShow.setVisibility(View.VISIBLE);
        }
        pictureOk = false;
    }

    // 往下一页
    private void toNextPage(int step) {
        Intent intent = new Intent();
        switch (step) {
            case 0:
                // 1、是否显示新特性
                String currentVer = VersionUtil.getVersionName(context);
                String ignoreVer = Config.getIgnoredVersion();
                if (VersionUtil.compareVersion(currentVer, ignoreVer) > 0) {
                    Config.setIgnoreVersion(currentVer);
                    intent.setClass(context, FeatureActivity.class);
                    break;
                }
            case 1:
                // 2、是否显示欢迎页
                if (Config.getWelcomePic() != null && !StringUtil.nullOrEmpty(Config.getWelcomePic().getState())) {
                    if ("0".equals(Config.getWelcomePic().getState())) {
                        intent.setClass(context, WelcomeActivity.class);
                        break;
                    }
                }
            case 2:
                // 3、是否登录
                MyApp myApp = MyApp.getInstance();
                myApp.setUserInfo(Config.loadUserInfo());
                if (null != myApp.getUserInfo() && null != myApp.getUserInfo().getMobile()) {
                    intent.setClass(context, MainActivity.class);// 进入主页
                } else {
                    intent.setClass(context, LoginActivity.class);// 进入登录页
                }
        }
        startActivity(intent);
        finish();
    }

    // 下载图片，在回调里保存
    private void downloadPic(String url) {
        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBody> call = service.downloadFileWithUrl(url);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // 写文件
                    pictureOk = writeFile(response.body());
                    if (pictureOk) {
                        String localFilePath = getFilesDir() + "/" + WELCOME_PIC_NAME;
                        // 把本地存的状态清除，保证下次获取请求失败时不会显示
                        Config.setWelcomePicOld(new WelcomePicOldInfo(picInfo.getVersionNum(), localFilePath));
                        timer.cancel();
                        showPicture(500);
                        if (!isFinishing()) {
                            timer.start();
                        }
                    }
                } else {
                    // 网络请求
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // 下载失败
//                LogUtil.e("Welcome","Message:"+t.getMessage());
            }
        });
    }

    private boolean writeFile(ResponseBody body) {
        try {
            File file = new File(getFilesDir(), WELCOME_PIC_NAME);
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];
                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(file);

                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;
                }
                outputStream.flush();
                return true;
            } catch (IOException e) {
                LogUtil.e(TAG, "writeFile catch in:\n"+e.getMessage());
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            LogUtil.e(TAG, "writeFile catch out:\n"+e.getMessage());
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Config.delWelcomePic();
        if (showBmp != null && !showBmp.isRecycled()) {
            showBmp.recycle();
            showBmp = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
