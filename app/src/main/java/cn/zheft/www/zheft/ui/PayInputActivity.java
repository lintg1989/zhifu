package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.RequestUrl;
import cn.zheft.www.zheft.retrofit.Response;
import cn.zheft.www.zheft.retrofit.ResponseUtil;
import cn.zheft.www.zheft.util.ClickUtil;
import cn.zheft.www.zheft.util.StringUtil;
import cn.zheft.www.zheft.util.ToastUtil;
import cn.zheft.www.zheft.view.PayInputView;
import cn.zheft.www.zheft.view.SquareTextView;

/**
 * 收款输入金额页面
 */
public class PayInputActivity extends BaseActivity implements PayInputView.OnInputListener, View.OnClickListener {

    private static final String TAG = PayInputActivity.class.getSimpleName();
    private Context mContext;

    private TextView tvMoney;
    private PayInputView payInputView;

    private int money;
    private int type;

    private String byType; // 微信还是支付宝
    private static final String PAY_BY_WECHAT = "41";
    private static final String PAY_BY_ALIPAY = "42";
    private static final String GOODS_TITLE = "二维码支付";
    private static final String GOODS_DESCRIPT = "APP端二维码支付";

    public static final int PAY_TYPE_SCAN = 0;// 扫码
    public static final int PAY_TYPE_CODE = 1;// 生成码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_input);

        mContext = this;

        if (getIntent() != null) {
            type = getIntent().getIntExtra("type", -1);
        }

        if (type == PAY_TYPE_SCAN) {
            setToolbar(R.string.home_qrcode_scan);
        } else if (type == PAY_TYPE_CODE) {
            setToolbar(R.string.home_qrcode_code);
        } else {
            ToastUtil.showShortMessage("请重试");
            finish();
            return;
        }

        initView();
    }

    private void initView() {
        tvMoney = (TextView) findViewById(R.id.tv_pay_input_amount);
        tvMoney.setText("0");

        payInputView = (PayInputView) findViewById(R.id.view_pay_input);
        payInputView.setOnInputListener(this);

        LinearLayout llWechat = (LinearLayout) findViewById(R.id.ll_pay_input_wechat);
        LinearLayout llAlipay = (LinearLayout) findViewById(R.id.ll_pay_input_alipay);
        llWechat.setOnClickListener(this);
        llAlipay.setOnClickListener(this);
    }

    @Override
    public void onInput(String num) {
        // 对inputStr加逗号处理
        money = StringUtil.fenToInt(StringUtil.yuanToFen(num));
        tvMoney.setText(StringUtil.addNumberComma(num));
    }

    @Override
    public void onClick(View v) {
        if (ClickUtil.cantClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.ll_pay_input_alipay:
                byType = PAY_BY_ALIPAY;
                break;
            case R.id.ll_pay_input_wechat:
                byType = PAY_BY_WECHAT;
                break;
            default:
                return;
        }
        gathering();
    }

    private void gathering() {
        if (money <= 0) {
            ToastUtil.showShortMessage("请输入有效金额");
            return;
        }
        if (money > 5000000) {
            ToastUtil.showShortMessage("金额过大，请重新输入");
            return;
        }

        // 根据type和byType进行收款设置
        if (type == PAY_TYPE_CODE) {
            serverConnect();
        } else if (type == PAY_TYPE_SCAN) {
            toQRCodeScan();
        }
    }

    private void serverConnect() {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        // 填充参数
        Map<String, Object> paras = new HashMap<>();
        paras.put("txnType", byType);
//        paras.put("merOrderId", "" + System.currentTimeMillis());
        paras.put("txnAmt", StringUtil.intToFen(money));
        paras.put("subject", GOODS_TITLE);
        paras.put("body", GOODS_DESCRIPT);
        paras.put("customerIp", "127.0.0.1");
        paras.put("mobile", MyApp.getInstance().getUserInfo().getMobile());
        // 发起请求（被扫需传手机号）
        MyRetrofit.init(mContext).url(RequestUrl.QR_BEISAO_PAY).params(paras)
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
                        ToastUtil.showShortMessage("请求失败，请重试");
                    }
                })
                .post();
    }

    private void dealServerData(Response body) {
        if (MyRetrofit.RESULT_SUCCESS == body.getResultcode()) {
            Map map = ResponseUtil.getMap(body.getData());
            Object resv = map.get("resv");
            Object ind = map.get("merOrderId");
            if (resv != null && ind != null) {
                toQRCodeCreate(resv.toString(), ind.toString());
            }
        } else {
            ToastUtil.showShortMessage(body.getResultmsg());
        }
    }

    private void toQRCodeCreate(String resv, String ind) {
        Intent intent = new Intent(mContext, QRCodeShowActivity.class);
        intent.putExtra("money", money);
        intent.putExtra("type", byType);
        intent.putExtra("resv", resv);
        intent.putExtra("ind", ind);
        startActivity(intent);
    }

    private void toQRCodeScan() {
        if (nonNetwork()) {
            return;
        }
        Intent intent = new Intent(mContext, QRCodeScanActivity.class);
        intent.putExtra("money", money);
        intent.putExtra("type", byType);
        startActivity(intent);
    }
}
