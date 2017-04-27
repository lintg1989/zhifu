package cn.zheft.www.zheft.view;

import android.widget.TextView;

import java.util.LinkedList;
import java.util.Random;

import cn.zheft.www.zheft.util.StringUtil;

/**
 * 数字滚动效果
 *
 * Thank for CSDN:liaoinstan
 * android开发游记：仿支付宝余额数字累加滚动效果的实现
 * http://blog.csdn.net/liaoinstan/article/details/50521775
 */

public class NumScrollAnim {

    //每秒刷新多少次
    private static final int COUNTPERS = 100;

    public static void startAnim(TextView textV, int num) {
        float floatNum = num / 100.0f;
        startAnim(textV, floatNum, 500);
    }

    public static void startAnim(TextView textV, float num) {
        startAnim(textV, num, 500);
    }

    public static void startAnim(TextView textV, float num, long time) {
        if (num == 0) {
            textV.setText(NumberFormat(num,2));
            return;
        }

        Float[] nums = splitNum(num, (int)((time/1000f)*COUNTPERS));
//        System.out.println("size:" + nums.length);

        Counter counter = new Counter(textV, nums, time);

        textV.removeCallbacks(counter);
        textV.post(counter);
    }

    private static Float[] splitnum(float num, int count) {
        Random random = new Random();
        float numtemp = num;
        float sum = 0;
        LinkedList<Float> nums = new LinkedList<>();
        nums.add(0f);
        while (true) {

            float nextFloat = NumberFormatFloat( (random.nextFloat()*num*2f)/(float)count, 2);
            if (nextFloat < 0.01f) {
                nextFloat = 0.01f;
            }
//            System.out.println("next:" + nextFloat);

            if (numtemp - nextFloat >= 0) {
                sum = NumberFormatFloat(sum + nextFloat, 2);
                nums.add(sum);
                numtemp -= nextFloat;
            } else {
                nums.add(num);
                return nums.toArray(new Float[0]);
            }
        }
    }

    private static Float[] splitNum(float num, int count) {
        Random random = new Random(System.currentTimeMillis());
        float sum = 0.0f;
        float unit = num / count;
        if ( NumberFormatFloat(unit, 2) <= 0.0f) {
            unit = 0.01f;  // 如果因子太小，就置为0.01（应用在金额上的简化处理）
        }
        unit = NumberFormatFloat(unit, 2);// 保留两位小数
        LinkedList<Float> nums = new LinkedList<>();
        nums.add(0.0f);  // 数组头

        for (int i = 1; i < count; i++) {
            float deviation = random.nextInt(200) / 100.0f;  // 系数（0~2）
            sum += unit * deviation;
//            System.out.println("next:" + sum);
            if (sum >= num) {
                break;
            } else {
                nums.add(sum);
            }
        }

        nums.add(num);  // 数组尾
        return nums.toArray(new Float[0]);
    }

    static class Counter implements Runnable {

        private final TextView view;
        private Float[] nums;
        private long pertime;

        private int i = 0;

        Counter(TextView view, Float[] nums, long time) {
            this.view = view;
            this.nums = nums;
            this.pertime = time/nums.length;
        }

        @Override
        public void run() {
            if (i>nums.length-1) {
                view.removeCallbacks(Counter.this);
                return;
            }
            view.setText( StringUtil.addNumberComma( NumberFormat(nums[i++],2) ) );
            view.removeCallbacks(Counter.this);
            view.postDelayed(Counter.this, pertime);
        }
    }

    private static String NumberFormat(float f,int m){
        return String.format("%."+m+"f",f);
    }

    private static float NumberFormatFloat(float f,int m){
        String strfloat = NumberFormat(f,m);
        return Float.parseFloat(strfloat);
    }

}
