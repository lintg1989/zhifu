package cn.zheft.www.zheft.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;

public class PayPasswordActivity extends BaseActivity {
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_password);
        setToolbar("支付密码",true,null,true);
        context = this;
    }

    // 修改支付密码
    public void changePayPwd(View view) {
        Intent intent = new Intent(context, ChangePasswordActivity.class);
        intent.putExtra("ToChangePwd","pay");
        startActivity(intent);
    }

    // 忘记支付密码
    public void forgetPayPwd(View view) {
        startActivity(new Intent(context, ForgetPayPwdActivity.class));
    }
}
