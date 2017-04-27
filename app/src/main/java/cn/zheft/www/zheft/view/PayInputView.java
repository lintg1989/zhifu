package cn.zheft.www.zheft.view;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.util.DensityUtil;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.StringUtil;
import cn.zheft.www.zheft.util.ToastUtil;

/**
 * 用于输入金额，通过set方法置入宽高，宽度填充屏幕，高度由内容数组计算得出
 */

public class PayInputView extends RelativeLayout implements View.OnClickListener {

    private static final String TAG = PayInputView.class.getSimpleName();
    private Context context;

    // 一些基础值
    private int mWidth;
    private int mHeight;

    private int unitHeight;
    private int unitWidth;

    private int margin = 2;

    private Paint mPaint;
    private Path mPath;

    private int colume = 4;// 四列

    private int row = 4; // 四行

    private int viewNum = 14; // 总共14个

    private List<String> text; // 存内容
    private List<TextView> textViews;

    private String inputStr;
    private String beforeInputStr; // 点击加号时需要将之前的inputStr保存
    private boolean isAdding = false;


    public PayInputView(Context context) {
        this(context, null);
    }

    public PayInputView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PayInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        text = new ArrayList<>();
        text.add("7");
        text.add("8");
        text.add("9");
        text.add("删除");
        text.add("4");
        text.add("5");
        text.add("6");
        text.add("+");
        text.add("1");
        text.add("2");
        text.add("3");
        text.add("=");
        text.add("0");
        text.add(".");
    }

    public void setBackground(int position, int resId) {

    }

    public void setOnInputListener(OnInputListener listener) {
        this.onInputListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = mWidth + margin * row;// 宽高等同(加margin值)
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int myHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mHeight, mode);
        super.onMeasure(widthMeasureSpec, myHeightMeasureSpec);

        // 单元格大小
        unitHeight = (mHeight - margin * (row - 1)) / row;
        unitWidth = (mWidth - margin * (colume - 1)) / colume;



        // 生成自定义的控件
        if (textViews == null) {
            textViews = new ArrayList<>();

            for (int i = 0; i < viewNum; i++) {

                TextView textView = new TextView(context);
                textView.setId( i + 1 );
                textView.setText(text.get(i));
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(getResources().getColor(R.color.text_color_dark));
//                textView.setBackground(getResources().getDrawable(R.drawable.layout_click_mask));

                float textSize = textView.getTextSize();
                textSize = DensityUtil.px2sp(context, textSize);
                if (i != 3) {
                    textSize *= 1.4f;
                } else {
                    textSize *= 1.2f;
                }
                textView.setTextSize(textSize);

                int bgColor = getResources().getColor(R.color.pay_input_num_bg);
                if (i == 3 || i == 7 || i == 11) {
                    bgColor = getResources().getColor(R.color.pay_input_func_bg);
                }
                textView.setBackgroundColor(bgColor);

                int height = unitHeight;
                int width = unitWidth;
                if (i == 11) {
                    height = height * 2 + margin;
                }
                if (i == 12) {
                    width = width * 2 + margin;
                }

                LayoutParams layoutParams = new LayoutParams(width, height);

                int marginLeft = 0;

                // 不是每行第一个，则设置为位置前一个右边
                if (i % colume != 0) {
                    layoutParams.addRule(RelativeLayout.RIGHT_OF, textViews.get(i - 1).getId());
                } else {
                    marginLeft = margin;
                }

                // 从第二行开始，设置为上一行同一位置View的下面
                if (i > colume - 1) {
                    layoutParams.addRule(RelativeLayout.BELOW, textViews.get(i - colume).getId());
                }

                layoutParams.setMargins(marginLeft, 0, margin, margin);
                textViews.add(textView);
                addView(textViews.get(i), layoutParams);

                textView.setOnClickListener(this);
            }
        }
    }

    /**
     * 计算得到每一个矩形的四个坐标点
     * 需要判断是否是特殊区块
     */
    private void initRect() {
        Rect rect = new Rect();
    }


    private OnInputListener onInputListener;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case 1:
                appendInput("7");
                break;
            case 2:
                appendInput("8");
                break;
            case 3:
                appendInput("9");
                break;
            case 4: // 删除
                backspace();
                break;
            case 5:
                appendInput("4");
                break;
            case 6:
                appendInput("5");
                break;
            case 7:
                appendInput("6");
                break;
            case 8: // +
                addClick();
                break;
            case 9:
                appendInput("1");
                break;
            case 10:
                appendInput("2");
                break;
            case 11:
                appendInput("3");
                break;
            case 12: // =
                equals();
                break;
            case 13:
                appendInput("0");
                break;
            case 14:
                appendInput(".");
                break;
            default:
                break;
        }
    }

    public interface OnInputListener {
        void onInput(String num);
    }


    private void appendInput(String input) {
        if (isAdding) {
            // Add按钮变色
            textViews.get(7).setBackgroundColor(getResources().getColor(R.color.pay_input_func_bg));
            isAdding = false;
        }

        String oldStr = inputStr;
        if (StringUtil.nullOrEmpty(oldStr)) {
            oldStr = "0";
        }
        int dotIndex = oldStr.indexOf(".");
        // 整数部分最多7位，允许输入“.”
        if ((dotIndex < 0 && oldStr.length() >= 7) && !input.equals(".")) {
//            ToastUtil.showShortMessage("金额超过上限");
            return;
        }
        // 使用setInput(oldStr + input)即可，但要排除三种特殊情况
        // 特例1：首位为 0 时输入数字
        if (oldStr.equals("0") && !input.equals(".")) {
            setInput(input);
            return;
        }
        // 特例2和3（有小数点的情况）
        if (dotIndex > 0) {
            // 特例2（输入“.”无效）或特例3（小数点已有两位）
            if (input.equals(".") || oldStr.length() - dotIndex > 2) {
                return;
            }
        }

        setInput(oldStr + input);
    }

    private void setInput(String input) {
        if (input != null && input.length() > 0) {
            inputStr = input;
        } else {
            inputStr = "0";
        }

        if (onInputListener != null) {
            onInputListener.onInput(inputStr);
        }
    }

    private void backspace() {
        if (inputStr != null && inputStr.length() > 0) {
            setInput(inputStr.substring(0, inputStr.length() - 1));
        }
    }

    // 点击加号
    private void addClick() {
        if (!isAdding) {
            // 如果是第二次输入，先加得结果
            if (!StringUtil.nullOrEmpty(beforeInputStr)) {
                inputStr = addInput();
                setInput(inputStr);
            }
            // Add按钮变背景色
            textViews.get(7).setBackgroundColor(getResources().getColor(R.color.pay_input_num_bg));

            isAdding = true;       // 变更状态
            beforeInputStr = inputStr;
            inputStr = null;
        }
    }

    private String addInput() {
        int amount = StringUtil.fenToInt(StringUtil.yuanToFen(beforeInputStr))
                + StringUtil.fenToInt(StringUtil.yuanToFen(inputStr));
        if (amount > 999999999) {
//            ToastUtil.showShortMessage("金额超过上限");
            amount = 999999999;
        }
        beforeInputStr = null;
        return StringUtil.fenToYuan(StringUtil.intToFen(amount));
    }

    // 等于 按钮
    private void equals() {
        inputStr = addInput();
        setInput(inputStr);
        textViews.get(7).setBackgroundColor(getResources().getColor(R.color.pay_input_func_bg));
        isAdding = false;
    }

    private void clearInput() {
        setInput("0");
        beforeInputStr = null;
        textViews.get(7).setBackgroundColor(getResources().getColor(R.color.pay_input_func_bg));
        isAdding = false;
    }
}
