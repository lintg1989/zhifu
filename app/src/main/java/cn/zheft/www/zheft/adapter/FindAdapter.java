package cn.zheft.www.zheft.adapter;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.design.coderbeanliang.swipeloadrecyclerview.CommAdapter;
import com.design.coderbeanliang.swipeloadrecyclerview.CommViewHolder;

import java.util.List;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.model.FindInfo;

/**
 * 发现页列表
 */

public class FindAdapter extends CommAdapter {

    private Context context;

    private int imageWidth;  // 图片宽度，使用 (int) screenWidth * 28.0f / 30.0f
    private int imageHeight; // 图片高度，根据宽高比 2:1 计算得到

    public FindAdapter(Context context, List datas, int itemLayoutId) {
        super(context, datas, itemLayoutId);
        this.context = context;

        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        imageWidth = (int) (screenWidth * 28.0f / 30.0f);
        imageHeight = imageWidth / 2;
    }

    @Override
    public void convert(CommViewHolder commViewHolder, Object o, int i) {
        if (o instanceof FindInfo) {
            TextView tvTitle = commViewHolder.getView(R.id.tv_item_find_title);
            tvTitle.setText(((FindInfo) o).getTitle());

            ImageView ivPic = commViewHolder.getView(R.id.iv_item_find_pic);
            ViewGroup.LayoutParams lp = ivPic.getLayoutParams();
            lp.width = imageWidth;
            lp.height = imageHeight;
            ivPic.setLayoutParams(lp);

            Glide.with(context)
                    .load(((FindInfo) o).getImageUrl())
                    .placeholder(R.mipmap.find_default)
                    .error(R.mipmap.find_default)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
//                    .override(700, 374)
//                    .fitCenter()
                    .crossFade()
                    .into(ivPic);
        }
    }
}
