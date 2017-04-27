package cn.zheft.www.zheft.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.SettingsInfo;
import cn.zheft.www.zheft.model.UserInfo;
import cn.zheft.www.zheft.retrofit.HttpUtil;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.ResponseBase;
import cn.zheft.www.zheft.retrofit.ResultCode;
import cn.zheft.www.zheft.retrofit.RetrofitAPI;
import cn.zheft.www.zheft.util.StringUtil;
import cn.zheft.www.zheft.util.ToastUtil;
import cn.zheft.www.zheft.view.GestureLockViewGroup;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 输入手势密码的页面
 * 手势密码登录时从网络获取，存储在本地，唤起时直接后台读取
 * 不能设为单例
 */
public class LockActivity extends BaseActivity {
    private Context context;
    private int lockFrom;// 启动参数
    private boolean unmatched; // 次数是否用尽

    private TextView tvPrompt;// 提示用textview

    private GestureLockViewGroup gestureLockViewGroup;

    private final static int RESULT_CODE_OFF = 2;// 关闭返回码
    private boolean maxTime = false;// 用于区分resetLock中的流程，错误次数达到后先清数据，忘记密码重置完服务器再清
    /**
     * 重新登录标记
     */
    private boolean reLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        context = this;

        Intent intent = getIntent();
        lockFrom = intent.getIntExtra("lockFrom",0);// 默认为0，为1代表来自启动页，为2来自设置页

        // 只有来自设置页时才有返回，且不禁用手势密码
        boolean canBack = false;
        if (lockFrom == 2) {
            canBack = true;
        } else {
            disablePatternLock();// 非设置页进入时禁掉手势
        }
        setToolbar(getString(R.string.pattern_lock),canBack,null,true);

        initView();
    }

    private void doNext(int lockFrom) {
        if (lockFrom == 2){
            resetLock();
        } else {
            MyApp.getInstance().setLockShowTime(System.currentTimeMillis());
            finish();
        }
    }

    private void initView() {
        TextView tvPhone = (TextView) findViewById(R.id.tv_phone);
        tvPhone.setText(StringUtil.dealPhoneNum(MyApp.getInstance().getUserInfo().getMobile()));

        tvPrompt = (TextView) findViewById(R.id.tv_pattern);

        int umatchedTime = Config.getUnmatchedTimes();
        if (umatchedTime < 5) {
            tvPrompt.setTextColor(getResources().getColor(R.color.pattern_error_color));
            tvPrompt.setText("剩余次数" + umatchedTime + "次");
        }

        // 手势密码
        gestureLockViewGroup = (GestureLockViewGroup) findViewById(R.id.gesture_lock_view_group);
        gestureLockViewGroup.setPattern(false);
        gestureLockViewGroup.setUnMatchExceedBoundary(umatchedTime);
        gestureLockViewGroup.setAnswer(MyApp.getInstance().getUserInfo().getPatternLock());
        gestureLockViewGroup.setOnGestureLockViewListener(new GestureLockViewGroup.OnGestureLockViewListener() {
            @Override
            public void onBlockSelected(int cId) {
                // Do Nothing
            }

            @Override
            public void onGestureEvent(boolean matched) {
                unmatched = !matched;
                if (matched) {
                    tvPrompt.setTextColor(getResources().getColor(R.color.theme_color_app));
                    tvPrompt.setText("解锁成功");

                    Config.setUnmatchedTimes(5);
                    gestureLockViewGroup.reset();
                    doNext(lockFrom);
                } else {
                    tvPrompt.setTextColor(getResources().getColor(R.color.pattern_error_color));
                    tvPrompt.setText("剩余次数" + gestureLockViewGroup.getTryTimes() + "次");
                    Config.setUnmatchedTimes(gestureLockViewGroup.getTryTimes());
                    gestureLockViewGroup.reset();
                }
            }

            @Override
            public void onUnmatchedExceedBoundary() {
                // 达到错误次数时的处理
                maxTime = true;
                resetLock();
                logoutRequest();    // 调取退出登录接口
                Config.setUsed();   // 重置APP为初次使用状态
                // 转到登录密码
                showDialog();
            }

            @Override
            public void onFirstSetPattern(boolean patternOk) {
                // Do Nothing
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        LogUtil.e("Lock", "NowResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
//        LogUtil.e("Lock", "NowPause");
//        MyApp.getInstance().setLockShowTime(System.currentTimeMillis());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // 用户点击返回键时（代表未解锁）退出app
        if (lockFrom != 2) {
//            LogUtil.e("Close","AllClear");
            MyApp.getInstance().exitApp();
            finish();
        }
    }

    // 忘记密码跳转
    public void forgetPattern(View view) {
        // 提示需要重新登录
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("忘记密码");
        builder.setMessage("忘记手势密码需要重新登录");
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do Nothing
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 在这里清空用户数据后跳转至登录页面
                reLogin = true;

                resetLock();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);// 不能被返回键取消
        builder.setTitle("重新登录");
        builder.setMessage("手势密码已失效，请重新登录");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 转至登录页
                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
                MyApp.getInstance().exitApp();
            }
        });
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_SEARCH) {
                    return true;// 屏蔽搜索键
                }
                return false;
            }
        });
        builder.show();
    }

    private void resetSuccess() {
        // 从设置页进来时才执行
        ToastUtil.showShortMessage("手势密码已关闭");
        // 保存用户信息
        SettingsInfo settingsInfo = new SettingsInfo("0", "", "1","0");
        settingsInfo.setHavePayPassword(MyApp.getInstance().getUserInfo().getHasPayPwd());
        UserInfo userInfo = new UserInfo(
                MyApp.getInstance().getDeviceId(),
                MyApp.getInstance().getUserInfo().getMobile(),
                settingsInfo);// 设置信息
        Config.saveUserInfo(userInfo);
        setResult(RESULT_CODE_OFF);
        finish();
    }

    private void resetLock() {
        if (!maxTime) {
            if (nonNetwork()) {
                return;
            }
            startProgress();
        }
        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                "gesture", "2");

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<Object>> call2 = service.postSetPassword(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                "gesture", "2", cipher);// 2是手势密码，gesture为置空（关闭）标识
        call2.enqueue(new Callback<ResponseBase<Object>>() {
            @Override
            public void onResponse(Call<ResponseBase<Object>> call, Response<ResponseBase<Object>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        // 手势密码重置成功
                        if (!unmatched && lockFrom == 2 && !reLogin) {
                            // 设置页关闭手势密码成功
                            resetSuccess();
                        } else if (!maxTime && reLogin) {
                            // 非最大次数，表明此处是点击忘记密码后执行的片段，先重置再清本地
                            logoutRequest();    // 调取退出登录接口
                            Config.setUsed();   // 重置APP为初次使用状态
                            // 转至登录页
                            Intent intent = new Intent(context, LoginActivity.class);
                            startActivity(intent);
                            MyApp.getInstance().exitApp();
                        }
                    } else {
                        boolean result = resultCode(response.body().getResultCode());// 101返回true
                        if (!result) {
                            // 解析码
                            ToastUtil.showShortMessage("手势重置失败");
                            finish();
                        } else {
                            if (!response.body().getResultCode().equals("101")) {
                                finish();
                            }
                        }
                    }
                } else {
                    ResultCode.responseCode(response.code());
                    finish();
                }
                closeProgress();
            }

            @Override
            public void onFailure(Call<ResponseBase<Object>> call, Throwable t) {
                ResultCode.callFailure(t.getMessage() + "手势密码未清除");
                closeProgress();
                finish();
            }
        });
    }

    private void logoutRequest() {
        String token = MyApp.getInstance().getDeviceToken();
        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                token );

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<Object>> call = service.postLogout(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
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
