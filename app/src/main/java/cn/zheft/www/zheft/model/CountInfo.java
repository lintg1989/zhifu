package cn.zheft.www.zheft.model;

/**
 * 账户信息
 * 包含账户类型、消费金额、消费笔数
 */
public class CountInfo {
    private String type; // 账户类型（1普通pos/2二维码）
    private String totalAmount; // 消费金额
    private String tradeCount;  // 消费笔数

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getTradeCount() {
        return tradeCount;
    }

    public void setTradeCount(String tradeCount) {
        this.tradeCount = tradeCount;
    }
}
