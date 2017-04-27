package cn.zheft.www.zheft.app;

import android.app.Activity;
import android.app.Application;

import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.uuzuche.lib_zxing.activity.ZXingLibrary;
import com.yolanda.nohttp.Logger;
import com.yolanda.nohttp.NoHttp;

import org.litepal.LitePalApplication;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import cn.zheft.www.zheft.BuildConfig;
import cn.zheft.www.zheft.MyPushIntentService;
import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.DeviceInfo;
import cn.zheft.www.zheft.model.ImagePathInfo;
import cn.zheft.www.zheft.model.SelectDateInfo;
import cn.zheft.www.zheft.model.UserInfo;
import cn.zheft.www.zheft.model.VersionInfo;
import cn.zheft.www.zheft.util.DateUtil;
import cn.zheft.www.zheft.util.DeviceUtil;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.StringUtil;
import cn.zheft.www.zheft.util.ToastUtil;

/**
 * Created by Administrator on 2016/4/23 0023.
 * 自定义application，本身为单例类但这里使用单例的方式获取唯一实例
 */
public class MyApp extends Application {
    private static final String TAG = MyApp.class.getSimpleName();
    private static MyApp instance = null;
    // 用于保存所有的单例activity，在手势密码页重写onBackPressed进行应用退出
    private List<Activity> activityList = new LinkedList<>();

    public static MyApp getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //初始化nohttp
        NoHttp.init(this);

        //开启调试模式，可以看到日志
        Logger.setTag("NoHttp");
        Logger.setDebug(BuildConfig.DEBUG);

        instance = this;
        LitePalApplication.initialize(this);
        Config.init(this);
        ToastUtil.init(this);
        // 二维码
        ZXingLibrary.initDisplayOpinion(this);

        deviceId = DeviceUtil.getUniquePsuedoID();

        // 友盟推送
        PushAgent mPushAgent = PushAgent.getInstance(this);
        mPushAgent.register(new IUmengRegisterCallback() {
            @Override
            public void onSuccess(String s) {
                LogUtil.e(TAG, "Token:"+s);
                deviceToken = s;
            }

            @Override
            public void onFailure(String s, String s1) {
                ToastUtil.showShortMessage("Token获取失败，可能会影响推送功能");
            }
        });
        mPushAgent.setDebugMode(false);
        mPushAgent.setDisplayNotificationNumber(5);
        mPushAgent.setPushIntentServiceClass(MyPushIntentService.class);
    }

    // 添加activity到容器中
    public void addActivity(Activity activity) {
//        LogUtil.e(TAG, "Add:" + activity.getClass().getSimpleName());
        activityList.add(activity);
    }

    // 删除activity，否则会一直持有activity对象
    public void delActivity(Activity activity) {
//        LogUtil.e(TAG, "Del:" + activity.getClass().getSimpleName());
        activityList.remove(activity);
    }

    // 遍历所有activity并finish
    public void exitApp() {
        for (Activity activity : activityList) {
            activity.finish();
        }
        activityList.clear();
        System.gc();// 要不要回收一次垃圾？
    }

    // 按数量删除列表里后加入的activity（包括当前activity）
    public void finishActivitys(int num) {
        int size = activityList.size();
        if (num > 0 && num < size) {
            for (int i = 0; i < num; i++) {
                // finish会调用到delActivity
//                LogUtil.e(TAG, "Fin:" + activityList.get(size - 1 - i).getClass().getSimpleName());
                activityList.get(size - 1- i).finish();
            }
        }
    }

    /**
     * ----------------------- 保存一些公共的变量和方法 ------------------------------
     */
    private ImagePathInfo imagePathInfo = new ImagePathInfo();//照片路劲

    private Long lockShowTime;// 保存当前时间
    private String deviceId;//设备ID
    private String deviceToken;// Umeng Token
    private UserInfo userInfo;
    private VersionInfo versionInfo = null;//版本信息

    private String innerTermCode = "all"; // 内部终端号（即设备号）需初始为“all”
    private List<DeviceInfo> selectedDevice = new ArrayList<>();
    private SelectDateInfo selectedDate;

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Long getLockShowTime() {
        return lockShowTime;
    }

    public void setLockShowTime(Long lockShowTime) {
        this.lockShowTime = lockShowTime;
    }

    public VersionInfo getVersionInfo() {
        return versionInfo;
    }

    public void setVersionInfo(VersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }

    public String getInnerTermCode() {
        return innerTermCode;
    }

    public void setInnerTermCode(String innerTermCode) {
        this.innerTermCode = innerTermCode;
    }

    public String getDeviceToken() {
        if (StringUtil.nullOrEmpty(deviceToken)) {
            LogUtil.e(TAG, "友盟Token为空");
        }
        return deviceToken;
    }

    public List<DeviceInfo> getSelectedDevice() {
        initSelectedDevice();
        return selectedDevice;
    }

    public void setSelectedDevice(List<DeviceInfo> selectedDevice) {
        this.selectedDevice = selectedDevice;
    }

    public SelectDateInfo getSelectedDate() {
        initSelectedDate();
        return selectedDate;
    }

    public void setSelectedDate(SelectDateInfo selectDate) {
        this.selectedDate = selectDate;
    }

    private void initSelectedDevice() {
        if (selectedDevice == null) {
            selectedDevice = new ArrayList<>();
        }
        if (selectedDevice.isEmpty()) {
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.setName( getString(R.string.select_device_label_all_name) );
            deviceInfo.setCode( getString(R.string.select_device_label_all_code) );
            selectedDevice.add(deviceInfo);
        }
    }

    private void initSelectedDate() {
        if (selectedDate == null) {
            selectedDate = new SelectDateInfo(null, null, SelectDateInfo.DATE_LABEL_NULL);
        }
    }


    public ImagePathInfo getImagePathInfo() {
        return imagePathInfo;
    }

    public void setImagePathInfo(ImagePathInfo imagePathInfo) {
        this.imagePathInfo = imagePathInfo;
    }
}
