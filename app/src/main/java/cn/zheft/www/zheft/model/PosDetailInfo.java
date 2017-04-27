package cn.zheft.www.zheft.model;

import java.io.Serializable;

/**
 * 小票详情
 */
public class PosDetailInfo implements Serializable {
    private String merchantCode; //商户编号
    private String name;         //商户名
    private String terminalCode; //终端号
    private String operatorNum;  //操作员号
    private String fromBankNum;  //发卡行号
    private String toBankNum;    //收单行号
    private String cardNum;      //卡号
    private String tradeType;    //交易类型
    private String expTime;      //有效期
    private String batchCode;    //批次号
    private String pingzhengCode;//凭证号
    private String cankaoCode;   //参考号
    private String tradeDate;    //交易日期
    private String amount;       //交易金额
    private String failedReason; //失败原因
    private String status;

    public String getMerchantCode() {
        return merchantCode;
    }

    public void setMerchantCode(String merchantCode) {
        this.merchantCode = merchantCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTerminalCode() {
        return terminalCode;
    }

    public void setTerminalCode(String terminalCode) {
        this.terminalCode = terminalCode;
    }

    public String getOperatorNum() {
        return operatorNum;
    }

    public void setOperatorNum(String operatorNum) {
        this.operatorNum = operatorNum;
    }

    public String getFromBankNum() {
        return fromBankNum;
    }

    public void setFromBankNum(String fromBankNum) {
        this.fromBankNum = fromBankNum;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(String tradeDate) {
        this.tradeDate = tradeDate;
    }

    public String getCankaoCode() {
        return cankaoCode;
    }

    public void setCankaoCode(String cankaoCode) {
        this.cankaoCode = cankaoCode;
    }

    public String getPingzhengCode() {
        return pingzhengCode;
    }

    public void setPingzhengCode(String pingzhengCode) {
        this.pingzhengCode = pingzhengCode;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public void setBatchCode(String batchCode) {
        this.batchCode = batchCode;
    }

    public String getExpTime() {
        return expTime;
    }

    public void setExpTime(String expTime) {
        this.expTime = expTime;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public String getCardNum() {
        return cardNum;
    }

    public void setCardNum(String cardNum) {
        this.cardNum = cardNum;
    }

    public String getToBankNum() {
        return toBankNum;
    }

    public void setToBankNum(String toBankNum) {
        this.toBankNum = toBankNum;
    }

    public String getFailedReason() {
        return failedReason;
    }

    public void setFailedReason(String failedReason) {
        this.failedReason = failedReason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
