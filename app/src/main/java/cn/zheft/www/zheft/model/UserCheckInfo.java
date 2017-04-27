package cn.zheft.www.zheft.model;

import java.io.Serializable;

/**
 * 商户确认信息
 */
public class UserCheckInfo implements Serializable {
    private String isVerified;     // 0未核对/1已核对
    private String merLegalPerson; // 法人
    private String merchantName;   // 商户名称
    private String paperNum;       // 证件号
    private String accountNo;      // 开户账号

    public String getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(String isVerified) {
        this.isVerified = isVerified;
    }

    public String getMerLegalPerson() {
        return merLegalPerson;
    }

    public void setMerLegalPerson(String merLegalPerson) {
        this.merLegalPerson = merLegalPerson;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getPaperNum() {
        return paperNum;
    }

    public void setPaperNum(String paperNum) {
        this.paperNum = paperNum;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }
}
