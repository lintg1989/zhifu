package cn.zheft.www.zheft.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/5/7 0007.
 * 设备列表信息，包含：
 * 1、内部终端号
 * 2、名称
 * 3、默认设备标识（0非默认/1默认）
 */
public class DeviceInfo implements Serializable {
    private String code;
    private String name;
    private String status;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
