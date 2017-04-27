package cn.zheft.www.zheft.util;

import java.util.List;

/**
 * 字符串转换
 */
public class StringUtil {

    // 检查字符串是否为空
    public static boolean nullOrEmpty(String s) {
        if (s == null || s.length() == 0) {
            return true;
        }
        return false;
    }

    // 如果字符串为空返回“”
    public static String nullToEmpty(String s) {
        if (nullOrEmpty(s)) {
            return "";
        }
        return s;
    }

    // 处理手势密码的字符串转换
    public static String listToStr(List<Integer> list) {
        String str = "";
        if (list == null || list.size() <= 0) {
            return str;
        }
        for (int i = 0; i < list.size(); i++) {
            str += list.get(i) + "";
        }
        return str;
    }

    // 给手机号码加星号处理
    public static String dealPhoneNum(String phone) {
        String result = "";
        if (phone != null && !phone.isEmpty()) {
            for (int i = 0; i < phone.length(); i++) {
                if (i > 2 && i < 7 ) {
                    result += "*";
                } else {
                    result += phone.charAt(i);
                }
            }
        } else {
            result = "****";
        }
        return result;
    }

    // 服务器返回的分为单位字符串转换成元为单位
    public static String fenToYuan(String fen) {
        if (nullOrEmpty(fen)) {
            return "0.00";
        }
        try {
            switch (fen.length()) {
                case 1:
                    return "0.0" + fen;
                case 2:
                    return "0."+ fen;
                default:
                    return fen.substring(0,fen.length() - 2) + "." + fen.substring(fen.length() - 2);
            }
        } catch (Exception e) {
            LogUtil.e("Catch","StringUtil.FenToYuan");
            return "0.00";
        }
    }

    // 提现时将元为单位转换成分为单位
    public static String yuanToFen(String yuan) {
        String result;
        if (nullOrEmpty(yuan)) {
            return "0";
        }
        try {
            int dotIndex = yuan.indexOf(".");
            if (dotIndex == -1) {
                result = yuan + "00";
            } else {
                switch (yuan.length() - dotIndex) {
                    case 1:
                        result = yuan.substring(0,dotIndex) + "00";
                        break;
                    case 2:
                        result = yuan.substring(0,dotIndex) + yuan.substring(dotIndex + 1) + "0";
                        break;
                    default:
                        result = yuan.substring(0,dotIndex) + yuan.substring(dotIndex + 1);
                        break;
                }
            }
            while (result.length() > 1 && "0".equals(String.valueOf(result.charAt(0)))) {
                result = result.substring(1);
            }
        } catch (Exception e) {
            LogUtil.e("Catch","StringUtil.YuanToFen");
            result = "0";
        }
        return result;
    }

    // 将分为单位的字符串转换为int型
    public static int fenToInt(String fen) {
        if (nullOrEmpty(fen)) {
            return 0;
        }
        try {
            return Integer.valueOf(fen);
        } catch (Exception e) {
            LogUtil.e("Catch","StringUtil.FenToInt");
            return 0;
        }
    }

    // 将int型转换为分为单位的字符串
    public static String intToFen(int fen) {
        try {
            return String.valueOf(fen);
        } catch (Exception e) {
            LogUtil.e("Catch","StringUtil.IntToFen");
            return "0";
        }
    }

    // 将int型转换为字符串
    public static String intToStr(int num) {
        return intToFen(num);
    }

    // 将string型转为int
    public static int strToInt(String str) {
        return fenToInt(str);
    }

    public static String checkHttpHead(String httpUrl) {
        if (httpUrl == null) {
            return "";
        }
        if (!httpUrl.contains("http://")) {
            return "http://" + httpUrl;
        }
        return httpUrl;
    }

    // 数字字串加“，”
    public static String addNumberComma(String moneyStr) {
        moneyStr = nullToEmpty(moneyStr);
        String dotPart = "";
        String intPart = "";
        String result = "";
        int dotIndex = moneyStr.indexOf(".");
        if (dotIndex > 0) {
            dotPart = moneyStr.substring(dotIndex);
            intPart = moneyStr.substring(0, dotIndex);
        } else {
            intPart = moneyStr;
        }
        int length = intPart.length();
        for (int i = 0; i < length; i++) {
            result = intPart.charAt(length - 1 - i) + result;
            if ((i + 1 < length) && ((i + 1) % 3 == 0)) {
                result = "," + result;
            }
        }
        return result + dotPart;
    }

    public static int doubleStrToInt(String str) {

        try {
            // 有可能是double、float等
            Double tmp = Double.valueOf(str);
            long result = Math.round(tmp);
            return (int) result;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }
}
