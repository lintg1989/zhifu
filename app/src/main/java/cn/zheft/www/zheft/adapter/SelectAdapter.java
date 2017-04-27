package cn.zheft.www.zheft.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.widget.TextView;

import com.design.coderbeanliang.swipeloadrecyclerview.CommAdapter;
import com.design.coderbeanliang.swipeloadrecyclerview.CommViewHolder;

import java.util.List;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.model.DeviceSelectInfo;

/**
 * 设备选择标签列表
 */

public class SelectAdapter extends CommAdapter {
    private Context mContext;

    public SelectAdapter(Context context, List datas, int itemLayoutId) {
        super(context, datas, itemLayoutId);
        mContext = context;
    }

    @Override
    public void convert(CommViewHolder commViewHolder, Object o, int i) {
        if (o instanceof DeviceSelectInfo) {
            TextView tvDevice = commViewHolder.getView(R.id.tv_item_device_select_name);
            tvDevice.setText(((DeviceSelectInfo) o).getName());
            // 下面对颜色进行特殊处理，其中“all”的背景特殊处理
            if (((DeviceSelectInfo) o).isSelected()) {

                if ( !"all".equals( ((DeviceSelectInfo) o).getCode() ) ) {
                    tvDevice.setTextColor(mContext.getResources().getColor(R.color.select_label_color_orange));
                    tvDevice.setBackground(mContext.getResources().getDrawable(R.drawable.drawable_device_select_stroke));
                } else {
                    tvDevice.setTextColor(mContext.getResources().getColor(R.color.total_white));
                    tvDevice.setBackground(mContext.getResources().getDrawable(R.drawable.drawable_device_select_all));
                }

            } else {
                tvDevice.setTextColor(mContext.getResources().getColor(R.color.text_color_dark));
                tvDevice.setBackground(mContext.getResources().getDrawable(R.drawable.drawable_device_select_full));
            }
        }
    }
}
