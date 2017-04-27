package cn.zheft.www.zheft.model;

/**
 * 版本信息
 */
public class VersionInfo {
    private String haveNewVersion;
    private String forceUpdate;
    private String version;
    private String remark;
    private String downloadUrl;

    public String getHaveNewVersion() {
        return haveNewVersion;
    }

    public void setHaveNewVersion(String haveNewVersion) {
        this.haveNewVersion = haveNewVersion;
    }

    public String getForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(String forceUpdate) {
        this.forceUpdate = forceUpdate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
