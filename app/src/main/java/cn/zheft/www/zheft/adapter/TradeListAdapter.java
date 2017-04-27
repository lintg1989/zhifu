package cn.zheft.www.zheft.adapter;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.design.coderbeanliang.swipeloadrecyclerview.CommAdapter;
import com.design.coderbeanliang.swipeloadrecyclerview.CommViewHolder;

import java.util.Calendar;
import java.util.List;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.model.PosInfo;
import cn.zheft.www.zheft.util.DateUtil;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.StringUtil;

/**
 * 交易列表的Adapter，即以前的流水页
 */

public class TradeListAdapter extends CommAdapter {
    private static final String TAG = TradeListAdapter.class.getSimpleName();
    private Context context;
    private String yearStr;

    public TradeListAdapter(Context context, List datas, int itemLayoutId) {
        super(context, datas, itemLayoutId);
        this.context = context;
        int year = Calendar.getInstance().get(Calendar.YEAR);
        yearStr = String.valueOf(year);
    }

    @Override
    public void convert(CommViewHolder commViewHolder, Object o, int i) {
        if (o instanceof PosInfo) {

            TextView tvAmount = commViewHolder.getView(R.id.tv_item_trade_amount);
            TextView tvDate = commViewHolder.getView(R.id.tv_item_trade_date);
            TextView tvStatus = commViewHolder.getView(R.id.tv_item_trade_status_name);
            ImageView ivIcon = commViewHolder.getView(R.id.iv_item_trade_icon);

            tvAmount.setText( StringUtil.addNumberComma( ((PosInfo) o).getAmount() ) );
            tvDate.setText( DateUtil.lineToCharacterNoYear( ((PosInfo) o).getInsertDate(), yearStr ) );

            int colorResId = R.color.text_color_dark;
            if (((PosInfo) o).getStatus() != null && ((PosInfo) o).getStatus() == 3) {
                colorResId = R.color.trade_list_item_text_waiting;  // 待支付
            }

            tvStatus.setText(((PosInfo) o).getStatusName());
            tvStatus.setTextColor(context.getResources().getColor(colorResId));

            int iconResId = getIconResIdByType(((PosInfo) o).getPayTypeCode());
            ivIcon.setImageResource(iconResId);
        }
    }

    private int getIconResIdByType(Integer typeCode) {
        int resId = 0;
        if (typeCode != null) {
            switch (typeCode) {
                case 41:
                    resId = R.mipmap.trade_icon_wechat;
                    break;
                case 42:
                    resId = R.mipmap.trade_icon_alipay;
                    break;
                case 99:// 99代表POS收款
                    resId = R.mipmap.trade_icon_pos;
                    break;
                default:
                    LogUtil.e(TAG, "Type code is not matched: " + typeCode);
                    break;
            }
        } else {
            LogUtil.e(TAG, "Type code is null!");
        }

        if (resId == 0) {
            resId = R.mipmap.trade_icon_pos;
        }

        return resId;
    }
}
