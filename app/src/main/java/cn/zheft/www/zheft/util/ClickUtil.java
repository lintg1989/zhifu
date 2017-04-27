package cn.zheft.www.zheft.util;

/**
 * Created by Administrator on 2016/6/7 0007.
 */
public class ClickUtil {
    private static long lastClickTime;

    // 防止重复点击
    public static boolean cantClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if ( 0 < timeD && timeD < 500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }
}
