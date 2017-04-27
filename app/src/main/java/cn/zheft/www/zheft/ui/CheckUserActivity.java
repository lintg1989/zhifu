package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.HongbaoInfo;
import cn.zheft.www.zheft.model.UserCheckInfo;
import cn.zheft.www.zheft.retrofit.HttpUtil;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.ResponseBase;
import cn.zheft.www.zheft.retrofit.ResultCode;
import cn.zheft.www.zheft.retrofit.RetrofitAPI;
import cn.zheft.www.zheft.util.ClickUtil;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.StringUtil;
import cn.zheft.www.zheft.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 确认商户信息页面
 * 拦截返回事件
 */
public class CheckUserActivity extends BaseActivity {
    private Context context;
    private UserCheckInfo checkInfo;
    private MyApp myApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_user);
        setToolbar(getString(R.string.check_user_info), false, null, true);
        doubleClickBack();   // 双击退出
        context = this;
        myApp = MyApp.getInstance();

        Intent intent = getIntent();
        checkInfo = (UserCheckInfo)intent.getSerializableExtra("user_check_info");
        updateData(checkInfo);
    }

    // 更新数据
    private void updateData(UserCheckInfo info) {
        if (info == null) {
            return;
        }
        TextView tvMerLegal = (TextView) findViewById(R.id.tv_check_mer_legal_name);
        TextView tvMerchant = (TextView) findViewById(R.id.tv_check_merchant_name);
        TextView tvPaperNum = (TextView) findViewById(R.id.tv_check_paper_number);
        TextView tvAccount = (TextView) findViewById(R.id.tv_check_account_no);
        tvMerLegal.setText(StringUtil.nullToEmpty(info.getMerLegalPerson()));
        tvMerchant.setText(StringUtil.nullToEmpty(info.getMerchantName()));
        tvPaperNum.setText(StringUtil.nullToEmpty(info.getPaperNum()));
        tvAccount.setText(StringUtil.nullToEmpty(info.getAccountNo()));
    }

    public void checkOk(View view) {
        if (ClickUtil.cantClick()) {
            return;
        }
        try {
            String deviceId = myApp.getDeviceId();
            String mobile = myApp.getUserInfo().getMobile();
            toVerify(deviceId, mobile);
        } catch (Exception e) {
            ToastUtil.showShortMessage("个人信息错误，请重新打开应用");
        }
    }

    private void toVerify(final String deviceId, final String mobile) {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        String cipher = HttpUtil.getExtraKey(
                deviceId,
                Config.DEVICE_TYPE,
                mobile );

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<Object>> call = service.postCheckOk(
                deviceId,
                Config.DEVICE_TYPE,
                mobile,
                cipher);
        call.enqueue(new Callback<ResponseBase<Object>>() {
            @Override
            public void onResponse(Call<ResponseBase<Object>> call, Response<ResponseBase<Object>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        ToastUtil.showShortMessage("商户信息已确认");
                        getHongbao(deviceId, mobile);
                    } else {
                        if (!resultCode(response.body().getResultCode())) {
                            // 解析码
                            ToastUtil.showShortMessage("商户不存在"); // 返回码为3
                        }
                    }
                } else {
                    ResultCode.responseCode(response.code());
                }
                closeProgress();
            }

            @Override
            public void onFailure(Call<ResponseBase<Object>> call, Throwable t) {
                ResultCode.callFailure(t.getMessage());
                closeProgress();
            }
        });
    }

    private void getHongbao(String deviceId, final String mobile) {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        String cipher = HttpUtil.getExtraKey(
                deviceId,
                Config.DEVICE_TYPE,
                mobile );

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<HongbaoInfo>> call = service.postHongbaoInfo(
                deviceId,
                Config.DEVICE_TYPE,
                mobile,
                cipher);
        call.enqueue(new Callback<ResponseBase<HongbaoInfo>>() {
            @Override
            public void onResponse(Call<ResponseBase<HongbaoInfo>> call, Response<ResponseBase<HongbaoInfo>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        toNextStep(mobile, response.body().getData());
                    } else {
                        if (!resultCode(response.body().getResultCode())) {
                            // 解析码
                        }
                    }
                } else {
                    ResultCode.responseCode(response.code());
                }
                closeProgress();
            }

            @Override
            public void onFailure(Call<ResponseBase<HongbaoInfo>> call, Throwable t) {
                ResultCode.callFailure(t.getMessage());
                closeProgress();
            }
        });
    }

    private void toNextStep(String mobile, HongbaoInfo info) {
        if (info == null) {
            ToastUtil.showShortMessage("个人信息错误，请重新打开应用");
            return;
        }
        if ("0".equals(info.getHongbaoCount())) {
            Config.saveUserInfo(myApp.getUserInfo());
            startActivity(new Intent(context, MainActivity.class));
        } else {
            Intent intent = new Intent(context, HongbaoActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("hongbao_info", info);
            intent.putExtras(bundle);
            startActivity(intent);
        }
        myApp.exitApp();
    }
}
