package cn.zheft.www.zheft.ui;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import cn.zheft.www.zheft.BuildConfig;
import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.SettingsPushInfo;
import cn.zheft.www.zheft.model.VersionInfo;
import cn.zheft.www.zheft.model.WelcomePicInfo;
import cn.zheft.www.zheft.retrofit.HttpUtil;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.ResponseBase;
import cn.zheft.www.zheft.retrofit.ResultCode;
import cn.zheft.www.zheft.retrofit.RetrofitAPI;
import cn.zheft.www.zheft.util.CountDownTimerUtil;
import cn.zheft.www.zheft.util.DeviceUtil;
import cn.zheft.www.zheft.util.StringUtil;
import cn.zheft.www.zheft.util.ToastUtil;
import cn.zheft.www.zheft.util.VersionUtil;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 启动页
 */
public class SplashActivity extends BaseActivity {
    private Context context;
    private static int WAITING_TIME = 2000;// 启动页最大等待时间

    private MyApp myApp = null;
//    private Bitmap logoBitmap = null;

//    private boolean loadingOk = false; // 读取缓存完成
    private boolean checkPicOk = false;// 检查新图片请求完成
    private boolean firstInstall = false;// 首次安装

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTransScreen();// 透明状态栏和虚拟键
        setContentView(R.layout.activity_splash);
        disablePatternLock();// 启动页是直接跳转至手势密码页，无需在onResume时启用
        context = this;
        myApp = MyApp.getInstance();
        myApp.setDeviceId(DeviceUtil.getUniquePsuedoID()); // 获取设备码

        timer.start();// 开启倒计时

        clearNotify();
        // 初始化推送设置
        initPushSetting();

        // 初始化view
        initView();
        // 初始化数据
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    private static final int PERMISSION_REQUEST_UMENG = 1;

    private void initPushSetting() {
        if (Config.getPushSetting() == null) {
            Config.setPushSetting(new SettingsPushInfo("1","1","1"));
        }
    }

    private void initView() {
//        try {
//            logoBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.splash_logo);
//            ImageView logoView = (ImageView) findViewById(R.id.iv_splash_logo);
//            logoView.setImageBitmap(logoBitmap);
//            loadingOk = true;
//        } catch (Exception e) {
//            //
//        }
        // 标识Debug版本与服务器
        if (BuildConfig.DEBUG) {
            ((TextView) findViewById(R.id.tv_debug_sign))
                    .setText(context.getString(R.string.test_debug_server) + Config.BASE_URL_DEBUG);
        }
    }

    //读取时间小于1s按1s显示，大于1s按读取时间显示
    private void initData() {
        getVersionCode();
        checkWelcomePic(); // 检查是否有新欢迎图片

        if (!Config.getUsed()) {
            Config.setUsed();
            Config.setIgnoreVersion("1.0.0");
            firstInstall = true;
        }
    }

    // 计时器总共两秒，每隔一定时间判断一次网络请求是否完成
    private CountDownTimerUtil timer = new CountDownTimerUtil(2000, 300) {
        @Override
        public void onTick(long millisUntilFinished) {
            if (millisUntilFinished >= 1200) {
                return;
            }
            checkLoadingStatus(false);
        }

        @Override
        public void onFinish() {
            checkLoadingStatus(true);
        }
    };

    // 获取版本号
    private void getVersionCode() {
        if (nonNetwork()) {
            return;
        }

        String cipher = HttpUtil.getExtraKey(
                myApp.getDeviceId(),
                Config.DEVICE_TYPE,
                VersionUtil.getVersionName(context));

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<VersionInfo>> call = service.postCheckUpdate(
                myApp.getDeviceId(),
                Config.DEVICE_TYPE,
                VersionUtil.getVersionName(context),
                cipher );
        call.enqueue(new Callback<ResponseBase<VersionInfo>>() {
            @Override
            public void onResponse(Call<ResponseBase<VersionInfo>> call, final Response<ResponseBase<VersionInfo>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        // 请求成功
                        if (!isFinishing()) {
                            checkUpdate(response.body().getData());
                        }
                    } else {
                        if (!resultCode(response.body().getResultCode())) {
                            // 解析码
                        }
                    }
                } else {
//                    ResultCode.responseCode(response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBase<VersionInfo>> call, Throwable t) {
//                ResultCode.callFailure(t.getMessage());
            }
        });
    }

    private void checkUpdate(VersionInfo info) {
        if (info == null) {
            return;
        }
        String curVer = VersionUtil.getVersionName(context);
        String ignVer = Config.getIgnoredVersion();
        if (ignVer != null
                && info.getVersion() != null
                && info.getVersion().equals(ignVer)) {
            return;
        }
        if (curVer.equals(info.getVersion())) {
            Config.setIgnoreVersion(info.getVersion());// 把当前版本号设为忽略版本号
            return;
        }
        if (VersionUtil.compareVersion(curVer, info.getVersion()) >0) {
            myApp.setVersionInfo(info);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (logoBitmap != null && !logoBitmap.isRecycled()) {
//            logoBitmap.recycle();
//            logoBitmap = null;
//        }
    }

    private void clearNotify() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.cancelAll();
    }

    /**
     * 网络请求，检查是否要下载新图片
     */
    private void checkWelcomePic() {
        if (nonNetwork()) {
            return;
        }

        String cipher = HttpUtil.getExtraKey(
                myApp.getDeviceId(),
                Config.DEVICE_TYPE,
                Config.WELCOME_PIC_MODEL );

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<WelcomePicInfo>> call = service.postWelcomePic(
                myApp.getDeviceId(),
                Config.DEVICE_TYPE,
                Config.WELCOME_PIC_MODEL,
                cipher );
        call.enqueue(new Callback<ResponseBase<WelcomePicInfo>>() {
            @Override
            public void onResponse(Call<ResponseBase<WelcomePicInfo>> call, Response<ResponseBase<WelcomePicInfo>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        // 请求成功
                        if (!isFinishing()) {
                            changeNewPicStatus(response.body().getData());
                        }
                    } else {
                        if (!resultCode(response.body().getResultCode())) {
                            // 解析码
                        }
                    }
                } else {
//                    ResultCode.responseCode(response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBase<WelcomePicInfo>> call, Throwable t) {
                // do nothing
            }
        });
    }

    // 更改新图片状态
    private void changeNewPicStatus(WelcomePicInfo info) {
        if (info == null) {
            return;
        }

        Config.setWelcomePic(info);
        checkPicOk = true;
    }

    // 加载是否完成
    private void checkLoadingStatus(boolean forceStop) {
        if (forceStop || checkPicOk) {
            if (timer != null) {
                timer.cancel();
            }
            // 询问权限
            PermissionGen.with(this).addRequestCode(PERMISSION_REQUEST_UMENG)
                    .permissions(
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE )
                    .request();

            // 下一页
//            toNextPage(0);
        }
    }

    @PermissionSuccess(requestCode = PERMISSION_REQUEST_UMENG)
    public void permissionSuccess() {
        toNextPage(0);
    }

    @PermissionFail(requestCode = PERMISSION_REQUEST_UMENG)
    public void permissionFailed() {
        ToastUtil.showShortMessage("「状态信息」和「存储」权限被禁止\n可能会影响部分功能");
        toNextPage(0);
    }

    // 往下一页
    private void toNextPage(int step) {
        Intent intent = new Intent();
        switch (step) {
            case 0:
                // 1、是否显示新特性
                String currentVer = VersionUtil.getVersionName(context);
                String ignoreVer = Config.getIgnoredVersion();
                if (firstInstall || VersionUtil.compareVersion(ignoreVer, currentVer) > 0) {
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
}
