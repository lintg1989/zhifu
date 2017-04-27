package cn.zheft.www.zheft.model;

/**
 * 提现规则说明等
 */
public class WithdrawInfo {
    private String amountLimit;// 单笔限额
    private String remainTimes;// 剩余可提现次数
    private String remark;     // 提现规则说明

    public String getAmountLimit() {
        return amountLimit;
    }

    public void setAmountLimit(String amountLimit) {
        this.amountLimit = amountLimit;
    }

    public String getRemainTimes() {
        return remainTimes;
    }

    public void setRemainTimes(String remainTimes) {
        this.remainTimes = remainTimes;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
