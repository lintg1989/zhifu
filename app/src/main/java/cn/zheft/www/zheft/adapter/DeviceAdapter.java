package cn.zheft.www.zheft.adapter;

import android.content.Context;
import android.widget.TextView;

import com.design.coderbeanliang.swipeloadrecyclerview.CommAdapter;
import com.design.coderbeanliang.swipeloadrecyclerview.CommViewHolder;

import java.util.List;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.model.DeviceInfo;

/**
 * “我的设备”列表适配器
 */
public class DeviceAdapter extends CommAdapter {

    public DeviceAdapter(Context context, List datas, int itemLayoutId) {
        super(context, datas, itemLayoutId);
    }

    @Override
    public void convert(CommViewHolder commViewHolder, Object o, int i) {
        if (o instanceof DeviceInfo) {
            TextView tvDevice = commViewHolder.getView(R.id.tv_item_device);
            tvDevice.setText(((DeviceInfo) o).getName());
        }
    }

}
