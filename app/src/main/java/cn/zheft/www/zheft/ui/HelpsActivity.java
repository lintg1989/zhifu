package cn.zheft.www.zheft.ui;

import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;

/**
 * 帮助与反馈页面
 * 静态页面仅处理几个跳转
 */
public class HelpsActivity extends BaseActivity {
    private Context context;

    private RelativeLayout rlProblems;
    private RelativeLayout rlSuggests;
    private RelativeLayout rlContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helps);
        setToolbar(R.string.helps_and_feedback);
        context = this;

        initView();
        initEvent();
    }

    private void initView() {
        rlProblems = (RelativeLayout) findViewById(R.id.rlayout_to_problems);
        TextView tvProblems = (TextView) rlProblems.findViewById(R.id.tv_click_layout);
        tvProblems.setText(R.string.common_problem);

        rlSuggests = (RelativeLayout) findViewById(R.id.rlayout_to_suggest);
        TextView tvSuggests = (TextView) rlSuggests.findViewById(R.id.tv_click_layout);
        tvSuggests.setText(R.string.feedbacks);

        rlContact = (RelativeLayout) findViewById(R.id.rlayout_to_contact);
        TextView tvContact = (TextView) rlContact.findViewById(R.id.tv_click_layout);
        tvContact.setText(R.string.contact_us);
    }

    private void initEvent() {
        rlProblems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProblemActivity.class);
                startActivity(intent);
            }
        });
        rlSuggests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, FeedbackActivity.class);
                startActivity(intent);
            }
        });
        rlContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ContactActivity.class);
                startActivity(intent);
            }
        });
    }
}
