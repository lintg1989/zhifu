package cn.zheft.www.zheft.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.model.CountInfo;
import cn.zheft.www.zheft.model.DayTradeInfo;
import cn.zheft.www.zheft.retrofit.HttpUtil;
import cn.zheft.www.zheft.retrofit.MyRetrofit;
import cn.zheft.www.zheft.retrofit.ResponseBase;
import cn.zheft.www.zheft.retrofit.ResultCode;
import cn.zheft.www.zheft.retrofit.RetrofitAPI;
import cn.zheft.www.zheft.util.ClickUtil;
import cn.zheft.www.zheft.util.DateUtil;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.StringUtil;
import cn.zheft.www.zheft.util.ToastUtil;
import cn.zheft.www.zheft.view.CustomCountView;
import cn.zheft.www.zheft.view.PickDateDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 统计页面
 */
public class CountActivity extends BaseActivity implements View.OnClickListener,
        PickDateDialog.OnPickDateClickListener{

    private static final String TAG = CountActivity.class.getSimpleName();
    private Context mContext;

    private TextView tvStart;  // 起始日期
    private TextView tvEnd;    // 结束日期
    private TextView tvPosNum;     // POS笔数
    private TextView tvPosAmount;  // POS金额
    private TextView tvCodeNum;    // 二维码笔数
    private TextView tvCodeAmount; // 二维码金额
    private TextView tvChartHead;

    private Button btSearch;

    private Calendar calendarStart;// 起始日期
    private Calendar calendarEnd;  // 结束日期
    private Calendar calendarMax;  // 最大日期（今日）
    private Calendar calendarMin;  // 最小日期（六月前）

    private CustomCountView chartView; // 自定义图表

    private PickDateDialog pickDialogStart;
    private PickDateDialog pickDialogEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count);
        setToolbar(R.string.count_title);
        mContext = this;

        initView();
        initData();
        initChart(); // 获取过去7天的交易数据并制表
    }

    private void initView() {
        tvStart = (TextView) findViewById(R.id.tv_start_date);
        tvEnd = (TextView) findViewById(R.id.tv_end_date);
        tvPosAmount = (TextView) findViewById(R.id.tv_count_pos_amount);
        tvPosNum = (TextView) findViewById(R.id.tv_count_pos_num);
        tvCodeAmount = (TextView) findViewById(R.id.tv_count_qrcode_amount);
        tvCodeNum = (TextView) findViewById(R.id.tv_count_qrcode_num);

        btSearch = (Button) findViewById(R.id.btn_search);
        chartView = (CustomCountView) findViewById(R.id.ccv_count);
        tvChartHead = (TextView) findViewById(R.id.tv_count_chart_head);

        tvStart.setOnClickListener(this);
        tvEnd.setOnClickListener(this);
        btSearch.setOnClickListener(this);

        pickDialogStart = new PickDateDialog(mContext);
        pickDialogEnd = new PickDateDialog(mContext);
    }

    private void initData() {
        // 初始化日历日期
        calendarMin = DateUtil.getDate(0, -6, 1, DateUtil.DAY_TIME_START);
        calendarStart = DateUtil.resetTime(DateUtil.DAY_TIME_START);
        calendarEnd = DateUtil.resetTime(DateUtil.DAY_TIME_END);
        calendarMax = DateUtil.resetTime(DateUtil.DAY_TIME_END);

        tvStart.setText(DateUtil.calendarToChar(calendarStart));
        tvEnd.setText(DateUtil.calendarToChar(calendarEnd));

        // 初始化统计图表日期
        initDaysHead();

        getData();// 初始化页面时调取数据
    }

    private void initChart() {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile() );

        // 取得消费统计
        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<List<DayTradeInfo>>> call = service.postTradeInfo(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                cipher);
        call.enqueue(new Callback<ResponseBase<List<DayTradeInfo>>>() {
            @Override
            public void onResponse(Call<ResponseBase<List<DayTradeInfo>>> call,
                                   Response<ResponseBase<List<DayTradeInfo>>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        setChartData(response.body().getData());// 图表传参
                    } else {
                        if (!resultCode(response.body().getResultCode())) {
                            // 解析码
                            ResultCode.chartDays(response.body().getResultCode());
                        }
                    }
                } else {
                    ResultCode.responseCode(response.code());
                }
                closeProgress();
            }

            @Override
            public void onFailure(Call<ResponseBase<List<DayTradeInfo>>> call, Throwable t) {
                ResultCode.callFailure(t.getMessage());
                closeProgress();
            }
        });
    }

    private void showDialogStart() {
        String btnText = getString(R.string.ensure);
        int btnPositive = DialogInterface.BUTTON_POSITIVE;
        pickDialogStart.setButton(btnPositive, btnText, this);
        pickDialogStart.setMinDate(calendarMin);
        pickDialogStart.setMaxDate(calendarEnd);
        pickDialogStart.showDialog(calendarStart);
    }

    private void showDialogEnd() {
        String btnText = getString(R.string.ensure);
        int btnPositive = DialogInterface.BUTTON_POSITIVE;
        pickDialogEnd.setButton(btnPositive, btnText, this);
        pickDialogEnd.setMinDate(calendarStart);
        pickDialogEnd.setMaxDate(calendarMax);
        pickDialogEnd.showDialog(calendarEnd);
    }

    private void getData() {
        if (nonNetwork()) {
            return;
        }
        startProgress();
        String cipher = HttpUtil.getExtraKey(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                DateUtil.calendarToStr(calendarStart),
                DateUtil.calendarToStr(calendarEnd));

        // 取得消费统计
        RetrofitAPI service = MyRetrofit.create(RetrofitAPI.class);
        Call<ResponseBase<List<CountInfo>>> call = service.postCountList(
                MyApp.getInstance().getDeviceId(),
                Config.DEVICE_TYPE,
                MyApp.getInstance().getUserInfo().getMobile(),
                DateUtil.calendarToStr(calendarStart),
                DateUtil.calendarToStr(calendarEnd),
                cipher );
        call.enqueue(new Callback<ResponseBase<List<CountInfo>>>() {
            @Override
            public void onResponse(Call<ResponseBase<List<CountInfo>>> call, Response<ResponseBase<List<CountInfo>>> response) {
                if (response.isSuccessful()) {
                    if (ResultCode.RESULT_SUCCESS.equals(response.body().getResultCode())) {
                        List<CountInfo> countInfos = response.body().getData();
                        updateData(countInfos);// 更新页面
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
            public void onFailure(Call<ResponseBase<List<CountInfo>>> call, Throwable t) {
                ResultCode.callFailure(t.getMessage());
                closeProgress();
            }
        });
    }

    private void updateData(List<CountInfo> infos) {
        if (infos.size() == 0) {
            return;// 没有数据不展示
        }
        for (int i = 0; i < infos.size(); i++) {
            if (infos.get(i).getType().equals("1")) {
                tvPosAmount.setText( StringUtil.addNumberComma( infos.get(i).getTotalAmount() ) + getString(R.string.yuan_zh));
                tvPosNum.setText( infos.get(i).getTradeCount() + "笔" );
            } else if (infos.get(i).getType().equals("2")) {
                tvCodeAmount.setText( StringUtil.addNumberComma( infos.get(i).getTotalAmount() ) + getString(R.string.yuan_zh));
                tvCodeNum.setText( infos.get(i).getTradeCount() + "笔" );
            }
        }
    }

    private void initDaysHead() {
        try {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -7);
            String headStart = DateUtil.calendarToStrNoLine(cal);
            List<Integer> date = new ArrayList<>();
            List<Float> amount = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                amount.add(0.0f);
                date.add(dateToInteger(DateUtil.calendarToStrNoLine(cal)));
                cal.add(Calendar.DATE, 1);
            }
            cal.add(Calendar.DATE, -1);
            String headEnd = DateUtil.calendarToStrNoLine(cal);
            setDaysHead(headStart, headEnd);
            chartView.setData(date, amount);
        } catch (Exception e) {
            setDaysHead(null, null);
        }
    }

    private void setChartData(List<DayTradeInfo> infos) {
        LogUtil.e("Count", "SetChartData");
        List<Integer> date = new ArrayList<>();
        List<Float> amount = new ArrayList<>();
        for (DayTradeInfo info : infos) {
            date.add(dateToInteger(info.getDate()));
            amount.add(stringToFloat(info.getAmount()));
        }
        chartView.setData(date, amount);
        setDaysHead(infos.get(0).getDate(), infos.get(infos.size()-1).getDate());
    }

    private void setDaysHead(String start, String end) {
        String headText = getResources().getString(R.string.count_chart_title)
                + " (" + dateToHead(start) + " - " + dateToHead(end) + ")";
        tvChartHead.setText(headText);
    }

    private Float stringToFloat(String s) {
        try {
            return Float.valueOf(s);
        } catch (Exception e) {
            return 0.0f;
        }
    }

    private Integer dateToInteger(String s) {
        try {
            return Integer.valueOf(s.substring(s.length()-2));
        } catch (Exception e) {
            return 0;
        }
    }

    private String dateToHead(String date) {
        if (!StringUtil.nullOrEmpty(date) && date.length() >= 4) {
            int length = date.length();
            return date.substring(length-4, length-2) + "." + date.substring(length-2);
        }
        return "00.00";
    }

    @Override
    public void onClick(View v) {
        if (ClickUtil.cantClick()) {
            return;
        }

        switch (v.getId()) {
            case R.id.tv_start_date:
                showDialogStart();
                break;
            case R.id.tv_end_date:
                showDialogEnd();
                break;
            case R.id.btn_search:
                getData(); // 调取查询接口
                MobclickAgent.onEvent(mContext, "um_account_statistics_search"); // 友盟统计
                break;
            default:
        }
    }

    @Override
    public void onPickDateClick(PickDateDialog dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            // 通过dialog区分点击事件来源
            if (dialog.equals( pickDialogStart )) {
                // 设置起始日期
                calendarStart = dialog.getCalendar( PickDateDialog.TIME_START );
                tvStart.setText( DateUtil.calendarToChar(calendarStart) );
            } else if (dialog.equals( pickDialogEnd )) {
                // 设置结束日期
                calendarEnd = dialog.getCalendar( PickDateDialog.TIME_END );
                tvEnd.setText( DateUtil.calendarToChar(calendarEnd) );
            }
        }
    }
}
