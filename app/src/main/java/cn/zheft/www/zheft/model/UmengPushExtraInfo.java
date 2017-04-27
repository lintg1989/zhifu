package cn.zheft.www.zheft.model;

/**
 * 用于接收Umeng推送的自定义字段
 */
public class UmengPushExtraInfo {
    private String msgType;
    private String sendTime;
    private String innerTermCode;
    private String titleType;

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getInnerTermCode() {
        return innerTermCode;
    }

    public void setInnerTermCode(String innerTermCode) {
        this.innerTermCode = innerTermCode;
    }

    public String getTitleType() {
        return titleType;
    }

    public void setTitleType(String titleType) {
        this.titleType = titleType;
    }
}
