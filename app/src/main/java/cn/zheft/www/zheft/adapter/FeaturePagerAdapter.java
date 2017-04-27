package cn.zheft.www.zheft.adapter;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Liaoliang on 2016/4/23.
 * 用于新特性页面的适配器
 * Thank for 'IT_xiao小巫'
 * http://blog.csdn.net/wwj_748/article/details/50571515
 */
public class FeaturePagerAdapter extends PagerAdapter {
    private List<View> views;

    public FeaturePagerAdapter(List<View> views) {
//        super();
        this.views = views;
    }

    @Override
    public int getCount() {
        if (views != null) {
            return views.size();
        }
        return 0;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(views.get(position));
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(views.get(position),0);
        return views.get(position);
    }
}
