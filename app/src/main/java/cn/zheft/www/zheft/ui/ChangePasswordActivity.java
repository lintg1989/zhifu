package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;

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

public class ChangePasswordActivity extends BaseActivity {
    private Context context;
    private ClearEditText cetOldPwd;
    private ClearEditText cetNewPwd;
    private ClearEditText cetNewPwd2; // 确认新密码

    private String type = "";// 更改密码类型
    private static final String PWD_TYPE_LOGIN = "login";
    private static final String PWD_TYPE_PAY = "pay";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        context = this;

        Intent intent = getIntent();
        type = intent.getStringExtra("ToChangePwd");

        switch (type) {
            case PWD_TYPE_LOGIN:
                setToolbar(R.string.change_login_pwd);
                initView();
                break;
            case PWD_TYPE_PAY:
                setToolbar(R.string.change_pay_password);
                initPayView();
                break;
            default:
                ToastUtil.showShortMessage("未知错误");
                finish();
                break;
        }
    }

    private void initView() {
        cetOldPwd = (ClearEditText) findViewById(R.id.cet_old_password);
        cetNewPwd = (ClearEditText) findViewById(R.id.cet_new_password);
        cetNewPwd2 = (ClearEditText) findViewById(R.id.cet_new_password_twice);
    }

    // 改变XML中默认的针对登录密码的输入过滤策略
    private void initPayView() {
        initView();
        cetOldPwd.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        cetOldPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        cetNewPwd.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        cetNewPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        cetNewPwd2.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
        cetNewPwd2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
    }

    // 更改密码
    public void changePassword(View view) {
        if (ClickUtil.cantClick()) {
            return;
        }
        switch (type) {
            case PWD_TYPE_LOGIN:
                checkPwdLogin();
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

    private void checkPwdLogin() {
        if (CheckUtil.checkOldPwd(cetOldPwd.getText().toString().trim())
                && CheckUtil.checkNewPwd(cetNewPwd.getText().toString().trim())
                && CheckUtil.checkPwdTwice(cetNewPwd2.getText().toString().trim())) {
            if (CheckUtil.checkPwdSame(
                    cetNewPwd.getText().toString().trim(),
                    cetNewPwd2.getText().toString().trim() )) {
                modifyPwd("1",cetOldPwd.getText().toString().trim(),cetNewPwd.getText().toString().trim());
            }
        }
    }

    private void checkPwdPay() {
        if (CheckUtil.checkPayPwd(cetOldPwd.getText().toString().trim(), 1)
                && CheckUtil.checkPayPwd(cetNewPwd.getText().toString().trim(), 2)
                && CheckUtil.checkPayPwd(cetNewPwd2.getText().toString().trim(), 3)) {
            if (CheckUtil.checkPwdSame(
                    cetNewPwd.getText().toString().trim(),
                    cetNewPwd2.getText().toString().trim() )) {
                modifyPwd("3",cetOldPwd.getText().toString().trim(),cetNewPwd.getText().toString().trim());
            }
        }
    }

    private void modifyPwd(String typePwd, String oldPwd, String newPwd) {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        String encryptOld = MD5Util.pwdEncrypt(oldPwd);// 加密
        String encryptNew = MD5Util.pwdEncrypt(newPwd);// 加密

//        LogUtil.e("ChangePwd","OldPwd:" + encryptOld + "\n" + "NewPwd:" + encryptNew);
        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                typePwd, encryptOld, encryptNew);

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<Object>> call = service.postModifyPwd(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                typePwd, encryptOld, encryptNew, cipher );
        call.enqueue(new Callback<ResponseBase<Object>>() {
            @Override
            public void onResponse(Call<ResponseBase<Object>> call, Response<ResponseBase<Object>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        // 成功后的处理分为登陆密码和支付密码
                        if (PWD_TYPE_LOGIN.equals(type)) {
                            //提示成功，转至登录页面
                            ToastUtil.showShortMessage(R.string.password_changed_to_relogin);
                            startActivity(new Intent(context, LoginActivity.class));
                            Config.setUsed();
                            MyApp.getInstance().exitApp();
                        } else if (PWD_TYPE_PAY.equals(type)) {
                            ToastUtil.showShortMessage("支付密码修改成功");
                            finish();
                        }
                    } else {
                        if(!resultCode(response.body().getResultCode())) {
                            // 解析码
                            ResultCode.modifyPwd(response.body().getResultCode());
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
