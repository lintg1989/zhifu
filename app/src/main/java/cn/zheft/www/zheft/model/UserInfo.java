package cn.zheft.www.zheft.model;

import java.io.Serializable;

/**
 * Created by LiaoLiang on 2016/5/5 0005.
 * 存储用户基本信息
 */
public class UserInfo implements Serializable {
    private String deviceId;
    private String type = "1";    // 设备类型（1安卓/2iOS）
    private String mobile;        // 手机号
    private String patternLock;   // 手势密码
    private String patternOn;     // 手势开关（0停1开）
    private String patternShow;   // 手势轨迹（0停1开）
    private String hasPayPwd;   // 支付密码（0无1有）

    public UserInfo(String deviceId, String mobile, SettingsInfo settingsInfo) {
        this.deviceId = deviceId;
        this.type = "1";
        this.mobile = mobile;
        this.patternLock = settingsInfo.getGesPassword();
        this.patternOn = settingsInfo.getGesState();
        this.patternShow = settingsInfo.getGesTrail();
        this.hasPayPwd = settingsInfo.getHavePayPassword();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPatternLock() {
        return patternLock;
    }

    public void setPatternLock(String patternLock) {
        this.patternLock = patternLock;
    }

    public String getPatternOn() {
        return patternOn;
    }

    public void setPatternOn(String patternOn) {
        this.patternOn = patternOn;
    }

    public String getPatternShow() {
        return patternShow;
    }

    public void setPatternShow(String patternShow) {
        this.patternShow = patternShow;
    }

    public String getHasPayPwd() {
        return hasPayPwd;
    }

    public void setHasPayPwd(String hasPayPwd) {
        this.hasPayPwd = hasPayPwd;
    }
}
