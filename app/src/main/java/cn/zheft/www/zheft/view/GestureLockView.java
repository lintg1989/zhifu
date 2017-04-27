package cn.zheft.www.zheft.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/**
 * 手势密码容器类
 * Thank for Zhang Hongyang
 * http://blog.csdn.net/lmj623565791/article/details/36236113
 */
public class GestureLockView extends View {
    private static final String TAG = "GestureLockView";

    // GestureLockView的三种状态
    // 把finger_up状态设为false状态
    enum Mode {
        STATUS_NO_FINGER, STATUS_FINGER_ON, STATUS_FINGER_UP
    }

    // GestureLockView的当前状态
    private Mode mCurrentStatus = Mode.STATUS_NO_FINGER;

    // 宽度
    private int mWidth;
    // 高度
    private int mHeight;
    // 外圆半径
    private int mRadius;
    // 外圆笔画宽度
    private int mStrokeWidth = 2;
    // 圆心坐标
    private int mCenterX;
    private int mCenterY;
    private Paint mPaint;

    // 箭头（小三角最长边的一半长度 = mArrawRate * mWidth / 2）
    private float mArrowRate = 0.2f;
    private int mArrowDegree = -1;
    private Path mArrowPath;

    // 内圆半径比例
    private float mInnerCircleRadiusRate = 0.2f;

    // 四个颜色，可由用户自定义，初始化时由GestureLockViewGroup传入
    private int mColorNoFingerInner;
    private int mColorNoFingerOutter;
    private int mColorFingerOn;
    private int mColorFingerUp;

    public GestureLockView(Context context,
                           int colorNoFingerInner, int colorNoFingerOutter,
                           int colorFingerOn, int colorFingerUp) {
        super(context);
        this.mColorNoFingerInner = colorNoFingerInner;
        this.mColorNoFingerOutter = colorNoFingerOutter;
        this.mColorFingerOn = colorFingerOn;
        this.mColorFingerUp = colorFingerUp;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArrowPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        // 取长和宽中的小值，以得到半径
        mWidth = mWidth < mHeight ? mWidth : mHeight;
        mRadius = mCenterX = mCenterY = mWidth / 2;
        mRadius -= mStrokeWidth / 2;

        // 绘制三角形，初始时是默认箭头朝上的等腰三角形，绘制结束后，
        // 根据两个GestureLockView决定需要旋转多少度
        float mArrowLength = mWidth / 2 * mArrowRate;
        mArrowPath.moveTo(mWidth / 2, mStrokeWidth + 2);
        mArrowPath.lineTo(mWidth / 2 - mArrowLength, mStrokeWidth + 2 + mArrowLength);
        mArrowPath.lineTo(mWidth / 2 + mArrowLength, mStrokeWidth + 2 + mArrowLength);
        mArrowPath.close();
        mArrowPath.setFillType(Path.FillType.WINDING);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        switch (mCurrentStatus) {
            case STATUS_FINGER_ON:
                // 绘制外圆
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setColor(mColorFingerOn);
                mPaint.setStrokeWidth(2);
                canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
                // 绘制内圆
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(mCenterX, mCenterY, mRadius * mInnerCircleRadiusRate, mPaint);
                break;
            case STATUS_FINGER_UP:
                // 绘制外圆
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setColor(mColorFingerUp);
                mPaint.setStrokeWidth(2);
                canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
                // 绘制内圆
                mPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(mCenterX, mCenterY, mRadius * mInnerCircleRadiusRate, mPaint);
                if (showArrow) {
                    drawArrow(canvas);
                }
                break;
            case STATUS_NO_FINGER:
                // 绘制外圆
//                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setColor(mColorNoFingerOutter);
                mPaint.setStrokeWidth(2);
                canvas.drawCircle(mCenterX, mCenterY, mRadius, mPaint);
                // 绘制内圆
//                mPaint.setStyle(Paint.Style.FILL);
//                mPaint.setColor(mColorNoFingerInner);
//                canvas.drawCircle(mCenterX, mCenterY, mRadius * mInnerCircleRadiusRate, mPaint);
                break;
        }
    }

    /**
     * 绘制箭头
     * @param canvas
     */
    private void drawArrow(Canvas canvas) {
        if (mArrowDegree != -1) {
            mPaint.setStyle(Paint.Style.FILL);
            canvas.save();
            canvas.rotate(mArrowDegree, mCenterX, mCenterY);
            canvas.drawPath(mArrowPath, mPaint);
            canvas.restore();
        }
    }

    /**
     * 设置当前模式并重绘界面
     * @param mode
     */
    public void setMode(Mode mode) {
        this.mCurrentStatus = mode;
        invalidate();
    }

    public void setArrowDegree(int degree) {
        this.mArrowDegree = degree;
    }

    // 该get方法应该用在drawArrow方法中，变量不该直接使用
    public int getArrowDegree() {
        return this.mArrowDegree;
    }

    //用于改变颜色
    public void setViewColor(int color) {
        mColorFingerUp = color;
    }
    private boolean showArrow = true;//默认显示
    // 用于设置是否绘制三角形箭头
    public void setShowArrow(boolean show) {
        this.showArrow = show;
    }
}