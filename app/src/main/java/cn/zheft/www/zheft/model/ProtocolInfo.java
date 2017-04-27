package cn.zheft.www.zheft.model;

/**
 * 文案
 */
public class ProtocolInfo {

    private String title; // 协议标题
    private String content; // 内容
    private String name;   // 附加值

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
