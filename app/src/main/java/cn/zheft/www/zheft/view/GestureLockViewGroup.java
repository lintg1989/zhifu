package cn.zheft.www.zheft.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.util.LogUtil;

/**
 * Created by Administrator on 2016/4/28 0028.
 * 手势密码容器组
 * Thank for Zhang Hongyang
 * http://blog.csdn.net/lmj623565791/article/details/36236113
 *
 * 整体包含n*n个GestureLockView,每个GestureLockView间间隔mMarginBetweenLockView
 * 最外层的GestureLockView与容器存在mMarginBetweenLockView的外边距
 * 关于GestureLockView的边长（n*n）：
 * n * mGestureLockViewWidth + ( n + 1 ) * mMarginBetweenLockView = mWidth ;
 * 得：mGestureLockViewWidth = 4 * mWidth / ( 5 * mCount + 1 )
 * 注：mMarginBetweenLockView = mGestureLockViewWidth * 0.25 ;
 * 上上式中4和5由上式1/0.25比例得来，因此要改圆圈大小，只改0.25会出问题，原因就在这里
 * 新式：mGestureLockViewWidth = marginRate * mWidth / ( (marginRate + 1) * mCount + 1 )
 * 新添 marginRate(圆圈/间距)(原式中的4)
 */

public class GestureLockViewGroup extends RelativeLayout {
    private static final String TAG = "GestureLockViewGroup";

    //值为false时不允许进行界面点击操作
    private boolean canClick = true;
    private boolean showPath = true;//显示轨迹

    //保存所有的GestureLockView
    private GestureLockView[] mGestureLockViews;
    //每个边上的GestureLockView的个数
    private int mCount = 3;
    //存储答案（为了方便这里改成ArrayList）
    private List<Integer> mAnswer = new ArrayList<>();
//    private int[] mAnswer = { 0, 1, 2, 5, 8 };
    //保存用户选中的GestureLockView的id
    private List<Integer> mChoose = new ArrayList<>();

    private Paint mPaint;

    // 圈/间距
    private int marginRate = 2;

    //每个GestureLockView中间的间距，设为：mGestureLockViewWidth / marginRate
    private int mMarginBetweenLockView;

    // GestureLockView的边长 marginRate * mWidth / ( (marginRate + 1) * mCount + 1 )
    private int mGestureLockViewWidth;

    // 无手指触摸的状态下内圆的颜色
    private int mNoFingerInnerCircleColor = 0xff3595ff;//0xFF939090;
    // 无手指触摸的状态下外圆的颜色
    private int mNoFingerOuterCircleColor = 0xff3595ff;//0xFFE0DBDB;
    // 手指触摸的状态下内圆和外圆的颜色
    private int mFingerOnColor = 0xff3595ff;
    // 手指抬起的状态下内圆和外圆的颜色
    private int mFingerUpColor = 0xff3595ff;
    // 错误状态下的颜色
    private int mFingerWrongColor = 0xffff4444;

    // 宽度
    private int mWidth;
    // 高度
    private int mHeight;

    private Path mPath;

    // 指引线的开始位置x
    private int mLastPathX;
    // 指引线的开始位置y
    private int mLastPathY;
    // 指引线的结束位置
    private Point mTmpTarget = new Point();

    // 最大尝试次数
    private int mTryTimes = 5;

    // 是否初次设置密码
    private boolean firstSet = false;

    // 回调接口
    private OnGestureLockViewListener mOnGestureLockViewListener;

    public GestureLockViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GestureLockViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        // 获得所有自定义的参数的值
        TypedArray array = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.GestureLockViewGroup, defStyle, 0);
        int n = array.getIndexCount();

        for (int i = 0; i < n; i++) {
            int attr = array.getIndex(i);
            switch (attr) {
                case R.styleable.GestureLockViewGroup_color_no_finger_inner_circle:
                    mNoFingerInnerCircleColor = array.getColor(attr, mNoFingerInnerCircleColor);
                    break;
                case R.styleable.GestureLockViewGroup_color_no_finger_outer_circle:
                    mNoFingerOuterCircleColor = array.getColor(attr, mNoFingerOuterCircleColor);
                    break;
                case R.styleable.GestureLockViewGroup_color_finger_on:
                    mFingerOnColor = array.getColor(attr, mFingerOnColor);
                    break;
                case R.styleable.GestureLockViewGroup_color_finger_up:
                    mFingerUpColor = array.getColor(attr, mFingerUpColor);
                    break;
                case R.styleable.GestureLockViewGroup_count:
                    mCount = array.getInt(attr, 3);
                    break;
                case R.styleable.GestureLockViewGroup_tryTimes:
                    mTryTimes = array.getInt(attr, 5);
                default:
                    break;
            }
        }

        array.recycle();

        // 初始化画笔
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
        // mPaint.setStrokeWidth(20);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        // mPaint.setColor(Color.parseColor("#aaffffff"));
        mPaint.setAlpha(255);
        mPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);

        mHeight = mWidth = mWidth < mHeight ? mWidth : mHeight;
        // setMeasuredDimension(mWidth, mHeight);

        // 初始化mGestureLockViews
        if (mGestureLockViews == null) {
            mGestureLockViews = new GestureLockView[mCount * mCount];
            // 计算每个GestureLockView的宽度
            mGestureLockViewWidth = (int) (marginRate * mWidth * 1.0f / ((marginRate + 1) * mCount + 1));
            // 计算每个GestureLockView的间距
//            mMarginBetweenLockView = (int) (mGestureLockViewWidth * 0.25);
            mMarginBetweenLockView = mGestureLockViewWidth / marginRate;
            // 设置画笔的宽度为GestureLockView的内圆直径稍微小点（不喜欢的话，随便设）
            mPaint.setStrokeWidth(mGestureLockViewWidth * 0.01f);

            for (int i = 0; i < mGestureLockViews.length; i++) {
                // 初始化每个GestureLockView
                mGestureLockViews[i] = new GestureLockView(getContext(),
                        mNoFingerInnerCircleColor, mNoFingerOuterCircleColor,
                        mFingerOnColor, mFingerUpColor);
                mGestureLockViews[i].setId(i + 1);
                // 设置参数，主要是定位GestureLockView间的位置
                LayoutParams lockerParams = new LayoutParams(
                        mGestureLockViewWidth, mGestureLockViewWidth);

                // 不是每行的第一个，则设置位置为前一个的右边
                if (i % mCount != 0) {
                    lockerParams.addRule(RelativeLayout.RIGHT_OF,
                            mGestureLockViews[i - 1].getId());
                }
                // 从第二行开始，设置为上一行同一位置View的下面
                if (i > mCount - 1) {
                    lockerParams.addRule(RelativeLayout.BELOW,
                            mGestureLockViews[i - mCount].getId());
                }
                // 设置右下左上的边距
                int rightMargin = mMarginBetweenLockView;
                int bottomMargin = mMarginBetweenLockView;
                int leftMagin = 0;
                int topMargin = 0;

                // 每个View都有右外边距和底外边距 第一行的有上外边距 第一列的有左外边距
                // 第一行
                if (i >= 0 && i < mCount) {
                    topMargin = mMarginBetweenLockView;
                }
                // 第一列
                if (i % mCount == 0) {
                    leftMagin = mMarginBetweenLockView;
                }

                lockerParams.setMargins(leftMagin, topMargin, rightMargin, bottomMargin);
                mGestureLockViews[i].setMode(GestureLockView.Mode.STATUS_NO_FINGER);//这里有点问题？
                addView(mGestureLockViews[i], lockerParams);

                //LogUtil.e(TAG, "mWidth = " + mWidth + " ,  mGestureViewWidth = " + mGestureLockViewWidth + " , mMarginBetweenLockView = " + mMarginBetweenLockView);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
//                LogUtil.e(TAG, "-------------Down------------");
                mPaint.setColor(mFingerOnColor);
                mPaint.setAlpha(255);
                GestureLockView child = getChildIdByPos(x, y);
                if (!canClick || child == null) {
                    // 不能点击时，以及没点中图案点时直接返回
                    return false;
                } else {
                    int cId = child.getId();
                    if (!mChoose.contains(cId)) {
                        mChoose.add(cId);
                        child.setMode(GestureLockView.Mode.STATUS_FINGER_ON);
                        if (mOnGestureLockViewListener != null) {
                            mOnGestureLockViewListener.onBlockSelected(cId);
                        }
                        // 设置指引线的起点
                        mLastPathX = child.getLeft() / 2 + child.getRight() / 2;
                        mLastPathY = child.getTop() / 2 + child.getBottom() / 2;

                        // 当前添加为第一个
//                        LogUtil.e(TAG,"Add_First");
//                        if (showPath) {
//                            LogUtil.e("ShowPath","MoveTo");
//
//                        }
                        mPath.moveTo(mLastPathX, mLastPathY);
                    }
                }
                // 有滑动接着画，没滑动直接进入up事件
            case MotionEvent.ACTION_MOVE:
//                LogUtil.e(TAG,"-------------Move------------");
                child = getChildIdByPos(x, y);
                if (child != null) {
                    int cId = child.getId();
                    if (!mChoose.contains(cId)) {
                        // 在这里加一个方法，判断是否经过某个值，例如1、3经过2，把2也加入
                        int subId = checkChoose(cId);
                        if (subId != 0) {
                            mChoose.add(subId);
                            GestureLockView subChild = mGestureLockViews[subId - 1];
                            subChild.setMode(GestureLockView.Mode.STATUS_FINGER_ON);
                            if (mOnGestureLockViewListener != null) {
                                mOnGestureLockViewListener.onBlockSelected(cId);
                            }
                            // 设置指引线的起点
                            mLastPathX = subChild.getLeft() / 2 + subChild.getRight() / 2;
                            mLastPathY = subChild.getTop() / 2 + subChild.getBottom() / 2;

                            // 非第一个，将两者使用线连上
                            mPath.lineTo(mLastPathX, mLastPathY);
                        }
                        // 中间点加入完成，继续添加当前选择的点
                        mChoose.add(cId);
                        child.setMode(GestureLockView.Mode.STATUS_FINGER_ON);
                        if (mOnGestureLockViewListener != null) {
                            mOnGestureLockViewListener.onBlockSelected(cId);
                        }
                        // 设置指引线的起点
                        mLastPathX = child.getLeft() / 2 + child.getRight() / 2;
                        mLastPathY = child.getTop() / 2 + child.getBottom() / 2;

                        // 非第一个，将两者使用线连上
                        mPath.lineTo(mLastPathX, mLastPathY);
                    }
                } else {
//                    LogUtil.e(TAG,"-----------MoveEnd-----------");
                }
                // 指引线的终点

                mTmpTarget.x = x;
                mTmpTarget.y = y;
                break;
            case MotionEvent.ACTION_UP:
//                LogUtil.e(TAG, "--------------Up-------------");
                // 回调是否成功
                if (mOnGestureLockViewListener != null && mChoose.size() > 0) {
                    //如果是初次设置图案，不需要checkAnswer()，但需要setAnswer()
                    if (firstSet) {
                        if (mChoose.size() < 4) {
                            //图案不合法，画为红色
                            setViewColor(false);
                            mOnGestureLockViewListener.onFirstSetPattern(false);
                        } else {
                            //图案合法，画为蓝色
                            setViewColor(true);
                            setAnswer(mChoose);
                            mOnGestureLockViewListener.onFirstSetPattern(true);
                        }
                    } else {
                        //答案不对时改用红色画
                        if (!checkAnswer()) {
                            setViewColor(false);
                            this.mTryTimes--;
                        } else {
                            setViewColor(true);
                        }
                        mOnGestureLockViewListener.onGestureEvent(checkAnswer());
                        if (this.mTryTimes == 0) {
                            // 剩余次数为0时进行一些处理（在使用该手势密码的activity中覆写）
                            mOnGestureLockViewListener.onUnmatchedExceedBoundary();
                        }
                    }
                }

//                LogUtil.e(TAG, "mUnMatchExceedBoundary = " + mTryTimes);
//                LogUtil.e(TAG, "mChoose = " + mChoose);
//                LogUtil.e(TAG, "mAnswer = " + mAnswer);
                // 将终点设置位置为起点，即取消指引线
                mTmpTarget.x = mLastPathX;
                mTmpTarget.y = mLastPathY;

                // 改变子元素的状态（根据对错的boolean值）
                changeItemMode();
                // 计算每个元素中箭头需要旋转的角度
                for (int i = 0; i + 1 < mChoose.size(); i++) {
//                    LogUtil.e("CacluateArrowDegree","NowNum"+i);
                    int childId = mChoose.get(i);
                    int nextChildId = mChoose.get(i + 1);

                    GestureLockView startChild = (GestureLockView) findViewById(childId);
                    GestureLockView nextChild = (GestureLockView) findViewById(nextChildId);

                    int dx = nextChild.getLeft() - startChild.getLeft();
                    int dy = nextChild.getTop() - startChild.getTop();
                    // 计算角度
                    int angle = (int) Math.toDegrees(Math.atan2(dy, dx)) + 90;
                    startChild.setArrowDegree(angle);
                }
                break;
        }
        invalidate();
        return true;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (!showPath) {
            return;
        }
        // 绘制GestureLockView间的连线
        if (mPath != null) {
//            LogUtil.e("DrawPath","");
            canvas.drawPath(mPath, mPaint);
        }
        // 绘制指引线
        if (mChoose.size() > 0) {
            if (mLastPathX != 0 && mLastPathY != 0) {
                canvas.drawLine(mLastPathX, mLastPathY, mTmpTarget.x, mTmpTarget.y, mPaint);
            }
        }
    }

    private void changeItemMode() {
        for (GestureLockView gestureLockView : mGestureLockViews) {
            if (mChoose.contains(gestureLockView.getId())) {
                gestureLockView.setShowArrow(showPath);//设置是否显示轨迹（包括路径和箭头）
                gestureLockView.setViewColor(mFingerUpColor);
                gestureLockView.setMode(GestureLockView.Mode.STATUS_FINGER_UP);
//                LogUtil.e("CgMode","NowUp:"+gestureLockView.getId());
            }
        }
   }

    //必要的重置
    public void reset() {
        final int resetTime = getResources().getInteger(R.integer.gesture_lock_reset_time);
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                canClick = false;
                try {
                    Thread.sleep(resetTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return 0;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                // 重设的部分
                mChoose.clear();
                mPath.reset();
                for (GestureLockView gestureLockView : mGestureLockViews) {
                    gestureLockView.setMode(GestureLockView.Mode.STATUS_NO_FINGER);
                    gestureLockView.setArrowDegree(-1);
                }
                invalidate();
                canClick = true;
                // 重设的部分
            }
        }.execute();
    }

    // 检查用户绘制的 手势是否正确
    private boolean checkAnswer() {
        return mChoose.equals(mAnswer);
    }

    // 检查当前坐标是否在child中
    private boolean checkPositionInChild(View child, int x, int y) {
        // 设置了内边距，即x,y必须落入下GestureLockView的内部中间的小区域中
        // 可以通过调整padding使得x,y落入范围减小，或者不设置padding
        int padding = (int) (mGestureLockViewWidth * 0.15);

        return (x >= child.getLeft() + padding) && (x <= child.getRight() - padding)
                && (y >= child.getTop() + padding) && (y <= child.getBottom() - padding);
    }

    // 通过x,y获得落入的GestureLockView
    private GestureLockView getChildIdByPos(int x, int y) {
        for (GestureLockView gestureLockView : mGestureLockViews) {
            if (checkPositionInChild(gestureLockView, x, y)) {
                return gestureLockView;
            }
        }
        return null;
    }

    /**
     * 设置回调接口
     * @param listener
     */
    public void setOnGestureLockViewListener(OnGestureLockViewListener listener) {
        this.mOnGestureLockViewListener = listener;
    }

    /**
     * 对外公布设置答案的方法
     * @param answer
     */
    public void setAnswer(List<Integer> answer) {
        this.mAnswer.clear();
//        LogUtil.e(TAG, "setAnsr = " + answer);
        for (int i = 0; i < answer.size(); i++) {
            this.mAnswer.add(answer.get(i));
        }
    }

    // 外部可用该方法设置答案
    public void setAnswer(String answer) {
        List<Integer> answerList = new ArrayList<>();
        for (int i = 0; i < answer.length(); i++ ) {
            answerList.add((int) answer.charAt(i) - 48);
        }
        setAnswer(answerList);
    }

    public int getTryTimes() {
        return mTryTimes;// 返回剩余次数
    }

    public List<Integer> getChoose() {
        return mChoose;//不能返回mAnswer，因为设置有误时mAnswer为空
    }

    /**
     * 设置最大实验次数
     * @param boundary
     */
    public void setUnMatchExceedBoundary(int boundary) {
        this.mTryTimes = boundary;
    }


    // listener
    public interface OnGestureLockViewListener {
        // 单独选中的元素的id
        void onBlockSelected(int cId);

        // 是否匹配
        void onGestureEvent(boolean matched);

        // 超过尝试次数
        void onUnmatchedExceedBoundary();

        // 首次设置密码成功
        void onFirstSetPattern(boolean patternOk);
    }

    // 是否是初次设置密码，该参数会决定 ACTION_UP 中是否检查答案
    public void setPattern(boolean firstSet) {
        this.firstSet = firstSet;
    }

    // 是否隐藏路径
    public void setShowPath(boolean showPath) {
        this.showPath = showPath;
    }

    // 传入true，设为蓝色，传入false，设为红色
    private void setViewColor(boolean isOk) {
        if (isOk) {
            mFingerUpColor = mFingerOnColor;
            mPaint.setColor(mFingerUpColor);
            mPaint.setAlpha(255);
        } else {
            mFingerUpColor = mFingerWrongColor;
            mPaint.setColor(mFingerUpColor);
            mPaint.setAlpha(255);
        }
    }

    private int checkChoose(int cId) {
        int lastChoose = mChoose.get(mChoose.size() - 1);

        if (lastChoose == 1 && ( cId == 3||cId == 7||cId == 9 )) {
            return (lastChoose + cId) / 2 ;
        } else if (lastChoose == 3 && ( cId == 1||cId == 7||cId==9 )) {
            return (lastChoose + cId) / 2 ;
        } else if (lastChoose == 7 && ( cId == 1||cId == 3||cId==9 )) {
            return (lastChoose + cId) / 2 ;
        } else if (lastChoose == 9 && ( cId == 1||cId == 3||cId==7 )) {
            return (lastChoose + cId) / 2 ;
        } else if ((lastChoose == 2 && cId == 8) || (lastChoose == 8 && cId == 2)) {
            return (lastChoose + cId) / 2 ;
        } else if ((lastChoose == 4 && cId == 6) || (lastChoose == 6 && cId == 4)) {
            return (lastChoose + cId) / 2 ;
        } else {
            return 0;
        }
    }

}
