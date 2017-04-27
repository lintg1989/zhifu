package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
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

/**
 * 忘记支付密码
 */
public class ForgetPayPwdActivity extends BaseActivity {
    private Context context;
    private TextView tvGetVerify; // 获取验证码
    private ClearEditText cetVerify;
    private ClearEditText cetPayPwd1;
    private ClearEditText cetPayPwd2;

    private boolean getCode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pay_pwd);
        setToolbar(R.string.forget_pay_password);
        context = this;

        initView();
    }

    private void initView() {
        cetPayPwd1 = (ClearEditText) findViewById(R.id.cet_set_new_password);
        cetPayPwd2 = (ClearEditText) findViewById(R.id.cet_confirm_new_password);
        cetVerify = (ClearEditText) findViewById(R.id.cet_input_verify_code);
        tvGetVerify = (TextView) findViewById(R.id.tv_get_verify_code);
    }

    // 获取验证码
    public void forgetPayVerify(View view) {
        if (ClickUtil.cantClick()) {
            return;
        }
        cetVerify.setText("");
        getVerifyCode();
    }

    // 确定按钮事件
    public void forgetPayOk(View view) {
        if (ClickUtil.cantClick()) {
            return;
        }
        if (!getCode) {
            ToastUtil.showShortMessage("请先获取验证码！");
            return;
        }
        // 进行支付密码格式检测
        if (CheckUtil.checkSetPay(cetPayPwd1.getText().toString().trim(),1)
                && CheckUtil.checkSetPay(cetPayPwd2.getText().toString().trim(),2)
                && CheckUtil.checkVerify(cetVerify.getText().toString().trim())) {
            if (CheckUtil.checkPwdSame(cetPayPwd1.getText().toString().trim(),
                    cetPayPwd2.getText().toString().trim())) {
                //进行支付密码更改请求
                toResetPayPwd(cetVerify.getText().toString().trim(),cetPayPwd2.getText().toString().trim());
            }
        }
    }

    // 调取短信接口
    private void getVerifyCode() {
        if (nonNetwork()) {
            return;
        }
        startProgress();

        // 末位参数代表请求来源于--- 1、认领；2、忘登录密码；3、忘支付密码
        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                "3" );

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<Object>> call = service.postVerifyCode(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                "3",
                cipher );
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

    // 提交新的支付密码
    private void toResetPayPwd(String code, String password) {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        String encryptPwd = MD5Util.pwdEncrypt(password);// 加密

        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                "2",
                code,
                encryptPwd );

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<Object>> call = service.postForgetPwd(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                "2",
                code,
                encryptPwd,
                cipher );
        call.enqueue(new Callback<ResponseBase<Object>>() {
            @Override
            public void onResponse(Call<ResponseBase<Object>> call, Response<ResponseBase<Object>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        //提示成功
                        ToastUtil.showShortMessage("重置支付密码成功");
                        finish();
                    } else {
                        if (!resultCode(response.body().getResultCode())) {
                            // 解析码
                            if ("3".equals(response.body().getResultCode())) {
                                ToastUtil.showShortMessage("验证码错误");
                            } else {
                                ToastUtil.showShortMessage("重置支付密码失败，请重试");
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
}
