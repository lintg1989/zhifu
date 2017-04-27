package cn.zheft.www.zheft.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.model.QRPayInfo;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.RequestUrl;
import cn.zheft.www.zheft.retrofit.Response;
import cn.zheft.www.zheft.retrofit.ResponseUtil;
import cn.zheft.www.zheft.util.DateUtil;
import cn.zheft.www.zheft.util.StringUtil;
import cn.zheft.www.zheft.util.ToastUtil;
import cn.zheft.www.zheft.view.ViewAnim;

/**
 * 用户支付的结果页面
 */
public class PayReceivedActivity extends BaseActivity {
    private static final String TAG = PayReceivedActivity.class.getSimpleName();
    private Context mContext;

    private static final int PAGE_FROM_PAY_QRCODE = 1;
    private static final int PAGE_FROM_PAY_HISTORY = 0;
    private static final String PAY_BY_WECHAT = "41";
    private static final String PAY_BY_ALIPAY = "42";

    private String ind;
    private QRPayInfo payInfo;
    private boolean tradeStatus;

    private ImageView ivStatus;
    private TextView tvStatus;
    private TextView tvAmount;
    private TextView tvType;
    private TextView tvTime;
    private TextView tvCode;
    private TextView tvHint;
    private TextView tvReason;

    private LinearLayout llReason;
    private LinearLayout llMask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_received);
        setToolbar(R.string.pay_received_title);
        mContext = this;

        // 初始化数据
        if (getIntent() != null) {
            ind = getIntent().getStringExtra("tradeId");
        }
        if (StringUtil.nullOrEmpty(ind)) {
            ToastUtil.showShortMessage("交易ID为空");
            finish();
        }

        initView();
        initData();
    }

    private void initView() {
        ivStatus = (ImageView) findViewById(R.id.iv_pay_received_icon);
        tvStatus = (TextView) findViewById(R.id.tv_pay_received_status);
        tvAmount = (TextView) findViewById(R.id.tv_pay_received_amount);
        tvType = (TextView) findViewById(R.id.tv_pay_received_pay_type);
        tvTime = (TextView) findViewById(R.id.tv_pay_received_create_time);
        tvCode = (TextView) findViewById(R.id.tv_pay_received_code);
        tvHint = (TextView) findViewById(R.id.tv_pay_received_time_reason_hint);
        tvReason = (TextView) findViewById(R.id.tv_pay_received_time_reason);

        llReason = (LinearLayout) findViewById(R.id.ll_pay_received_reason);
        llMask = (LinearLayout) findViewById(R.id.ll_pay_received_mask);
    }

    private void initData() {
        serverConnect();
    }

    private void setData(QRPayInfo info) {
        String reason = null;
        switch (info.getRespCode()) {
            case "1001":
                reason = DateUtil.lineToCharacter( DateUtil.numToLine(info.getSuccTime()) );
                break;
            default:
                reason = info.getRespMsg();
                break;
        }

        initBaseData(info.getRespCode());

        String type;
        if (PAY_BY_ALIPAY.equals(info.getTxnType())) {
            type = "支付宝支付";
        } else if (PAY_BY_WECHAT.equals(info.getTxnType())) {
            type = "微信支付";
        } else {
            type = null;
        }
        tvAmount.setText( StringUtil.addNumberComma( dealTxnAmt(info.getTxnAmt()) ) );
        tvType.setText(type);
        tvTime.setText( DateUtil.lineToCharacter( DateUtil.numToLine(info.getTxnTime()) ) );
        tvCode.setText(info.getMerOrderId());
        tvReason.setText(reason);

        hideMask();
    }

    private void initBaseData(String code) {
        int hintResId;
        int iconResId;
        int stateResId;

        switch (code) {
            case "1001":
                hintResId = R.string.pay_received_pay_time;
                iconResId = R.mipmap.pay_success;
                stateResId = R.string.pay_received_success;
                break;
            case "0000":
            case "1111":
                hintResId = R.string.pay_received_pay_status;
                iconResId = R.mipmap.pay_wait;
                stateResId = R.string.pay_received_waiting;
                llReason.setVisibility(View.GONE);
                break;
            case "8888":
                hintResId = R.string.pay_received_fail_reason;
                iconResId = R.mipmap.pay_failure;
                stateResId = R.string.pay_received_closed;
                break;
            default:
                hintResId = R.string.pay_received_fail_reason;
                iconResId = R.mipmap.pay_failure;
                stateResId = R.string.pay_received_failure;
                break;
        }

        ivStatus.setImageDrawable(getResources().getDrawable(iconResId));
        tvStatus.setText(stateResId);
        tvHint.setText(hintResId);
    }

    private void serverConnect() {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        Map<String, Object> paras = new HashMap<>();
        paras.put("mobile", MyApp.getInstance().getUserInfo().getMobile());
        paras.put("tradeNo", ind);
        MyRetrofit.init(mContext).url(RequestUrl.QUERY_QRPAY_INFO).params(paras)
                .success(new MyRetrofit.HttpSuccess() {
                    @Override
                    public void onSuccess(Response response) {
                        closeProgress();
                        dealServerData(response);
                    }
                })
                .failure(new MyRetrofit.HttpFailure() {
                    @Override
                    public void onFailure(String message) {
                        closeProgress();
                        ToastUtil.showShortMessage("网络异常");
                    }
                })
                .post();
    }

    private void dealServerData(Response body) {
        if (MyRetrofit.RESULT_SUCCESS == body.getResultcode()) {
            QRPayInfo info = ResponseUtil.getData(body.getData(), QRPayInfo.class);
            if (info != null) {
                setData(info);
            } else {
                ToastUtil.showShortMessage("数据为空");
            }
        } else {
            ToastUtil.showShortMessage(body.getResultmsg());
        }
    }

    // 返回的数据单位为分，前补0
    private String dealTxnAmt(String amt) {
        if (StringUtil.nullOrEmpty(amt)) {
            return "0";
        }
        int index = 0;
        while ('0' == (amt.charAt(index))) {
            index++;
        }
        return StringUtil.fenToYuan(amt.substring(index));
    }

    private void hideMask() {
        ViewAnim.hide(llMask, 1000);
    }
}
