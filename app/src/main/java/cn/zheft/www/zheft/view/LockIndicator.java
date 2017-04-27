package cn.zheft.www.zheft.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import cn.zheft.www.zheft.R;

/**
 * Created by Administrator on 2016/4/28 0028.
 *
 *
 * 手势密码图案提示
 * @author wulianghuan
 *
 */
public class LockIndicator extends View {
    private int numRow = 3;	// 行
    private int numColum = 3; // 列
    private int patternWidth = 40;
    private int patternHeight = 40;
    private int f = 5;
    private int g = 5;
    private int strokeWidth = 3;
    private Paint paint = null;
    private Drawable patternNoraml = null;
    private Drawable patternPressed = null;
    private String lockPassStr; // 手势密码

    public LockIndicator(Context paramContext) {
        super(paramContext);
    }

    public LockIndicator(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet, 0);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.STROKE);
        patternNoraml = getResources().getDrawable(R.mipmap.pattern_lock_dot_nor);
        patternPressed = getResources().getDrawable(R.mipmap.pattern_lock_dot_sel);
        if (patternPressed != null) {
            patternWidth = patternPressed.getIntrinsicWidth();
            patternHeight = patternPressed.getIntrinsicHeight();
            this.f = (patternWidth / 4);
            this.g = (patternHeight / 4);
            patternPressed.setBounds(0, 0, patternWidth, patternHeight);
            patternNoraml.setBounds(0, 0, patternWidth, patternHeight);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if ((patternPressed == null) || (patternNoraml == null)) {
            return;
        }
        // 绘制3*3的图标
        for (int i = 0; i < numRow; i++) {
            for (int j = 0; j < numColum; j++) {
                paint.setColor(getResources().getColor(R.color.colorAccent));
                int i1 = j * patternHeight + j * this.g;
                int i2 = i * patternWidth + i * this.f;
                canvas.save();
                canvas.translate(i1, i2);
                String curNum = String.valueOf(numColum * i + (j + 1));
                if (!TextUtils.isEmpty(lockPassStr)) {
                    if (lockPassStr.indexOf(curNum) == -1) {
                        // 未选中
                        patternNoraml.draw(canvas);
                    } else {
                        // 被选中
                        patternPressed.draw(canvas);
                    }
                } else {
                    // 重置状态
                    patternNoraml.draw(canvas);
                }
                canvas.restore();
            }
        }
    }

    @Override
    protected void onMeasure(int paramInt1, int paramInt2) {
        if (patternPressed != null)
            setMeasuredDimension(numColum * patternHeight + this.g
                    * (-1 + numColum), numRow * patternWidth + this.f
                    * (-1 + numRow));
    }

    /**
     * 请求重新绘制
     * @param paramString 手势密码字符序列
     */
    public void setPath(String paramString) {
        lockPassStr = paramString;
        invalidate();
    }

}