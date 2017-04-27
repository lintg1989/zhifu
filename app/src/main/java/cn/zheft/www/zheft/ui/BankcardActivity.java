package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.design.coderbeanliang.swipeloadrecyclerview.SwipeLoadRecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.adapter.BankcardAdapter;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.BankcardInfo;
import cn.zheft.www.zheft.retrofit.HttpUtil;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.ResponseBase;
import cn.zheft.www.zheft.retrofit.ResultCode;
import cn.zheft.www.zheft.retrofit.RetrofitAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 银行卡列表
 */
public class BankcardActivity extends BaseActivity implements
        BankcardAdapter.OnItemClickListener, SwipeLoadRecyclerView.OnLoadListener {
    private Context mContext;
    private static final int BANKCARD_PAGE_NUM = 1000;// 一次加载10条数据
    private static final int RESULT_CODE_BANK = 1; // 银行卡返回码

    private SwipeLoadRecyclerView swipeLoadRecyclerView;
    private BankcardAdapter bankcardAdapter;

    private int fromState;// 为1代表银行卡可选

    private List<BankcardInfo> bankcards = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bankcard);
        setToolbar(R.string.my_bankcards);
        mContext = this;

        Intent intent = getIntent();
        fromState = intent.getIntExtra("from",0);

        initView();
    }

    private void initView() {
        bankcardAdapter = new BankcardAdapter(mContext, bankcards, R.layout.item_bankcard);

        swipeLoadRecyclerView = (SwipeLoadRecyclerView) findViewById(R.id.swipe_load_refresh_view_bankcard);
        swipeLoadRecyclerView.setAdapter(bankcardAdapter);
        swipeLoadRecyclerView.setPageVolume(BANKCARD_PAGE_NUM);
        swipeLoadRecyclerView.setOnLoadListener(this);
        swipeLoadRecyclerView.onRefresh();

        if (fromState == 1) {
            bankcardAdapter.setOnItemClickListener(this); // 设置点击监听
        }

    }

    private void getData(int page) {
        if (nonNetwork()) {
            swipeLoadRecyclerView.netError();
            return;
        }
        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile() );

        // 按分页取得数据（目前只查一页）
        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<List<BankcardInfo>>> call = service.postCardList(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                cipher );
        call.enqueue(new Callback<ResponseBase<List<BankcardInfo>>>() {
            @Override
            public void onResponse(Call<ResponseBase<List<BankcardInfo>>> call, Response<ResponseBase<List<BankcardInfo>>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        swipeLoadRecyclerView.update(response.body().getData());
                        return;

                    } else {
                        if(!resultCode(response.body().getResultCode())) {
                            // 解析码
                            if (!"3".equals(response.body().getResultCode())) {
//                                ToastUtil.showShortMessage("请刷新重试");
                            }
                        }
                    }
                } else {
                    ResultCode.responseCode(response.code());
                }
                swipeLoadRecyclerView.netError();
            }

            @Override
            public void onFailure(Call<ResponseBase<List<BankcardInfo>>> call, Throwable t) {
                ResultCode.callFailure(t.getMessage());
                swipeLoadRecyclerView.netError();
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        if (bankcards.size() > position) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("card", bankcards.get(position));
            Intent intent = new Intent();
            intent.putExtras(bundle);
            setResult(RESULT_CODE_BANK,intent);
            finish();// 关闭才能回传
        }
    }

    @Override
    public void swipeLoadFromPage(int page) {
        getData(page);
    }
}
