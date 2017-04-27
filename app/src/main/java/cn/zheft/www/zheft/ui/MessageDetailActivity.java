package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.model.MessageInfo;

public class MessageDetailActivity extends BaseActivity {
    private Context context;
    private TextView tvType;
    private TextView tvDate;
    private TextView tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail);
        setToolbar(R.string.message_detail);
        context = this;

        Intent intent = this.getIntent();
        MessageInfo messageInfo = (MessageInfo) intent.getSerializableExtra("message");

        initView();
        initData(messageInfo);// 初始化数据
    }

    private void initView() {
        tvType = (TextView) findViewById(R.id.tv_message_detail_type);
        tvDate = (TextView) findViewById(R.id.tv_message_detail_date);
        tvContent = (TextView) findViewById(R.id.tv_message_detail_content);
    }

    private void initData(MessageInfo info) {
        if (info == null) {
            findViewById(R.id.text_error).setVisibility(View.VISIBLE);
            return;
        }
        tvType.setText(info.getType());
        tvDate.setText(info.getDate());
        tvContent.setText(info.getContent());
    }
}
