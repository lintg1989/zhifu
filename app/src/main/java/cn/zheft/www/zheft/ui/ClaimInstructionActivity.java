package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.ProtocolInfo;
import cn.zheft.www.zheft.retrofit.HttpUtil;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.ResponseBase;
import cn.zheft.www.zheft.retrofit.ResultCode;
import cn.zheft.www.zheft.retrofit.RetrofitAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 认领说明
 * 静态页面
 */
public class ClaimInstructionActivity extends BaseActivity {
    private Context context;
    private TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim_instruction);
        setToolbar(R.string.claim_instruction);
        disablePatternLock();
        context = this;

        tvContent = (TextView) findViewById(R.id.tv_claim_instruction_content);
        getData();
    }

    private void getData() {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                "1"); //参数“1”为认领说明

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<ProtocolInfo>> call = service.postProtocol(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                "1",
                cipher );
        call.enqueue(new Callback<ResponseBase<ProtocolInfo>>() {
            @Override
            public void onResponse(Call<ResponseBase<ProtocolInfo>> call, Response<ResponseBase<ProtocolInfo>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        // 请求成功
                        updateContent(response.body().getData().getContent());
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
            public void onFailure(Call<ResponseBase<ProtocolInfo>> call, Throwable t) {
                ResultCode.callFailure(t.getMessage());
                closeProgress();
            }
        });
    }

    private void updateContent(String content) {
        try {
            tvContent.setText(content);
        } catch (Exception e) {
            tvContent.setText("未知错误，请重试");
        }
    }
}
