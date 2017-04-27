package cn.zheft.www.zheft.model;

import java.io.Serializable;

/**
 * 提现明细列表
 */
public class BalanceInfo implements Serializable {
    private String id;         // id
    private String createTime; // 刷卡时间
    private String title;      // 行为
    private String amount;     // 金额
    private String fund;       // 可提现余额
    private Integer dataType;   // 行为类型
    private Integer tradeType;  // 交易类型
    private Integer statusCode; // 状态码（类型为4：预约提现时，状态码3表示处理成功，此时显示金额）
    private String status;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getFund() {
        return fund;
    }

    public void setFund(String fund) {
        this.fund = fund;
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public Integer getTradeType() {
        return tradeType;
    }

    public void setTradeType(Integer tradeType) {
        this.tradeType = tradeType;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
