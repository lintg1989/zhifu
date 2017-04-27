package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.retrofit.HttpUtil;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.ResponseBase;
import cn.zheft.www.zheft.retrofit.ResultCode;
import cn.zheft.www.zheft.retrofit.RetrofitAPI;
import cn.zheft.www.zheft.util.CheckUtil;
import cn.zheft.www.zheft.util.ClickUtil;
import cn.zheft.www.zheft.util.MD5Util;
import cn.zheft.www.zheft.util.ToastUtil;
import cn.zheft.www.zheft.view.ClearEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgetPasswordActivity extends BaseActivity {
    private Context context;
    private TextView tvGetVerify; // 获取验证码
    private ClearEditText cetPhone;
    private ClearEditText cetVerify;
    private ClearEditText cetPassword;

    private String phoneTemp; //暂存手机号
    private boolean getCode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);
        // 自定义toolbar，带title、可以返回，右侧无按钮，不显示底线
        setToolbar(getString(R.string.forget_password), true, null, false);
        disablePatternLock();
        context = this;

        initView();
    }

    private void initView() {
        cetPhone = (ClearEditText) findViewById(R.id.cet_input_phone);
        cetVerify = (ClearEditText) findViewById(R.id.cet_input_verify_code);
        cetPassword = (ClearEditText) findViewById(R.id.cet_input_password);
        tvGetVerify = (TextView) findViewById(R.id.tv_get_verify_code);
    }

    // 忘记密码确认按钮
    public void forgetPassword(View view) {
        if (ClickUtil.cantClick()) {
            return;
        }
        // 进行各种检测
        if (CheckUtil.checkPhone(cetPhone.getText().toString().trim())) {
            if (phoneTemp == null || !getCode) {
                ToastUtil.showShortMessage("请先获取验证码！");
                return;
            }
            if (!CheckUtil.checkVerify(cetVerify.getText().toString().trim())
                    && CheckUtil.checkPassword(cetPassword.getText().toString().trim())) {
                return;
            }
            if (phoneTemp.equals(cetPhone.getText().toString().trim())) {
                // 未变动手机号时才能提交
                commitNewPwd();
            } else {
                ToastUtil.showShortMessage(R.string.error_you_changed_phone_number);
            }
        }
    }

    // 获取验证码
    public void forgetGetVerify(View view) {
        if (CheckUtil.checkPhone(cetPhone.getText().toString().trim())) {
            cetVerify.setText("");
            getVerifyCode(cetPhone.getText().toString().trim());
        }
    }

    // 计时器60秒倒计时，1秒间隔
    private CountDownTimer timer  = new CountDownTimer(60000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            tvGetVerify.setClickable(false);
            // 改用placeholder（怎么改？）
            tvGetVerify.setText(millisUntilFinished / 1000 + "s");
        }

        @Override
        public void onFinish() {
            tvGetVerify.setText(R.string.get_verify_code);
            tvGetVerify.setClickable(true);
        }
    };

    // 调取短信接口
    private void getVerifyCode(String phone) {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        phoneTemp =  phone;// 暂存手机号

        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE, phone, "2");

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<Object>> call = service.postVerifyCode(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                phone, "2", cipher );// 忘记密码末尾参数为“2”
        call.enqueue(new Callback<ResponseBase<Object>>() {
            @Override
            public void onResponse(Call<ResponseBase<Object>> call, Response<ResponseBase<Object>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        ToastUtil.showShortMessage(R.string.send_verify_code);
                        getCode = true;
                        timer.start();
                    } else {
                        if(!resultCode(response.body().getResultCode())) {
                            // 解析码
                            ResultCode.verifyCode(response.body().getResultCode());
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

    // 提交密码接口
    private void commitNewPwd() {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        String encryptPwd = MD5Util.pwdEncrypt(cetPassword.getText().toString().trim());// 加密

        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                phoneTemp,
                "1",
                cetVerify.getText().toString().trim(),
                encryptPwd );

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<Object>> call = service.postForgetPwd(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                phoneTemp,
                "1",
                cetVerify.getText().toString().trim(),
                encryptPwd,
                cipher );
        call.enqueue(new Callback<ResponseBase<Object>>() {
            @Override
            public void onResponse(Call<ResponseBase<Object>> call, Response<ResponseBase<Object>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        //提示成功，转至登录页面
                        ToastUtil.showShortMessage(R.string.password_changed_to_relogin);
//                        startActivity(new Intent(context,LoginActivity.class));
                        finish();
                    } else {
                        if(!resultCode(response.body().getResultCode())) {
                            // 解析码
                            if ("3".equals(response.body().getResultCode())) {
                                ToastUtil.showShortMessage("验证码错误");
                            } else {
                                ToastUtil.showShortMessage("重置密码失败，请重试");
                            }
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
}
