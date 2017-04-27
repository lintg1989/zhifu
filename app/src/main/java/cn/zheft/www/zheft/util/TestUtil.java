package cn.zheft.www.zheft.util;

import android.util.Log;

import cn.zheft.www.zheft.BuildConfig;

/**
 * 测试用
 */

public class TestUtil {
    private static String TAG = TestUtil.class.getSimpleName();
    private static long timeStart;
    private static long timeEnd;

    public TestUtil() {
    }

    public long timingStart() {
        Log.d(TAG, "TimingStart ------------");
        timeStart = System.currentTimeMillis();
        return timeStart;
    }

    public long timingEnd() {
        timeEnd = System.currentTimeMillis();
        Log.d(TAG, "TimingEnd --------------");
        Log.e(TAG, "Duration: " + (timeEnd - timeStart));
        return timeEnd;
    }

    public void start() {
        if ( !BuildConfig.DEBUG ) {
            return;
        }

        test();
    }

    private void test() {

        timingStart();
        for (int i = 0; i < 100; i++) {
            DateUtil.timeFormat("20160512021000");
            DateUtil.timeFormat("20171205220856");
        }
        timingEnd();

        timingStart();
        for (int i = 0; i < 100; i++) {
            DateUtil.numToLine("20160512021000");
            DateUtil.numToLine("20171205220856");
        }
        timingEnd();

        String test2 = DateUtil.numToLine("2016051202100");
        String test = DateUtil.numToLine("20171205220856");
        Log.d(TAG, test2);
        Log.d(TAG, test);
    }
}
