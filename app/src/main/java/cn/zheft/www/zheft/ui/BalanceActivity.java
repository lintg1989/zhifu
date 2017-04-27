package cn.zheft.www.zheft.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.design.coderbeanliang.swipeloadrecyclerview.SwipeLoadRecyclerView;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.adapter.BalanceAdapter;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.AccountInfo;
import cn.zheft.www.zheft.model.BalanceInfo;
import cn.zheft.www.zheft.retrofit.HttpUtil;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.ResponseBase;
import cn.zheft.www.zheft.retrofit.ResultCode;
import cn.zheft.www.zheft.retrofit.RetrofitAPI;
import cn.zheft.www.zheft.util.ClickUtil;
import cn.zheft.www.zheft.util.StringUtil;
import cn.zheft.www.zheft.view.NumScrollAnim;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 余额页面
 */
public class BalanceActivity extends BaseActivity implements BaseActivity.OnMenuClicked,
        SwipeLoadRecyclerView.OnLoadListener, BalanceAdapter.OnItemClickListener {

    private Context mContext;
    private int fund = 0;  // 可提现余额，由上一页传入

    private TextView tvBalance; // 余额Header
    private TextView tvNoMore;  // 列表footer

    // 列表相关
    private static int PAGE_NUM_BALANCE = 20;
    private List<BalanceInfo> detailInfos = new ArrayList<>();

    private SwipeLoadRecyclerView swipeLoadRecyclerView;
    private BalanceAdapter balanceAdapter;

    private boolean shouldUpdate = true;
    private MyBroadcastReceiver broadcastReceiver;
    public static final String LOCAL_ACTION_BALANCE_UPDATE = "local.action.balance_should_update";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);
        setToolbar(getString(R.string.account_balance), true, getString(R.string.account_cashout), true);
        setOnMenuClickedLisener(this);
        mContext = this;

        initView();
        initBroadcast();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        swipeLoadRecyclerView.onRefresh();
        if (shouldUpdate) {
            swipeLoadRecyclerView.onRefresh();
            shouldUpdate = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(mContext)
                .unregisterReceiver(broadcastReceiver);
    }

    private void initView() {
        // Header部分
        View headerView = LayoutInflater.from(mContext).inflate(R.layout.layout_balance_header, null);
        tvBalance = (TextView) headerView.findViewById(R.id.tv_balance);
        tvBalance.setText(StringUtil.fenToYuan("0"));
        View footerView = LayoutInflater.from(mContext).inflate(R.layout.item_footer_balance, null);
        tvNoMore = (TextView) footerView.findViewById(R.id.tv_item_footer_balance);

        balanceAdapter = new BalanceAdapter(mContext, detailInfos, R.layout.item_balance);
        balanceAdapter.setOnItemClickListener(this);
        swipeLoadRecyclerView = (SwipeLoadRecyclerView) findViewById(R.id.swipe_load_refresh_view_balance);
        swipeLoadRecyclerView.setAdapter( balanceAdapter );
        swipeLoadRecyclerView.setPageVolume( PAGE_NUM_BALANCE );
        // swipeLoadRecyclerView.setHeaderChangeable(false);
        swipeLoadRecyclerView.setAdapterHeader(headerView);
        swipeLoadRecyclerView.setAdapterFooter(footerView);
        swipeLoadRecyclerView.setOnLoadListener(this);
    }

    private void initBroadcast() {
        broadcastReceiver = new MyBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(LOCAL_ACTION_BALANCE_UPDATE);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(broadcastReceiver, filter);
    }


    private void getData(int page) {
        if (nonNetwork()) {
            swipeLoadRecyclerView.netError();
            return;
        }
        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                String.valueOf(page),
                "0");// 查询第一页数据，类型为全部交易

        // 按分页取得数据
        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<List<BalanceInfo>>> call = service.postCashDetail(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                String.valueOf(page),
                "0",
                cipher);
        call.enqueue(new Callback<ResponseBase<List<BalanceInfo>>>() {
            @Override
            public void onResponse(Call<ResponseBase<List<BalanceInfo>>> call,
                                   Response<ResponseBase<List<BalanceInfo>>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        // 开始准备数据
                        dealBalanceData(response.body());
                    } else {
                        if(!resultCode(response.body().getResultCode())) {
                            ResultCode.withdrawDetail(response.body().getResultCode());
                        }
                        swipeLoadRecyclerView.update(null);
                    }
                } else {
                    ResultCode.responseCode(response.code());
                    swipeLoadRecyclerView.netError();
                }
            }

            @Override
            public void onFailure(Call<ResponseBase<List<BalanceInfo>>> call, Throwable t) {
                ResultCode.callFailure(t.getMessage());
                swipeLoadRecyclerView.netError();
            }
        });
    }

    private void getBalance() {
        if (nonNetwork()) {
            return;
        }
        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile());

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<AccountInfo>> call = service.postAccountInfo(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                cipher );
        call.enqueue(new Callback<ResponseBase<AccountInfo>>() {
            @Override
            public void onResponse(Call<ResponseBase<AccountInfo>> call, Response<ResponseBase<AccountInfo>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        // 保存可提现余额
                        fund = StringUtil.fenToInt(response.body().getData().getFund());
                        // 显示数据
                        NumScrollAnim.startAnim(tvBalance, fund);
                    } else {
                        if (!resultCode(response.body().getResultCode())) {
                            ResultCode.accountInfo(response.body().getResultCode());
                        }
                    }
                } else {
                    ResultCode.responseCode(response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBase<AccountInfo>> call, Throwable t) {
                ResultCode.callFailure(t.getMessage());
            }
        });
    }

    @Override
    public void onItemClick(View view, int i) {
        if (ClickUtil.cantClick()) {
            return;
        }
        // 点击跳转
        Intent intent = new Intent(mContext, OrderDetailActivity.class);
        intent.putExtra("info", detailInfos.get(i));
        startActivity(intent);
    }

    @Override
    public void onBtnClicked() {
        // 提现
        if (ClickUtil.cantClick()) {
            return;
        }
        Intent intent = new Intent(mContext, CashOutActivity.class);
        intent.putExtra("fund", fund);
        startActivity(intent);
        MobclickAgent.onEvent(mContext, "um_account_withdraw"); // 友盟统计
    }

    @Override
    public void swipeLoadFromPage(int page) {
        if (page == 1) {
            getBalance();// 刷新的时候更新表头
        }
        getData(page);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String actionStr = intent.getAction();
            if (LOCAL_ACTION_BALANCE_UPDATE.equals(actionStr)) {
                shouldUpdate = true;
            }
        }
    }

    private void dealBalanceData(ResponseBase<List<BalanceInfo>> body) {
        // noOneYearAgoData与其他提示统一处理
        if ("oneYearAgoData".equals(body.getResultmsg())) {
            tvNoMore.setText(getString(R.string.balance_year_ago));
        } else {
            tvNoMore.setText(getString(R.string.item_foot_empty));
        }
        swipeLoadRecyclerView.update(body.getData());
    }

}
