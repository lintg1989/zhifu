package cn.zheft.www.zheft.model;

/**
 * 推送设置
 * 0停1开
 */
public class SettingsPushInfo {
    private String notify; // 是否显示通知
    private String sound;  // 是否声音提示
    private String vibrate;// 是否震动
    private String light;  // 是否亮灯（目前没用到）

    public SettingsPushInfo() {
        //
    }

    public SettingsPushInfo(String notify, String sound, String vibrate) {
        this.notify = notify;
        this.sound = sound;
        this.vibrate = vibrate;
        this.light = "1";
    }

    public String getNotify() {
        return notify;
    }

    public void setNotify(String notify) {
        this.notify = notify;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String getVibrate() {
        return vibrate;
    }

    public void setVibrate(String vibrate) {
        this.vibrate = vibrate;
    }

    public String getLight() {
        return light;
    }

    public void setLight(String light) {
        this.light = light;
    }
}
