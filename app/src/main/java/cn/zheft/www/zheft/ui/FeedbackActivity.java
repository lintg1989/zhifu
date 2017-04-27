package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
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
import cn.zheft.www.zheft.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedbackActivity extends BaseActivity {
    private Context context;

    private static int REQUEST_CODE = 0;// 请求码（作用？）

    private TextView tvType;
    private EditText etContent;
    private String[] types;

    private int result = 0;// 保存返回码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        setToolbar(R.string.feedbacks);
        context = this;

        types = getResources().getStringArray(R.array.feedback_types);
        initView();
    }

    private void initView() {
        etContent = (EditText) findViewById(R.id.et_feedbacks);
        RelativeLayout rlType = (RelativeLayout) findViewById(R.id.rlayout_feedback_type);
        tvType = (TextView) rlType.findViewById(R.id.tv_click_layout);
        tvType.setText(R.string.select_feedback_type);

        rlType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FeedbackTypeActivity.class);
                intent.putExtra("type", result);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_CODE == requestCode) {
            if (resultCode > 0 && resultCode < 4) {
                LogUtil.e("ResultCode", "" + resultCode);
                result = resultCode;
                tvType.setText(types[resultCode - 1]);
            }
        }
    }

    public void feedbackCommit(View view) {
        // 提交
        if (checkInput()) {
            sendAdvice();
        }
    }

    private boolean checkInput() {
        if (result == 0) {
            ToastUtil.showShortMessage("请选择反馈类型");
            return false;
        } else if (etContent == null || etContent.getText().toString().trim().isEmpty()) {
            ToastUtil.showShortMessage("请填写反馈内容");
            return false;
        } else {
            return true;
        }
    }

    private void sendAdvice() {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                String.valueOf(result),
                MyApp.getInstance().getUserInfo().getMobile());
//                String.valueOf(result),
//                etContent.getText().toString() );// 暂时不加该参数

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<Object>> call = service.postAdvice(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                String.valueOf(result),
                etContent.getText().toString(),
                MyApp.getInstance().getUserInfo().getMobile(),
                cipher );
        call.enqueue(new Callback<ResponseBase<Object>>() {
            @Override
            public void onResponse(Call<ResponseBase<Object>> call, Response<ResponseBase<Object>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        ToastUtil.showShortMessage("提交成功");
                        etContent.setText("");
                    } else {
                        if(!resultCode(response.body().getResultCode())) {
                            // 解析码
                            ToastUtil.showShortMessage("未知错误，请重试");
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
