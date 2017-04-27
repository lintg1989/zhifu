package cn.zheft.www.zheft.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.model.DeviceSelectInfo;

/**
 * Created by Administrator on 2016/5/6 0006.
 * “我的设备”列表适配器
 */
public class DeviceSelectAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;

    private boolean isEnd = false; // 数据是否已加载到头
    private boolean isStart = true; // 是否第一次加载

    private OnItemClickListener onItemClickListener;
    private List<DeviceSelectInfo> data;

    // 传入参数
    public DeviceSelectAdapter(Context context, List<DeviceSelectInfo> data) {
        this.context = context;
        this.data = data;
    }

    // 没有更多数据时在使用该适配器的activity中主动调用该方法设置isEnd为true
    public void setEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }
    // 首次加载数据时在使用该适配器的activity中主动调用该方法设置isStart为true
    public void setStart(boolean isStart) {
        this.isStart = isStart;
    }

    // 点击事件接口
    public interface OnItemClickListener {
        void onItemClick(View view, int position); // 点击列表项
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == AdapterViewHolder.TYPE_ITEM) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_device_select,
                    parent, false);
            return new DeviceViewHolder(view);
        }
//        else if (viewType == AdapterViewHolder.TYPE_START) {
//            View view = LayoutInflater.from(context).inflate(R.layout.item_footer_null,
//                    parent, false);
//            return new AdapterViewHolder.FooterViewHolder(view);
//        } else if (viewType == AdapterViewHolder.TYPE_END) {
//            View view = LayoutInflater.from(context).inflate(R.layout.item_footer_empty,
//                    parent, false);
//            return new AdapterViewHolder.FooterViewHolder(view);
//        } else if (viewType == AdapterViewHolder.TYPE_LOAD) {
//            View view = LayoutInflater.from(context).inflate(R.layout.item_footer_load,
//                    parent, false);
//            return new AdapterViewHolder.FooterViewHolder(view);
//        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DeviceViewHolder) {
            // 只有holder为设备条目时才执行这部分
            ((DeviceViewHolder) holder).tvDevice.setText(data.get(position).getName());
            if (data.get(position).isSelected()) {
                ((DeviceViewHolder) holder).ivChecked.setVisibility(View.VISIBLE);
            } else {
                ((DeviceViewHolder) holder).ivChecked.setVisibility(View.INVISIBLE);
            }
            if (data.get(position).isHasMsg()) {
                ((DeviceViewHolder) holder).ivBadge.setVisibility(View.VISIBLE);
            } else {
                ((DeviceViewHolder) holder).ivBadge.setVisibility(View.INVISIBLE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = holder.getLayoutPosition();
                    onItemClickListener.onItemClick(holder.itemView, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
//        return data.size() + 1;// 返回的计数多一个，用作footer
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
//        if (isStart) {
//            return AdapterViewHolder.TYPE_START;
//        }
//        if (position + 1 == getItemCount()) {
//            if (isEnd) {
//                return AdapterViewHolder.TYPE_END;
//            } else {
//                return AdapterViewHolder.TYPE_LOAD;
//            }
//        } else {
//            return AdapterViewHolder.TYPE_ITEM;// 普通数据项
//        }
        return AdapterViewHolder.TYPE_ITEM;// 普通数据项
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView tvDevice;
        ImageView ivBadge;
        ImageView ivChecked;
        public DeviceViewHolder(View itemView) {
            super(itemView);
            tvDevice = (TextView) itemView.findViewById(R.id.tv_item_device);
            ivBadge = (ImageView) itemView.findViewById(R.id.iv_item_device_badge);
            ivChecked = (ImageView) itemView.findViewById(R.id.iv_item_device_checked);
        }
    }
}
