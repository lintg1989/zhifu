package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.HongbaoInfo;
import cn.zheft.www.zheft.model.SettingsInfo;
import cn.zheft.www.zheft.model.UserCheckInfo;
import cn.zheft.www.zheft.model.UserInfo;
import cn.zheft.www.zheft.retrofit.HttpUtil;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.ResponseBase;
import cn.zheft.www.zheft.retrofit.ResultCode;
import cn.zheft.www.zheft.retrofit.RetrofitAPI;
import cn.zheft.www.zheft.util.CheckUtil;
import cn.zheft.www.zheft.util.ClickUtil;
import cn.zheft.www.zheft.util.MD5Util;
import cn.zheft.www.zheft.util.StringUtil;
import cn.zheft.www.zheft.util.ToastUtil;
import cn.zheft.www.zheft.view.ClearEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 设置密码，此时认领过程还没完成，服务器接收到密码才算认领完成
 */
public class SetPasswordActivity extends BaseActivity {
    private Context context;
    private ClearEditText cetPwd;
    private ClearEditText cetPwd2; // 确认密码

    private String mobile;
    private String code;
    private String type = ""; // 密码类型
    private static final String PWD_TYPE_CLAIM = "claim";
    private static final String PWD_TYPE_PAY = "pay";

    private String deviceId = "";
    private SettingsInfo settingsInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        deviceId = MyApp.getInstance().getDeviceId();

        Intent intent = getIntent();
        type = intent.getStringExtra("ToSetPwd");

        switch (type) {
            case PWD_TYPE_CLAIM:
                // 设置登录密码
                setContentView(R.layout.activity_set_password);
                // 自定义toolbar，带title、可返回，右侧无按钮，不显示底线
                setToolbar(getString(R.string.set_password), true, null, false);
                disablePatternLock();

                mobile = intent.getStringExtra("mobile");
                code = intent.getStringExtra("code");
                break;
            case PWD_TYPE_PAY:
                // 设置支付密码
                setContentView(R.layout.activity_set_pay_pwd);
                setToolbar(R.string.set_pay_password);
                break;
            default:
                // 异常
                setContentView(R.layout.activity_set_pay_pwd);
                setToolbar(R.string.set_pay_password);
                ToastUtil.showShortMessage("未知错误，请重试");
                finish();
                break;
        }

        initView();
    }

    private void initView() {
        cetPwd = (ClearEditText) findViewById(R.id.cet_set_password);
        cetPwd2 = (ClearEditText) findViewById(R.id.cet_set_password_twice);
    }

    // 点击确认按钮的事件
    public void setPasswordOk(View view) {
        if (ClickUtil.cantClick()) {
            return;
        }
        switch (type) {
            case PWD_TYPE_CLAIM:
                checkPwdClaim();
                break;
            case PWD_TYPE_PAY:
                checkPwdPay();
                break;
            default:
                ToastUtil.showShortMessage("未知错误");
                finish();
                break;
        }
    }

    private void checkPwdClaim() {
        if (mobile == null || code == null) {
            ToastUtil.showShortMessage(R.string.error_illegal_phone_and_code);
            return;
        }
        if (CheckUtil.checkPassword(cetPwd.getText().toString().trim())
                && CheckUtil.checkPwdTwice(cetPwd2.getText().toString().trim())) {
            if (CheckUtil.checkPwdSame(
                    cetPwd.getText().toString().trim(),
                    cetPwd2.getText().toString().trim())) {
                // 密码匹配则调用认领接口
                toValidate(cetPwd.getText().toString().trim());
                Config.setUserPhone(mobile);
            }
        }
    }

    private void checkPwdPay() {
        if (CheckUtil.checkSetPay(cetPwd.getText().toString().trim(), 1)
                && CheckUtil.checkSetPay(cetPwd2.getText().toString().trim(), 2)) {
            if (CheckUtil.checkPwdSame(
                    cetPwd.getText().toString().trim(),
                    cetPwd2.getText().toString().trim())) {
                // 密码匹配则调用设置支付密码接口
                toSetPayPwd(cetPwd.getText().toString().trim());
            }
        }
    }

    private void toValidate(String password) {
        if (nonNetwork()) {
            return;
        }
        startProgress();

        String token = MyApp.getInstance().getDeviceToken();

        String encryptPwd = MD5Util.pwdEncrypt(password);// 加密

        String cipher = HttpUtil.getExtraKey(
                deviceId,
                Config.DEVICE_TYPE,
                mobile,
                code,
                encryptPwd,
                token );

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<Object>> call = service.postValidate(
                deviceId,
                Config.DEVICE_TYPE,
                mobile,
                code,
                encryptPwd,
                token,
                cipher );
        call.enqueue(new Callback<ResponseBase<Object>>() {
            @Override
            public void onResponse(Call<ResponseBase<Object>> call, Response<ResponseBase<Object>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        // 认领成功，设置等调完核对信息和红包接口后再进行存储
                        ToastUtil.showShortMessage("认领成功");
                        getCheckInfo(mobile);
                    } else {
                        if (!resultCode(response.body().getResultCode())) {
                            ResultCode.claimCode(response.body().getResultCode());
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

    private void toSetPayPwd(String password) {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        String encryptPwd = MD5Util.pwdEncrypt(password);// 加密

        String cipher = HttpUtil.getExtraKey(
                deviceId,
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                encryptPwd,
                "3" );

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<Object>> call = service.postSetPassword(
                deviceId,
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                encryptPwd,
                "3",
                cipher);
        call.enqueue(new Callback<ResponseBase<Object>>() {
            @Override
            public void onResponse(Call<ResponseBase<Object>> call, Response<ResponseBase<Object>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        // 手势密码设置成功
                        ToastUtil.showShortMessage("设置成功");
                        MyApp.getInstance().getUserInfo().setHasPayPwd("1");
                        Config.saveUserInfo(MyApp.getInstance().getUserInfo()); // 存储用户信息
                        finish();
                    } else {
                        if(!resultCode(response.body().getResultCode())) {
                            ToastUtil.showShortMessage("手机号码为空");
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

    // ----------------- 下部分为认领时记录核对信息与红包状态时的方法 -----------------
    private void getCheckInfo(final String mobile) {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        String cipher = HttpUtil.getExtraKey(
                deviceId,
                Config.DEVICE_TYPE,
                mobile );
        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<UserCheckInfo>> call = service.postCheckInfo(
                deviceId,
                Config.DEVICE_TYPE,
                mobile,
                cipher);
        call.enqueue(new Callback<ResponseBase<UserCheckInfo>>() {
            @Override
            public void onResponse(Call<ResponseBase<UserCheckInfo>> call, Response<ResponseBase<UserCheckInfo>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        beforeHongbao(mobile, response.body().getData());
                    } else {
                        if(!resultCode(response.body().getResultCode())) {
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
            public void onFailure(Call<ResponseBase<UserCheckInfo>> call, Throwable t) {
                ResultCode.callFailure(t.getMessage());
                closeProgress();
            }
        });
    }

    private void getHongbaoInfo(final String mobile) {
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
                        afterHongbao(mobile, response.body().getData());
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

    private void beforeHongbao(String mobile, UserCheckInfo checkInfo) {
        if (checkInfo == null) {
            errorInfo();
            return;
        }
        if ("1".equals(checkInfo.getIsVerified())) {
            // 已核对，获取红包信息
            getHongbaoInfo(mobile);
        } else {
            // 转核对信息页
            saveSettings(mobile);
            // 跳转至核对信息页面
            Intent intent = new Intent(context, CheckUserActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("user_check_info", checkInfo);
            intent.putExtras(bundle);
            startActivity(intent);
            MyApp.getInstance().exitApp();
        }
    }

    private void afterHongbao(String mobile, HongbaoInfo hongbaoInfo) {
        if (hongbaoInfo == null) {
            errorInfo();
            return;
        }
        saveSettings(mobile);
        if ("0".equals(hongbaoInfo.getHongbaoCount())) {
            // 已领，转至主页
            Config.saveUserInfo(MyApp.getInstance().getUserInfo());
            startActivity(new Intent(context, MainActivity.class));
        } else {
            // 跳转至红包页面
            Intent intent = new Intent(context, HongbaoActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("hongbao_info", hongbaoInfo);
            intent.putExtras(bundle);
            startActivity(intent);
        }
        MyApp.getInstance().exitApp();
    }

    private void saveSettings(String mobile) {
        settingsInfo = new SettingsInfo("0", "", "1", "0");
        UserInfo userInfo = new UserInfo(
                deviceId,
                mobile,
                settingsInfo);// 存储返回值（设置信息）
        MyApp.getInstance().setUserInfo(userInfo);
    }

    private void errorInfo() {
        ToastUtil.showShortMessage("用户信息错误，请重新运行应用");
    }
}
