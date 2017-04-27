package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.util.ClickUtil;
import cn.zheft.www.zheft.util.StringUtil;

/**
 * 红包消息页面
 */
public class MessageHongbaoActivity extends BaseActivity {
    private Context context;
    private String amount; // 面额

    private LinearLayout llBack; // 返回
    private TextView tvAmount;   // 红包面额
    private TextView tvDetail;   // 查看明细

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTransStatusBar();
        setContentView(R.layout.activity_message_hongbao);
        context = this;

        Intent intent = getIntent();
        amount = intent.getStringExtra("hongbaoAmount");

        initView();
        initEvent();
    }

    private void initView() {
        llBack = (LinearLayout) findViewById(R.id.ll_hongbao_back);
        tvDetail = (TextView) findViewById(R.id.tv_hongbao_detail);
        tvAmount = (TextView) findViewById(R.id.tv_hongbao_amount);
        // 设置面额
        if (!StringUtil.nullOrEmpty(amount)) {
            tvAmount.setText(amount);
        }
    }

    private void initEvent() {
        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        tvDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 至明细页面
            }
        });
    }
}
