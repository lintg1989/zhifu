package cn.zheft.www.zheft.fragm;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.design.coderbeanliang.swipeloadrecyclerview.SwipeLoadRecyclerView;
import com.umeng.analytics.MobclickAgent;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.adapter.SelectAdapter;
import cn.zheft.www.zheft.app.BaseFragment;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.DeviceInfo;
import cn.zheft.www.zheft.model.DeviceSelectInfo;
import cn.zheft.www.zheft.model.MsgDeviceInfo;
import cn.zheft.www.zheft.model.SelectDateInfo;
import cn.zheft.www.zheft.retrofit.HttpUtil;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.ResponseBase;
import cn.zheft.www.zheft.retrofit.ResultCode;
import cn.zheft.www.zheft.retrofit.RetrofitAPI;
import cn.zheft.www.zheft.util.ClickUtil;
import cn.zheft.www.zheft.util.DateUtil;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.view.PickDateDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 筛选抽屉对应的Fragment
 * 2017/03/15 by liao
 */
public class SelectFragment extends BaseFragment implements SwipeLoadRecyclerView.OnLoadListener,
        SelectAdapter.OnItemClickListener, Button.OnClickListener,
        PickDateDialog.OnPickDateClickListener {

    private static final String TAG = SelectFragment.class.getSimpleName();
    public static final String INTERACTION_TAG = SelectFragment.class.getSimpleName();
    private static final String ARG_PARAM = "param";
    private Context mContext;
    private String mParam;

    private OnFragmentInteractionListener mListener;

    private SwipeLoadRecyclerView swipeLoadRecyclerView;
    private GridLayoutManager gridLayoutManager;
    private List<DeviceSelectInfo> devices;
    private SelectAdapter selectAdapter;

    private Calendar calendarStart;// 起始日期
    private Calendar calendarEnd;  // 结束日期
    private Calendar calendarMax;  // 最大日期（今日）

    private PickDateDialog pickDateStart;
    private PickDateDialog pickDateEnd;

    private TextView tvDateStart; // 起始日期
    private TextView tvDateEnd;   // 结束日期
    private TextView tvDateWeek;  // 一周内
    private TextView tvDateMonth; // 一个月内
    private TextView tvDateSeason;// 三月内

    private int dateLabelFlag; // 日期标签

    public SelectFragment() {
        // Required empty public constructor
    }

    public static SelectFragment newInstance(String param) {
        SelectFragment fragment = new SelectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM, param);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam = getArguments().getString(ARG_PARAM);
        }
        mContext = this.getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select, container, false);
        initView(view);
        return view;
    }

    // 用户点击了筛选按钮
    public void onSelectPressed() {
        MyApp.getInstance().setSelectedDevice(getSelectedDevices(devices));
        MyApp.getInstance().setSelectedDate(getSelectedDate());
        if (mListener != null) {
            mListener.onFragmentInteraction(INTERACTION_TAG);
        }

        // 友盟事件统计：点击筛选按钮
        MobclickAgent.onEvent(mContext, "um_trade_siftButton");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPickDateClick(PickDateDialog dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            // 选择日期确定
            if (dialog.equals( pickDateStart )) {
                calendarStart = pickDateStart.getCalendar( PickDateDialog.TIME_START );
                tvDateStart.setText( DateUtil.calendarToChar(calendarStart) );
                cleanDateLabel();
            } else if (dialog.equals( pickDateEnd )) {
                calendarEnd = pickDateEnd.getCalendar( PickDateDialog.TIME_END );
                tvDateEnd.setText( DateUtil.calendarToChar(calendarEnd) );
                cleanDateLabel();
            }
        }
    }

    @Override
    public void swipeLoadFromPage(int page) {
        getData(page);// 获取设备列表
    }

    /**
     * 由包含本Fragment的Activity实现该接口
     * 用于与Activity通信
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String tag);
    }

    private void initView(View view) {

        devices = new ArrayList<>();
        selectAdapter = new SelectAdapter(mContext, devices, R.layout.item_device_selection);
        selectAdapter.setOnItemClickListener(this);
        gridLayoutManager = new GridLayoutManager(mContext, 2);
        swipeLoadRecyclerView = (SwipeLoadRecyclerView) view.findViewById(R.id.swipe_load_select_view);
        swipeLoadRecyclerView.setLayoutManager(gridLayoutManager);
        swipeLoadRecyclerView.setAdapter(selectAdapter);
        swipeLoadRecyclerView.setShowNoMore(false);
        swipeLoadRecyclerView.setOnLoadListener(this);

        Button btnOk = (Button) view.findViewById(R.id.btn_fragment_select);
        btnOk.setOnClickListener(this);

        tvDateStart = (TextView) view.findViewById(R.id.tv_select_date_start);
        tvDateEnd = (TextView) view.findViewById(R.id.tv_select_date_end);
        tvDateWeek = (TextView) view.findViewById(R.id.tv_select_date_week);
        tvDateMonth = (TextView) view.findViewById(R.id.tv_select_date_month);
        tvDateSeason = (TextView) view.findViewById(R.id.tv_select_date_season);

        tvDateStart.setOnClickListener(this);
        tvDateEnd.setOnClickListener(this);
        tvDateWeek.setOnClickListener(this);
        tvDateMonth.setOnClickListener(this);
        tvDateSeason.setOnClickListener(this);

        pickDateStart = new PickDateDialog(mContext);
        pickDateEnd = new PickDateDialog(mContext);

        calendarMax = DateUtil.resetTime(DateUtil.DAY_TIME_END);

        initDateStatus();
    }

    @Override
    public void onClick(View v) {
        if (ClickUtil.cantClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.btn_fragment_select:
                onSelectPressed();
                break;
            case R.id.tv_select_date_start:
                showDialogStart();
                break;
            case R.id.tv_select_date_end:
                showDialogEnd();
                break;
            case R.id.tv_select_date_week:
                selectDateLabel(SelectDateInfo.DATE_LABEL_WEEK);
                break;
            case R.id.tv_select_date_month:
                selectDateLabel(SelectDateInfo.DATE_LABEL_MONTH);
                break;
            case R.id.tv_select_date_season:
                selectDateLabel(SelectDateInfo.DATE_LABEL_SEASON);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(View view, int i) {
        changeDeviceStatus(i);
    }

    public void refreshData() {
        // 更新列表
        swipeLoadRecyclerView.onRefresh();
        // 更新日期选择状态
        initDateStatus();
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

        // 取得设备列表
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
                    List<DeviceSelectInfo> infos = new ArrayList<>();
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        // 开始准备数据
                        infos = initDeviceList(response.body().getData());
                        // 取得后
                    } else {
                        if (!resultCode(response.body().getResultCode())) {
                            // 解析码
                        }
                    }
                    swipeLoadRecyclerView.update(infos, true);
                    setSelected(devices);
                } else {
                    ResultCode.responseCode(response.code());
                    swipeLoadRecyclerView.update(null, false);
                }
            }

            @Override
            public void onFailure(Call<ResponseBase<List<DeviceInfo>>> call, Throwable t) {
                ResultCode.callFailure(t.getMessage());
                swipeLoadRecyclerView.update(null, false);
            }
        });
    }

    private void initDateStatus() {
        // 更新选择日期的状态
        SelectDateInfo dateInfo = MyApp.getInstance().getSelectedDate();
        int flag = dateInfo.getLabelFlag();
        if (flag > -1 && flag < 4) {
            selectDateLabel(flag);
        } else {
            tvDateStart.setText(DateUtil.calendarToChar(dateInfo.getDateStart()));
            tvDateEnd.setText(DateUtil.calendarToChar(dateInfo.getDateEnd()));
            dateLabelFlag = SelectDateInfo.DATE_LABEL_NULL;
        }
    }

    private List<DeviceSelectInfo> initDeviceList(List<DeviceInfo> deviceInfos) {
        if (deviceInfos == null || deviceInfos.size() <= 0) {
            return null;
        }
        List<DeviceSelectInfo> selectInfos = new ArrayList<>();
        DeviceSelectInfo allInfo = new DeviceSelectInfo();
        allInfo.setName("全部");
        allInfo.setCode("all");
        allInfo.setSelected(false);
        allInfo.setHasMsg(false);
        DeviceSelectInfo qrcodeInfo = new DeviceSelectInfo();
        qrcodeInfo.setName("二维码付款");
        qrcodeInfo.setCode("QRCode");
        qrcodeInfo.setSelected(false);
        qrcodeInfo.setHasMsg(false);
        selectInfos.add(0, allInfo);
        selectInfos.add(1, qrcodeInfo);

        for (DeviceInfo deviceInfo : deviceInfos) {
            allInfo = new DeviceSelectInfo();
            allInfo.setName(deviceInfo.getName());
            allInfo.setCode(deviceInfo.getCode());
            allInfo.setSelected(false);
            allInfo.setHasMsg(false);
            selectInfos.add(allInfo);
        }

        // 不能放在这里因changeDeviceStatus会用到devices列表，此时列表还未更新
        // setSelected(selectInfos);

        return selectInfos;

//        setHasMsg();// 设置小红点
    }

    private void setHasMsg() {
        try {
            List<MsgDeviceInfo> msgDeviceInfos = DataSupport.findAll(MsgDeviceInfo.class);
            // 设置消息红点
            for (int i = 0; i < devices.size(); i++) {
                if (msgDeviceInfos.size() <= 0) {
                    break;
                }
                for (int j = 0; j < msgDeviceInfos.size(); j++) {
                    if (devices.get(i).getCode().equals(msgDeviceInfos.get(j).getTermCode())) {
                        devices.get(i).setHasMsg(true);
                        msgDeviceInfos.remove(j);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.e("DeviceSelectActivity","Set HasMsg Error!" + e.getMessage());
        }
    }

    // 点击设备标签后
    private void changeDeviceStatus(int position) {
        if (devices != null && devices.size() > position) {

            if ( "all".equals(devices.get(position).getCode()) ) {
                if (!devices.get(position).isSelected()) {
                    // 选择“全部”需特殊处理，循环勾选所有设备
                    for (DeviceSelectInfo info : devices) { info.setSelected(true); }
                    selectAdapter.notifyItemRangeChanged(0, devices.size());
                }
            } else {
                boolean selected = devices.get(position).isSelected();
                devices.get(position).setSelected(!selected);

                // 反选后判断是否最后一台、是否全选
                if (selected && getSelectedDeviceNum(devices) < 1) {
                    // 在之前选中的情况下取消选择，判断是否没选中
                    devices.get(position).setSelected(true);
                } else {
                    selectAdapter.notifyItemChanged(position);
                }

                // 检测“all”标签的状态
                if (getSelectedDeviceNum(devices) == devices.size() - 1 ) {
                    // 应该开启
                    if (!devices.get(0).isSelected()) {
                        devices.get(0).setSelected(true);
                        selectAdapter.notifyItemChanged(0);
                    }
                } else {
                    // 应该关闭
                    if (devices.get(0).isSelected()) {
                        devices.get(0).setSelected(false);
                        selectAdapter.notifyItemChanged(0);
                    }
                }
            }
        }
    }

    // 返回不包含“all”的选中设备数
    private int getSelectedDeviceNum(List<DeviceSelectInfo> devices) {
        int nowSelectedNum = 0;
        for (int i = 1; i < devices.size(); i++) {
            if (devices.get(i).isSelected()) {
                nowSelectedNum++;
            }
        }
        return nowSelectedNum;
    }

    // 返回包含“all”的选中设备数
    private List<DeviceInfo> getSelectedDevices(List<DeviceSelectInfo> devices) {
        List<DeviceInfo> deviceInfos = new ArrayList<>();
        if (devices != null && devices.size() > 0) {
            for (int i = 0; i < devices.size(); i++) {
                if (devices.get(i).isSelected()) {

                    DeviceInfo info = new DeviceInfo();
                    info.setCode(devices.get(i).getCode());
                    info.setName(devices.get(i).getName());
                    deviceInfos.add(info);

                    if ( "all".equals( info.getCode() ) ) {
                        deviceInfos.clear();
                        deviceInfos.add(info);
                        break;
                    }

                }
            }
        }
        return deviceInfos;
    }

    private SelectDateInfo getSelectedDate() {
        Calendar start = DateUtil.copyFrom(calendarStart);
        Calendar end = DateUtil.copyFrom(calendarEnd);
        SelectDateInfo dateInfo = new SelectDateInfo(start, end, dateLabelFlag);
        return dateInfo;
    }

    private void setSelected(List<DeviceSelectInfo> list) {
        // 获取当前选中设备
        List<DeviceInfo> selectedDevices = MyApp.getInstance().getSelectedDevice();
        // 设置标签状态
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < selectedDevices.size(); j++) {
                String innerCode = list.get(i).getCode();
                if (selectedDevices.get(j).getCode().equals(innerCode)) {
//                    list.get(i).setSelected(true);
                    changeDeviceStatus(i);
                    // 如果是全部就跳出
                    if (innerCode.equals("all")) {
                        break;
                    }
                }
            }
        }
    }

    private void showDialogStart() {
        String btnText = getString(R.string.ensure);
        int btnPositive = DialogInterface.BUTTON_POSITIVE;
        pickDateStart.setButton(btnPositive, btnText, this);
        pickDateStart.setMaxDate(calendarEnd);
        pickDateStart.showDialog(calendarStart);
    }

    private void showDialogEnd() {
        String btnText = getString(R.string.ensure);
        int btnPositive = DialogInterface.BUTTON_POSITIVE;
        pickDateEnd.setButton(btnPositive, btnText, this);
        pickDateEnd.setMinDate(calendarStart);
        pickDateEnd.setMaxDate(calendarMax);
        pickDateEnd.showDialog(calendarEnd);
    }

    private void selectDateLabel(int flag) {
        if (flag == SelectDateInfo.DATE_LABEL_MONTH) {

            cleanDateLabel();
            setLabelSelected(tvDateMonth, true);
            calendarStart = DateUtil.getDate(0, -1, 0, DateUtil.DAY_TIME_START);
            calendarEnd = DateUtil.resetTime(DateUtil.DAY_TIME_END);
            tvDateStart.setText(DateUtil.calendarToChar(calendarStart));
            tvDateEnd.setText(DateUtil.calendarToChar(calendarEnd));
            dateLabelFlag = SelectDateInfo.DATE_LABEL_MONTH;

        } else if (flag == SelectDateInfo.DATE_LABEL_SEASON) {

            cleanDateLabel();
            setLabelSelected(tvDateSeason, true);
            calendarStart = DateUtil.getDate(0, -3, 0, DateUtil.DAY_TIME_START);
            calendarEnd = DateUtil.resetTime(DateUtil.DAY_TIME_END);
            tvDateStart.setText(DateUtil.calendarToChar(calendarStart));
            tvDateEnd.setText(DateUtil.calendarToChar(calendarEnd));
            dateLabelFlag = SelectDateInfo.DATE_LABEL_SEASON;

        } else if (flag == SelectDateInfo.DATE_LABEL_WEEK){

            cleanDateLabel();
            setLabelSelected(tvDateWeek, true);
            calendarStart = DateUtil.getDate(0, 0, -6, DateUtil.DAY_TIME_START);
            calendarEnd = DateUtil.resetTime(DateUtil.DAY_TIME_END);
            tvDateStart.setText(DateUtil.calendarToChar(calendarStart));
            tvDateEnd.setText(DateUtil.calendarToChar(calendarEnd));
            dateLabelFlag = SelectDateInfo.DATE_LABEL_WEEK;

        } else if (flag == SelectDateInfo.DATE_LABEL_NULL){
            SelectDateInfo info = MyApp.getInstance().getSelectedDate();
            if (info.getDateStart() == null || info.getDateEnd() == null) {
                cleanDateLabel();
                setLabelSelected(tvDateWeek, true);
                calendarStart = DateUtil.getDate(0, 0, -6, DateUtil.DAY_TIME_START);
                calendarEnd = DateUtil.resetTime(DateUtil.DAY_TIME_END);
                tvDateStart.setText(DateUtil.calendarToChar(calendarStart));
                tvDateEnd.setText(DateUtil.calendarToChar(calendarEnd));
            } else {
                tvDateStart.setText(DateUtil.calendarToChar(info.getDateStart()));
                tvDateEnd.setText(DateUtil.calendarToChar(info.getDateEnd()));
            }


//            if (calendarStart == null || calendarEnd == null) {
//                cleanDateLabel();
//                setLabelSelected(tvDateWeek, true);
//                calendarStart = DateUtil.getDate(0, 0, -6, DateUtil.DAY_TIME_START);
//                calendarEnd = DateUtil.resetTime(DateUtil.DAY_TIME_END);
//                tvDateStart.setText(DateUtil.calendarToChar(calendarStart));
//                tvDateEnd.setText(DateUtil.calendarToChar(calendarEnd));
//            } else {
//                tvDateStart.setText(DateUtil.calendarToChar(calendarStart));
//                tvDateEnd.setText(DateUtil.calendarToChar(calendarEnd));
//            }
            // LogUtil.e(TAG, "Illegal date label flag:" + flag);
        }
    }

    private void cleanDateLabel() {
        setLabelSelected(tvDateWeek, false);
        setLabelSelected(tvDateMonth, false);
        setLabelSelected(tvDateSeason, false);
        dateLabelFlag = SelectDateInfo.DATE_LABEL_NULL;
    }

    private void setLabelSelected(TextView label, boolean selected) {
        // 设置标签选中状态
        if (selected) {
            label.setTextColor(mContext.getResources().getColor(R.color.select_label_color_orange));
            label.setBackground(mContext.getResources().getDrawable(R.drawable.drawable_device_select_stroke));
        } else {
            label.setTextColor(mContext.getResources().getColor(R.color.text_color_dark));
            label.setBackground(mContext.getResources().getDrawable(R.drawable.drawable_device_select_full));
        }
    }

}
