package cn.zheft.www.zheft.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.util.LogUtil;

/**
 * 方形的TextView
 * 指定某一边，另一边设置倍数
 */

public class SquareTextView extends TextView {

    // 默认倍数为1，两个值都指定时按宽度设置
    private int colume = 1;
    private int row = 1;

    public SquareTextView(Context context) {
        this(context, null);
    }

    public SquareTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SquareTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 获得自定义样式属性
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SquareTextView, defStyleAttr, 0);
        int n = array.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = array.getIndex(i);
            switch (attr) {
                case R.styleable.SquareTextView_colume:
                    colume = array.getInt(attr, 1);
                    break;
                case R.styleable.SquareTextView_row:
                    row = array.getInt(attr, 1);
                    break;
            }
        }
        array.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        super.onMeasure(MeasureSpec.makeMeasureSpec(width * colume, mode), MeasureSpec.makeMeasureSpec(width * row, mode));
    }
}
