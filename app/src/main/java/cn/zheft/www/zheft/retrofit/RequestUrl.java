package cn.zheft.www.zheft.retrofit;

import cn.zheft.www.zheft.util.StringUtil;

/**
 * 放置网络请求的地址
 */

public class RequestUrl {

    // 是否开通二维码
    public static final String IS_OPEN_QRCODE = "/onlinePay/queryIsOpenQr.htm";
    // 二维码支付主扫被扫
    public static final String QR_BEISAO_PAY = "/onlinePay/beisaoPay.htm";
    public static final String QR_ZHUSAO_PAY = "/onlinePay/zhuSaoPay.htm";

    // 查询Pos列表
    public static final String QUERY_LIST_PAY = "/onlinePay/queryPayList.htm";
    // 查询二维码交易详情
    public static final String QUERY_QRPAY_INFO = "/onlinePay/queryPay.htm";

    // 提现详情查询
    public static final String WITHDRAW_DETAIL = "/app/settle/withdraw/detail.htm";

    // 预约提现
    public static final String WITHDRAW_ORDER_ADD = "/app/settle/withdraw/add.htm";
    // 预约取消
    public static final String WITHDRAW_ORDER_CANCEL = "/app/settle/withdraw/cancel.htm";
    // 预约提现规则
    public static final String WITHDRAW_ORDER_RULES = "/app/settle/withdrawRule/list.htm";
    // 预约是否存在
    public static final String WITHDRAW_ORDER_EXIST = "/app/settle/withdrawPrep/isExist.htm";

    // 是否开通二维码（新接口）
    public static final String IS_OPEN_QRCODE_NEW = "/app/account/queryIsOpenQr.htm";
    // 老用户一键开通二维码
    public static final String OPEN_QRCODE_PAY = "/app/account/openBarcodePay.htm";
}
