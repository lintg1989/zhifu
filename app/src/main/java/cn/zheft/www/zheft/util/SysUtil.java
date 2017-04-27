package cn.zheft.www.zheft.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

import java.util.List;

/**
 * 判断应用是否在前台
 */
public class SysUtil {

    //在进程中去寻找当前APP的信息，判断是否在前台运行
    public static boolean isAppForeground(Context context) {
        try {
            String packageName = context.getPackageName();
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningAppProcessInfo> appProcesses = manager.getRunningAppProcesses();
            if (appProcesses != null && !appProcesses.isEmpty()) {
                for (RunningAppProcessInfo appProcess : appProcesses) {
                    if (appProcess.processName.equals(packageName)
                            && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // 判断应用是否存活
    public static boolean isAppAlive(Context context) {
        //
        if (context == null) {
            return false;
        }
        String packageName = "cn.zheft.www.zheft";

        ActivityManager manager = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);

        if (manager != null) {
            try {
                List<ActivityManager.RunningAppProcessInfo> processInfos = manager.getRunningAppProcesses();
                if (processInfos != null && !processInfos.isEmpty()) {
                    for (RunningAppProcessInfo info : processInfos) {
                        if (packageName.equals(info.processName)) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {

            }
        }
        return false;
    }

}
