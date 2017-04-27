package cn.zheft.www.zheft.fragm;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.design.coderbeanliang.swipeloadrecyclerview.SwipeLoadRecyclerView;
import com.umeng.analytics.MobclickAgent;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.adapter.TradeListAdapter;
import cn.zheft.www.zheft.app.BaseFragment;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.DeviceInfo;
import cn.zheft.www.zheft.model.MsgDeviceInfo;
import cn.zheft.www.zheft.model.PosInfo;
import cn.zheft.www.zheft.model.ProtocolInfo;
import cn.zheft.www.zheft.retrofit.HttpUtil;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.RequestUrl;
import cn.zheft.www.zheft.retrofit.Response;
import cn.zheft.www.zheft.retrofit.ResponseBase;
import cn.zheft.www.zheft.retrofit.ResponseUtil;
import cn.zheft.www.zheft.retrofit.ResultCode;
import cn.zheft.www.zheft.retrofit.RetrofitAPI;
import cn.zheft.www.zheft.ui.PayInputActivity;
import cn.zheft.www.zheft.ui.PayReceivedActivity;
import cn.zheft.www.zheft.ui.PosDetailActivity;
import cn.zheft.www.zheft.util.ClickUtil;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.ToastUtil;
import cn.zheft.www.zheft.view.OpenQRCodeDialog;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * A simple {@link Fragment} subclass.
 * Pos交易列表
 * POS_PAGE_NUM 属性设置一页加载多少条数据
 */
public class PosFragment extends BaseFragment implements View.OnClickListener,
        TradeListAdapter.OnItemClickListener,
        SwipeLoadRecyclerView.OnLoadListener,
        OpenQRCodeDialog.OnAgreeListener {

    public static final String INTERACTION_TAG = PosFragment.class.getSimpleName();
    private Context context;

    private String mParamPos;
    private static final String ARG_PARAM_POS = "paramPos";
    public static final String LOCAL_ACTION_TRADE_CHANGED = "local_action_trade_changed";// 流水更新
    private static final String LOCAL_ACTION_CHANGE_MESSAGE_POS = "local_action_change_message_pos";
    private static final String ACTION_CHANGE_POSLIST = "zheft.action_change_poslist";// 流水消息

    private static final int POS_PAGE_NUM = 20;// 一次加载20条数据

    private SwipeLoadRecyclerView swipeLoadRecyclerView;
    private TradeListAdapter listAdapter;
    private List<PosInfo> posInfos;

    private int msgNum;
    private boolean tradeChanged = true;// 用于记录流水是否改变
    private BroadcastReceiver broadcastReceiver; // 广播接收设备改变状态

    private OnFragmentInteractionListener mListener;

    private OpenQRCodeDialog openQRCodeDialog;

    private boolean qrcodeOpened = false;// 记录二维码是否已开通

    public PosFragment() {
        // Required empty public constructor
    }


    public static PosFragment newInstance(String param) {
        PosFragment fragment = new PosFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM_POS, param);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParamPos = getArguments().getString(ARG_PARAM_POS);
        }
        context = getActivity();
        setHasOptionsMenu(true);

        initBroadcast();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (tradeChanged) {
            MyApp.getInstance().setSelectedDate(null);
            MyApp.getInstance().setSelectedDevice(null);
            swipeLoadRecyclerView.onRefresh();
            tradeChanged = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pos, container, false);
        ImageView ivScan = (ImageView) view.findViewById(R.id.iv_home_scan);
        ImageView ivCode = (ImageView) view.findViewById(R.id.iv_home_code);
        ImageView ivSelect = (ImageView) view.findViewById(R.id.iv_home_select);
        ivScan.setOnClickListener(this);
        ivCode.setOnClickListener(this);
        ivSelect.setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        serverIsOpenQR();
    }

    private void initBroadcast() {
        // 注册设备切换广播监听
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                try {
                    String actionStr = intent.getAction();
                    if (LOCAL_ACTION_CHANGE_MESSAGE_POS.equals(actionStr)){
//                        setBadge();
                    } else if (LOCAL_ACTION_TRADE_CHANGED.equals(actionStr)){
                        tradeChanged = true;
                    } else {
                        LogUtil.e(INTERACTION_TAG, "No broadcast action matched!");
                    }
                } catch (Exception e) {
//                    LogUtil.e("PosFragment", "BroadcastReceiver Error!" + e.getMessage());
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(LOCAL_ACTION_TRADE_CHANGED);
        filter.addAction(LOCAL_ACTION_CHANGE_MESSAGE_POS);
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, filter);
    }

    private void initView() {
        int itemNull = R.layout.item_footer_trade_nodata;
        int itemLoad = R.layout.item_footer_load;
        int itemEnd = R.layout.item_footer_empty;
        int itemNet = R.layout.item_footer_net;

        posInfos = new ArrayList<>();
        listAdapter = new TradeListAdapter(context, posInfos, R.layout.item_trade);
        listAdapter.setOnItemClickListener(this);
        swipeLoadRecyclerView = (SwipeLoadRecyclerView) getActivity().findViewById(R.id.swipe_load_pos);
        swipeLoadRecyclerView.setPageVolume(POS_PAGE_NUM);
        swipeLoadRecyclerView.setAdapter(listAdapter);
        swipeLoadRecyclerView.setEmptyDataShowNullFooter(true);
        swipeLoadRecyclerView.setAdapterFooterLayout(itemNull, itemLoad, itemEnd, itemNet);
        swipeLoadRecyclerView.setOnLoadListener(this);
    }

    private void getData(final int page) {
        if (nonNetwork()) {
            swipeLoadRecyclerView.netError();
            return;
        }
        List<DeviceInfo> deviceArr = MyApp.getInstance().getSelectedDevice();
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < deviceArr.size(); i++) {
            codes.add(deviceArr.get(i).getCode());
        }

        Map<String, Object> paras = new HashMap<>();
        paras.put("mobile", MyApp.getInstance().getUserInfo().getMobile());
        paras.put("pageNum", String.valueOf(page));
        paras.put("innerTermCodeArray", codes);
        paras.put("startDate", MyApp.getInstance().getSelectedDate().getStartDateStr());
        paras.put("endDate", MyApp.getInstance().getSelectedDate().getEndDateStr());
        MyRetrofit.init(context).url(RequestUrl.QUERY_LIST_PAY).params(paras)
                .success(new MyRetrofit.HttpSuccess() {
                    @Override
                    public void onSuccess(cn.zheft.www.zheft.retrofit.Response response) {
                        dealData(response, page);
                    }
                })
                .failure(new MyRetrofit.HttpFailure() {
                    @Override
                    public void onFailure(String message) {
                        ToastUtil.showShortMessage("网络异常");
                        swipeLoadRecyclerView.update(null, false);
                    }
                })
                .post();
    }

    private void dealData(cn.zheft.www.zheft.retrofit.Response body, int page) {
        if (MyRetrofit.RESULT_SUCCESS == body.getResultcode()) {
            // 开始准备数据
            List<PosInfo> infos = ResponseUtil.getList(body.getData(), PosInfo.class);
            swipeLoadRecyclerView.update(infos, true);
            // 这个逻辑是否有问题？
            if (page == 1) {
                deletePosMsg(MyApp.getInstance().getInnerTermCode());

//                if (infos.size() > 0) {
//                    swipeLoadRecyclerView.getRecyclerView().scrollToPosition(0);
//                }
            }
        } else {
            ToastUtil.showShortMessage(body.getResultmsg());
            swipeLoadRecyclerView.update(null, false);
        }
    }

    // 实现列表项点击的事件接口
    @Override
    public void onItemClick(View view, int position) {
        if (ClickUtil.cantClick()) {
            return;
        }
        if (posInfos.get(position) != null
                && posInfos.get(position).getPayTypeCode() != null) {

            Intent intent = new Intent();
            int code = posInfos.get(position).getPayTypeCode();
            switch (code) {
                case 99:
                    intent.setClass(context, PosDetailActivity.class);
                    intent.putExtra("tradeId", posInfos.get(position).getInd());
                    startActivity(intent);
                    break;
                case 41:
                case 42:
                    intent.setClass(context, PayReceivedActivity.class);
                    intent.putExtra("tradeId", posInfos.get(position).getInd());
                    startActivity(intent);
                    break;
                default:
                    LogUtil.e(INTERACTION_TAG, "Illegal pay type code: "+code);
            }
            mobClick("um_pos_status", "status", posInfos.get(position).getStatusName());
        } else {
            LogUtil.e(INTERACTION_TAG,"列表为空");
        }
    }

    private void mobClick(String id, String key, String value) {
        // 友盟统计事件
        HashMap<String, String> map = new HashMap<>();
        map.put(key, value);
        MobclickAgent.onEvent(context, id, map);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(context)
                .unregisterReceiver(broadcastReceiver);
    }

    // MainActivity调用
    public void setMessage(int num) {
        msgNum = num;
    }

    private void deletePosMsg(String termCode) {
        if ("all".equals(termCode)) {
            DataSupport.deleteAll(MsgDeviceInfo.class);
        } else {
            DataSupport.deleteAll(MsgDeviceInfo.class, "termCode = ?", termCode);
        }
        if (getActivity() != null) {
            getActivity().sendBroadcast(new Intent(ACTION_CHANGE_POSLIST));
        }
    }

    // 筛选功能
    public void startSelection() {
        swipeLoadRecyclerView.onRefresh();
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
    public void swipeLoadFromPage(int page) {
        getData(page);
    }

    @Override
    public void onClick(View v) {
        if (ClickUtil.cantClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.iv_home_scan:
                toPayInput(PayInputActivity.PAY_TYPE_SCAN);
                mobClick("um_pos_QRCode", "type", "扫一扫");
                break;
            case R.id.iv_home_code:
                toPayInput(PayInputActivity.PAY_TYPE_CODE);
                mobClick("um_pos_QRCode", "type", "收款码");
                break;
            case R.id.iv_home_select:
                onSelectPress();
                break;
            default:
                break;
        }
    }

    private void toPayInput(int type) {
        if (qrcodeOpened) {
            Intent intent = new Intent(context, PayInputActivity.class);
            intent.putExtra("type", type);
            startActivity(intent);
        } else {
            serverIsOpenQR(type);
        }
    }

    private void onSelectPress() {
        if (mListener != null) {
            mListener.onFragmentInteraction(INTERACTION_TAG);
        }
    }

    @Override
    public void onAgree(boolean agreed) {
        if (agreed) {
            serverOpenQRCode();
        } else {
            ToastUtil.showShortMessage("请先认可协议");
        }
    }

    /**
     * 由包含本Fragment的Activity实现该接口
     * 用于与Activity通信
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String tag);
    }


    // 查询是否开通二维码
    private void serverIsOpenQR(final int type) {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        // 填充参数
        Map<String, Object> paras = new HashMap<>();
        paras.put("mobile", MyApp.getInstance().getUserInfo().getMobile());
        // 发起请求
        MyRetrofit.init(context).url(RequestUrl.IS_OPEN_QRCODE_NEW).params(paras)
                .success(new MyRetrofit.HttpSuccess() {
                    @Override
                    public void onSuccess(cn.zheft.www.zheft.retrofit.Response body) {
                        dealIsOpenQR(body, type);
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

    private void dealIsOpenQR(Response body, int type) {
        if (MyRetrofit.RESULT_SUCCESS == body.getResultcode()) {
            Map data = ResponseUtil.getMap(body.getData());
            String code = data.get("isOpenQr").toString();

            switch (code) {
                case "false":
                case "0":
                    // 未开通
                    // 还要判断是否显示一键开通Dialog，或者请求协议
                    qrcodeOpened = false;
                    serverQRAgreement();
                    return;
                case "true":
                case "1":
                    // 已开通
                    qrcodeOpened = true;
                    toPayInput(type);
                    break;
                case "9":
                    ToastUtil.showShortMessage("商户不存在");
                    break;
                default:
                    ToastUtil.showShortMessage("系统异常");
                    break;
            }

        } else {
            ToastUtil.showShortMessage(body.getResultmsg());
        }
        closeProgress();
    }

    // 一键开通
    private void serverOpenQRCode() {
        if (nonNetwork()) {
            return;
        }
        // 填充参数
        Map<String, Object> paras = new HashMap<>();
        paras.put("mobile", MyApp.getInstance().getUserInfo().getMobile());
        // 发起请求
        MyRetrofit.init(context).url(RequestUrl.OPEN_QRCODE_PAY).params(paras)
                .success(new MyRetrofit.HttpSuccess() {
                    @Override
                    public void onSuccess(cn.zheft.www.zheft.retrofit.Response body) {
                        closeProgress();
                        dealOpenQR(body);
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

    private void dealOpenQR(Response body) {
        if (MyRetrofit.RESULT_SUCCESS == body.getResultcode()) {
            ToastUtil.showShortMessage("开通成功");
        } else {
            ToastUtil.showShortMessage(body.getResultmsg());
        }
    }


    // 获取二维码收款服务协议
    private void serverQRAgreement() {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                "3"); //参数“3”为二维码协议

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<ProtocolInfo>> call = service.postProtocol(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                "3",
                cipher );
        call.enqueue(new Callback<ResponseBase<ProtocolInfo>>() {
            @Override
            public void onResponse(Call<ResponseBase<ProtocolInfo>> call, retrofit2.Response<ResponseBase<ProtocolInfo>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        // 请求成功
                        dealQRAgreement(response.body().getData());
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
            public void onFailure(Call<ResponseBase<ProtocolInfo>> call, Throwable t) {
                ResultCode.callFailure(t.getMessage());
                closeProgress();
            }
        });
    }

    private void dealQRAgreement(ProtocolInfo info) {
        if (info == null || info.getContent() == null) {
            ToastUtil.showShortMessage("数据异常");
            return;
        }
        // 弹框展示协议
        openQRCodeDialog = new OpenQRCodeDialog();
        openQRCodeDialog.setOnAgreeListener(this);
        openQRCodeDialog.showOpenQRDialog(getActivity(),
                info.getTitle(), info.getContent(), info.getName());
    }


    // 查询是否开通二维码（初始化时调用）
    private void serverIsOpenQR() {
        Map<String, Object> paras = new HashMap<>();
        paras.put("mobile", MyApp.getInstance().getUserInfo().getMobile());
        MyRetrofit.init(context).url(RequestUrl.IS_OPEN_QRCODE_NEW).params(paras)
                .success(new MyRetrofit.HttpSuccess() {
                    @Override
                    public void onSuccess(cn.zheft.www.zheft.retrofit.Response body) {
                        if (MyRetrofit.RESULT_SUCCESS == body.getResultcode()) {
                            Map data = ResponseUtil.getMap(body.getData());
                            if ("1".equals( data.get("isOpenQr").toString() )) {
                                qrcodeOpened = true;
                            }
                        }
                    }
                }).post();
    }
}
