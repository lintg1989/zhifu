package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;

/**
 * 选择反馈类型
 */
public class FeedbackTypeActivity extends BaseActivity {
    private Context context;
    private String[] types;
    private int getType;//从上一个页面传进来的值
    private ImageView ivChecked;

    private static int RESULT_ADVICE = 1;
    private static int RESULT_REPORT = 2;
    private static int RESULT_COORPER = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_type);
        setToolbar(R.string.feedback_type);
        context = this;

        Intent intent = getIntent();
        getType = intent.getIntExtra("type",0);
        types = getResources().getStringArray(R.array.feedback_types);
        initView();
    }

    private void initView() {
        TextView tvAdvice = (TextView) findViewById(R.id.tv_feedback_advice);
        TextView tvReport = (TextView) findViewById(R.id.tv_feedback_report);
        TextView tvCoorper = (TextView) findViewById(R.id.tv_feedback_coorper);
        tvAdvice.setText(types[0]);
        tvReport.setText(types[1]);
        tvCoorper.setText(types[2]);
        if (getType != 0) {
            setChecked(getType);
        }
    }

    public void feedbackAdvice(View view) {
        setChecked(RESULT_ADVICE);
        finish();
    }

    public void feedbackReport(View view) {
        setChecked(RESULT_REPORT);
        finish();
    }

    public void feedbackCoorper(View view) {
        setChecked(RESULT_COORPER);
        finish();
    }

    private void setChecked(int type) {
        if (ivChecked != null) {
            ivChecked.setVisibility(View.INVISIBLE);
        }
        switch (type) {
            case 1:
                ivChecked = (ImageView) findViewById(R.id.iv_feedback_advice);
                ivChecked.setVisibility(View.VISIBLE);
                break;
            case 2:
                ivChecked = (ImageView) findViewById(R.id.iv_feedback_report);
                ivChecked.setVisibility(View.VISIBLE);
                break;
            case 3:
                ivChecked = (ImageView) findViewById(R.id.iv_feedback_coorper);
                ivChecked.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
        setResult(type);
    }
}
