package cn.zheft.www.zheft.config;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;

import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.model.SettingsPushInfo;
import cn.zheft.www.zheft.model.UserInfo;
import cn.zheft.www.zheft.model.WelcomePicInfo;
import cn.zheft.www.zheft.model.WelcomePicOldInfo;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.SPUtil;

/**
 * Created by Administrator on 2016/5/5 0005.
 * 用于保存通用的值，例如文件名，存储目录等
 * 包含检查应用设置的方法，如手势密码、首次使用等等
 * 与toast相同，在application中传入全局context
 */
public class Config {

    /**
     * 是否是测试环境
     */
    public static final boolean DEBUG = false;

    private static Context mContext;
    //在application中调用init方法进行初始化，传入全局context
    public final static void init(Context context){
        mContext = context;
    }

    // 各种通用值
    public static String BASE_URL_DEBUG = "http://192.168.1.131:8080";    // 测试服
//    public static String BASE_URL_DEBUG = "http://116.62.25.89:8080";    // 线上测试服
//    public static String BASE_URL_DEBUG = "http://www.zheft.cn";         // 测试域名
//    public static String BASE_URL_DEBUG = "http://192.168.1.110:8080";    // 曲风服
//    public static String BASE_URL_DEBUG = "http://192.168.1.161:8080";    // Xi
//    public static String BASE_URL_DEBUG = "http://192.168.1.114:8080";    // 兵慧服
//    public static String BASE_URL_DEBUG = "http://192.168.1.178:8080";    // 飞哥
//    public static String BASE_URL_DEBUG = "http://192.168.1.162:8080";    //
    public static String BASE_URL_RELEASE = "http://www.zheft.cn";       // 域名

    public static String DEVICE_TYPE = "1"; //android设备类型默认为1
    public static String WELCOME_PIC_MODEL = "Android";// 欢迎页请求图片型号

    public static final String FILE_NAME = "shared_info";// 设置存储的文件名
    public static final String MSG_NO_NET = "网络未开启";

    public static final int HTTP_TIMEOUT = 5;// 请求超时时间，5秒
    public static final int LOCK_TIME = 60 * 1000;// 手势密码唤起时间间隔（1分钟）


    public static final String KEY_FIRST_USE = "Key_FirstUse";   // 首次使用后设为false
    public static final String KEY_USER_PHONE = "Key_UserPhone"; // 用户退出登录后不需要重输手机号
    public static final String KEY_USER_LOGIN = "Key_UserLogin"; // 对推送服务公开的数据，以确认当前登录状态
    public static final String KEY_USER_INFO = "Key_UserInfo";   // 保存用户信息
    public static final String KEY_UNMATCHED_TIME = "Key_UnmatchedTime"; // 输入错误次数
    public static final String KEY_IGNORED_VER = "Key_IgnoredVer";// 忽略的版本号
    public static final String KEY_PUSH_SETTING = "Key_PushSetting";// 保存推送消息设置

    public static final String KEY_PUSH_MESSAGE = "Key_PushToMessage";// 到Message页面

    public static final String KEY_WELCOME_PIC = "Key_WelcomePic"; // 欢迎页图片状态
    public static final String KEY_WELCOME_PIC_OLD = "Key_WelcomePicOld";// 旧图片

    public static final String KEY_FIND_LAST_UPDATE = "Key_FindLastUpdate";// “发现”上一次更新日期


    // 各种方法
    // 是否首次使用app（重置APP为初次使用状态，用户数据清空）
    public static void setUsed() {
        String ignoreVer = getIgnoredVersion();
        String userPhone = getUserPhone();
        WelcomePicOldInfo info = getWelcomePicOld();
        SPUtil.clear(mContext);
        SPUtil.put(mContext, KEY_FIRST_USE, true);
        if (ignoreVer != null) {
            setIgnoreVersion(ignoreVer);
        }
        if (userPhone != null) {
            setUserPhone(userPhone);
        }
        if (info != null) {
            setWelcomePicOld(info);
        }
        MyApp.getInstance().setUserInfo(null);
    }
    public static boolean getUsed() {
        // 由于存储只会存true，理论上只要包含就可以直接返回true
        return SPUtil.contains(mContext, KEY_FIRST_USE)
                && (boolean) SPUtil.get(mContext, KEY_FIRST_USE, SPUtil.SHARE_BOOLEAN);
    }

    // 保存手势密码剩余次数
    public static void setUnmatchedTimes(int num) {
        SPUtil.put(mContext, KEY_UNMATCHED_TIME, num);
    }
    public static int getUnmatchedTimes() {
        if (SPUtil.contains(mContext, KEY_UNMATCHED_TIME)) {
            return (int)SPUtil.get(mContext, KEY_UNMATCHED_TIME, SPUtil.SHARE_INTEGER);
        }
        return 5;// 不存在返回5（默认初始次数）
    }

    // 保存忽略的版本号
    public static void setIgnoreVersion(String version) {
        SPUtil.put(mContext, KEY_IGNORED_VER, version);
    }
    public static String getIgnoredVersion() {
        if (SPUtil.contains(mContext, KEY_IGNORED_VER)) {
            return (String)SPUtil.get(mContext, KEY_IGNORED_VER, SPUtil.SHARE_STRING);
        }
        return null;
    }

    // 保存用户登录时输入的手机号
    public static void setUserPhone(String phone) {
        SPUtil.putPublic(mContext, KEY_USER_PHONE, phone);
    }
    public static String getUserPhone() {
        if (SPUtil.containsPublic(mContext, KEY_USER_PHONE)) {
            return (String)SPUtil.getPublic(mContext, KEY_USER_PHONE, SPUtil.SHARE_STRING);
        }
        return null;
    }

    // 保存读取用户信息（退出登录时会clear userinfo字串）
    public static void saveUserInfo(UserInfo userInfo) {
        MyApp.getInstance().setUserInfo(userInfo);
        String infoStr = new Gson().toJson(userInfo);
        SPUtil.put(mContext, KEY_USER_INFO, infoStr);
        setUserLogin();
    }
    public static UserInfo loadUserInfo() {
        if (SPUtil.contains(mContext, KEY_USER_INFO)) {
            String infoStr = (String)SPUtil.get(mContext, KEY_USER_INFO, SPUtil.SHARE_STRING);
            UserInfo userInfo = new Gson().fromJson(infoStr, UserInfo.class);
            MyApp.getInstance().setUserInfo(userInfo);
            return userInfo;
        }
        return null;
    }

    // 保存用户登录状态，对外公开
    public static void setUserLogin() {
        SPUtil.putPublic(mContext, KEY_USER_LOGIN, true);
    }
    public static boolean getUserLogin() {
        return SPUtil.containsPublic(mContext, KEY_USER_LOGIN)
                && (boolean) SPUtil.getPublic(mContext, KEY_USER_LOGIN, SPUtil.SHARE_BOOLEAN);
    }
//    public static void delUserLogin() {
//        SPUtil.remove(mContext, KEY_USER_LOGIN);
//    }

    // 保存推送设置
    public static void setPushSetting(SettingsPushInfo info) {
        String infoStr = new Gson().toJson(info);
        SPUtil.putPublic(mContext, KEY_PUSH_SETTING, infoStr);
    }
    public static SettingsPushInfo getPushSetting() {
        if (SPUtil.containsPublic(mContext, KEY_PUSH_SETTING)) {
            String infoStr = (String)SPUtil.getPublic(mContext, KEY_PUSH_SETTING, SPUtil.SHARE_STRING);
            return new Gson().fromJson(infoStr, SettingsPushInfo.class);
        }
        return null;
    }

    public static void setToMessage() {
        SPUtil.putPublic(mContext, KEY_PUSH_MESSAGE, 1);
    }
    public static Integer getToMessage() {
        if (SPUtil.containsPublic(mContext, KEY_PUSH_MESSAGE)) {
            return (Integer)SPUtil.getPublic(mContext, KEY_PUSH_MESSAGE, SPUtil.SHARE_INTEGER);
        }
        return null;
    }
    public static void delToMessage() {
        if (SPUtil.containsPublic(mContext, KEY_PUSH_MESSAGE)) {
            SPUtil.remove(mContext, KEY_PUSH_MESSAGE);
        }
    }

    // 欢迎页信息
    public static void setWelcomePic(WelcomePicInfo info) {
        String infoStr = new Gson().toJson(info);
        SPUtil.put(mContext, KEY_WELCOME_PIC, infoStr);
    }
    public static WelcomePicInfo getWelcomePic() {
        if (SPUtil.contains(mContext, KEY_WELCOME_PIC)) {
            String infoStr = (String)SPUtil.get(mContext, KEY_WELCOME_PIC, SPUtil.SHARE_STRING);
            WelcomePicInfo info = new Gson().fromJson(infoStr, WelcomePicInfo.class);
            return info;
        }
        return null;
    }
    public static void delWelcomePic() {
        if (SPUtil.contains(mContext, KEY_WELCOME_PIC)) {
            SPUtil.remove(mContext, KEY_WELCOME_PIC);
        }
    }

    // 保存图片版本号
    public static void setWelcomePicOld(WelcomePicOldInfo oldInfo) {
        String infoStr = new Gson().toJson(oldInfo);
        SPUtil.put(mContext, KEY_WELCOME_PIC_OLD, infoStr);
    }
    public static WelcomePicOldInfo getWelcomePicOld() {
        if (SPUtil.contains(mContext, KEY_WELCOME_PIC_OLD)) {
            String infoStr = (String)SPUtil.get(mContext, KEY_WELCOME_PIC_OLD, SPUtil.SHARE_STRING);
            WelcomePicOldInfo info = new Gson().fromJson(infoStr, WelcomePicOldInfo.class);
            return info;
        }
        return null;
    }

    // 保存发现列表最近一次更新日期
    public static void setFindUpdDate(String date) {
        SPUtil.put(mContext, KEY_FIND_LAST_UPDATE, date);
    }
    public static String getFindUpdDate() {
        if (SPUtil.contains(mContext, KEY_FIND_LAST_UPDATE)) {
            return (String)SPUtil.get(mContext, KEY_FIND_LAST_UPDATE, SPUtil.SHARE_STRING);
        }
        return null;
    }

}
