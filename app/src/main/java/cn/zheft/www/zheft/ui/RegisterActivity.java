package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.os.Bundle;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;

public class RegisterActivity extends BaseActivity {
    private static final String TAG = RegisterActivity.class.getSimpleName();
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setToolbar(getString(R.string.register_qrcode_user), false, null, false);
        disablePatternLock();// 禁用手势密码
        doubleClickBack();   // 双击退出
        mContext = this;

        initView();
    }

    private void initView() {
        //
    }
}
