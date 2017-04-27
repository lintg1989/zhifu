package cn.zheft.www.zheft.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.util.LogUtil;
import cn.zheft.www.zheft.util.StringUtil;

/**
 * 控件在高度上比例为 1/6/2，6为线图区，2为日期区
 * 水平方向上折线图区占比为 1/16/1，即左右边距各为1/18
 * 水平边距使用值 mPaddingH = 16.0f, 可更改
 */
public class CustomCountView extends View {
    // 一些基础值
    private int mWidth;
    private int mHeight;
    // 四个值分别为图表区域 左X、右X、上Y、下Y
    private int rectLeft, rectRight, rectTop, rectBottom;
    private float mPaddingH = 36.0f;

    // 一些线宽等属性
    private int mPathWidth;    // 折线宽
    private int mLineWidth;    // 底线宽
    private int mStrokeWidth;  // 圆点线宽
    private int mCircleRadius; // 小圆半径

    private int dateSize;   // 日期数字大小
    private int amountSize; // 金额数字大小

    // 一些颜色值
    private int mDrawColor = 0xff3595ff;
    private int mDateColor = 0xff333333;// 日期数字颜色
    private int mBgTopColor = 0xffaadcff; // 背景渐变色

    private Paint mPaint;
    private Path mPath;
    private Shader shaderBg;
    private int[] shaderColor;

    private int arrayLength = 7;// 数组长度（默认为7）
    private Float amountMax = 1.0f; // 除数不能为0故这里给个初始值1
    private List<Integer> date;  // 存日期
    private List<Float> amount;  // 存金额浮点型
    private List<Integer> valueX;// 按控件高度比例计算得到的Y值
    private List<Integer> valueY;// 按控件宽度计算得到的X值

    private DecimalFormat decimalFormat;// 数字保留两位的格式

    // 各种构造方法
    public CustomCountView(Context context) {
        this(context, null);
    }

    public CustomCountView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomCountView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 获得自定义样式属性
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CustomCountView, defStyleAttr, 0);
        int n = array.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = array.getIndex(i);
            switch (attr) {
                case R.styleable.CustomCountView_drawColor:
                    mDrawColor = array.getColor(attr, Color.BLACK);
                    break;
            }
        }
        array.recycle();

        // 初始化画笔
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPath = new Path();
        shaderColor = new int[] {mBgTopColor, Color.WHITE};

        // 初始化
        date = new ArrayList<>();
        amount = new ArrayList<>();
        valueX = new ArrayList<>();
        valueY = new ArrayList<>();
        for (int i = 0; i < arrayLength; i++) {
            date.add(0);
            amount.add(0.0f);
        }

        decimalFormat = new DecimalFormat("0.00");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = (int) (mWidth * 0.6f);// 0.6是设计图上的宽高比例
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        int myHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mHeight, mode);

        super.onMeasure(widthMeasureSpec, myHeightMeasureSpec);

        // 得到矩形区域
        rectLeft = (int)(mWidth/mPaddingH);
        rectTop = (int)(mHeight/9.0f);
        rectRight = mWidth - rectLeft;
        rectBottom = mHeight - rectTop * 2;

        // 宽度/xxx得到小圆半径
        mCircleRadius = (int)(mWidth / 120.0f);
        mPathWidth = (int) (mCircleRadius/5.0f);
        mLineWidth = (int) (mPathWidth * 1.25f);
        mStrokeWidth = (int) (mLineWidth * 2.5f);

        dateSize = (int)(mHeight/18.0f);
        amountSize = (int)(mHeight/22.0f);

        getPoint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制一个渐变的矩形区域
        shaderBg = new LinearGradient(mWidth/2, 0, mWidth/2, rectBottom, shaderColor, null, Shader.TileMode.CLAMP );
        mPaint.setShader(shaderBg);
        mPaint.setAlpha(77); // 约30%透明度
        canvas.drawRect(0, 0, mWidth, rectBottom, mPaint);

        // 绘制纯白区域（封闭路径）
        mPath.reset();
        mPath.moveTo(0, 0);
        for (int i = 0; i < arrayLength; i++) {
            if (i == 0) {
                mPath.lineTo(0, valueY.get(i));
            }
            mPath.lineTo(valueX.get(i), valueY.get(i));
            if (i == arrayLength - 1) {
                mPath.lineTo(mWidth, valueY.get(i));
            }
        }
        mPath.lineTo(mWidth, 0);
        mPath.close();
        mPaint.reset();
        mPaint.setColor(Color.WHITE);
        canvas.drawPath(mPath, mPaint);

        // 绘制底线
        mPaint.reset();
        mPaint.setAlpha(255); // 无透明
        mPaint.setColor(mDrawColor);
        mPaint.setStrokeWidth(mLineWidth);
        canvas.drawLine(0, rectBottom, mWidth, rectBottom, mPaint);

        // 绘制折线（ 数组长度减一 次）
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(mPathWidth);
        mPaint.setAlpha(255); // 无透明
        for (int i = 0; i < arrayLength - 1; i++) {
            canvas.drawLine(valueX.get(i), valueY.get(i), valueX.get(i+1), valueY.get(i+1), mPaint);
        }

        // 绘制实心圆点
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);
        for (int i = 0; i < arrayLength; i++) {
            canvas.drawCircle(valueX.get(i), valueY.get(i), mCircleRadius, mPaint);
        }

        // 绘制空心圆点
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mDrawColor);
        mPaint.setStrokeWidth(mStrokeWidth);
        for (int i = 0; i < arrayLength; i++) {
            canvas.drawCircle(valueX.get(i), valueY.get(i), mCircleRadius, mPaint);
        }

        // 绘制数字
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextSize(amountSize);
        for (int i = 0; i < arrayLength; i++) {
            String amountStr = floatToStr(amount.get(i));
            amountStr = StringUtil.addNumberComma(amountStr);
            if (i == 0) {
                mPaint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(amountStr, rectLeft/2, valueY.get(i) - amountSize, mPaint);
            } else if (i == arrayLength - 1) {
                mPaint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(amountStr, mWidth - rectLeft/2, valueY.get(i) - amountSize, mPaint);
            } else {
                mPaint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(amountStr, valueX.get(i), valueY.get(i) - amountSize, mPaint);
            }
        }
        // 绘制日期
        mPaint.setTextSize(dateSize);
        mPaint.setColor(mDateColor);
        mPaint.setTextAlign(Paint.Align.CENTER);
        for (int i = 0; i < arrayLength; i++) {
            String dateStr = String.valueOf(date.get(i));
            if (i == arrayLength - 2) {
                dateStr = "前天";
            }
            if (i == arrayLength - 1) {
                dateStr = "昨天";
                canvas.drawText( dateStr, mWidth - mPaint.measureText(dateStr)/2, rectBottom + dateSize * 2, mPaint);
            } else {
                canvas.drawText( dateStr, valueX.get(i), rectBottom + dateSize * 2, mPaint);
            }
        }

        // 这句避免按下home键会造成背景混乱的问题
        // 按下home背景混乱起因是最后的绘制setStyle(STROKE)导致，具体原因不明
        mPaint.reset();
    }

    private void getPoint() {
        valueY.clear();
        valueX.clear();
        // 得到n个Y值
        for (int i = 0; i < arrayLength; i++) {
            valueY.add( rectBottom - (int)(amount.get(i)/amountMax * (rectBottom - rectTop)) );
        }

        // 得到X轴两点之间间距与n个X值
        int paddingX = (int)((mWidth - rectLeft * 2)/(arrayLength - 1));
        valueX.add(rectLeft);
        for (int i = 1; i < arrayLength; i++) {
            valueX.add( valueX.get(i-1) + paddingX );
        }
    }

    // 设置数据
    public void setData(List<Integer> dateSet, List<Float> amountSet) {
        if (dateSet == null || amountSet == null
                || dateSet.size() < 2 || amountSet.size() < 2
                || dateSet.size() != amountSet.size()) {
            LogUtil.e("CustomCountView", "DataError");
            return;
        }
        LogUtil.e("CustomCountView", "DataSet");
        date.clear();
        amount.clear();
        amountMax = 0.0f;
        arrayLength = dateSet.size();
        for (int i = 0; i < arrayLength; i++) {
            date.add(dateSet.get(i));
            amount.add(amountSet.get(i));
            if (amountSet.get(i) > amountMax) {
                amountMax = amountSet.get(i);
            }
        }
        if (amountMax <= 0.0f) {
            amountMax = 1.0f;
        }
        getPoint();
        postInvalidate();
    }

    private String floatToStr(Float flt) {
        try {
            return decimalFormat.format(flt);
        } catch (Exception e) {
            return "0.00";
        }
    }
}
