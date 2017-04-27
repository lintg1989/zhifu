package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
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
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.StringUtil;
import cn.zheft.www.zheft.util.ToastUtil;
import cn.zheft.www.zheft.view.GestureLockViewGroup;
import cn.zheft.www.zheft.view.LockIndicator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 设置手势密码
 * toolbar右侧放置跳过按钮
 * 从不同页面进入该页选用不同状态
 * fromState = 0 设置页进入；fromState = 1 初始设置页进入
 */
public class CreatePatternActivity extends BaseActivity {
    private Context context;
    private TextView tvPrompt;// 提示用textview
    private TextView tvReset;// 重设
    private ColorStateList defaultTextColor;// 保存TextView默认颜色

    private int fromState;

    private GestureLockViewGroup gestureLockViewGroup;
    private LockIndicator lockIndicator;

    private final static int RESULT_CODE_ON = 1;// 开启返回码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_pattern);

        Intent intent = getIntent();
        fromState = intent.getIntExtra("from", 0);

        if (fromState == 0) {
            setToolbar(R.string.set_pattern_lock);
        } else {
            // 自定义toolbar，title为空、不带返回，右侧文字按钮，不显示底线
            setToolbar(null, false, getString(R.string.skip_set_pattern), false);
            setOnMenuClickedLisener(new OnMenuClicked() {
                @Override
                public void onBtnClicked() {
                    startActivity(new Intent(context, MainActivity.class));
                    finish();
                }
            });
        }
        context = this;

        initView();
    }

    private void initView() {
        tvPrompt = (TextView) findViewById(R.id.tv_set_pattern);
        tvReset = (TextView) findViewById(R.id.tv_reset_pattern);
        defaultTextColor = tvPrompt.getTextColors();

        lockIndicator = (LockIndicator) findViewById(R.id.gesture_lock_dots);
        // 手势密码
        gestureLockViewGroup = (GestureLockViewGroup) findViewById(R.id.gesture_lock_view_group);
        gestureLockViewGroup.setPattern(true); // 这句表明该页面为设置密码状态
        gestureLockViewGroup.setUnMatchExceedBoundary(1000000); // 设置密码时不限错误次数
        gestureLockViewGroup.setOnGestureLockViewListener(new GestureLockViewGroup.OnGestureLockViewListener() {
            @Override
            public void onBlockSelected(int cId) {
                //Do nothing
            }

            @Override
            public void onGestureEvent(boolean matched) {
                LogUtil.e("OnEvent", matched + "");
                if (matched) {
//                    tvPrompt.setTextColor(getResources().getColor(R.color.theme_color_app));
//                    tvPrompt.setText("设置成功");
                    String pattern = StringUtil.listToStr(gestureLockViewGroup.getChoose());

                    savePattern(pattern);// 上传至服务器（该过程未成功怎么办？）
                } else {
                    tvReset.setVisibility(View.VISIBLE);
                    tvPrompt.setTextColor(getResources().getColor(R.color.pattern_error_color));
                    tvPrompt.setText("手势密码不一致，请重新绘制");
//                    lockIndicator.setPath(StringUtil.listToStr(gestureLockViewGroup.getChoose()));
                    gestureLockViewGroup.reset();
                }
            }

            @Override
            public void onUnmatchedExceedBoundary() {
                // 达到错误次数时的处理，此处无需处理
            }

            @Override
            public void onFirstSetPattern(boolean patternOk) {
                if (patternOk) {
//                    Toast.makeText(context, "设置了初始密码", Toast.LENGTH_SHORT).show();
                    tvPrompt.setText(R.string.set_pattern_lock_twice);
                    gestureLockViewGroup.setPattern(false);//下一次需要判断是否正确
                } else {
                    tvPrompt.setTextColor(getResources().getColor(R.color.pattern_error_color));
                    tvPrompt.setText("需要四个点以上");
//                    Toast.makeText(context, "需要四个点以上", Toast.LENGTH_SHORT).show();
                }
//                LogUtil.e("str","is:"+listToStr(gestureLockViewGroup.getChoose()));
                lockIndicator.setPath(StringUtil.listToStr(gestureLockViewGroup.getChoose()));
                gestureLockViewGroup.reset();
            }
        });
        // 重设
        tvReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockIndicator.setPath("");
                gestureLockViewGroup.setPattern(true);
                tvPrompt.setTextColor(defaultTextColor);
                tvPrompt.setText(R.string.set_pattern_lock);
                tvReset.setVisibility(View.INVISIBLE);
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
                        tvPrompt.setTextColor(getResources().getColor(R.color.theme_color_app));
                        tvPrompt.setText("设置成功");
                        // 保存手势密码
                        MyApp.getInstance().getUserInfo().setPatternOn("1");// 手势密码开
                        MyApp.getInstance().getUserInfo().setPatternLock(psw); // 手势密码
                        Config.saveUserInfo(MyApp.getInstance().getUserInfo());

                        if (fromState == 1) {
                            Intent intent = new Intent(context, MainActivity.class);
                            startActivity(intent);
                        } else {
                            setResult(RESULT_CODE_ON);
                        }
                        finish();
                    } else {
                        if (fromState == 1) {
                            Intent intent = new Intent(context, MainActivity.class);
                            startActivity(intent);
                        }
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
}
