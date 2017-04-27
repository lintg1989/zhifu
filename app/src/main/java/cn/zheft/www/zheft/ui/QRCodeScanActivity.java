package cn.zheft.www.zheft.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.gson.internal.LinkedTreeMap;
import com.uuzuche.lib_zxing.activity.CaptureFragment;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import java.util.HashMap;
import java.util.Map;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.fragm.PosFragment;
import cn.zheft.www.zheft.model.QRPayInfo;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.RequestUrl;
import cn.zheft.www.zheft.retrofit.Response;
import cn.zheft.www.zheft.retrofit.ResponseBase;
import cn.zheft.www.zheft.retrofit.ResponseUtil;
import cn.zheft.www.zheft.retrofit.ResultCode;
import cn.zheft.www.zheft.retrofit.RetrofitAPI;
import cn.zheft.www.zheft.retrofit.RetrofitService;
import cn.zheft.www.zheft.util.ClickUtil;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.StringUtil;
import cn.zheft.www.zheft.util.ToastUtil;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;

public class QRCodeScanActivity extends BaseActivity {
    private static final String TAG = QRCodeScanActivity.class.getSimpleName();
    private Context mContext;

    // 相机权限请求码
    private static final int PERMISSION_REQUEST_CAMERA = 1;

    private int money;
    private String type;

    private static final String PAY_BY_WECHAT = "41";
    private static final String PAY_BY_ALIPAY = "42";
    private static final String GOODS_TITLE = "二维码支付";
    private static final String GOODS_DESCRIPT = "APP端二维码支付";

    private CaptureFragment captureFragment;

    private PopupWindow waitWindow;// 等待用户支付的窗口
    private PopupWindow exitWindow;// 在交易等待过程中按返回键的弹出窗口

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTransStatusBar();
        setContentView(R.layout.activity_qrcode_scan);
        mContext = this;

        if (getIntent() != null) {
            money = getIntent().getIntExtra("money", 0);
            type = getIntent().getStringExtra("type");
        }
        if (money == 0 || StringUtil.nullOrEmpty(type)) {
            ToastUtil.showShortMessage("未知错误，请重试");
            finish();
        }

        // 请求相机权限
        PermissionGen.with(this).addRequestCode( PERMISSION_REQUEST_CAMERA )
                .permissions( Manifest.permission.CAMERA )
                .request();

        initView();
    }

    @PermissionFail(requestCode = PERMISSION_REQUEST_CAMERA)
    public void permissionFailed() {
        ToastUtil.showShortMessage("请在“设置-权限管理”中开启相机权限");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 500);
    }

    private void initView() {
        TextView tvType = (TextView) findViewById(R.id.tv_qrcode_scan_type);
        TextView tvAmount = (TextView) findViewById(R.id.tv_qrcode_scan_amount);

        String typeText = "";
        if (PAY_BY_ALIPAY.equals(type)) {
            typeText = "支付宝";
        } else if (PAY_BY_WECHAT.equals(type)) {
            typeText = "微信";
        } else {
            ToastUtil.showShortMessage("未知错误，请重试");
            finish();
        }

        String amtStr = StringUtil.addNumberComma( StringUtil.fenToYuan(StringUtil.intToFen(money)) );
        tvAmount.setText("￥ " + amtStr);
        tvType.setText(typeText);

        // 扫码界面初始化
        captureFragment = new CaptureFragment();
        CodeUtils.setFragmentArgs(captureFragment, R.layout.layout_qrcode_scan);
        captureFragment.setAnalyzeCallback(analyzeCallback);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_qrcode_scan_container, captureFragment).commit();
    }

    CodeUtils.AnalyzeCallback analyzeCallback = new CodeUtils.AnalyzeCallback() {
        @Override
        public boolean onAnalyzeSuccess(Bitmap mBitmap, String result) {
            // 连扫模块
            if (checkQRCodeRight(result)) {
                serverConnect(result);
                return true;
            } else {
                ToastUtil.showShortMessage("无效付款码，请重试");
                captureFragment.restartScan();
                return false;
            }
        }

        @Override
        public void onAnalyzeFailed() {
            ToastUtil.showShortMessage("扫码失败，请重试");
        }
    };

    public void onQRCodeScanClick(View view) {
        if (ClickUtil.cantClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.rl_qrcode_scan_back:
                quitScanActivity();
                break;
            default:
                break;
        }
    }

    private void quitScanActivity() {
        // 点击返回按钮时先判断是否在等待，等待支付时给提示
        if (isPopupWindowShowing(waitWindow)) {
            closeWaiting();
            startExiting();
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (isPopupWindowShowing(waitWindow)) {
            closeWaiting();
            startExiting();
        } else {
            super.onBackPressed();
        }
    }

    private void serverConnect(final String payCode) {
        if (nonNetwork()) {
            return;
        }
        startWaiting();
        // 填充参数
        Map<String, Object> paras = new HashMap<>();
        paras.put("txnType", type);
//        paras.put("merOrderId", "" + System.currentTimeMillis());
        paras.put("txnAmt", StringUtil.intToFen(money));
        paras.put("subject", GOODS_TITLE);
        paras.put("body", GOODS_DESCRIPT);
        paras.put("customerIp", "127.0.0.1");
        paras.put("mobile", MyApp.getInstance().getUserInfo().getMobile());
        paras.put("payCode", payCode);
        int timeout = 60 * 10; // 十分钟
        RetrofitAPI api = new RetrofitService.Builder()
                .timeout(timeout)
                .build(RetrofitAPI.class);
        MyRetrofit.init(mContext).url(RequestUrl.QR_ZHUSAO_PAY).params(paras).api(api)
                .success(new MyRetrofit.HttpSuccess() {
                    @Override
                    public void onSuccess(Response response) {
                        closeWaiting();
                        dealServerData(response);
                    }
                })
                .failure(new MyRetrofit.HttpFailure() {
                    @Override
                    public void onFailure(String message) {
                        closeWaiting();
                        ToastUtil.showShortMessage("请重试");
                        finish();
                    }
                })
                .timeout(new MyRetrofit.HttpTimeout() {
                    @Override
                    public void onTimeout(String message) {
//                        closeWaiting();
                    }
                })
                .post();
    }

    private void dealServerData(Response body) {
        if (MyRetrofit.RESULT_SUCCESS == body.getResultcode()) {
//            ToastUtil.showShortMessage(body.getResultmsg());
            QRPayInfo info = ResponseUtil.getData(body.getData(), QRPayInfo.class);
            if (info != null && "1001".equals(info.getRespCode())) {
                ToastUtil.showShortMessage("支付成功");
            }

            closeWaiting();
            toMainPage();
        } else {
            closeWaiting();
            ToastUtil.showShortMessage(body.getResultmsg());
        }
    }

    private void toMainPage() {
        // 通知Pos列表刷新
        LocalBroadcastManager.getInstance(mContext)
                .sendBroadcast(new Intent(PosFragment.LOCAL_ACTION_TRADE_CHANGED));
//        MyApp.getInstance().finishActivitys(2);
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("setPage", 0);// 切换到Pos列表
        startActivity(intent);
    }

    private void startWaiting() {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_qrcode_scan_wait, null);
        waitWindow = new PopupWindow(view,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT, true);
        waitWindow.setFocusable(false);
        waitWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    private void closeWaiting() {
        if (isPopupWindowShowing(waitWindow)) {
            waitWindow.dismiss();
        }
    }

    private void startExiting() {
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.dialog_qrcode_scan_exit, null);
        exitWindow = new PopupWindow(view,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT, true);
        exitWindow.showAsDropDown(view);
        TextView tvBack = (TextView) view.findViewById(R.id.tv_dialog_qrcode_scan_exit_back);
        TextView tvList = (TextView) view.findViewById(R.id.tv_dialog_qrcode_scan_exit_list);
        tvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeExiting();
                finish();
            }
        });
        tvList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 切换到Pos列表
                toMainPage();
            }
        });
    }

    private void closeExiting() {
        if (isPopupWindowShowing(exitWindow)) {
            exitWindow.dismiss();
        }
    }

    private boolean isPopupWindowShowing(PopupWindow window) {
        return window != null && window.isShowing();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isPopupWindowShowing(waitWindow)) {
            waitWindow.dismiss();
            waitWindow = null;
        }
        if (isPopupWindowShowing(exitWindow)) {
            exitWindow.dismiss();
            exitWindow = null;
        }
    }

    private boolean checkQRCodeRight(String code) {
        if (code != null && code.length() == 18) {
            // 判断是否纯数字
            for (int i = 0; i < code.length(); i++) {
                int num = (int) code.charAt(i) - 48;
                if (num < 0 || num > 9) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
