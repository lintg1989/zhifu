package cn.zheft.www.zheft.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cn.zheft.www.zheft.R;

/**
 * 二维码支付金额输入键盘
 */

public class GridInputAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;

    private List<String> data;
    private OnItemClickListener onItemClickListener;

    public GridInputAdapter(Context mContext, List<String> data) {
        this.mContext = mContext;
        this.data = data;
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
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_pay_input, parent, false);
        return new GridInputViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof GridInputViewHolder) {
            ((GridInputViewHolder) holder).tvText.setText(data.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class GridInputViewHolder extends RecyclerView.ViewHolder {
        TextView tvText;

        public GridInputViewHolder(View itemView) {
            super(itemView);
            tvText = (TextView) itemView.findViewById(R.id.tv_item_pay_input);
        }
    }
}
