package cn.zheft.www.zheft.ui;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import cn.zheft.www.zheft.R;
import cn.zheft.www.zheft.adapter.FeaturePagerAdapter;
import cn.zheft.www.zheft.app.BaseActivity;
import cn.zheft.www.zheft.app.MyApp;
import cn.zheft.www.zheft.config.Config;
import cn.zheft.www.zheft.util.StringUtil;
import cn.zheft.www.zheft.util.VersionUtil;

/**
 * Created by liaoliang on 16/4/23.
 * 新特性页面
 * Thank for 'IT_xiao小巫'
 * http://blog.csdn.net/wwj_748/article/details/50571515
 *
 * 内存占用太大且无法回收，需要修改成四张图片的形式，手动释放bitmap内存
 */

public class FeatureActivity extends BaseActivity {
    private Context context;
    private ViewPager viewPager;
    private List<View> viewList;
    private Button btnEnter;
    private FeaturePagerAdapter fpAdapter;

    private int screenWidth;     // 屏幕宽度
    private int circleLeft;      // 圆圈左侧坐标

    private float cloudLeftX = -0.04f;// (云X - 圆X)/圆H
    private float cloudLeftY = 0.16f; // (云Y - 圆Y)/圆W
    private float cloudRightX = -0.23f;
    private float cloudRightY = 0.62f;

    private ImageView ivBtn; // 点击进入BTN

    private int animTime = 333;    // 单位：毫秒
    private int textTime = 100;    // 单位：毫秒（文字显示的时间基数）

    //新特性页资源
    private static final int[] views = {
            R.layout.feature_view1,
            R.layout.feature_view2,
            R.layout.feature_view3,
            R.layout.feature_view4 };

    //底部小圆点图片
    private ImageView[] dots;
    //记录当前选中位置
    private int currentIndex;

    // 记录动画执行完毕的状态
    private boolean animFin1 = false;
    private boolean animFin2 = false;
    private boolean animFin3 = false;
    private boolean animFin4 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTransScreen();// 透明状态栏和虚拟键
//        setFullScreen();// 全屏
        setContentView(R.layout.activity_feature);
        disablePatternLock();
        context = this;

        viewList = new ArrayList<>();

        //初始化新特性页面视图列表
        for (int i = 0; i < views.length; i++) {
            View view = LayoutInflater.from(this).inflate(views[i],null);
            //最后一页支持点击跳转事件
            if (i == views.length - 1) {
                view.setTag("enter");
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        enterNext();
                    }
                });
            }
            viewList.add(view);
        }

        viewPager = (ViewPager) findViewById(R.id.viewpager_feature);
        //初始化adapter
        fpAdapter = new FeaturePagerAdapter(viewList);
        viewPager.setAdapter(fpAdapter);
        viewPager.addOnPageChangeListener(new PageChangeListener());
        viewPager.setCurrentItem(0);

        initDots();

//        initScreen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
//        System.exit(0);// 会关闭整个应用
    }

    private void initDots() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.llayout_feature_dots);
        dots = new ImageView[views.length];

        // 循环取得小点图片
        for (int i = 0; i < views.length; i++) {
            // 得到一个LinearLayout下面的每一个子元素
            dots[i] = (ImageView) ll.getChildAt(i);
            dots[i].setEnabled(false);// 都设为灰色
//            dots[i].setOnClickListener(this);
            dots[i].setTag(i);// 设置位置tag，方便取出与当前位置对应
        }

        currentIndex = 0;
        dots[currentIndex].setEnabled(true); // 设置为白色，即选中状态
    }

    private void enterNext() {
        toNextPage(1);// 参数1：是否显示欢迎页
    }

    /**
     * 设置当前view
     * @param position
     */
    private void setCurView(int position) {
        if (position < 0 || position >= views.length) {
            return;
        }
        viewPager.setCurrentItem(position);
    }

    /**
     * 设置当前指示点
     * @param position
     */
    private void setCurDot(int position) {
        if (position < 0 || position > views.length || currentIndex == position) {
            return;
        }
        dots[position].setEnabled(true);
        dots[currentIndex].setEnabled(false);
        currentIndex = position;
    }


    //页面滑动监听
    private class PageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // 当前页面被滑动时调用
            // 参数1 :当前页面，及你点击滑动的页面
            // 参数2 :当前页面偏移的百分比
            // 参数3 :当前页面偏移的像素位置
        }

        @Override
        public void onPageSelected(int position) {
            // 当新的页面被选中时调用
            // 设置底部小点选中状态
            setCurDot(position);
            switch (position) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    initPageFour(viewList.get(3));
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            // 当滑动状态改变时调用
            //参数值： ==1正在滑动， ==2滑动完毕， ==0没有任何操作
//            if (state == 2)  {
//
//            }
        }
    }

//    private void initScreen() {
//        // 获取屏幕宽度
//        Point screenSize = new Point();
//        getWindowManager().getDefaultDisplay().getSize(screenSize);
//        screenWidth = screenSize.x;
//
//        viewPager.post(new Runnable() {
//            @Override
//            public void run() {
//                // 初始化第一个动画
//                initPageOne(viewList.get(0));
//            }
//        });
//    }

    private void initPageFour(View view) {
        if (animFin4) {
            return;
        }
        animFin4 = true;

        ivBtn = (ImageView) view.findViewById(R.id.iv_feature_four_btn);
        ObjectAnimator.ofFloat(ivBtn, "translationY", ivBtn.getBottom(), 0).setDuration(animTime * 2).start();
    }

    // 往下一页
    private void toNextPage(int step) {
        Intent intent = new Intent();
        switch (step) {
            case 0:
                // 1、是否显示新特性
                String currentVer = VersionUtil.getVersionName(context);
                String ignoreVer = Config.getIgnoredVersion();
                if (VersionUtil.compareVersion(currentVer, ignoreVer) > 0) {
                    Config.setIgnoreVersion(currentVer);
                    intent.setClass(context, FeatureActivity.class);
                    break;
                }
            case 1:
                // 2、是否显示欢迎页
                if (Config.getWelcomePic() != null && !StringUtil.nullOrEmpty(Config.getWelcomePic().getState())) {
                    if ("0".equals(Config.getWelcomePic().getState())) {
                        intent.setClass(context, WelcomeActivity.class);
                        break;
                    }
                }
            case 2:
                // 3、是否登录
                MyApp myApp = MyApp.getInstance();
                myApp.setUserInfo(Config.loadUserInfo());
                if (null != myApp.getUserInfo() && null != myApp.getUserInfo().getMobile()) {
                    intent.setClass(context, MainActivity.class);// 进入主页
                } else {
                    intent.setClass(context, LoginActivity.class);// 进入登录页
                }
        }
        startActivity(intent);
        finish();
    }
}
