package cn.zheft.www.zheft.util;

import android.util.Log;

import java.lang.reflect.Field;

import cn.zheft.www.zheft.BuildConfig;

/**
 * Log信息
 */
public class LogUtil {

    public static void v(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.w(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, msg);
        }
    }

    public static String getFields(Object object) {
        if (object == null) {
            return "Null in LogUtil.getFields()";
        }
        String result = "-------- " + object.getClass().getName() + " --------\n";

        Class<?> c = null;
        try {
            c = Class.forName(object.getClass().getName());
            Field[] fields = c.getDeclaredFields();
            for (Field f : fields) {
                // 设为可访问
                f.setAccessible(true);
                String field = f.toString().substring(f.toString().lastIndexOf(".") + 1);
                result += field + ":  " + f.get(object) + "\n";
            }
            result += "-------- finish --------\n";
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            result += "-------- class not found --------\n";
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            result += "-------- illegal access exception --------\n";
        }

        return result;
    }
}
