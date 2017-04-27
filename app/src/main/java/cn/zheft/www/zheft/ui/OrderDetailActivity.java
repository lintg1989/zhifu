package cn.zheft.www.zheft.ui;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.adapter.OrderDetailAdapter;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.model.BalanceInfo;
import cn.zheft.www.zheft.model.KeyValuePair;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.RequestUrl;
import cn.zheft.www.zheft.retrofit.Response;
import cn.zheft.www.zheft.retrofit.ResponseUtil;
import cn.zheft.www.zheft.util.ClickUtil;
import cn.zheft.www.zheft.util.DateUtil;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.MD5Util;
import cn.zheft.www.zheft.util.StringUtil;
import cn.zheft.www.zheft.util.ToastUtil;
import cn.zheft.www.zheft.view.PayPwdDialog;
import cn.zheft.www.zheft.view.ViewAnim;

/***
 * @author Lin
 * 预约提现详情
 *
 */
public class OrderDetailActivity extends BaseActivity implements PayPwdDialog.OnPayPwdListener {

    /**
     * 类型ICON
     */
    private ImageView ivIcon;
    /**
     * 类型
     */
    private TextView tvType;
    /**
     * 提现金额
     */
    private TextView tvAmount;
    /**
     * 手续费
     */
    private TextView tvCharge;

    /**
     * 提现信息
     */
    private ListView lv_order_info;

    private BalanceInfo balanceInfo;

    private Context mContext;
    private List<KeyValuePair> valueMaps;

    private OrderDetailAdapter orderDetailAdapter;

    private PayPwdDialog payPwdDialog;

    private LinearLayout llCancel;// 取消预约

    private LinearLayout llMask;
    private boolean showAmount = true;
    private boolean showFee = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        setToolbar(R.string.order_withdraw_title);

        mContext = this;

        if (getIntent() != null) {
            balanceInfo = (BalanceInfo) getIntent().getSerializableExtra("info");
        }
        if (balanceInfo == null) {
            ToastUtil.showShortMessage("数据异常");
            finish();
            return;
        }

        initView();
        initData();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        llMask = (LinearLayout) findViewById(R.id.ll_order_detail_mask);
//        ivIcon = (ImageView) findViewById(R.id.iv_order_detail_icon);
        tvAmount = (TextView) findViewById(R.id.tv_order_detail_amount);
        tvCharge = (TextView) findViewById(R.id.tv_order_detail_fee);
        tvType = (TextView) findViewById(R.id.tv_order_detail_type);
        lv_order_info = (ListView) findViewById(R.id.lv_order_info);
        lv_order_info.setDividerHeight(0);

        payPwdDialog = new PayPwdDialog();
        payPwdDialog.setPrompt("取消预约");

        llCancel = (LinearLayout) findViewById(R.id.ll_order_detail_cancel);
        if (4 == balanceInfo.getTradeType() && 1 == balanceInfo.getStatusCode()) {
            llCancel.setVisibility(View.VISIBLE);
        }
        if (4 == balanceInfo.getTradeType() && 3 != balanceInfo.getStatusCode()) {
            showAmount = false;
        }
        if (1 == balanceInfo.getTradeType() || 4 == balanceInfo.getTradeType()) {
            showFee = true;
        }
    }

    private void initData() {
        valueMaps = new ArrayList<KeyValuePair>();
        serverConnect();
    }

    private void serverConnect() {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        // 填充参数
        MyApp myApp = MyApp.getInstance();
        Map<String, Object> paras = new HashMap<>();
        paras.put("mobile", myApp.getUserInfo().getMobile());
        paras.put("id", balanceInfo.getId());
        paras.put("dataType",balanceInfo.getDataType());
        // 发起请求
        MyRetrofit.init(mContext).url(RequestUrl.WITHDRAW_DETAIL).params(paras)
                .success(new MyRetrofit.HttpSuccess() {
                    @Override
                    public void onSuccess(Response body) {
                        closeProgress();
                        dealServerData(body);
                    }
                })
                .failure(new MyRetrofit.HttpFailure() {
                    @Override
                    public void onFailure(String message) {
                        closeProgress();
                        ToastUtil.showShortMessage(message);
                    }
                })
                .post();
    }

    private void dealServerData(Response body) {
        if (MyRetrofit.RESULT_SUCCESS == body.getResultcode()) {
            valueMaps = ResponseUtil.getList(body.getData(), KeyValuePair.class);
            updateData(valueMaps);
        } else {
            ToastUtil.showShortMessage(body.getResultmsg());
        }
    }

    private void updateData(List<KeyValuePair> infos) {
        for (int i = 0; i < infos.size(); i++) {
            switch (infos.get(i).getKey()) {
                case "tradeType":
//                    int resId = getIconResByType(infos.get(i).getValue());
//                    ivIcon.setImageResource(resId);
                    infos.remove(i);
                    i--;
                    break;
                case "fee":
                    setFee( infos.get(i).getValue() ); // 手续费
                    infos.remove(i);
                    i--;
                    break;
                case "totalAmount":
                    setAmount( infos.get(i).getValue() );
                    infos.remove(i);
                    i--;
                    break;
                case "类型":
                    tvType.setText(infos.get(i).getValue());
                    break;
                default:
                    break;
            }
        }

        orderDetailAdapter = new OrderDetailAdapter(mContext, infos);
        lv_order_info.setAdapter(orderDetailAdapter);

        hideMask();
    }

    // Icon已取消
    private int getIconResByType(String type) {
        int resId = 0;
        switch (type) {
            case "1":
                resId = R.mipmap.balance_withdraw;
                break;
            case "2":
                resId = R.mipmap.balance_income;
                break;
            case "3":
                resId = R.mipmap.balance_hongbao;
                break;
            case "4":
                resId = R.mipmap.balance_withdraw_order;
                break;
            default:
                break;
        }

        if (resId == 0) {
            resId = R.mipmap.balance_income;
        }

        return resId;
    }

    @Override
    public void onInputFinish(String password) {
        // 输入完成
        // 调取消预约接口
        toCancelOrder(password);
    }

    @Override
    public void onForgetPwd() {
        if (ClickUtil.cantClick()) {
            return;
        }
        // 到忘记密码页
        startActivity(new Intent(mContext, ForgetPayPwdActivity.class));
    }

    @Override
    public void toSetPwd() {
        if (ClickUtil.cantClick()) {
            return;
        }
        // 到设置页
        Intent intent = new Intent(mContext, SetPasswordActivity.class);
        intent.putExtra("ToSetPwd", "pay");
        startActivity(intent);
    }

    private void toCancelOrder(String password) {
        if (nonNetwork()) {
            return;
        }
        // 调取消预约接口
        startProgress();
        String encryptPwd = MD5Util.pwdEncrypt(password);// 加密
        // 填充参数
        MyApp myApp = MyApp.getInstance();
        Map<String, Object> paras = new HashMap<>();
        paras.put("mobile", myApp.getUserInfo().getMobile());
        paras.put("id", balanceInfo.getId());
        paras.put("payPassword", encryptPwd);
        // 发起请求
        MyRetrofit.init(mContext).url(RequestUrl.WITHDRAW_ORDER_CANCEL).params(paras)
                .success(new MyRetrofit.HttpSuccess() {
                    @Override
                    public void onSuccess(Response body) {
                        payPwdDialog.clearInput();
                        closeProgress();
                        dealCancelData(body);
                    }
                })
                .failure(new MyRetrofit.HttpFailure() {
                    @Override
                    public void onFailure(String message) {
                        closeProgress();
                        ToastUtil.showShortMessage(message);
                        payPwdDialog.clearInput();
                    }
                })
                .post();
    }

    private void dealCancelData(Response body) {
        if (MyRetrofit.RESULT_SUCCESS == body.getResultcode()) {

            // 提现成功
            ToastUtil.showShortMessage("预约已取消");
            payPwdDialog.closeDialog();
            // 通知更新
            Intent intent = new Intent(BalanceActivity.LOCAL_ACTION_BALANCE_UPDATE);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            finish();

        } else if (3 == body.getResultcode()) {

            int times = StringUtil.strToInt(body.getData().toString());
            String msg = "密码错误，您还可输入" + times + "次";
            payPwdDialog.showPayPwdError(mContext, msg, this);

        } else if (8 == body.getResultcode()) {

            int minutes = StringUtil.strToInt(body.getData().toString());
            String msg = "支付密码已被锁定，请" + 3 + "小时后再试";
            payPwdDialog.showPayPwdLock(mContext, msg, this);

        } else {
            ToastUtil.showShortMessage(body.getResultmsg());
        }
    }

    public void cancelOrder(View view) {
        if (ClickUtil.cantClick()) {
            return;
        }
        // 取消预约
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("取消预约");
        builder.setMessage("确定要取消预约吗？");
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showPwdDialog();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void showPwdDialog() {
        // 检测是否有支付密码
        if ("1".equals(MyApp.getInstance().getUserInfo().getHasPayPwd())) {
            payPwdDialog.showPayPwdDialog(mContext, "全部金额", this);
        } else {
            payPwdDialog.showSetPwdDialog(mContext, this);
        }
    }

    private void hideMask() {
        ViewAnim.hide(llMask, 1000);
    }

    private void setFee( String fee ) {
        if (showFee) {
            String feeText = "提现手续费 ";
            feeText += StringUtil.fenToYuan( fee );
            feeText += "元";
            tvCharge.setText( feeText );
            tvCharge.setVisibility(View.VISIBLE);
        } else {
            tvCharge.setVisibility(View.INVISIBLE);
        }

    }

    private void setAmount( String amt ) {
        String result = "";
        if (showAmount) {
            result = getAmountStr( amt );
        } else {
            result = amt;
        }
        tvAmount.setText(result);
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
