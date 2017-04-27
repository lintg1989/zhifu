package cn.zheft.www.zheft.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Calendar;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.HongbaoInfo;
import cn.zheft.www.zheft.model.MessageInfo;
import cn.zheft.www.zheft.retrofit.HttpUtil;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.ResponseBase;
import cn.zheft.www.zheft.retrofit.ResultCode;
import cn.zheft.www.zheft.retrofit.RetrofitAPI;
import cn.zheft.www.zheft.util.ClickUtil;
import cn.zheft.www.zheft.util.DateUtil;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 红包页面
 * 拦截返回事件
 * 拆红包在主页之前，故无需发广播通知界面消息数目更新
 */
public class HongbaoActivity extends BaseActivity {
    private Context context;
    private HongbaoInfo hongbaoInfo;

    private RelativeLayout rlBg;
    private RelativeLayout rlAmount;// 面额
    private LinearLayout llBack;
    private ImageView ivOpen;
    private ImageView ivUnder;
    private ImageView ivHead;
    private TextView tvDetail; // 查看明细
    private TextView tvMiddle; // 中间文字说明
    private TextView tvAmount; // 红包面额

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTransStatusBar();
        setContentView(R.layout.activity_hongbao);
        doubleClickBack();   // 双击退出
        context = this;

        Intent intent = getIntent();
        hongbaoInfo = (HongbaoInfo)intent.getSerializableExtra("hongbao_info");

        initView();
        initEvent();
    }

    private void initView() {
        llBack = (LinearLayout) findViewById(R.id.ll_hongbao_back);
        rlBg = (RelativeLayout) findViewById(R.id.rl_hongbao_bg);
        rlAmount = (RelativeLayout) findViewById(R.id.rl_hongbao_amount);
        ivUnder = (ImageView) findViewById(R.id.iv_hongbao_under);
        ivHead = (ImageView) findViewById(R.id.iv_hongbao_head_before);
        ivOpen = (ImageView) findViewById(R.id.iv_hongbao_open);
        tvDetail = (TextView) findViewById(R.id.tv_hongbao_detail);
        tvMiddle = (TextView) findViewById(R.id.tv_hongbao_mid);
        tvAmount = (TextView) findViewById(R.id.tv_hongbao_amount);
        if (hongbaoInfo != null && hongbaoInfo.getMoney() != null) {
            tvAmount.setText(hongbaoInfo.getMoney());
        }
    }

    private void initEvent() {
        ivOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHongbao();
            }
        });
        tvDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 至明细页面
            }
        });
        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, MainActivity.class));
                finish();
            }
        });
    }

    // 开红包
    private void openHongbao() {
        if (ClickUtil.cantClick()) {
            return;
        }
        try {
            String deviceId = MyApp.getInstance().getDeviceId();
            String mobile = MyApp.getInstance().getUserInfo().getMobile();
            String hongbaoCode = hongbaoInfo.getHongbaoCode();
            getOpenData(deviceId, mobile, hongbaoCode);//请求红包接口
        } catch (Exception e) {
            ToastUtil.showShortMessage("个人信息错误，请重新打开应用");
        }
    }

    /**
     * 请求红包领取的接口
     * @param deviceId
     * @param mobile
     * @param code
     */
    private void getOpenData(String deviceId, String mobile, String code) {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        String cipher = HttpUtil.getExtraKey(deviceId, Config.DEVICE_TYPE, mobile, code);

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<Object>> call = service.postHongbaoOpen(
                deviceId,
                Config.DEVICE_TYPE,
                mobile,
                code,
                cipher);
        call.enqueue(new Callback<ResponseBase<Object>>() {
            @Override
            public void onResponse(Call<ResponseBase<Object>> call, Response<ResponseBase<Object>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        toNextStep();
                    } else {
                        if (!resultCode(response.body().getResultCode())) {
                            // 解析码
                            ResultCode.openHongbao(response.body().getResultCode());
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

    // 认领完红包后保存用户数据
    private void toNextStep() {
        Config.saveUserInfo(MyApp.getInstance().getUserInfo());
        rlBg.setBackgroundColor(getResources().getColor(R.color.hongbao_bg_after));
        ivOpen.setVisibility(View.INVISIBLE);
        ivUnder.setVisibility(View.INVISIBLE);
        ivHead.setImageResource(R.mipmap.hongbao_head_after);
        rlAmount.setVisibility(View.VISIBLE);
        llBack.setVisibility(View.VISIBLE);
        tvDetail.setVisibility(View.VISIBLE);
        tvMiddle.setText(R.string.hongbao_after);
        tvMiddle.setTextColor(getResources().getColor(R.color.hongbao_text_orange));

        saveHongbaoMessage();// 保存红包消息
    }

    private void saveHongbaoMessage() {
        MessageInfo messageInfo = new MessageInfo();

        messageInfo.setPhone(Config.getUserPhone());
        messageInfo.setContent("新手红包 ￥" + tvAmount.getText());
        messageInfo.setType("红包");
        messageInfo.setDate(DateUtil.getNowFullStr());
        messageInfo.setRead(false);
        messageInfo.save();
    }

}
