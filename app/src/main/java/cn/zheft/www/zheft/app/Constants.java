package cn.zheft.www.zheft.app;

import cn.zheft.www.zheft.config.Config;

/**
 * Created by Lin on 2017/4/24.
 */

public class Constants {

    /**
     * request Code 从相册选择照
     **/
    public final static int RC_PICK_PICTURE_FROM_GALLERY_ORIGINAL = 1001;

    /**
     * 服务器地址
     */
    public static final String SERVER ;

    static {
        if (Config.DEBUG){
            SERVER = "";
        } else {
            SERVER = "";
        }
    }

    public static final String URL_IMAGE = SERVER + "";

}
