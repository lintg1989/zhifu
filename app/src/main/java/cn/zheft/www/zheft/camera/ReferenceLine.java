package cn.zheft.www.zheft.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import cn.zheft.www.zheft.util.CameraUtils;

/**
 * Created by Lin on 2017/4/21.
 */

public class ReferenceLine extends View {

    private Paint mLinePaint;

    public ReferenceLine(Context context) {
        super(context);
        init();
    }

    public ReferenceLine(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ReferenceLine(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(Color.parseColor("#45e0e0e0"));
        mLinePaint.setStrokeWidth(1);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        int screenWidth = CameraUtils.getScreenWH(getContext()).widthPixels;
        int screenHeight = CameraUtils.getScreenWH(getContext()).heightPixels;

        int width = screenWidth/3;
        int height = screenHeight/3;

        for (int i = width, j = 0;i < screenWidth && j<2;i += width, j++) {
            canvas.drawLine(i, 0, i, screenHeight, mLinePaint);
        }
        for (int j = height,i = 0;j < screenHeight && i < 2;j += height,i++) {
            canvas.drawLine(0, j, screenWidth, j, mLinePaint);
        }
    }
}
