package cn.zheft.www.zheft.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.jungly.gridpasswordview.GridPasswordView;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.ui.ForgetPayPwdActivity;
import cn.zheft.www.zheft.ui.SetPasswordActivity;
import cn.zheft.www.zheft.util.StringUtil;

/**
 * 用于输入支付密码相关的Dialog
 * By Liao on 2017/3/17
 */

public class PayPwdDialog {
    private GridPasswordView gridPasswordView;
    private AlertDialog inputDialog;

    private String textTitle;
    private String textPrompt;

    public PayPwdDialog() {
    }

    public interface OnPayPwdListener {

        // 密码输入完成点击确定
        void onInputFinish(String password);

        // 忘记密码
        void onForgetPwd();

        // 设置密码
        void toSetPwd();
    }

    public void clearInput() {
        if (gridPasswordView != null) {
            gridPasswordView.clearPassword();
        }
    }

    public void closeDialog() {
        if (inputDialog != null && inputDialog.isShowing()) {
            inputDialog.dismiss();
        }
    }

    public void setTitle(String title) {
        textTitle = title;
    }

    public void setPrompt(String prompt) {
        textPrompt = prompt;
    }

    public void showPayPwdDialog(Context context, int fen, OnPayPwdListener listener) {
        String fenStr = "¥" + StringUtil.fenToYuan(StringUtil.intToFen(fen));
        showPayPwdDialog(context, fenStr, listener);
    }

    public void showPayPwdDialog(final Context context, String amountStr, final OnPayPwdListener listener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_pay_password, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        inputDialog = builder.create();
        inputDialog.setCanceledOnTouchOutside(false); // 设置点击周围不取消
        inputDialog.show();
        // 自动显示键盘
        inputDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        inputDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        // 初始化支付密码输入dialog的各种view
        gridPasswordView = (GridPasswordView) view.findViewById(R.id.pay_pwd_view);
        gridPasswordView.setOnPasswordChangedListener(new GridPasswordView.OnPasswordChangedListener() {
            @Override
            public void onTextChanged(String psw) {
            }

            @Override
            public void onInputFinish(String psw) {
                if (listener != null) {
                    listener.onInputFinish(psw);
                }
            }
        });
        ImageView ivClose = (ImageView) view.findViewById(R.id.iv_close_pay_pwd);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputDialog.dismiss();
            }
        });

        textTitle = textTitle == null ? context.getString(R.string.input_pay_password) : textTitle;
        textPrompt = textPrompt == null ? context.getString(R.string.pay_dialog_cash_out) : textPrompt;

        TextView tvTitle = (TextView) view.findViewById(R.id.tv_pay_pwd_title);
        tvTitle.setText(textTitle);

        TextView tvPrompt = (TextView) view.findViewById(R.id.tv_pay_pwd_prompt);
        tvPrompt.setText(textPrompt);

        TextView tvForget = (TextView) view.findViewById(R.id.tv_forget_pay_pwd);
        tvForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onForgetPwd();
                }
            }
        });
        TextView tvAmount = (TextView) view.findViewById(R.id.tv_pay_pwd_amount);
        tvAmount.setText(amountStr);
    }

    public void showSetPwdDialog(Context context, final OnPayPwdListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("支付密码");
        builder.setMessage("请先设置支付密码");
        builder.setNegativeButton("取消", null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.toSetPwd();
                }
            }
        });
        AlertDialog dialogSet = builder.create();
        dialogSet.setCanceledOnTouchOutside(false); // 设置点击周围不取消
        dialogSet.show();
    }

    public void showPayPwdError(Context context, String message, final OnPayPwdListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("支付密码错误");
        builder.setMessage(message);
        builder.setNegativeButton("再次输入", null);
        builder.setPositiveButton("忘记密码", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (listener != null) {
                    listener.onForgetPwd();
                }
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void showPayPwdLock(Context context, String message, final OnPayPwdListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("支付密码锁定");
        builder.setMessage(message);
        builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (inputDialog != null && inputDialog.isShowing()) {
                    inputDialog.dismiss();
                }
            }
        });
        builder.setNegativeButton("重置密码", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (listener != null) {
                    listener.onForgetPwd();
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
    }

}
