package com.king.app.coolg_kt.page.video.phone;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.king.app.coolg_kt.R;
import com.king.app.coolg_kt.utils.DebugLog;
import com.king.app.coolg_kt.utils.ScreenUtils;

/**
 * Created by Administrator on 2017/4/2 0002.
 * 主页随着NestedScrollView滚动产生的相应变化事件
 * 采用继承AppBarLayout.ScrollingViewBehavior，扩展变化事件的方法
 * 完全遵循AppBarLayout.ScrollingViewBehavior改变collapse效果的同时，
 * 对dependency和child执行额外的变化效果：
 * 改变toolbar图标以及文字
 */

public class HomeBehavior extends AppBarLayout.ScrollingViewBehavior {

    /**
     * 收起多少高度时，显示ContentScrim的内容
     */
    private int scrimTop;
    private int totalSpace;

    public HomeBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        totalSpace = context.getResources().getDimensionPixelSize(R.dimen.app_bar_height)
            - ScreenUtils.dp2px(48);
        // 为负数
//        scrimTop = context.getResources().getDimensionPixelSize(R.dimen.home_scrim_visible_height)
//            - context.getResources().getDimensionPixelSize(R.dimen.player_basic_head_height);
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
        boolean result = super.layoutDependsOn(parent, child, dependency);
        return result;
    }

    /**
     *
     * @param parent
     * @param child NestedScrollView of layout_content_home
     * @param dependency AppBarLayout of layout_app_bar_home
     * @return
     */
    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
        boolean result = super.onDependentViewChanged(parent, child, dependency);

        // toolbar
        updateToolbar(dependency);

        return result;
    }

    private void updateToolbar(View dependency) {
        DebugLog.e("dependency top=" + dependency.getTop());
        CollapsingToolbarLayout ctl = (CollapsingToolbarLayout) dependency.findViewById(R.id.ctl_toolbar);
        View view = dependency.findViewById(R.id.actionbar);
        view.setBackgroundColor(getColor(dependency.getTop()));
    }

    private int getColor(int viewTop) {
        int alpha = (int) (((float) Math.abs(viewTop) / (float) totalSpace) * 255);
        if (alpha < 135) {
            alpha = 0;
        }
        int color = Color.argb(alpha, 0xff, 0xff, 0xff);
        return color;
    }

}
