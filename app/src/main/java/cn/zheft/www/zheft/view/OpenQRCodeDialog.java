package cn.zheft.www.zheft.view;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.util.ClickUtil;

/**
 * 开通二维码服务协议
 */

public class OpenQRCodeDialog implements View.OnClickListener {

    private Activity activity;
    private PopupWindow popupWindow;
    private boolean agree;

    private OnAgreeListener onAgreeListener;

    public OpenQRCodeDialog() {
    }

    @Override
    public void onClick(View v) {
        if (ClickUtil.cantClick()) {
            return;
        }
        switch (v.getId()) {
            case R.id.iv_dialog_open_qrcode_close:
                closeDialog();
                break;
            case R.id.iv_dialog_open_qrcode_agree:
                checkedAgreeButton(v);
                break;
            case R.id.tv_dialog_open_qrcode_agree:
                agreePressed();
                break;
            default:
                break;
        }
    }

    public interface OnAgreeListener {
        void onAgree(boolean agreed);
    }

    public void setOnAgreeListener(OnAgreeListener onAgreeListener) {
        this.onAgreeListener = onAgreeListener;
    }

    private void closeDialog() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    public void showOpenQRDialog(final Activity context, String title, String agreement, String extra) {
        activity = context;
        agree = true;

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_open_qrcode_agreement, null);

        popupWindow = new PopupWindow(view,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT, true);

        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setAnimationStyle(R.style.MyPopupWindowAnim);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0 ,0); // 用这个会没有动效，需配合AnimationStyle
        // popupWindow.showAsDropDown(view);// 这样会导致滑动闪烁，但有默认动画效果


        ImageView ivClose = (ImageView) view.findViewById(R.id.iv_dialog_open_qrcode_close);
        ImageView ivAgree = (ImageView) view.findViewById(R.id.iv_dialog_open_qrcode_agree);
        TextView tvTitle = (TextView) view.findViewById(R.id.tv_dialog_open_qrcode_title);
        TextView tvAgree = (TextView) view.findViewById(R.id.tv_dialog_open_qrcode_agree);
        TextView tvExtra = (TextView) view.findViewById(R.id.tv_dialog_open_qrcode_extra);
        TextView tvAgreement = (TextView) view.findViewById(R.id.tv_dialog_open_qrcode_agreement);

        ivClose.setOnClickListener(this); // 右上角x点击事件
        ivAgree.setOnClickListener(this); // “同意”RadioButton
        tvAgree.setOnClickListener(this); // 同意按钮
        tvTitle.setText(title);
        tvExtra.setText("已阅读" + extra);
        tvAgreement.setText(agreement);   // 协议内容

        // 全屏变暗
        setWindowAlpha(context, 0.6f);
//
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                // 屏幕暗度恢复
                setWindowAlpha(context, 1.0f);
            }
        });

    }

    private void setWindowAlpha(Activity context, float alpha) {
        WindowManager.LayoutParams params = context.getWindow().getAttributes();
        params.alpha = alpha;
        context.getWindow().setAttributes(params);
    }

    private void checkedAgreeButton(View view) {
        if (view instanceof ImageView) {
            if (agree) {
                agree = false;
                ((ImageView) view).setImageResource(R.mipmap.check_button_cancel);
            } else {
                agree = true;
                ((ImageView) view).setImageResource(R.mipmap.check_button_ok);
            }
        }
    }

    private void agreePressed() {
        if (onAgreeListener != null) {
            onAgreeListener.onAgree(agree);
        }
        if (agree) {
            closeDialog();
        }
    }
}
