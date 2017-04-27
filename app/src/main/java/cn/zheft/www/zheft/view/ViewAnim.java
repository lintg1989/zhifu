package cn.zheft.www.zheft.view;

import android.animation.ObjectAnimator;
import android.view.View;

/**
 * 遮罩
 */

public class ViewAnim {

    public static void hide(final View view, final long duration) {

        if (view != null && view.getVisibility() == View.VISIBLE) {

//            view.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.0f).setDuration( duration ).start();
//                }
//            }, 100);

            view.post(new Runnable() {
                @Override
                public void run() {
                    ObjectAnimator.ofFloat(view, "alpha", 1.0f, 0.0f).setDuration( duration ).start();
                }
            });

        }

    }

}
