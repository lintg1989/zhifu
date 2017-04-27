package cn.zheft.www.zheft.model;

/**
 * 账户信息
 */
public class AccountInfo {
    private String incomeToday;    // 今日入账
    private String incomeMonth;    // 当月入账
    private String fund;           // 可提现余额
    private String incomeTomorrow; // 明日到账

    public String getIncomeToday() {
        return incomeToday;
    }

    public void setIncomeToday(String incomeToday) {
        this.incomeToday = incomeToday;
    }

    public String getIncomeMonth() {
        return incomeMonth;
    }

    public void setIncomeMonth(String incomeMonth) {
        this.incomeMonth = incomeMonth;
    }

    public String getFund() {
        return fund;
    }

    public void setFund(String fund) {
        this.fund = fund;
    }

    public String getIncomeTomorrow() {
        return incomeTomorrow;
    }

    public void setIncomeTomorrow(String incomeTomorrow) {
        this.incomeTomorrow = incomeTomorrow;
    }
}
