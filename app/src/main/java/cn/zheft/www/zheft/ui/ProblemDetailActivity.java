package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.ProblemDetailInfo;
import cn.zheft.www.zheft.retrofit.HttpUtil;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.ResponseBase;
import cn.zheft.www.zheft.retrofit.ResultCode;
import cn.zheft.www.zheft.retrofit.RetrofitAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 问题详情
 */
public class ProblemDetailActivity extends BaseActivity {
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_problem_detail);
        setToolbar(R.string.problem_detail);
        context = this;

        Intent intent = getIntent();
        String ind = intent.getStringExtra("ProblemInd");
        String title = intent.getStringExtra("ProblemTitle");

        if (ind != null && title != null) {
            ((TextView) findViewById(R.id.tv_problem_title)).setText(title);
            getData(ind);
        } else {
            TextView tvError = (TextView) findViewById(R.id.text_error);
            tvError.setVisibility(View.VISIBLE);
        }
    }

    private void getData(String id) {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                id );

        // 取得问题详情
        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<ProblemDetailInfo>> call = service.postQADetail(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                id,
                cipher);
        call.enqueue(new Callback<ResponseBase<ProblemDetailInfo>>() {
            @Override
            public void onResponse(Call<ResponseBase<ProblemDetailInfo>> call, Response<ResponseBase<ProblemDetailInfo>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        initView(response.body().getData());
                    } else {
                        if(!resultCode(response.body().getResultCode())) {
                            // 解析码
                        }
                    }
                } else {
                    ResultCode.responseCode(response.code());
                }
                closeProgress();
            }

            @Override
            public void onFailure(Call<ResponseBase<ProblemDetailInfo>> call, Throwable t) {
                ResultCode.callFailure(t.getMessage());
                closeProgress();
            }
        });
    }

    private void initView(ProblemDetailInfo problem) {
        TextView tvTitle = (TextView) findViewById(R.id.tv_problem_title);
        TextView tvContent = (TextView) findViewById(R.id.tv_problem_content);

        tvTitle.setText(problem.getTitle());
        tvContent.setText(problem.getContent());
    }
}
