package cn.zheft.www.zheft.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.util.ClickUtil;

/**
 * 联系我们
 */
public class ContactActivity extends BaseActivity {
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        setToolbar(R.string.contact_us);
        context = this;

        //客服电话点击事件
        TextView textView = (TextView) findViewById(R.id.tv_service_tele);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ClickUtil.cantClick()) {
                    return;
                }
                //询问是否拨打电话
                showDialog();
            }
        });
        TextView textView1 = (TextView) findViewById(R.id.tv_website);
        textView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://"+getString(R.string.company_website_text));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

    private void callService() {
        String phone = getString(R.string.service_tele_no);
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
//        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
//        if (ActivityCompat.checkSelfPermission(context,
//                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//
//            ToastUtil.showShortMessage(R.string.no_call_permission);
////            requestPermissions(new String[] {}, );
//            return;
//        }
        startActivity(intent);
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.call_service_tele);
        builder.setMessage(R.string.service_tele_no_show);
        builder.setPositiveButton(R.string.ensure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                callService();
            }
        });
        builder.setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

}
