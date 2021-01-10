package com.king.app.coolg_kt.utils;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/8/8 9:41
 */
public class RippleUtil {

    /**
     * create ripple drawable which include normal background and ripple color
     * ripple will fill view's visible region except normalColor's alpha is 0
     *
     * @param normalColor if its alpha is 0, the ripple effect will be borderless
     * @param rippleColor
     *            useless, ripple will fill view's visible region no matter it
     *            was true or false
     * @return
     */
    public static Drawable getRippleBackground(final int normalColor,
                                               final int rippleColor/* , final boolean clip */) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final ColorStateList rippleStateList = new ColorStateList(
                    new int[][] { new int[] { android.R.attr.state_pressed },
                            new int[0] },
                    new int[] { rippleColor, rippleColor });
            final int alpha = Color.alpha(normalColor);
            final Drawable content = alpha > 0 ? new ColorDrawable(normalColor)
                    : null;
            // final Drawable mask = clip ? new ShapeDrawable(new RectShape())
            // : null;
            // return new RippleDrawable(rippleStateList, content, mask);
            return new RippleDrawable(rippleStateList, content, null);
        } else {
            final StateListDrawable backgroundDrawable = new StateListDrawable();
            backgroundDrawable.addState(
                    new int[] { android.R.attr.state_pressed },
                    new ColorDrawable(rippleColor));
            backgroundDrawable.addState(new int[] {}, new ColorDrawable(
                    normalColor));
            return backgroundDrawable;
        }
    }

    /**
     * create ripple drawable which include normal background and ripple color
     * ripple will fill view's visible region
     * @param normalColor
     * @param rippleColor
     * @param clip if false and normalColor's alpha is 0, ripple effect will be borderless
     * @return
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Drawable getRippleBackground(final int normalColor,
                                               final int rippleColor, final boolean clip) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final ColorStateList rippleStateList = new ColorStateList(
                    new int[][] { new int[] { android.R.attr.state_pressed },
                            new int[0] },
                    new int[] { rippleColor, rippleColor });
            final int alpha = Color.alpha(normalColor);
            final Drawable content = alpha > 0 ? new ColorDrawable(normalColor)
                    : null;
            final Drawable mask = clip ? new ShapeDrawable(new RectShape())
                    : null;
            return new RippleDrawable(rippleStateList, content, mask);
//			return new RippleDrawable(rippleStateList, content, null);
        } else {
            final StateListDrawable backgroundDrawable = new StateListDrawable();
            backgroundDrawable.addState(
                    new int[] { android.R.attr.state_pressed },
                    new ColorDrawable(rippleColor));
            backgroundDrawable.addState(new int[] {}, new ColorDrawable(
                    normalColor));
            return backgroundDrawable;
        }
    }

    /**
     * create borderless effect ripple drawable
     * borderless ripple couldn't have normal status color
     * @param color
     * @return
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Drawable getBorderlessRippleBackground(int color) {
        ColorStateList rippleStateList = new ColorStateList(new int[][] {
                new int[] { android.R.attr.state_pressed }, new int[0] },
                new int[] { color, color });
        return new RippleDrawable(rippleStateList, null, new ShapeDrawable(new OvalShape()));
    }

    /**
     * 获取带正常颜色、按压时颜色，可设置conner radius的drawable
     * @param normalColor
     * @param selectedColor
     * @param radius
     * @return
     */
    public static Drawable getPressableRoundDrawable(int normalColor, int selectedColor, int radius) {
        GradientDrawable normal = new GradientDrawable();
        normal.setColor(normalColor);
        normal.setCornerRadius(radius);
        GradientDrawable press = new GradientDrawable();
        press.setColor(selectedColor);
        press.setCornerRadius(radius);
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[] { android.R.attr.state_pressed }, press);
        drawable.addState(new int[] {}, normal);
        return drawable;
    }
}
