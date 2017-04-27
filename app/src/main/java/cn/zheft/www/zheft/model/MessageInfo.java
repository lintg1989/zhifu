package cn.zheft.www.zheft.model;

import org.litepal.crud.DataSupport;

import java.io.Serializable;

/**
 * 消息
 * 用于系统消息与数据库存储
 */
public class MessageInfo extends DataSupport implements Serializable{
    private long id;  // 数据库中使用的id
    private String phone;
    private String type;
    private String date;
    private String content;
    private boolean read;  // 是否读取

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
