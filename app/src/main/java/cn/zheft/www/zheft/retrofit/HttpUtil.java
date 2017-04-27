package cn.zheft.www.zheft.retrofit;

import android.util.Log;

import cn.zheft.www.zheft.util.DateUtil;
import cn.zheft.www.zheft.util.MD5Util;
import cn.zheft.www.zheft.util.StringUtil;

/**
 * 进行请求参数的加密
 */
public class HttpUtil {

    private static String zheftKey = "a987a24cdd11983e77872e61a07eea68";

    public static String getExtraKey(String...args) {
        String result = "";
        String key = zheftKey + DateUtil.getTodayStr();
        key = MD5Util.md5(key);
//        LogUtil.e("GetExtraKey","Key:"+key);
        for (String arg : args) {
            if (arg != null) {
                result += arg;
            } else {
//                LogUtil.e("GetExtraKey","Param is Null!");
            }
        }
//        LogUtil.e("GetExtraKey","Param:"+result);
        result += key;
        result = MD5Util.md5(result);
        return swap(result);
    }

    private static String swap(String str) {
        String result = "";
        for (int i = 0; i < str.length(); i++) {
            switch (i) {
                case 1:result += str.charAt(22);break;
                case 2:result += str.charAt(30);break;
                case 5:result += str.charAt(25);break;
                case 8:result += str.charAt(27);break;
                case 9:result += str.charAt(12);break;
                case 12:result += str.charAt(9);break;
                case 13:result += str.charAt(20);break;
                case 17:result += str.charAt(18);break;
                case 18:result += str.charAt(17);break;
                case 20:result += str.charAt(13);break;
                case 22:result += str.charAt(1);break;
                case 25:result += str.charAt(5);break;
                case 27:result += str.charAt(8);break;
                case 30:result += str.charAt(2);break;
                default:result += str.charAt(i);break;
            }
        }
        return result;
    }
}
