package cn.zheft.www.zheft.retrofit;

import cn.zheft.www.zheft.BuildConfig;
import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.ToastUtil;

/**
 * 网络请求时返回的执行码
 */
public class ResultCode {

    public static String RESULT_SUCCESS = "0";//网络请求返回码成功值

//    public static String RESULT_CODE_ILLEGAL = "100"; //非法请求
//    public static String RESULT_CODE_SINGLE = "101"; //登录冲突

//    public static boolean resultCode(String code) {
//
//    }

    // 网络请求返回码 404、400等
    public static void responseCode(int code) {
        ToastUtil.showShortMessage("网络异常");
//        switch (code) {
//            case 404:
//                ToastUtil.showShortMessage("Not Found");
//                break;
//            case 500:
//                ToastUtil.showShortMessage("Internal Server Error");
//                break;
//            default:
//                ToastUtil.showShortMessage("请求失败，请重试");
//                break;
//        }
    }

    // 网络请求failure操作
    public static void callFailure(String msg) {
        ToastUtil.showShortMessage(R.string.error_poor_network_environment);
    }

    // 调取验证码得到的返回值
    public static void verifyCode(String resultCode) {
        switch (resultCode) {
            case "0":
                ToastUtil.showShortMessage("验证码已发送");
                break;
            case "1":
                ToastUtil.showShortMessage("系统异常");
                break;
            case "2":
                ToastUtil.showShortMessage("手机号码为空");
                break;
            case "3":
                ToastUtil.showShortMessage("商户不存在");
                break;
            case "4":
                ToastUtil.showShortMessage("已认领，请直接登录");
                break;
            case "5":
                ToastUtil.showShortMessage("未认领，请先认领新用户");
                break;
            case "6":
                ToastUtil.showShortMessage("获取验证码失败");
                break;
            case "7":
                ToastUtil.showShortMessage("今日验证次数过多");
                break;
            default:
                ToastUtil.showShortMessage("未知错误");
                break;
        }
    }

    // 认领异常返回值
    public static void claimCode(String resultCode) {
        switch (resultCode) {
            case "1":
                ToastUtil.showShortMessage("系统异常");
                break;
            case "2":
                ToastUtil.showShortMessage("手机号码为空");
                break;
            case "3":
                ToastUtil.showShortMessage("验证码错误");
                break;
            case "4":
                ToastUtil.showShortMessage("手机号码不存在");
                break;
            default:
                ToastUtil.showShortMessage("未知错误");
                break;
        }
    }

    public static void modifyPwd(String resultCode) {
        switch (resultCode) {
            case "1":
                ToastUtil.showShortMessage("系统异常");
                break;
            case "2":
                ToastUtil.showShortMessage("手机号码为空");
                break;
            case "3":
                ToastUtil.showShortMessage("原始密码错误");
                break;
            default:
                ToastUtil.showShortMessage("未知错误，请重试");
                break;
        }
    }

    public static void withdraw(String resultCode) {
        switch (resultCode) {
            case "1":
                ToastUtil.showShortMessage("系统异常");
                break;
            case "2":
                ToastUtil.showShortMessage("手机号码为空");
                break;
            case "3":
                ToastUtil.showShortMessage("支付密码错误");
                break;
            case "4":
                ToastUtil.showShortMessage("支付密码为空");
                break;
            case "5":
            case "6":
            case "7":
            case "8":
            case "9":
                ToastUtil.showShortMessage("插入提现记录失败");
                break;
            case "10":
                ToastUtil.showShortMessage("不符合提现规则");
                break;
            case "11":
                ToastUtil.showShortMessage("超出单笔提现最大值");
                break;
            case "12":
                ToastUtil.showShortMessage("超出单日提现次数");
                break;
            default:
                ToastUtil.showShortMessage("未知错误，请重试");
                break;
        }
    }

    // 账户信息
    public static void accountInfo(String resultCode) {
        switch (resultCode) {
            case "1":
                ToastUtil.showShortMessage("系统异常");
                break;
            case "2":
                ToastUtil.showShortMessage("手机号码为空");
                break;
            case "3":
                ToastUtil.showShortMessage("手机号对应内部商户号不存在");
                break;
            case "4":
                ToastUtil.showShortMessage("内部商户号对应appUserId不存在");
                break;
            case "5":
                ToastUtil.showShortMessage("交易记录不存在对应的内部商户号");
                break;
            default:
                ToastUtil.showShortMessage("未知错误，请重试");
                break;
        }
    }

    // 提现信息与文案
    public static void withdrawInfo(String resultCode) {
        switch (resultCode) {
            case "2":
                ToastUtil.showShortMessage("手机号码为空");
                break;
            case "3":
                ToastUtil.showShortMessage("提现规则为空");
                break;
            default:
                ToastUtil.showShortMessage("未知错误，请重试");
                break;
        }
    }

    // 账户明细
    public static void withdrawDetail(String resultCode) {
        switch (resultCode) {
            case "2":
                ToastUtil.showShortMessage("手机号码为空");
                break;
            case "3":
                ToastUtil.showShortMessage("不存在对应的内部商户号");
                break;
            default:
                ToastUtil.showShortMessage("未知错误，请重试");
                break;
        }
    }

    // 七天交易统计图表
    public static void chartDays(String resultCode) {
        switch (resultCode) {
            case "2":
                ToastUtil.showShortMessage("开始时间为空");
                break;
            case "3":
                ToastUtil.showShortMessage("无商户信息");
                break;
            case "4":
                ToastUtil.showShortMessage("时间段为空");
                break;
            default:
                ToastUtil.showShortMessage("获取图表信息失败");
        }
    }

    public static void openHongbao(String resultCode) {
        switch (resultCode) {
            case "2":
                ToastUtil.showShortMessage("参数为空");
                break;
            case "3":
                ToastUtil.showShortMessage("红包活动已结束");
                break;
            case "4":
                ToastUtil.showShortMessage("无效红包");
                break;
            default:
                ToastUtil.showShortMessage("红包未知错误");
        }
    }
}
