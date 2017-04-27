package cn.zheft.www.zheft.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Created by Administrator on 2016/4/28 0028.
 * 工具类，用于检测应用是否具备某项权限
 * 在Android M版本及以上需要确认权限
 * 低版本直接返回true
 */
public class PermissionUtil {
    private static boolean checkBuildVersion() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean checkCanCall(Context context) {
        if (checkBuildVersion()) {
            return context.checkSelfPermission(Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }
}
