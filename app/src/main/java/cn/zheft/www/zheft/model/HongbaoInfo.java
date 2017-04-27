package cn.zheft.www.zheft.model;

import java.io.Serializable;

/**
 * 红包信息
 */
public class HongbaoInfo implements Serializable {
    private String hongbaoCount; // 待领取红包数，没有则返回0
    private String hongbaoCode;  // 红包编码固定为0
    private String hongbaoType;  // 红包类型：1激活商户红包
    private String money;        // 红包面额

    public String getHongbaoCount() {
        return hongbaoCount;
    }

    public void setHongbaoCount(String hongbaoCount) {
        this.hongbaoCount = hongbaoCount;
    }

    public String getHongbaoCode() {
        return hongbaoCode;
    }

    public void setHongbaoCode(String hongbaoCode) {
        this.hongbaoCode = hongbaoCode;
    }

    public String getHongbaoType() {
        return hongbaoType;
    }

    public void setHongbaoType(String hongbaoType) {
        this.hongbaoType = hongbaoType;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }
}
