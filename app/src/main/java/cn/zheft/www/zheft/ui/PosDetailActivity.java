package cn.zheft.www.zheft.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.PosDetailInfo;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.retrofit.HttpUtil;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.ResponseBase;
import cn.zheft.www.zheft.retrofit.ResultCode;
import cn.zheft.www.zheft.retrofit.RetrofitAPI;
import cn.zheft.www.zheft.util.StringUtil;
import cn.zheft.www.zheft.util.ToastUtil;
import cn.zheft.www.zheft.view.ViewAnim;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * POS交易详情页（小票）
 * 网络请求成功后msg.what为0代表成功，1表示失败
 */
public class PosDetailActivity extends BaseActivity {
    private Context context;
    private LinearLayout llMask;
    private PosDetailInfo posDetailInfo = new PosDetailInfo();//这里需要new吗？
    private boolean tradeStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos_detail);
        setToolbar(R.string.pos_receipt);
        context = this;

        Intent intent = getIntent();
        String ind = intent.getStringExtra("tradeId");

        if (!StringUtil.nullOrEmpty(ind)) {
            getData(ind);
        } else {
            ToastUtil.showShortMessage("未知错误");
            finish();
        }

        initView();
    }

    private void initView() {
        llMask = (LinearLayout) findViewById(R.id.ll_pos_detail_mask);
    }

    private void getData(String tradeId) {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                tradeId );

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<PosDetailInfo>> call = service.postTradeDetail(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                tradeId,
                cipher );
        call.enqueue(new Callback<ResponseBase<PosDetailInfo>>() {
            @Override
            public void onResponse(Call<ResponseBase<PosDetailInfo>> call, Response<ResponseBase<PosDetailInfo>> response) {
                if (response.isSuccessful()) {
                    if (response.body().getResultCode().equals(ResultCode.RESULT_SUCCESS)) {
                        posDetailInfo = response.body().getData();
                        showData();
                    } else {
                        if(!resultCode(response.body().getResultCode())) {
                            // 解析码
                            if ("2".equals(response.body().getResultCode())) {
                                ToastUtil.showShortMessage("交易记录ID为空");
                            } else {
                                ToastUtil.showShortMessage("未知错误");
                            }
                        }
                    }
                } else {
                    ResultCode.responseCode(response.code());
                }
                closeProgress();
            }

            @Override
            public void onFailure(Call<ResponseBase<PosDetailInfo>> call, Throwable t) {
                ResultCode.callFailure(t.getMessage());
                closeProgress();
            }
        });
    }

    private void showData() {
        // Mask
        // 填充数据
        TextView tvMerchantId = (TextView) findViewById(R.id.tv_merchant_id);
        TextView tvMerchantName = (TextView) findViewById(R.id.tv_merchant_name);
        TextView tvTerminalCode = (TextView) findViewById(R.id.tv_terminal_code);
        TextView tvOperatorCode = (TextView) findViewById(R.id.tv_operator_code);
        tvMerchantId.setText(StringUtil.nullToEmpty(posDetailInfo.getMerchantCode()));
        tvMerchantName.setText(StringUtil.nullToEmpty(posDetailInfo.getName()));
        tvTerminalCode.setText(StringUtil.nullToEmpty(posDetailInfo.getTerminalCode()));
        tvOperatorCode.setText(StringUtil.nullToEmpty(posDetailInfo.getOperatorNum()));

        TextView tvFromBank = (TextView) findViewById(R.id.tv_from_bank);
        TextView tvToBank = (TextView) findViewById(R.id.tv_to_bank);
        TextView tvBankcard = (TextView) findViewById(R.id.tv_bankcard_num);
        TextView tvTradeType = (TextView) findViewById(R.id.tv_trade_type);
        TextView tvExpTime = (TextView) findViewById(R.id.tv_exp_time);
        TextView tvBatchCode = (TextView) findViewById(R.id.tv_batch_code);
        TextView tvDocCode = (TextView) findViewById(R.id.tv_doc_code);
        TextView tvRefCode = (TextView) findViewById(R.id.tv_ref_code);
        tvFromBank.setText(StringUtil.nullToEmpty(posDetailInfo.getFromBankNum()));
        tvToBank.setText(StringUtil.nullToEmpty(posDetailInfo.getToBankNum()));
        tvBankcard.setText(StringUtil.nullToEmpty(posDetailInfo.getCardNum()));
        tvTradeType.setText(StringUtil.nullToEmpty(posDetailInfo.getTradeType()));
        tvExpTime.setText(StringUtil.nullToEmpty(posDetailInfo.getExpTime()));
        tvBatchCode.setText(StringUtil.nullToEmpty(posDetailInfo.getBatchCode()));
        tvDocCode.setText(StringUtil.nullToEmpty(posDetailInfo.getPingzhengCode()));
        tvRefCode.setText(StringUtil.nullToEmpty(posDetailInfo.getCankaoCode()));

        TextView tvTradeDate = (TextView) findViewById(R.id.tv_trade_date);
        TextView tvTradeAmount = (TextView) findViewById(R.id.tv_trade_amount);
        tvTradeDate.setText(StringUtil.nullToEmpty(posDetailInfo.getTradeDate()));
        tvTradeAmount.setText("RMB  " +  StringUtil.addNumberComma( posDetailInfo.getAmount()) );

        tradeStatus = "交易成功".equals(posDetailInfo.getStatus());

        // 交易失败
        if (!tradeStatus) {
            LinearLayout llReason = (LinearLayout) findViewById(R.id.llayout_pos_detail_reason);
            ImageView ivFailed = (ImageView) findViewById(R.id.iv_failed_logo);
            TextView tvReason = (TextView) findViewById(R.id.tv_trade_failed);

            llReason.setVisibility(View.VISIBLE);
            ivFailed.setVisibility(View.VISIBLE);
            tvReason.setText(StringUtil.nullToEmpty(posDetailInfo.getFailedReason()));
        }

        hideMask();
    }

    private void hideMask() {
        ViewAnim.hide(llMask, 1000);
    }

}
