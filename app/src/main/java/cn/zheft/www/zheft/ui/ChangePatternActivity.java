package cn.zheft.www.zheft.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.retrofit.HttpUtil;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.ResponseBase;
import cn.zheft.www.zheft.retrofit.ResultCode;
import cn.zheft.www.zheft.retrofit.RetrofitAPI;
import cn.zheft.www.zheft.util.StringUtil;
import cn.zheft.www.zheft.util.ToastUtil;
import cn.zheft.www.zheft.view.GestureLockViewGroup;
import cn.zheft.www.zheft.view.LockIndicator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePatternActivity extends BaseActivity {
    private Context context;
    private TextView tvPrompt;// 提示用textview
    private TextView tvChange;// 先作忘记密码，后改为重设
    private ColorStateList defaultTextColor;// 保存TextView默认颜色

    private int fromState;
    private boolean isCheck = true;// 为true是验证旧密码状态，false为设置新密码状态
    private boolean maxTime = false;// 用于区分resetLock中的流程，错误次数达到后先清数据，忘记密码重置完服务器再清

    private GestureLockViewGroup gestureLockViewGroup;
    private LockIndicator lockIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pattern);
        setToolbar(R.string.change_pattern_lock);
        context = this;

        initView();
    }

    private void initView() {
        tvPrompt = (TextView) findViewById(R.id.tv_change_pattern);
        tvChange = (TextView) findViewById(R.id.tv_change_forget_reset);
        defaultTextColor = tvPrompt.getTextColors();

        int umatchedTime = Config.getUnmatchedTimes();
        if (umatchedTime < 5) {
            tvPrompt.setTextColor(getResources().getColor(R.color.pattern_error_color));
            tvPrompt.setText("剩余次数" + umatchedTime + "次");
        }

        lockIndicator = (LockIndicator) findViewById(R.id.gesture_lock_dots);
        // 手势密码
        gestureLockViewGroup = (GestureLockViewGroup) findViewById(R.id.gesture_lock_view_group);
        gestureLockViewGroup.setPattern(false);
        gestureLockViewGroup.setAnswer(MyApp.getInstance().getUserInfo().getPatternLock());
        gestureLockViewGroup.setUnMatchExceedBoundary(umatchedTime);
        gestureLockViewGroup.setOnGestureLockViewListener(new GestureLockViewGroup.OnGestureLockViewListener() {
            @Override
            public void onBlockSelected(int cId) {
                //
            }

            @Override
            public void onGestureEvent(boolean matched) {
                if (isCheck) {
                    // 验证旧密码
                    if (matched) {
                        tvChange.setText("    重设    ");
                        tvChange.setVisibility(View.INVISIBLE);

                        tvPrompt.setTextColor(defaultTextColor);
                        tvPrompt.setText("请绘制新手势密码");
                        Config.setUnmatchedTimes(5);
                        gestureLockViewGroup.reset();
                        isCheck = false;
                        gestureLockViewGroup.setPattern(true);
                        gestureLockViewGroup.setUnMatchExceedBoundary(1000000);
                        lockIndicator.setVisibility(View.VISIBLE);// 显示
                    } else {
                        tvChange.setVisibility(View.VISIBLE);
                        tvPrompt.setTextColor(getResources().getColor(R.color.pattern_error_color));
                        tvPrompt.setText("手势密码错误，剩余" + gestureLockViewGroup.getTryTimes() + "次");
                        Config.setUnmatchedTimes(gestureLockViewGroup.getTryTimes());
//                    lockIndicator.setPath(StringUtil.listToStr(gestureLockViewGroup.getChoose()));
                        gestureLockViewGroup.reset();
                    }
                } else {
                    // 输入新密码
                    if (matched) {
                        tvPrompt.setTextColor(getResources().getColor(R.color.theme_color_app));
                        tvPrompt.setText("设置成功");
//                        Toast.makeText(context, "您已成功设置了手势密码", Toast.LENGTH_SHORT).show();
                        String pattern = StringUtil.listToStr(gestureLockViewGroup.getChoose());
//                        lockIndicator.setPath(pattern);
//                        gestureLockViewGroup.reset();
                        savePattern(pattern);// 上传至服务器（该过程未成功怎么办？）
                    } else {
                        tvChange.setVisibility(View.VISIBLE);
                        tvPrompt.setTextColor(getResources().getColor(R.color.pattern_error_color));
                        tvPrompt.setText("手势密码不一致，请重新绘制");
//                        lockIndicator.setPath(StringUtil.listToStr(gestureLockViewGroup.getChoose()));
                        gestureLockViewGroup.reset();
                    }
                }
            }

            @Override
            public void onUnmatchedExceedBoundary() {
                // 达到错误次数时的处理，添加时效（5分钟后重试？）
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
                if (patternOk) {
//                    Toast.makeText(context, "设置了新密码", Toast.LENGTH_SHORT).show();
                    tvPrompt.setText(R.string.set_pattern_lock_twice);
                    gestureLockViewGroup.setPattern(false);//下一次需要判断是否正确
                } else {
//                    tvPrompt.setText(R.string.set_pattern_lock_twice);
                    Toast.makeText(context, "需要四个点以上", Toast.LENGTH_SHORT).show();
                }
                lockIndicator.setPath(StringUtil.listToStr(gestureLockViewGroup.getChoose()));
                gestureLockViewGroup.reset();
            }
        });
        tvChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCheck) {
                    forgetPattern();
                } else {
                    lockIndicator.setPath("");
                    gestureLockViewGroup.setPattern(true);
                    tvPrompt.setTextColor(defaultTextColor);
                    tvPrompt.setText(R.string.set_pattern_lock);
                    tvChange.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    // 保存手势密码
    private void savePattern(final String psw) {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                psw, "2");

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<Object>> call = service.postSetPassword(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                psw, "2", cipher);
        call.enqueue(new Callback<ResponseBase<Object>>() {
            @Override
            public void onResponse(Call<ResponseBase<Object>> call, Response<ResponseBase<Object>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        // Save
                        // 保存手势密码
                        MyApp.getInstance().getUserInfo().setPatternOn("1");// 手势密码开
                        MyApp.getInstance().getUserInfo().setPatternLock(psw); // 手势密码
                        Config.saveUserInfo(MyApp.getInstance().getUserInfo());

                        finish();
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
            public void onFailure(Call<ResponseBase<Object>> call, Throwable t) {
                ResultCode.callFailure(t.getMessage());
                closeProgress();
            }
        });
    }

    // 忘记密码跳转
    private void forgetPattern() {
        // 提示需要重新登录
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("忘记密码");
        builder.setMessage("忘记手势密码需要重新登录");
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 在这里清空用户数据后跳转至登录页面
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
                        if (!maxTime) {
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
                            ToastUtil.showShortMessage("手势设置失败，请重试");
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
                ResultCode.callFailure(t.getMessage());
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
