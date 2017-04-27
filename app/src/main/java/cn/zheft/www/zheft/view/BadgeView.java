package cn.zheft.www.zheft.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import cn.zheft.www.zheft.util.StringUtil;

/**
 * 带数目的消息显示自定义view
 */
public class BadgeView extends View {
    private static final String TAG = BadgeView.class.getName();
    private View mView;
    // 控件宽高值
    private int mWidth;
    private int mHeight;

    private Rect mBound; // 文本绘制范围
    private Paint mPaint; // 画笔

    private String mText; // 文字
    private int mTextSize;   // 文字大小
    private int mBackRadius; // 半圆半径
    private int mTextColor = 0xffffffff;  // 文字颜色
    private int mBackColor = 0xffff4444;  // 背景颜色

    private int mCenterLeft; // 左半圆圆心

    public BadgeView(Context context) {
        this(context, null);
    }

    public BadgeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BadgeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mView = this;
        mView.setVisibility(INVISIBLE);

        // 初始化文字
        mText = "99+";
        // 初始化画笔
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBound = new Rect();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        mBackRadius = mHeight/2;
        mTextSize = (int) (mHeight * 0.75f);
        mPaint.setTextSize(mTextSize);
        mPaint.getTextBounds(mText, 0, mText.length(), mBound);

        mWidth = (int)(mHeight * 1.5f);
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mText == null || mText.length() <= 0 || "0".equals(mText)) {
            return;
        }
        mView.setVisibility(VISIBLE);

        mCenterLeft = (3 - mText.length()) * mHeight/4 + mBackRadius;

        // 绘制背景
        mPaint.setColor(mBackColor);
        mPaint.setAntiAlias(true);
        canvas.drawRect(mCenterLeft, 0, mWidth - mBackRadius, mHeight, mPaint);
        // 绘制左右半圆
        canvas.drawCircle(mCenterLeft, mHeight / 2, mBackRadius, mPaint);
        canvas.drawCircle(mWidth - mBackRadius, mHeight / 2, mBackRadius, mPaint);

        // 绘制文字
        mPaint.setColor(mTextColor);
        mPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(mText, (mCenterLeft - mBackRadius + mWidth) / 2, mHeight * 0.8f, mPaint);
    }

    public void setData(int num) {
        mText = StringUtil.intToStr(num);
        mView.setVisibility(VISIBLE);
        postInvalidate();
    }

}
