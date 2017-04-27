package cn.zheft.www.zheft.view;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import cn.zheft.www.zheft.util.StringUtil;

/**
 * 管理输入过滤相关
 */

public class AmountInputUtil {

    public static void addAmountInputFilter(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 如果第一位输入为0则第二位必须为"."
                if (s.length() > 1 && "0".equals(String.valueOf(s.toString().charAt(0)))) {
                    if (!".".equals(String.valueOf(s.toString().charAt(1)))) {
                        editText.setText("0");   // 设为"0"
                        editText.setSelection(1);// 移动光标到末位（即1）
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                editText.removeTextChangedListener(this);

                // 如果第一位为"."则清空
                if (s.length() > 0 && ".".equals(String.valueOf(s.toString().charAt(0)))) {
                    editText.setText("");
                }
                // 防止输入超过两位小数点
                int dotIndex = s.toString().indexOf(".");
                if (dotIndex != -1 && (s.length() - dotIndex) > 3) {
                    String text = s.toString().substring(0, dotIndex + 3);
                    editText.setText( text );
                    editText.setSelection(dotIndex + 3);
                }

                editText.addTextChangedListener(this);
            }
        });
    }
}
