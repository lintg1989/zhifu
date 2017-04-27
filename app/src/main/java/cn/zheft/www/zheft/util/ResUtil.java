package cn.zheft.www.zheft.util;

import android.content.Context;
import android.graphics.drawable.Drawable;

/**
 * Created by Liao on 2017/3/16 0016.
 *
 * 获取Res资源的类
 */

public class ResUtil {
    public static final String DRAWABLE = "drawable";
    public static final String MIPMAP = "mipmap";
    public static final String LAYOUT = "layout";
    public static final String COLOR = "color";
    public static final String STYLE = "style";
    public static final String ID = "id";//??

    public static int getColor(Context context, int colorResId) {
        return context.getResources().getColor(colorResId);
    }

    public static Drawable getDrawable(Context context, int drawableResId) {
        return context.getResources().getDrawable(drawableResId);
    }

    public static int getResId(Context context, String resNme, String defType) {
        return context.getResources().getIdentifier(resNme, defType, context.getPackageName());
    }
}
