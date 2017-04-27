package cn.zheft.www.zheft.adapter;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.design.coderbeanliang.swipeloadrecyclerview.CommAdapter;
import com.design.coderbeanliang.swipeloadrecyclerview.CommViewHolder;

import java.util.List;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.model.BankcardInfo;

/**
 *
 */

public class BankcardAdapter extends CommAdapter {
    private Context context;

    public BankcardAdapter(Context context, List datas, int itemLayoutId) {
        super(context, datas, itemLayoutId);
        this.context = context;
    }

    @Override
    public void convert(CommViewHolder commViewHolder, Object o, int i) {
        if (o instanceof BankcardInfo) {
            int resId = context.getResources().getIdentifier(
                    "bank" + ((BankcardInfo) o).getBankCode(),
                    "mipmap", context.getPackageName());

            ImageView ivBankIcon = commViewHolder.getView(R.id.iv_item_bankcard_icon);
            TextView tvBankName = commViewHolder.getView(R.id.tv_item_bankcard_name);
            TextView tvBankCode = commViewHolder.getView(R.id.tv_item_bankcard_num);

            ivBankIcon.setImageResource(resId == 0 ? R.mipmap.bank18 : resId);
            tvBankName.setText( bankCardName(((BankcardInfo) o).getBankcardName()) );
            tvBankCode.setText( ((BankcardInfo) o).getCard() );

        }

    }

    private String bankCardName(String name) {
        if (name != null) {
            if (name.length() > 8) {
                name = name.substring(0,8);
                name += "...";
            }
        } else {
            name = "其他银行";
        }
        return name;
    }
}
