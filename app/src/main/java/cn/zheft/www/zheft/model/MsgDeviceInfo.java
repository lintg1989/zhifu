package cn.zheft.www.zheft.model;

import org.litepal.crud.DataSupport;

/**
 * 设备推送消息
 * 用于交易推送与数据存储
 */
public class MsgDeviceInfo extends DataSupport {
    private String termCode;// 设备号

    public String getTermCode() {
        return termCode;
    }

    public void setTermCode(String termCode) {
        this.termCode = termCode;
    }
}
