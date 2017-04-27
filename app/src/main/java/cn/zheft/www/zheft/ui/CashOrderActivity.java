package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.BankcardInfo;
import cn.zheft.www.zheft.retrofit.HttpUtil;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.RequestUrl;
import cn.zheft.www.zheft.retrofit.ResponseBase;
import cn.zheft.www.zheft.retrofit.ResponseUtil;
import cn.zheft.www.zheft.retrofit.ResultCode;
import cn.zheft.www.zheft.retrofit.RetrofitAPI;
import cn.zheft.www.zheft.util.ClickUtil;
import cn.zheft.www.zheft.util.DateUtil;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.MD5Util;
import cn.zheft.www.zheft.util.StringUtil;
import cn.zheft.www.zheft.util.ToastUtil;
import cn.zheft.www.zheft.view.AmountInputUtil;
import cn.zheft.www.zheft.view.ClearEditText;
import cn.zheft.www.zheft.view.PayPwdDialog;
import cn.zheft.www.zheft.view.PickDateDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 预约提现
 */

public class CashOrderActivity extends BaseActivity implements PickDateDialog.OnPickDateClickListener,
        PayPwdDialog.OnPayPwdListener {

    private static final String TAG = CashOrderActivity.class.getSimpleName();
    private Context mContext;

    private String remark1; // 用于选择日期的提示
    private TextView tvCardName;
    private TextView tvInfo;
    private TextView tvDate;
    private ClearEditText cetAmount;
    private PayPwdDialog payPwdDialog;

    private PickDateDialog pickDateDialog;

    private boolean hasCard = false;// 是否选择了银行卡的标识
    private static final int REQUEST_CODE_BANK = 1;// 银行卡请求码
    private static final int RESULT_CODE_BANK = 1; // 银行卡返回码

    private boolean shouldFinish = false;//为true时禁止点击事件

    private String merCode; // 内部商户号，由所选银行卡得到

    private int amountLimit = 0; // 单笔限额，由接口取得
    private int dayTime = 0;    // 当日剩余可提现次数，由接口取得

    private Calendar calendarSel;// 选中日期

    private ArrayList<String> dates;// 存储可选日期

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_order);
        setToolbar(R.string.cash_order_withdraw);
        mContext = this;

        initView();
        getBankcards();
        getWithdrawInfo();
    }

    private void initView() {
        payPwdDialog = new PayPwdDialog();
        payPwdDialog.setPrompt(getString(R.string.cash_order_withdraw));

        tvCardName = (TextView) findViewById(R.id.tv_cash_order_card_name);
        tvInfo = (TextView) findViewById(R.id.tv_cash_order_instruction);
        tvDate = (TextView) findViewById(R.id.tv_cash_order_date);

        pickDateDialog = new PickDateDialog(mContext);
        pickDateDialog.setMinDate(DateUtil.getAfter(Calendar.DATE, 1));
        pickDateDialog.setMaxDate(DateUtil.getAfter(Calendar.MONTH, 1));
        pickDateDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", this);
        pickDateDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", this);
    }

    public void selectDate(View view) {
        if (ClickUtil.cantClick()) {
            return;
        }
        // 选择预约日期
        if (calendarSel != null) {
            pickDateDialog.showDialog(calendarSel);
        } else {
            pickDateDialog.showDialog(DateUtil.getAfter(Calendar.DATE, 1));
        }
    }

    public void toBankcard(View view) {
        if (ClickUtil.cantClick()) {
            return;
        }
        // 选择银行卡
        Intent intent = new Intent(mContext,BankcardActivity.class);
        intent.putExtra("from",1);//从提现进入时可选银行卡
        startActivityForResult(intent, REQUEST_CODE_BANK);
    }

    // 点击提现按钮事件
    public void cashOrder(View view) {
        if (shouldFinish) {
            return;
        }
        if (ClickUtil.cantClick()) {
            return;
        }
//        if (dayTime < 1) {
//            ToastUtil.showShortMessage("您已到达今日提现次数上限");
//            return;
//        }
        if (!hasCard) {
            ToastUtil.showShortMessage("请选择银行卡");
            return;
        }
        if (calendarSel == null) {
            ToastUtil.showShortMessage("请选择提现日期");
            return;
        }
        // 检测是否有支付密码
        if ("1".equals(MyApp.getInstance().getUserInfo().getHasPayPwd())) {
            payPwdDialog.showPayPwdDialog(mContext, "全部金额", this);
        } else {
            payPwdDialog.showSetPwdDialog(mContext, this);
        }

    }

    @Override
    public void onPickDateClick(PickDateDialog dialog, int which) {
        // 选择日期的点击事件
        if (which == DialogInterface.BUTTON_POSITIVE) {
            Calendar calendarSel = dialog.getCalendar();
            String dateStr = DateUtil.calendarToStr(calendarSel);
            if (!checkDateExist(dateStr, dates)) {
                dialog.setAfterClickCloseable(false);
                ToastUtil.showShortMessage(remark1);
            } else {
                this.calendarSel = calendarSel;
                dialog.setAfterClickCloseable(true);
                tvDate.setText( DateUtil.calendarToChar(calendarSel) );
            }
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            dialog.setAfterClickCloseable(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_BANK == requestCode) {
            if (RESULT_CODE_BANK == resultCode) {
                BankcardInfo card = (BankcardInfo) data.getSerializableExtra("card");
                setBankcardText(card);
            }
        }
    }

    // 获取银行卡，目前只有一张
    private void getBankcards() {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile());

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<List<BankcardInfo>>> call = service.postCardList(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                cipher);
        call.enqueue(new Callback<ResponseBase<List<BankcardInfo>>>() {
            @Override
            public void onResponse(Call<ResponseBase<List<BankcardInfo>>> call, Response<ResponseBase<List<BankcardInfo>>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())
                            && response.body().getData().size() > 0) {
                        setBankcardText(response.body().getData().get(0));
                    } else {
                        if (!resultCode(response.body().getResultCode())) {
                            // 解析码
                            if ("3".equals(response.body().getResultCode())) {
                                ToastUtil.showShortMessage("没有银行卡");
                            } else {
                                ToastUtil.showShortMessage("未知错误");
                            }
                        }
                    }
                } else {
                    ResultCode.responseCode(response.code());
                }
                closeProgress();
            }

            @Override
            public void onFailure(Call<ResponseBase<List<BankcardInfo>>> call, Throwable t) {
                ResultCode.callFailure(t.getMessage());
                closeProgress();
            }
        });
    }

    private void getWithdrawInfo() {
        if (nonNetwork()) {
            return;
        }
        // 获取预约提现规则
        startProgress();
        // 填充参数
        MyApp myApp = MyApp.getInstance();
        Map<String, Object> paras = new HashMap<>();
        paras.put("mobile", myApp.getUserInfo().getMobile());
        // 发起请求
        MyRetrofit.init(mContext).url(RequestUrl.WITHDRAW_ORDER_RULES).params(paras)
                .success(new MyRetrofit.HttpSuccess() {
                    @Override
                    public void onSuccess(cn.zheft.www.zheft.retrofit.Response body) {
                        closeProgress();
                        dealRulesData(body);
                    }
                })
                .failure(new MyRetrofit.HttpFailure() {
                    @Override
                    public void onFailure(String message) {
                        closeProgress();
                        ToastUtil.showShortMessage(message);
                        delayFinish();
                    }
                })
                .post();
    }

    private void setBankcardText(BankcardInfo info) {
        if (info == null
                ||  StringUtil.nullOrEmpty(info.getBankcardName())
                ||  StringUtil.nullOrEmpty(info.getCard())
                ||  StringUtil.nullOrEmpty(info.getCode()) ) {
            ToastUtil.showShortMessage("银行卡信息错误");
            return;
        }

        // 拼接银行卡名与卡号后四位字符串
        String text = "";
        if (info.getBankcardName().length() > 8) {
            text += info.getBankcardName().substring(0,8);
            text += "...";
        } else {
            text += info.getBankcardName();
        }
        text += "(";
        if (info.getCard().length() > 4) {
            text += info.getCard().substring(info.getCard().length() - 4);
        } else {
            text += info.getCard();
        }
        text += ")";

        merCode = info.getCode();
        tvCardName.setText(text);
        hasCard = true;
    }

    @Override
    public void onInputFinish(String password) {
        // 输入完成
        // 调提现接口
        toCashOrder(password);
    }

    @Override
    public void onForgetPwd() {
        if (ClickUtil.cantClick()) {
            return;
        }
        // 到忘记密码页
        startActivity(new Intent(mContext, ForgetPayPwdActivity.class));
    }

    @Override
    public void toSetPwd() {
        if (ClickUtil.cantClick()) {
            return;
        }
        // 到设置页
        Intent intent = new Intent(mContext, SetPasswordActivity.class);
        intent.putExtra("ToSetPwd", "pay");
        startActivity(intent);
    }

    private void toCashOrder(String password) {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        String encryptPwd = MD5Util.pwdEncrypt(password);// 加密
        // 填充参数
        MyApp myApp = MyApp.getInstance();
        Map<String, Object> paras = new HashMap<>();
        paras.put("mobile", myApp.getUserInfo().getMobile());
        paras.put("innerMerchantCode", merCode);
        paras.put("paymentDate",DateUtil.calendarToStr(calendarSel));
        paras.put("payPassword", encryptPwd);
        // 发起请求
        MyRetrofit.init(mContext).url(RequestUrl.WITHDRAW_ORDER_ADD).params(paras)
                .success(new MyRetrofit.HttpSuccess() {
                    @Override
                    public void onSuccess(cn.zheft.www.zheft.retrofit.Response body) {
                        closeProgress();
                        payPwdDialog.clearInput();
                        dealOrderData(body);
                    }
                })
                .failure(new MyRetrofit.HttpFailure() {
                    @Override
                    public void onFailure(String message) {
                        closeProgress();
                        ToastUtil.showShortMessage(message);
                        payPwdDialog.clearInput();
                    }
                })
                .post();
    }

    private void dealOrderData(cn.zheft.www.zheft.retrofit.Response body) {
        if (MyRetrofit.RESULT_SUCCESS == body.getResultcode()) {

            // 预约成功
            ToastUtil.showShortMessage("预约成功");
            payPwdDialog.closeDialog();

            // 通知更新
            Intent action = new Intent(BalanceActivity.LOCAL_ACTION_BALANCE_UPDATE);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(action);

            // 跳转
            Intent intent = new Intent(mContext, BalanceActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        } else if (3 == body.getResultcode()) {

            int times = StringUtil.strToInt(body.getData().toString());
            String msg = "密码错误，您还可输入" + times + "次";
            payPwdDialog.showPayPwdError(mContext, msg, this);

        } else if (8 == body.getResultcode()) {

            int minutes = StringUtil.strToInt(body.getData().toString());
            String msg = "支付密码已被锁定，请" + 3 + "小时后再试";
            payPwdDialog.showPayPwdLock(mContext, msg, this);

        } else {
            ToastUtil.showShortMessage(body.getResultmsg());
        }
    }

    private void dealRulesData(cn.zheft.www.zheft.retrofit.Response body) {
        if (MyRetrofit.RESULT_SUCCESS == body.getResultcode()) {
            // 提现规则拉取成功
            initRules(body.getData());
        } else {
            ToastUtil.showShortMessage(body.getResultmsg());
            delayFinish();
        }
    }

    private void initRules(Object data) {
        try {
            Map<String, Object> ruleMap = ResponseUtil.getMap(data);
            String remainTimesStr = (String) ruleMap.get("remainTimes");
            String amountLimitStr = (String) ruleMap.get("amountLimit");
            String remark = (String) ruleMap.get("remark");
            String fee = (String) ruleMap.get("fee");
            dates = (ArrayList<String>) ruleMap.get("dates");
            remark1 = (String) ruleMap.get("remark1");

//            dayTime = StringUtil.strToInt(remainTimesStr);
//            amountLimit = StringUtil.strToInt(amountLimitStr);
//
//            // 测试用
//            dayTime += 10;
//            amountLimit += 1000;
//
//            String infoStr = remark + "\n\n";
//            infoStr += "单笔限额" + StringUtil.fenToYuan(StringUtil.intToFen(amountLimit)) + "元，";// 测试用
////            infoStr += "单笔限额" + StringUtil.fenToYuan(amountLimitStr) + "元，";
//            infoStr += "今日还可转出" + dayTime + "次";

            tvInfo.setText(remark);
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showShortMessage("预约规则解析异常");
            delayFinish();
        }
    }

    private void delayFinish() {
        shouldFinish = true;
        // 拦截点击事件
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

    private boolean checkDateExist(String date, List<String> dates) {
        if (dates != null && date != null) {
            for (int i = 0; i < dates.size(); i++) {
                if (date.equals(dates.get(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    public void cashOrderAmount(View view) {
        ToastUtil.showShortMessage("全额提现");
    }
}
