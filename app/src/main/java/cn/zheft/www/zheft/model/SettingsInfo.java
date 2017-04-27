package cn.zheft.www.zheft.model;

/**
 * 设置信息
 * 登录请求的返回body
 * 包含手势密码和手势密码相关设置
 */
public class SettingsInfo {
    private String gesState; // 开启状态（0停/1开）
    private String gesPassword; // 手势密码
    private String gesTrail;  // 显示轨迹（0停/1显）
    private String havePayPassword; // 0无/1有

    public SettingsInfo(String gesState, String gesPassword, String gesTrail, String havePayPassword) {
        this.gesState = gesState;
        this.gesPassword = gesPassword;
        this.gesTrail = gesTrail;
        this.havePayPassword = havePayPassword;
    }

    public String getGesState() {
        return gesState;
    }

    public void setGesState(String gesState) {
        this.gesState = gesState;
    }

    public String getGesPassword() {
        return gesPassword;
    }

    public void setGesPassword(String gesPassword) {
        this.gesPassword = gesPassword;
    }

    public String getGesTrail() {
        return gesTrail;
    }

    public void setGesTrail(String gesTrail) {
        this.gesTrail = gesTrail;
    }

    public String getHavePayPassword() {
        return havePayPassword;
    }

    public void setHavePayPassword(String havePayPassword) {
        this.havePayPassword = havePayPassword;
    }
}
