package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.adapter.ProblemAdapter;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.ProblemInfo;
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

/**
 * 常见问题
 */
public class ProblemActivity extends BaseActivity implements ProblemAdapter.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener{
    private Context context;
    private static final int PROBLEM_PAGE_NUM = 100;// 全部加载

    private int page = 1;// 页数
    private boolean isLoading;
    private boolean isEnd = false;
    private boolean isStart = true;// 用于初次加载页面无数据显示时

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProblemAdapter problemAdapter;
    private LinearLayoutManager layoutManager;

    private List<ProblemInfo> problems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_problem);
        setToolbar(R.string.common_problem);
        context = this;

        initView();
        initData();// 初始化数据
    }

    private void initView() {
        // SwipeRefreshLayout部分
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout_problem);
        swipeRefreshLayout.setColorSchemeResources(R.color.theme_color_app);
        swipeRefreshLayout.setOnRefreshListener(this);
        // RecyclerView部分
        problemAdapter = new ProblemAdapter(context, problems);
        problemAdapter.setOnItemClickListener(this); // 设置点击监听
        layoutManager = new LinearLayoutManager(context);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_problem);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(problemAdapter);
        recyclerView.addOnScrollListener(scrollListener);
    }

    private void initData() {
        // 保证页面打开就显示进度条，但还是需要手动加载数据，或者使用onRefreshListener.onRefresh()
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        problemAdapter.setStart(true);
        getData();// 手动加载数据
    }

    private void getData() {
        if (nonNetwork()) {
            updateData();
            return;
        }
        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile() );

        // 按分页取得设备列表
        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<List<ProblemInfo>>> call = service.postQAList(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                cipher );
        call.enqueue(new Callback<ResponseBase<List<ProblemInfo>>>() {
            @Override
            public void onResponse(Call<ResponseBase<List<ProblemInfo>>> call, Response<ResponseBase<List<ProblemInfo>>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        // 开始准备数据
                        if (swipeRefreshLayout.isRefreshing()) {
                            problems.clear();
                        }
                        for (ProblemInfo problemInfo : response.body().getData()) {
                            problems.add(problemInfo);
                        }
//                        // 取得后
//                        page++;
                    } else {
                        if(!resultCode(response.body().getResultCode())) {
                            // 解析码
                        }
                    }
                } else {
                    ResultCode.responseCode(response.code());
                }
                updateData();// 更新页面
            }

            @Override
            public void onFailure(Call<ResponseBase<List<ProblemInfo>>> call, Throwable t) {
                ResultCode.callFailure(t.getMessage());
                updateData();
            }
        });
    }

    private void updateData() {
        problemAdapter.setStart(false);
        isLoading = false;
//        int lastNum = problems.size() % PROBLEM_PAGE_NUM;
//        isEnd = ( problems.size() == 0 || (lastNum != 0 && lastNum < PROBLEM_PAGE_NUM ));
        isEnd = (problems.size() < PROBLEM_PAGE_NUM * page);
        problemAdapter.setEnd(isEnd);
        problemAdapter.notifyDataSetChanged();
//        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                // 使用post进行加载状态的取消
                swipeRefreshLayout.setRefreshing(false);
            }
        });
//        problemAdapter.notifyItemRemoved(problemAdapter.getItemCount());
        page++;
    }

    @Override
    public void onItemClick(View view, int position) {
        // 点击事件
        if (problems.get(position) != null) {
//            ToastUtil.showShortMessage(position + ":" + problems.get(position).getName());
            Intent intent = new Intent(context, ProblemDetailActivity.class);
            intent.putExtra("ProblemInd",problems.get(position).getInd());
            intent.putExtra("ProblemTitle",problems.get(position).getTitle());
            startActivity(intent);
        } else {
            ToastUtil.showShortMessage("未知错误，请刷新后重试");
        }
    }

    @Override
    public void onRefresh() {
        // 正在加载时禁止下拉刷新
        if (isLoading) {
            swipeRefreshLayout.setRefreshing(false);   // 不加这一句会把数据清除
            return;
        }
        problemAdapter.setEnd(isEnd = false);
        page = 1;
        getData();
    }

    // RecyclerView的滚动监听
    private RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            // 列表最后一个元素可见那么 判断isRefreshing是否true
            int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
            if (lastVisibleItemPosition + 1 == problemAdapter.getItemCount()) {
                boolean isRefreshing = swipeRefreshLayout.isRefreshing();
                if (isEnd) {
                    return;
                }
                if (isRefreshing) {
//                    LogUtil.e("Problem", "IsRefreshing" + problemAdapter.getItemCount());
//                    problemAdapter.notifyItemRemoved(problemAdapter.getItemCount());
                    return;
                }
                if (!isLoading && !isEnd) {
                    isLoading = true;
                    // 执行耗时操作
                    getData();
                }
            }
        }
    };
}
