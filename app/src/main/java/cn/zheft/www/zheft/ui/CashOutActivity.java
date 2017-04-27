package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
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
import cn.zheft.www.zheft.model.WithdrawInfo;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 提现
 */
public class CashOutActivity extends BaseActivity implements PayPwdDialog.OnPayPwdListener,
        BaseActivity.OnMenuClicked {

    private Context mContext;

    private TextView tvCardName;
    private TextView tvInfo;
    private ClearEditText cetAmount;
    private PayPwdDialog payPwdDialog;

    // 金额以 分 为单位(int型)，金额字符串为“x.xx”形式
    private String amountStr;

    private int amount = 0;// 用户输入金额
    private int fund = 0;  // 可提现余额，由上一页传入
    private int amountLimit = 0; // 单笔限额，由接口取得
    private int dayTime = 0;    // 当日剩余可提现次数，由接口取得

    private boolean hasCard = false;// 是否选择了银行卡的标识
    private static final int REQUEST_CODE_BANK = 1;// 银行卡请求码
    private static final int RESULT_CODE_BANK = 1; // 银行卡返回码

    private boolean shouldFinish = false;//为true时禁止点击事件

    private String merCode; // 内部商户号，由所选银行卡得到
    private String remark1; // 用于当天不能提现时的提示
    private ArrayList<String> dates;// 存储可选日期

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_out);
        setToolbar(R.string.account_cashout, true, R.string.cash_order_withdraw, true);
        setOnMenuClickedLisener(this);
        mContext = this;

        Intent intent = getIntent();
        fund = intent.getIntExtra("fund", 0);

        initView();
        getBankcards();
        getWithdrawInfo();
        getWithdrawInfo2();
    }

    private void initView() {
        payPwdDialog = new PayPwdDialog();
        cetAmount = (ClearEditText) findViewById(R.id.cet_cashout_amount_input);
        tvCardName = (TextView) findViewById(R.id.tv_cashout_card_name);
        tvInfo = (TextView) findViewById(R.id.tv_cashout_instruction);

        String fundStr =  StringUtil.addNumberComma( StringUtil.fenToYuan(String.valueOf(fund)) );
        cetAmount.setHint(getResources().getString(R.string.cashout_amount_can_use) + fundStr + "元");
        AmountInputUtil.addAmountInputFilter(cetAmount);
    }

    // 点击提现按钮事件
    public void cashOut(View view) {
        if (shouldFinish) {
            return;
        }
        if (ClickUtil.cantClick()) {
            return;
        }

        String dateStr = DateUtil.calendarToStr(Calendar.getInstance());
        if (!checkDateExist(dateStr, dates)) {
            ToastUtil.showShortMessage(remark1);
            return;
        }
        if (!hasCard) {
            ToastUtil.showShortMessage("请选择银行卡");
            return;
        }
        if (cetAmount.getText().toString().trim().isEmpty()) {
            ToastUtil.showShortMessage("请输入金额");
            return;
        }
        amountStr = StringUtil.yuanToFen(cetAmount.getText().toString().trim());
        amount = StringUtil.fenToInt(amountStr);

        if (amount <= 0) {
            ToastUtil.showShortMessage("请输入有效金额");
            return;
        }
        if (dayTime < 1) {
            ToastUtil.showShortMessage("您已到达今日提现次数上限");
            return;
        }
        // 提现数额 与 余额/限额中较小的数额比较
        if (amount > (fund <= amountLimit ? fund : amountLimit)) {
            if (fund <= amountLimit) {
                ToastUtil.showShortMessage("输入金额不能超过可转出余额");
            } else {
                ToastUtil.showShortMessage("输入金额不能超过单笔限额");
            }
            return;
        }
        // 检测是否有支付密码
        if ("1".equals(MyApp.getInstance().getUserInfo().getHasPayPwd())) {
            payPwdDialog.showPayPwdDialog(mContext, amount, this);
        } else {
            payPwdDialog.showSetPwdDialog(mContext, this);
        }
    }

    // 跳转至银行卡页
    public void toBankcard(View view) {
        if (shouldFinish) {
            return;
        }
        Intent intent = new Intent(mContext,BankcardActivity.class);
        intent.putExtra("from",1);//从提现进入时可选银行卡
        startActivityForResult(intent, REQUEST_CODE_BANK);
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

    // 调取提现接口
    private void toCashOut(String password) {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        String encryptPwd = MD5Util.pwdEncrypt(password);// 加密

        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                amountStr,
                encryptPwd,
                merCode );

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<Object>> call = service.postWithdraw(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                amountStr,
                encryptPwd,
                merCode,
                cipher);
        call.enqueue(new Callback<ResponseBase<Object>>() {
            @Override
            public void onResponse(Call<ResponseBase<Object>> call, Response<ResponseBase<Object>> response) {
                if (response.isSuccessful()) {
                    closeProgress();
                    payPwdDialog.clearInput();
                    dealCashOutData(response.body());
                } else {
                    closeProgress();
                    payPwdDialog.clearInput();
                    ResultCode.responseCode(response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBase<Object>> call, Throwable t) {
                ResultCode.callFailure(t.getMessage());
                closeProgress();
                payPwdDialog.clearInput();
            }
        });
    }

    private void dealCashOutData(ResponseBase body) {

        if (ResultCode.RESULT_SUCCESS.equals(body.getResultCode())) {

            ToastUtil.showShortMessage("提现成功");
            payPwdDialog.closeDialog();

            // 通知更新
            Intent action = new Intent(BalanceActivity.LOCAL_ACTION_BALANCE_UPDATE);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(action);

            // 成功后回到余额页面
            finish();

        } else if (!resultCode(body.getResultCode())) {

            // 解析码
            if ("3".equals(body.getResultCode())) {

                Map data = (Map) body.getData();
                int time = StringUtil.doubleStrToInt( String.valueOf(data.get("passwordErrorNum")) );
                String msg = "密码错误，您还可输入" + time + "次";
                payPwdDialog.showPayPwdError(mContext, msg, this);

            } else if ("8".equals(body.getResultCode())){

                Map data = (Map) body.getData();
                int minStr = StringUtil.doubleStrToInt( String.valueOf(data.get("passwordErrorNum")) );
                String msg = "支付密码已被锁定，请" + 3 + "小时后再试";
                payPwdDialog.showPayPwdLock(mContext, msg, this);

            } else {
                if (body.getResultmsg() != null) {
                    ToastUtil.showShortMessage(body.getResultmsg());
                } else {
                    ToastUtil.showShortMessage("系统异常");
                }
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
        startProgress();
        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile());

        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<WithdrawInfo>> call = service.postWithdrawInfo(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                cipher);
        call.enqueue(new Callback<ResponseBase<WithdrawInfo>>() {
            @Override
            public void onResponse(Call<ResponseBase<WithdrawInfo>> call, Response<ResponseBase<WithdrawInfo>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        // 拼接字符串
                        setInfoText(response.body().getData());
                        amountLimit = StringUtil.fenToInt(response.body().getData().getAmountLimit());
                        dayTime = StringUtil.fenToInt(response.body().getData().getRemainTimes());
                    } else {
                        if (!resultCode(response.body().getResultCode())) {
                            ResultCode.withdrawInfo(response.body().getResultCode());
                        }
                        if (!"101".equals(response.body().getResultCode())) {
                            delayFinish();
                        }
                    }
                } else {
                    ResultCode.responseCode(response.code());
                    delayFinish();
                }
                closeProgress();
            }

            @Override
            public void onFailure(Call<ResponseBase<WithdrawInfo>> call, Throwable t) {
                ResultCode.callFailure(t.getMessage());
                closeProgress();
                delayFinish();
            }
        });
    }

    private void getWithdrawInfo2() {
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
//            String remainTimesStr = (String) ruleMap.get("remainTimes");
//            String amountLimitStr = (String) ruleMap.get("amountLimit");
//            String remark = (String) ruleMap.get("remark");
//            String fee = (String) ruleMap.get("fee");
            dates = (ArrayList<String>) ruleMap.get("dates");
            remark1 = (String) ruleMap.get("remark1");
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showShortMessage("提现规则解析异常");
            delayFinish();
        }
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

    private void setInfoText(WithdrawInfo info) {
        // 这里需要做拼接
        String amtStr = StringUtil.addNumberComma( StringUtil.fenToYuan(info.getAmountLimit()) );

        String infoStr;
        infoStr = info.getRemark() + "\n\n";
        infoStr += "单笔限额" + amtStr + "元，";
        infoStr += "今日还可转出" + info.getRemainTimes() + "次";
        tvInfo.setText(infoStr);
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

    @Override
    public void onInputFinish(String password) {
        // 调提现接口
        toCashOut(password);
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

    @Override
    public void onBtnClicked() {
        if (ClickUtil.cantClick()) {
            return;
        }
        // 预约提现检查预约是否存在
        checkOrder();
    }

    private void checkOrder() {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        // 填充参数
        MyApp myApp = MyApp.getInstance();
        Map<String, Object> paras = new HashMap<>();
        paras.put("mobile", myApp.getUserInfo().getMobile());
        // 发起请求
        MyRetrofit.init(mContext).url(RequestUrl.WITHDRAW_ORDER_EXIST).params(paras)
                .success(new MyRetrofit.HttpSuccess() {
                    @Override
                    public void onSuccess(cn.zheft.www.zheft.retrofit.Response body) {
                        closeProgress();
                        dealCheckData(body);
                    }
                })
                .failure(new MyRetrofit.HttpFailure() {
                    @Override
                    public void onFailure(String message) {
                        closeProgress();
                        ToastUtil.showShortMessage(message);
                    }
                })
                .post();
    }

    private void dealCheckData(cn.zheft.www.zheft.retrofit.Response body) {
        if (MyRetrofit.RESULT_SUCCESS == body.getResultcode()) {
            // 去预约页面
            Intent intent = new Intent(mContext, CashOrderActivity.class);
            startActivity(intent);
        } else {
            ToastUtil.showShortMessage(body.getResultmsg());
        }
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
}
