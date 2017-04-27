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
 * 登录页面
 * 1、获取登录信息后先暂存登录信息
 * 2、开始获取核对信息
 *    3、未核对进入核对页面
 *    4、已核对获取红包信息
 *       5、已领取红包进入主页
 *       6、未领取红包进入红包页面
 */
public class LoginActivity extends BaseActivity {
    private Context context;
    private ClearEditText cetPhone;
    private ClearEditText cetPassword;
    private MyApp myApp;

    private String phone;
    private SettingsInfo settingsInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // 自定义toolbar，title为空、不带返回，右侧文字按钮，不显示底线
        setToolbar(getString(R.string.login), false, null, false);
        disablePatternLock();// 禁用手势密码
        doubleClickBack();   // 双击退出
        context = this;
        myApp = MyApp.getInstance();

        initView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApp.getInstance().setLockShowTime(System.currentTimeMillis());
    }

    private void initView() {
        cetPhone = (ClearEditText) findViewById(R.id.cet_input_phone);
        cetPassword = (ClearEditText) findViewById(R.id.cet_input_password);
        String userPhone = Config.getUserPhone();
        if (!StringUtil.nullOrEmpty(userPhone)) {
            cetPhone.setText(userPhone);
            cetPhone.setSelection(userPhone.length());
        }
    }

    // 忘记密码
    public void loginForgetPwd(View view) {
        startActivity(new Intent(context, ForgetPasswordActivity.class));
    }

    // 认领新用户
    public void loginToClaim(View view) {
        startActivity(new Intent(context, ClaimActivity.class));
        finish();
    }

    public void loginToRegister(View view) {
        // 新用户注册
        startActivity(new Intent(context, RegisterActivity.class));
        finish();
    }

    // 确认登录
    public void loginOk(View view) {
        if (ClickUtil.cantClick()) {
            return;
        }
        if (CheckUtil.checkPhone(cetPhone.getText().toString().trim())
                && CheckUtil.checkPassword(cetPassword.getText().toString().trim())) {
            toLogin(cetPhone.getText().toString().trim(),cetPassword.getText().toString().trim());
            Config.setUserPhone(cetPhone.getText().toString().trim());
        }
    }

    private void toLogin(final String mobile, String password) {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        String token = MyApp.getInstance().getDeviceToken();

        String encryptPwd = MD5Util.pwdEncrypt(password);// 加密
        String cipher = HttpUtil.getExtraKey(
                myApp.getDeviceId(),
                Config.DEVICE_TYPE,
                mobile,
                encryptPwd,
                token );

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<SettingsInfo>> call = service.postLogin(
                myApp.getDeviceId(),
                Config.DEVICE_TYPE,
                mobile,
                encryptPwd,
                token,
                cipher);
        call.enqueue(new Callback<ResponseBase<SettingsInfo>>() {
            @Override
            public void onResponse(Call<ResponseBase<SettingsInfo>> call, Response<ResponseBase<SettingsInfo>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        settingsInfo = response.body().getData();
                        getCheckInfo(mobile);
                    } else {
                        if(!resultCode(response.body().getResultCode())) {
                            // 解析码
                            ToastUtil.showShortMessage("手机号或密码不正确");
                        }
                    }
                } else {
                    ResultCode.responseCode(response.code());
                }
                closeProgress();
            }

            @Override
            public void onFailure(Call<ResponseBase<SettingsInfo>> call, Throwable t) {
                ResultCode.callFailure(t.getMessage());
                closeProgress();
            }
        });
    }

    private void getCheckInfo(final String mobile) {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        String cipher = HttpUtil.getExtraKey(
                myApp.getDeviceId(),
                Config.DEVICE_TYPE,
                mobile );
        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<UserCheckInfo>> call = service.postCheckInfo(
                myApp.getDeviceId(),
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
                myApp.getDeviceId(),
                Config.DEVICE_TYPE,
                mobile );

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<HongbaoInfo>> call = service.postHongbaoInfo(
                myApp.getDeviceId(),
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
            finish();
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
            Config.saveUserInfo(myApp.getUserInfo());
            startActivity(new Intent(context, MainActivity.class));
            ToastUtil.showShortMessage("登录成功");
        } else {
            // 跳转至红包页面
            Intent intent = new Intent(context, HongbaoActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("hongbao_info", hongbaoInfo);
            intent.putExtras(bundle);
            startActivity(intent);
        }
        finish();
    }

    private void saveSettings(String mobile) {
        if (settingsInfo == null) {
            errorInfo();
            myApp.exitApp();
            return;
        }
        UserInfo userInfo = new UserInfo(
                myApp.getDeviceId(),
                mobile,
                settingsInfo);// 存储返回值（设置信息）
        myApp.setUserInfo(userInfo);
    }

    private void errorInfo() {
        ToastUtil.showShortMessage("用户信息错误，请重新运行应用");
    }
}
