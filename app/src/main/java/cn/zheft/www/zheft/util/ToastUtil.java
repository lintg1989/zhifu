package cn.zheft.www.zheft.util;

/**
 * Created by Liaoliang on 2016/4/23.
 * Toast工具类（已单例化）
 */

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
    private static final String TAG = ToastUtil.class.getSimpleName();

    private static Context mContext;

    /** 之前显示的内容 */
    private static String oldMsg;
    /** Toast对象 */
    private static Toast toast = null ;
    /** 第一次时间 */
    private static long oneTime = 0 ;
    /** 第二次时间 */
    private static long twoTime = 0 ;

    //在application中调用init方法进行初始化，传入全局context
    public final static void init(Context context){
        mContext = context;
    }

    /**
     * 显示Toast（单例）。这段代码来源网络，其中#1部分发生条件（two - one > duration）意义不明
     * @param context
     * @param message
     * @param duration
     */
    public static void showToast(Context context, String message, int duration) {
        // 如果应用不在前台不显示toast
        if (!SysUtil.isAppForeground(mContext)) {
            return;
        }

        if (message == null) {
            LogUtil.e(TAG, "Message is null");
            return;
        }

        if (toast == null) {
            toast = Toast.makeText(context, message, duration);
//			LogUtil.e("ToastUtil","#0");
            toast.show();
            oneTime = System.currentTimeMillis();
        } else {
            twoTime = System.currentTimeMillis();
            if (message.equals(oldMsg)) {
                if ( twoTime - oneTime > duration ) {
//					LogUtil.e("ToastUtil","#1");
                    toast.show();
                }
            } else {
//				LogUtil.e("ToastUtil","#2");
                oldMsg = message;
                toast.setText(message);
                toast.show();
            }
        }
        oneTime = twoTime;
    }

    public static void showToast(Context context, int resId, int duration) {
        showToast(context, context.getString(resId), duration);
    }

    //短时间显示
    public static void showShortMessage(String message){
        showToast(mContext, message, Toast.LENGTH_SHORT);
    }

    public static void showShortMessage(int message) {
        showToast(mContext, message, Toast.LENGTH_SHORT);
    }

    //长时间显示
    public static  void showLongMessage(String message){
        showToast(mContext, message, Toast.LENGTH_LONG);
    }

    public static  void showLongMessage(int message){
        showToast(mContext, message, Toast.LENGTH_LONG);
    }

}