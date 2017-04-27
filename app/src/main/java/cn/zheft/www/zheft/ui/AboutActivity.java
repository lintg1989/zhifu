package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.util.VersionUtil;

/**
 * 关于我们
 */

public class AboutActivity extends BaseActivity {
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setToolbar(R.string.about_us);
        context = this;

        TextView tvVersion = (TextView) findViewById(R.id.tv_about_version);
        tvVersion.setText(context.getString(R.string.version_code) + VersionUtil.getVersionName(context));
    }
}
