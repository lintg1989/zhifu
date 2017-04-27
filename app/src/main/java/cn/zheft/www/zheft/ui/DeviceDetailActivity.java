package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.DeviceDetailInfo;
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
 * 设备详情页
 */
public class DeviceDetailActivity extends BaseActivity {
    private Context context;
    private MenuItem menuItem;
    private String termCode;   // 终端号
    private String termName;   // 设备名
    private TextView tvName;
    private EditText etName;

    // 设备名称改变
    private static final String LOCAL_ACTION_TERNAME_CHANGED = "local_action_tername_changed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_detail);
        setToolbar(R.string.device_detail);
        context = this;

        Intent intent = getIntent();
        termCode = intent.getStringExtra("DeviceCode");
        termName = intent.getStringExtra("DeviceName");

        if (termCode != null) {
            if (termName != null && !"".equals(termName)) {
                setToolbar(termName, true, null, true);
            }
            getData(termCode);
        } else {
            TextView tvError = (TextView) findViewById(R.id.text_error);
            tvError.setVisibility(View.VISIBLE);
        }

        initView();
    }

    private void initView() {
        tvName = (TextView) findViewById(R.id.tv_device_name);
        tvName.setText(termName);
        etName = (EditText) findViewById(R.id.et_device_name);
//        etName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (!hasFocus) {
//                    LogUtil.e("Focus", "NoFo");
//                    etName.setText("");
//                    etName.setVisibility(View.INVISIBLE);
//                    tvName.setVisibility(View.VISIBLE);
//                    menuItem.setTitle(R.string.device_detail_menu_edit);
//                } else {
//                    LogUtil.e("Focus", "HasFo");
//                }
//            }
//        });
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        // 拦截点击事件
//        return super.onTouchEvent(event);
//    }

    private void getData(String code) {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                code);

        // 取得设备详情
        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<DeviceDetailInfo>> call = service.postPosDetail(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                code,
                cipher);
        call.enqueue(new Callback<ResponseBase<DeviceDetailInfo>>() {
            @Override
            public void onResponse(Call<ResponseBase<DeviceDetailInfo>> call, Response<ResponseBase<DeviceDetailInfo>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        initData(response.body().getData());
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
            public void onFailure(Call<ResponseBase<DeviceDetailInfo>> call, Throwable t) {
                ResultCode.callFailure(t.getMessage());
                closeProgress();
            }
        });
    }

    private void initData(DeviceDetailInfo device) {
        TextView tvCompany = (TextView) findViewById(R.id.tv_device_company);
        TextView tvCode = (TextView) findViewById(R.id.tv_device_code);
        TextView tvSn = (TextView) findViewById(R.id.tv_device_sn);

        tvCompany.setText(device.getCompany());
        tvCode.setText(device.getCode());
        tvSn.setText(device.getSn());
        tvName.setText(device.getTermName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        menuItem = menu.findItem(R.id.action_button).setTitle(R.string.device_detail_menu_edit);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_button:
                if (tvName.getVisibility() == View.INVISIBLE) {
                    toSaveName();
                } else {
                    toEditName();
                }
                break;
        }
        return true;
    }

    // 编辑设备名
    private void toEditName() {
        tvName.setVisibility(View.INVISIBLE);
        etName.setText(termName);
        etName.setVisibility(View.VISIBLE);
        etName.setSelection(termName.length());
        menuItem.setTitle(R.string.device_detail_menu_save);
        ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                .showSoftInput(etName, 0);// 值必须为0（原因？）
    }

    // 保存设备名
    private void toSaveName() {
        if (etName.getText() == null || "".equals(etName.getText().toString().trim())) {
            ToastUtil.showShortMessage("请填写设备名称");
            return;
        }
        setDeviceName(etName.getText().toString().trim());
    }

    // 请求设置设备名称接口
    private void setDeviceName(String name) {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                termCode,
                name);

        // 更改设备名称
        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<Object>> call = service.postTermName(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                termCode,
                name,
                cipher );
        call.enqueue(new Callback<ResponseBase<Object>>() {
            @Override
            public void onResponse(Call<ResponseBase<Object>> call, Response<ResponseBase<Object>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        saveNameSuccess();
                        ToastUtil.showShortMessage("保存成功");
                    } else {
                        if (!resultCode(response.body().getResultCode())) {
                            // 解析码
                            termNameResult(response.body().getResultCode());
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

    private void termNameResult(String resultCode) {
        switch (resultCode) {
            case "2":ToastUtil.showShortMessage("设备名称为空");break;
            case "3":ToastUtil.showShortMessage("内部终端号为空");break;
            case "4":ToastUtil.showShortMessage("更新终端名称失败");break;
            default:ToastUtil.showShortMessage(R.string.error_unknown);
        }
    }

    private void saveNameSuccess() {
        // 保存成功后
        termName = etName.getText().toString().trim();
        tvName.setText(termName);
        tvName.setVisibility(View.VISIBLE);
        etName.setVisibility(View.INVISIBLE);
        menuItem.setTitle(R.string.device_detail_menu_edit);
        setToolbar(termName, true, null, true);

        // 提醒设备列表页刷新
        LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
        lbm.sendBroadcast(new Intent(LOCAL_ACTION_TERNAME_CHANGED));
    }
}
