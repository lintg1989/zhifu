package cn.zheft.www.zheft.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.retrofit.HttpUtil;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.ResponseBase;
import cn.zheft.www.zheft.retrofit.RetrofitAPI;
import cn.zheft.www.zheft.util.ClickUtil;
import cn.zheft.www.zheft.util.StringUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingActivity extends BaseActivity {
    private Context context;

    private RelativeLayout rlChangePwd;
    private RelativeLayout rlPayPwd; // 支付密码
    private RelativeLayout rlMsgPush;// 消息推送
    private RelativeLayout rlChangePtn; // 修改手势密码
    private LinearLayout llHideSettings;// 根据openPattern状态进行显示隐藏的部分
    private FrameLayout flSwitchLock;
    private Switch swOpenPattern;
    private Switch swShowPattern;
    private RelativeLayout rlAboutus; // 关于我们

//    private SettingsInfo settingsInfo;// 设置
    private MyApp myApp;

    private final static int REQUEST_CODE = 1;// 请求码
    private final static int RESULT_CODE_ON = 1;// 开启返回码
    private final static int RESULT_CODE_OFF = 2;// 关闭返回码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setToolbar(R.string.settings);
        context = this;

        myApp = MyApp.getInstance();

        initView();
        initEvent();
    }

    private void initView() {
        rlChangePwd = (RelativeLayout) findViewById(R.id.rlayout_change_pwd);
        ((TextView) rlChangePwd.findViewById(R.id.tv_click_layout))
                .setText(getString(R.string.change_login_pwd));
        rlPayPwd = (RelativeLayout) findViewById(R.id.rlayout_pay_pwd);
        ((TextView) rlPayPwd.findViewById(R.id.tv_click_layout))
                .setText(getString(R.string.pay_password));
        rlMsgPush = (RelativeLayout) findViewById(R.id.rlayout_msg_push);
        ((TextView) rlMsgPush.findViewById(R.id.tv_click_layout))
                .setText(getString(R.string.message_push));
        rlAboutus = (RelativeLayout) findViewById(R.id.rlayout_to_aboutus);
        ((TextView) rlAboutus.findViewById(R.id.tv_click_layout))
                .setText(R.string.about_us);
        flSwitchLock = (FrameLayout) findViewById(R.id.fl_switch_lock);
        swOpenPattern = (Switch) findViewById(R.id.switch_pattern_lock);

        // 隐藏部分
        llHideSettings = (LinearLayout) findViewById(R.id.llayout_parttern_settings);
        rlChangePtn = (RelativeLayout) findViewById(R.id.rlayout_change_ptn);
        TextView tvCgPattern = (TextView) rlChangePtn.findViewById(R.id.tv_click_layout);
        tvCgPattern.setText(getString(R.string.change_pattern_lock));
        swShowPattern = (Switch) findViewById(R.id.switch_pattern_show);

        // 根据设置进行初始化
        if ("1".equals(myApp.getUserInfo().getPatternOn())
                && !StringUtil.nullOrEmpty(myApp.getUserInfo().getPatternLock())) {
            llHideSettings.setVisibility(View.VISIBLE);
            swOpenPattern.setChecked(true);
        }
//        if (myApp.getUserInfo().getPatternShow().equals("0")) {
//            swShowPattern.setChecked(false);
//        }


        Button btn_test = (Button) findViewById(R.id.btn_test);
        btn_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, UploadCardFirstActivity.class);
                startActivity(intent);
            }
        });

        Button btn_test2 = (Button) findViewById(R.id.btn_test2);
        btn_test2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingActivity.this, ShopPhotoFirstActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initEvent() {
        rlChangePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChangePasswordActivity.class);
                intent.putExtra("ToChangePwd","login");
                startActivity(intent);
            }
        });
        rlPayPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("1".equals(myApp.getUserInfo().getHasPayPwd())) {
                    // 到管理页
                    startActivity(new Intent(context, PayPasswordActivity.class));
                } else {
                    // 到设置页
                    Intent intent = new Intent(context, SetPasswordActivity.class);
                    intent.putExtra("ToSetPwd", "pay");
                    startActivity(intent);
                }
            }
        });
        rlMsgPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, SettingPushActivity.class));
            }
        });
        rlChangePtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, ChangePatternActivity.class));
            }
        });
        flSwitchLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (swOpenPattern.isChecked()) {
//                    LogUtil.e("Checked", "WantOff");
                    //开启状态时点击动作表示需要关闭手势密码
                    Intent intent = new Intent(context, LockActivity.class);
                    intent.putExtra("lockFrom", 2);
                    startActivityForResult(intent, REQUEST_CODE);
                } else {
//                    LogUtil.e("UnChecked","WantOn");
                    //关闭状态时点击表示需要开启，完成设置后将设置传至服务器，再更改本地设置
                    Intent intent = new Intent(context, CreatePatternActivity.class);
                    intent.putExtra("from", 0);
                    startActivityForResult(intent, REQUEST_CODE);
                }
            }
        });
        rlAboutus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AboutActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_CODE_ON) {
                swOpenPattern.setChecked(true);
                llHideSettings.setVisibility(View.VISIBLE);
            } else if (resultCode == RESULT_CODE_OFF) {
                swOpenPattern.setChecked(false);
                llHideSettings.setVisibility(View.GONE);
            }
        }
    }

    // 退出登录
    public void logout(View view) {
        if (ClickUtil.cantClick()) {
            return;
        }
        // 在这里清空用户数据后跳转至登录页面
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("退出登录");
        builder.setMessage("确认退出吗？");
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 在这里清空用户数据后跳转至登录页面
                logoutRequest();// 调用退出登录方法
                Config.setUsed();      // 重置APP为初次使用状态
                startActivity(new Intent(context, LoginActivity.class));
                MyApp.getInstance().exitApp(); // 关闭包括当前activity的所有activity
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // 设置点击周围不取消
        dialog.show();
    }

    private void logoutRequest() {
        String token = myApp.getDeviceToken();
        String cipher = HttpUtil.getExtraKey(
                myApp.getDeviceId(),
                Config.DEVICE_TYPE,
                myApp.getUserInfo().getMobile(),
                token );

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<Object>> call = service.postLogout(
                myApp.getDeviceId(),
                Config.DEVICE_TYPE,
                myApp.getUserInfo().getMobile(),
                token,
                cipher );
        call.enqueue(new Callback<ResponseBase<Object>>() {
            @Override
            public void onResponse(Call<ResponseBase<Object>> call, Response<ResponseBase<Object>> response) {
                // Do Nothing
            }

            @Override
            public void onFailure(Call<ResponseBase<Object>> call, Throwable t) {
                // Do Nothing
            }
        });
    }
}
