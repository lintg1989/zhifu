package cn.zheft.www.zheft.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import com.design.coderbeanliang.swipeloadrecyclerview.SwipeLoadRecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.adapter.DeviceAdapter;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.DeviceInfo;
import cn.zheft.www.zheft.retrofit.HttpUtil;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.ResponseBase;
import cn.zheft.www.zheft.retrofit.ResultCode;
import cn.zheft.www.zheft.retrofit.RetrofitAPI;
import cn.zheft.www.zheft.util.ToastUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 我的设备页面
 */
public class DeviceActivity extends BaseActivity implements
        DeviceAdapter.OnItemClickListener,
        SwipeLoadRecyclerView.OnLoadListener {
    private Context context;

    private static final int DEVICE_PAGE_NUM = 1000;// 一次加载1000条数据

    private DeviceAdapter deviceAdapter;
    private SwipeLoadRecyclerView swipeLoadRecyclerView;

    private List<DeviceInfo> devices = new ArrayList<>();

    // 设备名称改变
    private boolean terNameChanged = true;
    private static final String LOCAL_ACTION_TERNAME_CHANGED = "local_action_tername_changed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        setToolbar(R.string.my_devices);
        context = this;

        initView();

        // 注册设备切换广播监听
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        lbm.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                terNameChanged = true;
            }
        }, new IntentFilter(LOCAL_ACTION_TERNAME_CHANGED));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (terNameChanged) {
            swipeLoadRecyclerView.onRefresh();
            terNameChanged = false;
        }
    }

    private void initView() {
        deviceAdapter = new DeviceAdapter(context, devices, R.layout.item_device);
        deviceAdapter.setOnItemClickListener(this);

        swipeLoadRecyclerView = (SwipeLoadRecyclerView) findViewById(R.id.swipe_load_refresh_view_device);
        swipeLoadRecyclerView.setPageVolume(DEVICE_PAGE_NUM);
        swipeLoadRecyclerView.setAdapter(deviceAdapter);
        swipeLoadRecyclerView.setOnLoadListener(this);
    }

    private void getData(int page) {
        if (nonNetwork()) {
            swipeLoadRecyclerView.netError();
            return;
        }
        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile());

        // 按分页取得设备列表
        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<List<DeviceInfo>>> call = service.postPosList(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                cipher );
        call.enqueue(new Callback<ResponseBase<List<DeviceInfo>>>() {
            @Override
            public void onResponse(Call<ResponseBase<List<DeviceInfo>>> call, Response<ResponseBase<List<DeviceInfo>>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        swipeLoadRecyclerView.update(response.body().getData());
                        return;
                    } else {
                        if (!resultCode(response.body().getResultCode())) {
                            // 解析码
                        }
                    }
                } else {
                    ResultCode.responseCode(response.code());
                }
                swipeLoadRecyclerView.netError();
            }

            @Override
            public void onFailure(Call<ResponseBase<List<DeviceInfo>>> call, Throwable t) {
                ResultCode.callFailure(t.getMessage());
                swipeLoadRecyclerView.netError();
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        // 点击事件
        if (devices.get(position) != null) {
            Intent intent = new Intent(context, DeviceDetailActivity.class);
            intent.putExtra("DeviceCode",devices.get(position).getCode());
            intent.putExtra("DeviceName",devices.get(position).getName());
            startActivity(intent);
        } else {
            ToastUtil.showShortMessage("未知错误，请刷新后重试");
        }
    }

    @Override
    public void swipeLoadFromPage(int page) {
        getData(page);
    }
}
