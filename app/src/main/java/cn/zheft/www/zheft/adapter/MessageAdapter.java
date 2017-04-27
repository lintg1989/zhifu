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
import cn.zheft.www.zheft.model.MessageInfo;
import cn.zheft.www.zheft.util.DateUtil;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.StringUtil;

/**
 * 消息页列表适配器
 */
public class MessageAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private static final String TAG = MessageAdapter.class.getName();

    private boolean isEnd = false; // 数据是否已加载到头
    private boolean isStart = true; // 是否第一次加载

    private OnItemClickListener onItemClickListener;
    private List<MessageInfo> data;

    private String todayStr = "";// 今日时间字串

    private String strType = "";// 消息类型
    private int colorAfter;
    private int colorBeforeDark;
    private int colorBeforeLight;

    // 传入参数
    public MessageAdapter(Context context, List<MessageInfo> data) {
        this.context = context;
        this.data = data;

        strType = context.getResources().getString(R.string.hongbao);

        colorAfter = context.getResources().getColor(R.color.text_color_light);
        colorBeforeDark = context.getResources().getColor(R.color.text_color_dark);
        colorBeforeLight = context.getResources().getColor(R.color.message_list_not_read);
    }

    // 没有更多数据时在使用该适配器的activity中主动调用该方法设置isEnd为true
    public void setEnd(boolean isEnd) {
        this.isEnd = isEnd;
    }
    // 首次加载数据时在使用该适配器的activity中主动调用该方法设置isStart为true
    public void setStart(boolean isStart) {
        this.isStart = isStart;
    }

    // 设置当日时间
    public void setTodayStr(String todayStr) {
        this.todayStr = todayStr;
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
            View view = LayoutInflater.from(context).inflate(R.layout.item_message,
                    parent, false);
            return new MessageViewHolder(view);
        } else if (viewType == AdapterViewHolder.TYPE_START) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_footer_null,
                    parent, false);
            return new AdapterViewHolder.FooterViewHolder(view);
        } else if (viewType == AdapterViewHolder.TYPE_END) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_footer_empty,
                    parent, false);
            return new AdapterViewHolder.FooterViewHolder(view);
        } else if (viewType == AdapterViewHolder.TYPE_LOAD) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_footer_load,
                    parent, false);
            return new AdapterViewHolder.FooterViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MessageViewHolder) {
            // 只有holder为设备条目时才执行这部分
            int iconResId = strType.equals(data.get(position).getType()) ? R.mipmap.message_icon_hongbao : R.mipmap.message_icon_system;
            ((MessageViewHolder) holder).ivIcon.setImageResource(iconResId);
            ((MessageViewHolder) holder).tvType.setText(data.get(position).getType());
            ((MessageViewHolder) holder).tvDate.setText(DateUtil.changeDateStr(todayStr ,data.get(position).getDate()));
            ((MessageViewHolder) holder).tvContent.setText(getHongbaoContent(data.get(position).getType(), data.get(position).getContent()));
            if (data.get(position).isRead()) {
                ((MessageViewHolder) holder).ivBadge.setVisibility(View.INVISIBLE);
                ((MessageViewHolder) holder).tvType.setTextColor(colorAfter);
                ((MessageViewHolder) holder).tvDate.setTextColor(colorAfter);
                ((MessageViewHolder) holder).tvContent.setTextColor(colorAfter);
            } else {
                ((MessageViewHolder) holder).ivBadge.setVisibility(View.VISIBLE);
                ((MessageViewHolder) holder).tvType.setTextColor(colorBeforeDark);
                ((MessageViewHolder) holder).tvDate.setTextColor(colorBeforeLight);
                ((MessageViewHolder) holder).tvContent.setTextColor(colorBeforeLight);
            }
            // 点击事件
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
        return data.size() + 1;// 返回的计数多一个，用作footer
    }

    @Override
    public int getItemViewType(int position) {
        if (isStart) {
//            LogUtil.e("Adapter","Start");
            return AdapterViewHolder.TYPE_START;
        }
        if (position + 1 == getItemCount()) {
            if (isEnd) {
//                LogUtil.e("Adapter","End");
                return AdapterViewHolder.TYPE_END;
            } else {
//                LogUtil.e("Adapter","Load");
                return AdapterViewHolder.TYPE_LOAD;
            }
        } else {
            return AdapterViewHolder.TYPE_ITEM;// 普通数据项
        }
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvType;
        TextView tvDate;
        TextView tvContent;
        ImageView ivBadge;
        public MessageViewHolder(View itemView) {
            super(itemView);
            ivIcon = (ImageView) itemView.findViewById(R.id.iv_item_message_icon);
            tvType = (TextView) itemView.findViewById(R.id.tv_item_message_type);
            tvDate = (TextView) itemView.findViewById(R.id.tv_item_message_date);
            tvContent = (TextView) itemView.findViewById(R.id.tv_item_message_content);
            ivBadge = (ImageView) itemView.findViewById(R.id.iv_item_message_badge);
        }
    }

    // 如果是红包消息则截取金额后显示
    private String getHongbaoContent(String type, String content) {
        if (!StringUtil.nullOrEmpty(type) && !type.equals(strType)) {
            return StringUtil.nullToEmpty(content);
        }
        try {
            int index = content.indexOf("￥");
            LogUtil.e(TAG, "Index:"+index);
            if (index > 0) {
                return content.substring(0, index);
            }
            return content;
        } catch (Exception e) {
            return "";
        }
    }
}
