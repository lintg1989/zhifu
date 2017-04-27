package cn.zheft.www.zheft.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import cn.zheft.www.zheft.BuildConfig;
import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.VersionInfo;
import cn.zheft.www.zheft.retrofit.HttpUtil;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.ResponseBase;
import cn.zheft.www.zheft.retrofit.ResultCode;
import cn.zheft.www.zheft.retrofit.RetrofitAPI;
import cn.zheft.www.zheft.util.CheckUtil;
import cn.zheft.www.zheft.util.ClickUtil;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.StringUtil;
import cn.zheft.www.zheft.util.ToastUtil;
import cn.zheft.www.zheft.util.VersionUtil;
import cn.zheft.www.zheft.view.ClearEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 认领页面
 */
public class ClaimActivity extends BaseActivity {
    private Context context;
    private MyApp myApp;
    private Button btnNext;
    private ClearEditText cetPhone;  // 手机号
    private ClearEditText cetVerify; // 验证码
    private TextView tvGetVerify;    //获取验证码

    private String phoneTemp; //暂存手机号
    private boolean getCode = false;// 获取验证码成功时

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim);
        // 自定义toolbar，title为空、不带返回，右侧文字按钮，不显示底线
        setToolbar(getString(R.string.claim), false, null, false);
        disablePatternLock();// 禁用手势密码
        doubleClickBack();   // 双击退出
        context = this;

        initView();
    }

    private void initView() {
        btnNext = (Button) findViewById(R.id.btn_next);
        cetPhone = (ClearEditText) findViewById(R.id.cet_input_phone);
        cetVerify = (ClearEditText) findViewById(R.id.cet_input_verify_code);
        tvGetVerify = (TextView) findViewById(R.id.tv_get_verify_code);
        // 测试适配时在屏幕上显示的像素数
//        logHeight();
    }

    // 下一步
    public void claimNextStep(View view) {
        if (ClickUtil.cantClick()) {
            return;
        }
        if (CheckUtil.checkPhone(cetPhone.getText().toString().trim())) {
            if (phoneTemp == null || !getCode) {
                ToastUtil.showShortMessage("请先获取验证码！");
                return;
            }
            if (!CheckUtil.checkVerify(cetVerify.getText().toString().trim())) {
                return;
            }
            if (phoneTemp.equals(cetPhone.getText().toString().trim())) {
                // 未变动手机号时才能下一步
                Intent intent = new Intent(context, SetPasswordActivity.class);
                intent.putExtra("ToSetPwd","claim");
                intent.putExtra("mobile",cetPhone.getText().toString().trim());
                intent.putExtra("code",cetVerify.getText().toString().trim());
                startActivity(intent);
            } else {
                ToastUtil.showShortMessage(R.string.error_you_changed_phone_number);
            }
        }
    }

    // 查看认领说明
    public void claimInstruction(View view) {
        startActivity(new Intent(context, ClaimInstructionActivity.class));
    }

    // 查看用户协议
    public void claimAgreement(View view) {
        startActivity(new Intent(context, AgreementActivity.class));
    }

    // 已认领，直接登录
    public void claimToLogin(View view) {
        startActivity(new Intent(context, LoginActivity.class));
        finish();
    }

    public void claimToRegister(View view) {
        // 注册
        startActivity(new Intent(context, RegisterActivity.class));
        finish();
    }

    // 获取验证码
    public void claimGetVerify(View view) {
        if (ClickUtil.cantClick()) {
            return;
        }
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
        // 如果没得到device_token
        String token = MyApp.getInstance().getDeviceToken();

        phoneTemp =  phone;// 暂存手机号

        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE, phone, "1");

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<Object>> call = service.postVerifyCode(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                phone, "1", cipher);// 认领末尾参数为“1”
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

    @Override
    protected void onResume() {
        super.onResume();

        myApp = MyApp.getInstance();
        // 判断版本号
        if (myApp.getVersionInfo() == null) {
            return;
        }
        if (!"1".equals(myApp.getVersionInfo().getHaveNewVersion())) {
            return;
        }
        if (myApp.getVersionInfo().getForceUpdate().equals("1")) {
            updateDialogLarge(myApp.getVersionInfo());// 强制更新
            myApp.setVersionInfo(null);// 防止锁屏后再加载一次
            return;
        }

        int[] nowVer = VersionUtil.versionToArray(VersionUtil.getVersionName(context));
        int[] newVer = VersionUtil.versionToArray(myApp.getVersionInfo().getVersion());

        if (nowVer[0] < newVer[0]) {
            updateDialogLarge(myApp.getVersionInfo());// 大版本更新
        } else if (nowVer[1] < newVer[1]) {
            updateDialogMiddle(myApp.getVersionInfo()); // 中版本更新
        } else if (nowVer[2] < newVer[2]) {
            updateDialogSmall(myApp.getVersionInfo());// 小版本更新
        } else {
            LogUtil.e("Update", "ErrorVersion!");
        }
        myApp.setVersionInfo(null);// 防止锁屏后再加载一次
    }

    private void updateDialogLarge(final VersionInfo ver) {
        // 直接在页面上进行下载
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);// 不能取消
        builder.setTitle("版本更新" + ver.getVersion());
        builder.setMessage(ver.getRemark());
        builder.setPositiveButton("下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 开始下载
                Uri uri = Uri.parse(StringUtil.checkHttpHead(ver.getDownloadUrl()));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                myApp.exitApp();
            }
        });
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_SEARCH;
            }
        });
        builder.show();
    }

    private void updateDialogMiddle(final VersionInfo ver) {
        // 正常显示
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("版本更新" + ver.getVersion());
        builder.setMessage(ver.getRemark());
        builder.setPositiveButton("下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 开始下载
                Uri uri = Uri.parse(StringUtil.checkHttpHead(ver.getDownloadUrl()));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("取消", null);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // 设置点击周围不取消
        dialog.show();
    }

    private void updateDialogSmall(final VersionInfo ver) {
        // 可以忽略
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("版本更新" + ver.getVersion());
        builder.setMessage(ver.getRemark());
        builder.setPositiveButton("下载", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 开始下载
                Uri uri = Uri.parse(StringUtil.checkHttpHead(ver.getDownloadUrl()));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
        builder.setNeutralButton("忽略", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Config.setIgnoreVersion(ver.getVersion());
            }
        });
        builder.setNegativeButton("取消", null);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // 设置点击周围不取消
        dialog.show();
    }

    private void logHeight() {
        if (BuildConfig.DEBUG) {
            cetPhone.post(new Runnable() {
                @Override
                public void run() {
                    LogUtil.e("TestScreen", "48dp : " + cetPhone.getHeight() + "px\n"
                            + "percentage: "
                            + getWindow().getDecorView().getHeight()/cetPhone.getHeight()
                            + "%" );
                }
            });
        }
    }
}
