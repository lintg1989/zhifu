package cn.zheft.www.zheft.model;

import org.litepal.crud.DataSupport;

/**
 * 发现功能从后台调取的内容
 * 需要使用数据库进行存储
 */
public class FindInfo extends DataSupport {
    private long id;         // 数据库主键
    private String title;    // 标题
    private String imageUrl; // 图片链接
    private String linkUrl;  // 链接地址

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }
}
