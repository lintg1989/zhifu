package cn.zheft.www.zheft.model;

import java.io.Serializable;

/**
 * POS页列表信息
 */
public class PosInfo implements Serializable {
    private String ind;//交易记录ID
    private String amount;//交易金额
    private String insertDate;//交易时间
    private Integer status;//交易状态（1成功 2失败）
    private String statusName;
    private String payType;
    private Integer payTypeCode;

    public String getInd() {
        return ind;
    }

    public void setInd(String ind) {
        this.ind = ind;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getInsertDate() {
        return insertDate;
    }

    public void setInsertDate(String insertDate) {
        this.insertDate = insertDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public Integer getPayTypeCode() {
        return payTypeCode;
    }

    public void setPayTypeCode(Integer payTypeCode) {
        this.payTypeCode = payTypeCode;
    }
}
