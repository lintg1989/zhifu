package cn.zheft.www.zheft.model;

/**
 * 保存旧地址信息等
 */
public class WelcomePicOldInfo {
    private String versionNum;
    private String filePath;

    public WelcomePicOldInfo(String versionNum, String filePath) {
        this.versionNum = versionNum;
        this.filePath = filePath;
    }

    public String getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(String versionNum) {
        this.versionNum = versionNum;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
