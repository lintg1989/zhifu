package cn.zheft.www.zheft.model;

/**
 * 设备详情
 */
public class DeviceDetailInfo {
    private String termName; // 设备名称
    private String company;  // 厂家
    private String code;     // 机具型号
    private String sn;       // sn码

    public String getTermName() {
        return termName;
    }

    public void setTermName(String termName) {
        this.termName = termName;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }
}
