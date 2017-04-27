package cn.zheft.www.zheft.fragm;


import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.Map;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseFragment;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.AccountInfo;
import cn.zheft.www.zheft.retrofit.HttpUtil;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.RequestUrl;
import cn.zheft.www.zheft.retrofit.ResponseBase;
import cn.zheft.www.zheft.retrofit.ResponseUtil;
import cn.zheft.www.zheft.retrofit.ResultCode;
import cn.zheft.www.zheft.retrofit.RetrofitAPI;
import cn.zheft.www.zheft.ui.BalanceActivity;
import cn.zheft.www.zheft.ui.CountActivity;
import cn.zheft.www.zheft.ui.PayInputActivity;
import cn.zheft.www.zheft.util.ClickUtil;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.StringUtil;
import cn.zheft.www.zheft.util.ToastUtil;
import cn.zheft.www.zheft.view.NumScrollAnim;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 为保证缩小后还能在屏内留有一点边界
 * 缩放比例（全隐大小/正常大小）需要满足 比例r > 2-z/x
 * 其中 z表示屏宽，x表示view宽
 * 暂未计入viewDivide
 */
public class AccountFragment extends BaseFragment {
    private Context context;
    private MyApp myApp;
    private RelativeLayout rlProgressBar;
    private ImageButton ibtnOverlay;
    private boolean loadEnd = true;

    private static final String ARG_PARAM_ACC = "param";
    private String mParamAcc;

    private RelativeLayout rlBalance; // 余额
    private RelativeLayout rlCount;   // 统计
    private RelativeLayout rlQRCode;  // 二维码收款

    private TextView tvBalance;  // 余额数值（需要自己拼接“元”）
    private TextView tvToday;    // 今日交易额
    private TextView tvYesterday;// 昨日交易额
    private TextView tvMonth;    // 当月交易额

    // 可提现余额，传入提现页的参数（以分为单位）
    private int fund = 0;

    public AccountFragment() {
        // Required empty public constructor
    }

    public static AccountFragment newInstance(String param) {
        AccountFragment fragment = new AccountFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_ACC, param);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParamAcc = getArguments().getString(ARG_PARAM_ACC);
        }
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        rlProgressBar = (RelativeLayout) view.findViewById(R.id.account_progress_dialog);
        ibtnOverlay = (ImageButton) view.findViewById(R.id.ibtn_account_overlay);
        rlBalance = (RelativeLayout) view.findViewById(R.id.rlayout_to_balance);
        rlCount = (RelativeLayout) view.findViewById(R.id.rlayout_to_count);
        rlQRCode = (RelativeLayout) view.findViewById(R.id.rlayout_to_qrcode);

        tvBalance = (TextView) view.findViewById(R.id.tv_account_balance);
        tvToday = (TextView) view.findViewById(R.id.tv_income_today);
        tvMonth = (TextView) view.findViewById(R.id.tv_income_month);
        tvYesterday = (TextView) view.findViewById(R.id.tv_income_yesterday);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // 初始化一些重要参数
        tvBalance.setText(getString(R.string.account_init_amount) + "元");

        initEvent();
        myApp = MyApp.getInstance();
        // 初始化页面时调取数据
//        getData();
    }

    private void initEvent() {
        // 余额
        rlBalance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ClickUtil.cantClick()) {
                    startActivity(new Intent(context, BalanceActivity.class));
                    MobclickAgent.onEvent(context, "um_account_balance"); // 友盟统计:余额
                }
            }
        });
        rlCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ClickUtil.cantClick()) {
                    startActivity(new Intent(context, CountActivity.class));
                    MobclickAgent.onEvent(context, "um_account_statistics"); // 友盟统计:统计
                }
            }
        });
        rlQRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!ClickUtil.cantClick()) {
                    serverConnectPay();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getUserVisibleHint()) {
            getData();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getData();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    // 获取数据
    private void getData() {
        if (nonNetwork() || !loadEnd) {
            return;
        }
        startProgressDelay();
        String cipher = HttpUtil.getExtraKey(
                myApp.getDeviceId(),
                Config.DEVICE_TYPE,
                myApp.getUserInfo().getMobile());

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<AccountInfo>> call = service.postAccountInfo(
                myApp.getDeviceId(),
                Config.DEVICE_TYPE,
                myApp.getUserInfo().getMobile(),
                cipher );
        call.enqueue(new Callback<ResponseBase<AccountInfo>>() {
            @Override
            public void onResponse(Call<ResponseBase<AccountInfo>> call, Response<ResponseBase<AccountInfo>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        // 保存可提现余额
                        fund = StringUtil.fenToInt(response.body().getData().getFund());
                        // 显示数据
                        updateData(response.body().getData());
                    } else {
                        if (!resultCode(response.body().getResultCode())) {
                            ResultCode.accountInfo(response.body().getResultCode());
                        }
                    }
                } else {
                    ResultCode.responseCode(response.code());
                }
                closeProgressQuick();
            }

            @Override
            public void onFailure(Call<ResponseBase<AccountInfo>> call, Throwable t) {
                ResultCode.callFailure(t.getMessage());
                closeProgressQuick();
            }
        });
    }

    private void updateData(AccountInfo info) {
        if (info == null) {
            return;
        }

        NumScrollAnim.startAnim(tvToday , StringUtil.strToInt(info.getIncomeTomorrow()));
        NumScrollAnim.startAnim(tvYesterday, StringUtil.strToInt(info.getIncomeToday()));
        NumScrollAnim.startAnim(tvMonth, StringUtil.strToInt(info.getIncomeMonth()));
        tvBalance.setText( StringUtil.addNumberComma( StringUtil.fenToYuan(info.getFund()) ) + "元" );
    }

    private void startProgressDelay() {
        // 三百毫秒内加载完成就不显示加载圈
        ibtnOverlay.setVisibility(View.VISIBLE);
        loadEnd = false;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!loadEnd) {
                    rlProgressBar.setVisibility(View.VISIBLE);
                    ObjectAnimator.ofFloat(rlProgressBar, "alpha", 0.0f, 1.0f).setDuration(100).start();
                }
            }
        }, 300);
    }

    private void closeProgressQuick() {
        rlProgressBar.setVisibility(View.INVISIBLE);
        ibtnOverlay.setVisibility(View.INVISIBLE);
        loadEnd = true;
        ObjectAnimator.ofFloat(rlProgressBar, "alpha", 1.0f, 0.0f).setDuration(100).start();
    }

    private void serverConnectPay() {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        // 填充参数
        Map<String, Object> paras = new HashMap<>();
        paras.put("mobile", myApp.getUserInfo().getMobile());
        // 发起请求
        MyRetrofit.init(context).url(RequestUrl.IS_OPEN_QRCODE).params(paras)
                .success(new MyRetrofit.HttpSuccess() {
                    @Override
                    public void onSuccess(cn.zheft.www.zheft.retrofit.Response body) {
                        closeProgress();
                        dealServerData(body);
                    }
                })
                .failure(new MyRetrofit.HttpFailure() {
                    @Override
                    public void onFailure(String message) {
                        closeProgress();
                        ToastUtil.showShortMessage("网络异常");
                    }
                })
                .post();
    }

    private void dealServerData(cn.zheft.www.zheft.retrofit.Response body) {
        if (MyRetrofit.RESULT_SUCCESS == body.getResultcode()) {
            Map data = ResponseUtil.getMap(body.getData());
            if ("true".equals(data.get("isOpenQr"))) {
                startActivity(new Intent(context, PayInputActivity.class));
            } else {
                showNoQRCodeDialog();
            }
        } else {
            ToastUtil.showShortMessage(body.getResultmsg());
        }
    }

    private void showNoQRCodeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setTitle("提示");
        builder.setMessage(context.getString(R.string.account_qrcode_not_open));
        builder.setPositiveButton("确定",null);
        builder.show();
    }
}
