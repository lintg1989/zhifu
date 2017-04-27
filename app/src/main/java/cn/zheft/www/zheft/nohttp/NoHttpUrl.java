package cn.zheft.www.zheft.nohttp;

import cn.zheft.www.zheft.BuildConfig;
import cn.zheft.www.zheft.config.Config;

/**
 * 放置网络请求的地址
 */

public class NoHttpUrl {

    private static String getBaseUrl() {
        String baseUrl = null;
        if (BuildConfig.DEBUG) {
            baseUrl = Config.BASE_URL_DEBUG + "/posp-admin";
        } else {
            baseUrl = Config.BASE_URL_RELEASE;
        }
        return baseUrl;
    }

    // 是否开通二维码
    public static final String IS_OPEN_QRCODE = getBaseUrl() + "/onlinePay/queryIsOpenQr.htm";
    // 二维码支付主扫被扫
    public static final String QR_BEISAO_PAY = getBaseUrl() + "/onlinePay/beisaoPay.htm";
    public static final String QR_ZHUSAO_PAY = getBaseUrl() + "/onlinePay/zhuSaoPay.htm";

    // 查询Pos列表
    public static final String QUERY_LIST_PAY = getBaseUrl() + "/onlinePay/queryPayList.htm";
    // 查询二维码交易详情
    public static final String QUERY_QRPAY_INFO = getBaseUrl() + "/onlinePay/queryPay.htm";

    // 提现详情查询
    public static final String WITHDRAW_DETAIL = getBaseUrl() + "/app/settle/withdraw/detail.htm";

    // 预约提现
    public static final String WITHDRAW_ORDER_ADD = getBaseUrl() + "/app/settle/withdraw/add.htm";
    // 预约取消
    public static final String WITHDRAW_ORDER_CANCEL = getBaseUrl() + "/app/settle/withdraw/cancel.htm";
    // 预约提现规则
    public static final String WITHDRAW_ORDER_RULES = getBaseUrl() + "/app/settle/withdrawRule/list.htm";
    // 预约是否存在
    public static final String WITHDRAW_ORDER_EXIST = getBaseUrl() + "/app/settle/withdrawPrep/isExist.htm";

    // 是否开通二维码（新接口）
    public static final String IS_OPEN_QRCODE_NEW = getBaseUrl() + "/app/account/queryIsOpenQr.htm";
    // 老用户一键开通二维码
    public static final String OPEN_QRCODE_PAY = getBaseUrl() + "/app/account/openBarcodePay.htm";
}
