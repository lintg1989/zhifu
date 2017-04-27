package cn.zheft.www.zheft.model;

/**
 * 过去七天交易数据
 */
public class DayTradeInfo {
    private String date;
    private String amount;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
