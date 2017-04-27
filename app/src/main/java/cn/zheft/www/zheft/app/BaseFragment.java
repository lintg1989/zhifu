package cn.zheft.www.zheft.app;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.ui.LoginActivity;
import cn.zheft.www.zheft.util.NetUtil;
import cn.zheft.www.zheft.util.ToastUtil;

/**
 * A simple {@link Fragment} subclass.
 * 暂未用到
 */
public class BaseFragment extends Fragment {
    private Context context;
    private ProgressDialog progressDialog;

    public BaseFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
    }

    // 显示加载进度条
    public void startProgress() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context,R.style.MyLoadingDialog);
        }
        progressDialog.getWindow().setWindowAnimations(R.style.MyLoadingDialogAnim);// 设置动画
        progressDialog.setCancelable(false);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
    }

    // 关闭加载进度条
    public void closeProgress() {
        if (progressDialog != null) {
            // 推迟0.2秒
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(getResources().getInteger(R.integer.progress_dialog_duration));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                }
            }).start();
        }
    }

    public boolean resultCode(String code) {
        switch (code) {
            case "1":
                ToastUtil.showShortMessage("系统错误");
                return true;
            case "100":
                ToastUtil.showShortMessage("网络请求失败，请重试");
                return true;
            case "101":
                Config.setUsed();
                showLogoutDialog();
                return true;
        }
        return false;
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle("退出登录");
        builder.setMessage("您的账号在另一台设备上登录");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(context, LoginActivity.class));
                MyApp.getInstance().exitApp(); // 关闭包括当前activity的所有activity
            }
        });
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_SEARCH;
            }
        });
        builder.show();
    }

    // 是否无网络
    public boolean nonNetwork() {
        if (context == null) {
            return true;
        }
        if (!NetUtil.isNetworkConnected(context)) {
            ToastUtil.showShortMessage(Config.MSG_NO_NET);
            return true;
        }
        return false;
    }

}
