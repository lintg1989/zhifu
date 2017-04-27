package cn.zheft.www.zheft.model;

import java.io.Serializable;

/**
 * 二维码支付结果信息
 */

public class QRPayInfo implements Serializable {
    private String tn;        //第三方平台受理订单号（该值未返回）
    private String txnType;   // 支付类型
    private String txnTime;   // 订单创建时间
    private String merOrderId;// 订单编号
    private String txnAmt;    // 交易金额
    private String respCode;  // 交易状态
    private String respMsg;   // 交易信息
    private String succTime; // 付款时间

    public String getTn() {
        return tn;
    }

    public void setTn(String tn) {
        this.tn = tn;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getTxnTime() {
        return txnTime;
    }

    public void setTxnTime(String txnTime) {
        this.txnTime = txnTime;
    }

    public String getMerOrderId() {
        return merOrderId;
    }

    public void setMerOrderId(String merOrderId) {
        this.merOrderId = merOrderId;
    }

    public String getTxnAmt() {
        return txnAmt;
    }

    public void setTxnAmt(String txnAmt) {
        this.txnAmt = txnAmt;
    }

    public String getRespCode() {
        return respCode;
    }

    public void setRespCode(String respCode) {
        this.respCode = respCode;
    }

    public String getRespMsg() {
        return respMsg;
    }

    public void setRespMsg(String respMsg) {
        this.respMsg = respMsg;
    }

    public String getSuccTime() {
        return succTime;
    }

    public void setSuccTime(String succTime) {
        this.succTime = succTime;
    }
}
