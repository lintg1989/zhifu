package cn.zheft.www.zheft.fragm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.design.coderbeanliang.swipeloadrecyclerview.SwipeLoadRecyclerView;
import com.umeng.analytics.MobclickAgent;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.adapter.FindAdapter;
import cn.zheft.www.zheft.app.BaseFragment;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.FindInfo;
import cn.zheft.www.zheft.retrofit.HttpUtil;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.ResponseBase;
import cn.zheft.www.zheft.retrofit.ResultCode;
import cn.zheft.www.zheft.retrofit.RetrofitAPI;
import cn.zheft.www.zheft.ui.FindDetailActivity;
import cn.zheft.www.zheft.util.ClickUtil;
import cn.zheft.www.zheft.util.DateUtil;
import cn.zheft.www.zheft.util.LogUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link FindFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FindFragment extends BaseFragment implements FindAdapter.OnItemClickListener,
        SwipeLoadRecyclerView.OnLoadListener{

    private Context context;
    private View statusView; // 自定义状态栏
    private boolean isTransStatusbar;
    private static final int FIND_PAGE_NUM = 5;// 一次加载5条数据
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM_FIND = "paramFind";
    // TODO: Rename and change types of parameters
    private String mParamFind;

    private SwipeLoadRecyclerView swipeLoadRecyclerView;
    private FindAdapter findAdapter;

    private boolean fistLoad = true;

    private List<FindInfo> findInfos = new ArrayList<>();

    public FindFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param Parameter 1.
     * @return A new instance of fragment FindFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FindFragment newInstance(String param) {
        FindFragment fragment = new FindFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_FIND, param);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParamFind = getArguments().getString(ARG_PARAM_FIND);
        }
        context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_find, container, false);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_default);
        TextView tvTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        tvTitle.setText(R.string.find_title);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initData();
    }

    private void initView() {
        int itemEmpty = R.layout.item_footer_find_nodata;
        int itemLoad = R.layout.item_footer_load;
        int itemEnd = R.layout.item_footer_end;
        int itemNet = R.layout.item_footer_net;

        findAdapter = new FindAdapter(context, findInfos, R.layout.item_find);
        findAdapter.setOnItemClickListener(this);// 设置点击监听
        swipeLoadRecyclerView = (SwipeLoadRecyclerView) getActivity().findViewById(R.id.swipe_load_refresh_view_find);
        swipeLoadRecyclerView.setAdapter(findAdapter);
        swipeLoadRecyclerView.setOnLoadListener(this);
        swipeLoadRecyclerView.setEmptyDataShowNullFooter(true);
        swipeLoadRecyclerView.getRecyclerView().setItemViewCacheSize(40);// 列表缓存数量40
        swipeLoadRecyclerView.setAdapterFooterLayout(itemEmpty, itemLoad, itemEnd, itemNet);

        swipeLoadRecyclerView.onRefresh();

        // 状态栏高度(获取后更改)
        customStatusBar();
    }

    private void initData() {

    }

    // 获取本地数据
    private void getLocalData(int page) {

        List<FindInfo> infos = DataSupport.limit(FIND_PAGE_NUM)
                .offset(FIND_PAGE_NUM * (page-1))
                .find(FindInfo.class);

        swipeLoadRecyclerView.update(infos);
    }

    // 获取服务器数据
    private void getServerData() {
        if (nonNetwork()) {
            swipeLoadRecyclerView.update(null);
            return;
        }
        // pageNum和pageSize都传 -1 表示获取全部数据
        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                "-1",
                "-1" );
        // 获取数据
        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<List<FindInfo>>> call = service.postFindInfo(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                "-1",
                "-1",
                cipher );
        call.enqueue(new Callback<ResponseBase<List<FindInfo>>>() {
            @Override
            public void onResponse(Call<ResponseBase<List<FindInfo>>> call,
                                   Response<ResponseBase<List<FindInfo>>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        // 存储新数据
                        saveNewData(response.body().getData());
                        return;
                    } else {
                        if (!resultCode(response.body().getResultCode())) {
                            // 解析码
                        }
                    }
                } else {
                    ResultCode.responseCode(response.code());
                }
                swipeLoadRecyclerView.update(null);
            }

            @Override
            public void onFailure(Call<ResponseBase<List<FindInfo>>> call, Throwable t) {
                ResultCode.callFailure(t.getMessage());
                swipeLoadRecyclerView.update(null);
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        if (ClickUtil.cantClick()) {
            return;
        }
        if (findInfos == null || findInfos.get(position) == null) {
            return;
        }
        Intent intent = new Intent(context, FindDetailActivity.class);
        intent.putExtra("linkUrl",findInfos.get(position).getLinkUrl());
        startActivity(intent);

        // 友盟统计事件
        HashMap<String, String> map = new HashMap<>();
        map.put("type", findInfos.get(position).getTitle());
        MobclickAgent.onEvent(context, "um_discover_list", map);
    }

    private void saveNewData(final List<FindInfo> infos) {
        try {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    DataSupport.deleteAll(FindInfo.class);
                    for (FindInfo info : infos) {
                        info.save();
                    }
                    Config.setFindUpdDate(DateUtil.getTodayStr());
                    handler.sendEmptyMessage(0);
                }
            });
            thread.start();
        } catch (Exception e) {
            LogUtil.e("FindFregment","Save new data error!\n" + e.getMessage());
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            getLocalData(1);
            super.handleMessage(msg);
        }
    };

    public void setTransBar(boolean transBar) {
        isTransStatusbar = transBar;
    }

    private void customStatusBar() {
        statusView = getActivity().findViewById(R.id.view_status_bar_find);
        // 在5.0以上使用自己设置的标题栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            statusView.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Rect rect = new Rect();
                        getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
                        statusView.getLayoutParams().height = rect.top;
                        if (!isTransStatusbar) {
                            statusView.setVisibility(View.GONE);
                        }
                    } catch (Exception e) {
                        statusView.setVisibility(View.GONE);
                    }
                }
            });
        } else {
            statusView.setVisibility(View.GONE);
        }
    }

    @Override
    public void swipeLoadFromPage(int page) {

        // 打开app时需要判断本地加载还是网络加载
        if (fistLoad) {
            fistLoad = false;
            int result = DataSupport.count(FindInfo.class);
            if (DateUtil.getTodayStr().equals(Config.getFindUpdDate()) && result > 0) {
                getLocalData(1);// 加载本地数据
            } else {
                getServerData();// 加载服务器数据
            }
            return;
        }

        // 刷新时是getServer,滚动时是getLocal
        if (page > 1) {
            // 加载本地
            getLocalData(page);
        } else {
            getServerData();
        }
    }
}
