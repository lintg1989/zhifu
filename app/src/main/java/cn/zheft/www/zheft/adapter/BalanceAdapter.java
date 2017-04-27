package cn.zheft.www.zheft.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.design.coderbeanliang.swipeloadrecyclerview.CommAdapter;
import com.design.coderbeanliang.swipeloadrecyclerview.CommViewHolder;

import java.util.Calendar;
import java.util.List;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.model.BalanceInfo;
import cn.zheft.www.zheft.util.DateUtil;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.StringUtil;

/**
 * 余额页面明细列表
 *
 * Created by Liao on 2017/3/17
 */

public class BalanceAdapter extends CommAdapter {
    private static final String TAG = BalanceAdapter.class.getSimpleName();
    private String yearStr;

    public BalanceAdapter(Context context, List datas, int itemLayoutId) {
        super(context, datas, itemLayoutId);
        int year = Calendar.getInstance().get(Calendar.YEAR);
        yearStr = String.valueOf(year);
    }

    @Override
    public void convert(CommViewHolder commViewHolder, Object o, int i) {
        if (o instanceof BalanceInfo) {

            ImageView ivIcon = commViewHolder.getView(R.id.iv_item_balance_icon);
            TextView tvAction = commViewHolder.getView(R.id.tv_item_balance_action);
            TextView tvFund = commViewHolder.getView(R.id.tv_item_balance_fund);
            TextView tvDate = commViewHolder.getView(R.id.tv_item_balance_date);
            TextView tvAmount = commViewHolder.getView(R.id.tv_item_balance_amount);
            TextView tvStatus = commViewHolder.getView(R.id.tv_item_balance_status);

            int resId = getIconResIdByType(((BalanceInfo) o).getTradeType());
            ivIcon.setImageResource(resId);

            tvAction.setText(((BalanceInfo) o).getTitle());
            tvFund.setText("余额 " + StringUtil.addNumberComma( ((BalanceInfo) o).getFund() ));
            tvDate.setText(DateUtil.lineToCharacterNoYear( ((BalanceInfo) o).getCreateTime(), yearStr ));

            // 当( 提现（1）或预约提现（4）)  &&  状态非处理完成（3）时
            // 1、金额部分显示状态文字
            // 2、剩余金额（fund）需要隐藏；3、icon和action放垂直方向的中间
            if ( (((BalanceInfo) o).getTradeType() == 1 || ((BalanceInfo) o).getTradeType() == 4)
                    && ((BalanceInfo) o).getStatusCode() != 3) {

                tvStatus.setText(((BalanceInfo) o).getStatus());
                tvAmount.setText("");
                tvStatus.setVisibility(View.VISIBLE);
                tvAmount.setVisibility(View.INVISIBLE);

                tvFund.setVisibility(View.INVISIBLE);

                RelativeLayout.LayoutParams iconParams = (RelativeLayout.LayoutParams) ivIcon.getLayoutParams();
                iconParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                iconParams.addRule(RelativeLayout.CENTER_VERTICAL);
                RelativeLayout.LayoutParams actionParams = (RelativeLayout.LayoutParams) tvAction.getLayoutParams();
                actionParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                actionParams.addRule(RelativeLayout.CENTER_VERTICAL);
            } else {
                tvStatus.setText("");
                tvAmount.setText( getAmountStr( ((BalanceInfo) o).getAmount() ) );
                tvStatus.setVisibility(View.INVISIBLE);
                tvAmount.setVisibility(View.VISIBLE);

                tvFund.setVisibility(View.VISIBLE);

                RelativeLayout.LayoutParams iconParams = (RelativeLayout.LayoutParams) ivIcon.getLayoutParams();
                iconParams.addRule(RelativeLayout.CENTER_VERTICAL, 0);
                iconParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                RelativeLayout.LayoutParams actionParams = (RelativeLayout.LayoutParams) tvAction.getLayoutParams();
                actionParams.addRule(RelativeLayout.CENTER_VERTICAL, 0);
                actionParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            }

        }
    }

    private int getIconResIdByType(Integer typeCode) {
        int resId = 0;
        if (typeCode != null) {
            switch (typeCode) {
                case 1:
                    resId = R.mipmap.balance_withdraw; // 提现支出
                    break;
                case 2:
                    resId = R.mipmap.balance_income; // 刷卡收入
                    break;
                case 3:
                    resId = R.mipmap.balance_hongbao; // 新人红包
                    break;
                case 4:
                    resId = R.mipmap.balance_withdraw_order; // 预约提现
                    break;
                default:
                    LogUtil.e(TAG, "Type code is not matched: " + typeCode);
                    break;
            }
        } else {
            LogUtil.e(TAG, "Type code is null!");
        }

        if (resId == 0) {
            resId = R.mipmap.balance_income;
        }

        return resId;
    }

    // 系统返回的数字带+、-号，需要特殊处理
    private String getAmountStr(String srcAmt) {

        String sAmt = StringUtil.nullToEmpty(srcAmt);
        String amt = "";
        String sign = "";
        for (int i = 0; i < sAmt.length(); i++) {
            int num = sAmt.charAt(i) - 48;
            if (num < 0 || num > 9) {
                sign += sAmt.charAt(i);
            } else {
                amt = sAmt.substring(i);
                break;
            }
        }

        return sign + StringUtil.addNumberComma( amt );
    }
}
