package cn.zheft.www.zheft.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.zheft.www.zheft.R;

/**
 * Created by Administrator on 2016/5/7 0007.
 * 用于显示列表footer
 */
public class AdapterViewHolder {
    public static final int TYPE_ITEM = 0; // 普通项
    public static final int TYPE_LOAD = 1; //加载中...
    public static final int TYPE_START = 2;//初始加载
    public static final int TYPE_END = 3;  //没有更多数据了

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }
}
