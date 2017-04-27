package cn.zheft.www.zheft.model;

/**
 * 流水页供选择的设备列表信息，包含：
 * 1、内部终端号
 * 2、名称
 * 3、所选设备标识（未选中/选中）
 * 4、消息标识（无消息/有消息）
 */
public class DeviceSelectInfo {
    private String code;
    private String name;
    private boolean selected;
    private boolean hasMsg;

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

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isHasMsg() {
        return hasMsg;
    }

    public void setHasMsg(boolean hasMsg) {
        this.hasMsg = hasMsg;
    }
}
