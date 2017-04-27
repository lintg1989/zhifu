package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.fragm.PosFragment;
import cn.zheft.www.zheft.model.QRPayInfo;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.RequestUrl;
import cn.zheft.www.zheft.retrofit.Response;
import cn.zheft.www.zheft.retrofit.ResponseUtil;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.StringUtil;
import cn.zheft.www.zheft.util.ToastUtil;

public class QRCodeShowActivity extends BaseActivity {
    private static final String TAG = QRCodeShowActivity.class.getSimpleName();
    private Context mContext;

    private String ind;
    private String resv;
    private String type;
    private int money;
    private boolean isPause;

    private static final String PAY_BY_WECHAT = "41";
    private static final String PAY_BY_ALIPAY = "42";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_show);
        mContext = this;

        if (getIntent() != null) {
            money = getIntent().getIntExtra("money", 0);
            type = getIntent().getStringExtra("type");
            resv = getIntent().getStringExtra("resv");
            ind = getIntent().getStringExtra("ind");
        }
        if (money == 0 || StringUtil.nullOrEmpty(type)
                || StringUtil.nullOrEmpty(resv) || StringUtil.nullOrEmpty(ind)) {
            ToastUtil.showShortMessage("未知错误，请重试");
            finish();
        }

        initView();

        Intent intent = new Intent(PosFragment.LOCAL_ACTION_TRADE_CHANGED);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTask();
    }

    @Override
    protected void onPause() {
        super.onPause();
        endTask();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        endTask();
    }

    private void initView() {
        TextView tvHint = (TextView) findViewById(R.id.tv_qrcode_show_hint);
        TextView tvAmount = (TextView) findViewById(R.id.tv_qrcode_show_amount);
        ImageView ivCode = (ImageView) findViewById(R.id.iv_qrcode_show_code);
        ImageView ivBack = (ImageView) findViewById(R.id.iv_qrcode_show_back);

        String amtStr = StringUtil.addNumberComma( StringUtil.fenToYuan(StringUtil.intToFen(money)) );
        tvAmount.setText("￥ " + amtStr);
        if (PAY_BY_WECHAT.equals(type)) {
            tvHint.setText("微信扫一扫，向我付款");
        } else if (PAY_BY_ALIPAY.equals(type)) {
            tvHint.setText("支付宝扫一扫，向我付款");
        }
        Bitmap bitmap = CodeUtils.createImage(resv, 200, 200, null);
        ivCode.setImageBitmap(bitmap);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void serverConnect() {
        Map<String, Object> paras = new HashMap<>();
        paras.put("mobile", MyApp.getInstance().getUserInfo().getMobile());
        paras.put("tradeNo", ind);
        MyRetrofit.init(mContext).url(RequestUrl.QUERY_QRPAY_INFO).params(paras)
                .success(new MyRetrofit.HttpSuccess() {
                    @Override
                    public void onSuccess(Response response) {
                        dealServerData(response);
                    }
                })
                .post();
    }

    private void dealServerData(Response body) {
        if (isPause) {
            return;// 切到后台后不处理数据，resume时会重新调接口
        }
        if (MyRetrofit.RESULT_SUCCESS == body.getResultcode()) {
            QRPayInfo info = ResponseUtil.getData(body.getData(), QRPayInfo.class);
            if (info != null) {
                if ("1001".equals(info.getRespCode())) {
                    // 成功
                    ToastUtil.showShortMessage("交易成功");
                    toMainPage();
                } else if ("1111".equals(info.getRespCode()) || "0000".equals(info.getRespCode())){
                    // 正在进行中
                    LogUtil.e(TAG, "交易进行中");
                } else if ("8888".equals(info.getRespCode())){
                    // 交易关闭
                    ToastUtil.showShortMessage("交易关闭");
                    toMainPage();
                } else {
                    // 失败
                    ToastUtil.showShortMessage("交易失败");
                    toMainPage();
                }
            }
        }
    }

    private void toMainPage() {
        // 通知Pos列表刷新
        Intent action = new Intent(PosFragment.LOCAL_ACTION_TRADE_CHANGED);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(action);
        // 跳转
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("setPage", 0);
        startActivity(intent);
    }

    private void startTask() {
        isPause = false;
        timer = new Timer();
        task = new MyTimerTask();
        timer.schedule(task, 100L, 2000L);
    }

    private void endTask() {
        isPause = true;
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private Timer timer;
    private MyTimerTask task;
    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            LogUtil.e(TAG, "TimeTask...");
            serverConnect();
        }
    }
}
