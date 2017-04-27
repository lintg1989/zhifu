package cn.zheft.www.zheft.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/5/10 0010.
 * 银行卡列表信息
 */
public class BankcardInfo implements Serializable {
    private String code; // 内部商户号
    private String card; //卡号
    private String bankcardName; //卡名
    private String bankCode; //银行编码

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCard() {
        return card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankcardName() {
        return bankcardName;
    }

    public void setBankcardName(String bankcardName) {
        this.bankcardName = bankcardName;
    }
}
