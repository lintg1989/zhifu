package cn.zheft.www.zheft.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.zheft.www.zheft.R;

/**
 * 用于校验手机号格式、密码长度、验证码长度等
 */
public class CheckUtil {

    // 校验手机号，true为合格
    public static boolean checkPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            ToastUtil.showShortMessage("请输入手机号");
            return false;
        } else if (phone.length() < 11) {
            ToastUtil.showShortMessage("手机号错误");
            return false;
        } else if (!isMobile(phone)) {
            ToastUtil.showShortMessage("手机号错误");
            return false;
        }
        return true;
    }

    // 校验验证码格式
    public static boolean checkVerify(String verify) {
        if (verify == null || verify.isEmpty()) {
            ToastUtil.showShortMessage("请输入验证码");
            return false;
        } else if (verify.length() != 6) {
            ToastUtil.showShortMessage("验证码错误");
            return false;
        }
        return true;
    }

    // 校验密码格式
    public static boolean checkPassword(String pwd) {
        if (pwd == null || pwd.isEmpty()) {
            ToastUtil.showShortMessage("请输入密码");
            return false;
        } else if (!isPassword(pwd)){
            ToastUtil.showShortMessage("密码为6-12位大小写字母和数字");
            return false;
        }
        return true;
    }

    // 校验密码格式
    public static boolean checkOldPwd(String pwd) {
        if (pwd == null || pwd.isEmpty()) {
            ToastUtil.showShortMessage("请输入初始密码");
            return false;
        } else if (!isPassword(pwd)){
            ToastUtil.showShortMessage("初始密码为6-12位大小写字母和数字");
            return false;
        }
        return true;
    }

    // 校验密码格式
    public static boolean checkNewPwd(String pwd) {
        if (pwd == null || pwd.isEmpty()) {
            ToastUtil.showShortMessage("请输入新密码");
            return false;
        } else if (!isPassword(pwd)){
            ToastUtil.showShortMessage("新密码为6-12位大小写字母和数字");
            return false;
        }
        return true;
    }

    // 校验确认密码的格式
    public static boolean checkPwdTwice(String pwd) {
        if (pwd == null || pwd.isEmpty()) {
            ToastUtil.showShortMessage("请输入确认密码");
            return false;
        } else if (!isPassword(pwd)){
            ToastUtil.showShortMessage("确认密码为6-12位大小写字母和数字");
            return false;
        }
        return true;
    }

    // 校验密码是否一致
    public static boolean checkPwdSame(String pwd1, String pwd2) {
        if (pwd2.equals(pwd1)) {
            return true;
        } else {
            ToastUtil.showShortMessage(R.string.error_different_password);
            return false;
        }
    }

    // 校验设置支付密码格式
    public static boolean checkSetPay(String pwd, int time) {
        if (pwd == null || pwd.isEmpty()) {
            if (time == 1) {
                ToastUtil.showShortMessage("请输入支付密码");
            } else if (time == 2) {
                ToastUtil.showShortMessage("请输入确认密码");
            }
            return false;
        } else if (!isPwdPay(pwd)) {
            if (time == 1) {
                ToastUtil.showShortMessage("支付密码为6位数字");
            } else if (time == 2) {
                ToastUtil.showShortMessage("确认密码为6位数字");
            }
            return false;
        }
        return true;
    }

    // 校验更改支付密码格式
    public static boolean checkPayPwd(String pwd, int time) {
        if (pwd == null || pwd.isEmpty()) {
            if (time == 1) {
                ToastUtil.showShortMessage("请输入初始密码");
            } else if (time == 2) {
                ToastUtil.showShortMessage("请输入新密码");
            } else {
                ToastUtil.showShortMessage("请再次输入新密码");
            }
            return false;
        } else if (!isPwdPay(pwd)){
            if (time == 1) {
                ToastUtil.showShortMessage("初始支付密码为6位数字");
            } else if (time == 2) {
                ToastUtil.showShortMessage("新支付密码为6位数字");
            } else {
                ToastUtil.showShortMessage("确认密码为6位数字");
            }
            return false;
        }
        return true;
    }

    /**
     * 手机号验证
     * @param str
     * @return 验证通过返回true
     */
    public static boolean isMobile(String str) {
        Pattern p = null;
        Matcher m = null;
        boolean b = false;
        p = Pattern.compile("^[1][3,5,7,8][0-9]{9}$"); // 验证手机号
        m = p.matcher(str);
        b = m.matches();
        return b;
    }

    /**
     * 验证密码
     * @param str
     * @return
     */
    public static boolean isPassword(String str) {
        Pattern p = Pattern.compile("^[a-zA-Z0-9]{6,12}$");
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * 验证支付密码
     * @param str
     * @return
     */
    public static boolean isPwdPay(String str) {
        Pattern p = Pattern.compile("^[0-9]{6}$");
        Matcher m = p.matcher(str);
        return m.matches();
    }
}
