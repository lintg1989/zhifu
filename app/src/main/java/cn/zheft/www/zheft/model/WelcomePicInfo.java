package cn.zheft.www.zheft.model;

/**
 * 欢迎页面相关状态
 */
public class WelcomePicInfo {
    private String fileName;   // 文件名
    private String filePath;   // 文件路径
    private String versionNum; // 图片版本（与本地比较决定是否更新）
    private String state;      // 是否开启显示（0启用 1停用）
    private String upadteTime; // 更新时间（暂时没什么用，之前版本1.1.2）

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(String versionNum) {
        this.versionNum = versionNum;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getUpadteTime() {
        return upadteTime;
    }

    public void setUpadteTime(String upadteTime) {
        this.upadteTime = upadteTime;
    }
}
