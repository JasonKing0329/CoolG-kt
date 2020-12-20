package com.king.app.coolg_kt.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.king.app.coolg_kt.R;
import com.king.app.coolg_kt.utils.ScreenUtils;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2018/5/7 19:37
 */
public class StarRatingView extends LinearLayout implements View.OnTouchListener {

    private int starSize;
    private int starPadding;
    private int starSpace;
    private int starColor;
    private int starNumber;
    private float checkNumber;
    private boolean supportHalf;

    private ImageView[] stars;

    public OnStarChangeListener onStarChangeListener;

    public StarRatingView(Context context) {
        super(context);
        init(null);
    }

    public StarRatingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs == null) {
            starSize = ScreenUtils.dp2px(24);
            starPadding = ScreenUtils.dp2px(3);
            starSpace = ScreenUtils.dp2px(4);
            starNumber = 5;
            checkNumber = 5;
            starColor = Color.YELLOW;
            supportHalf = false;
        }
        else {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.StarRatingView);
            starSize = a.getDimensionPixelSize(R.styleable.StarRatingView_starSize, ScreenUtils.dp2px(24));
            starPadding = a.getDimensionPixelSize(R.styleable.StarRatingView_starPadding, ScreenUtils.dp2px(3));
            starSpace = a.getDimensionPixelSize(R.styleable.StarRatingView_starSpace, ScreenUtils.dp2px(4));
            starNumber = a.getInteger(R.styleable.StarRatingView_starNumber, 5);
            checkNumber = a.getFloat(R.styleable.StarRatingView_checkNumber, 0);
            starColor = a.getColor(R.styleable.StarRatingView_starColor, Color.YELLOW);
            supportHalf = a.getBoolean(R.styleable.StarRatingView_supportHalf, false);
        }
        createView();
        setOnTouchListener(this);
    }

    private void createView() {
        stars = new ImageView[starNumber];
        for (int i = 0; i < starNumber; i ++) {
            stars[i] = createStar(i);
            addView(stars[i]);
        }
    }

    private ImageView createStar(int index) {
        ImageView view = new ImageView(getContext());
        LayoutParams params = new LayoutParams(starSize, starSize);
        view.setPadding(starPadding, starPadding, starPadding, starPadding);
        if (index > 0 && starSpace > 0) {
            params.leftMargin = starSpace;
        }
        view.setLayoutParams(params);
        if (checkNumber > index) {
            // half
            if (supportHalf && checkNumber < index + 1) {
                view.setImageResource(R.drawable.ic_star_half_black_24dp);
            }
            else {
                view.setImageResource(R.drawable.ic_star_black_24dp);
            }
        }
        else {
            view.setImageResource(R.drawable.ic_star_border_black_24dp);
        }
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        updateIconColor(view, starColor);
        return view;
    }

    private void updateIconColor(ImageView icon, int color) {
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0x00ff00) >> 8;
        int blue = (color & 0x0000ff);
        icon.setColorFilter(Color.argb(255, red, green, blue));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                int left = 0;
                for (int i = 0; i < starNumber; i ++) {
                    int right = left + starSize;
                    int half = left + starSize / 2;
                    if (i > 0) {
                        right += starSpace;
                        half += starSpace;
                    }
                    if (x > left && x <= right) {
                        float number;
                        if (supportHalf && x < half) {
                            number = i + 0.5f;
                        }
                        else {
                            number = i + 1f;
                        }
                        if (number != checkNumber) {
                            changeCheckNumber(number);
                            checkNumber = number;
                        }
                        break;
                    }

                    left = right;
                }
                break;
            case MotionEvent.ACTION_OUTSIDE:
                if (checkNumber <= 1) {
                    changeCheckNumber(0);
                    checkNumber = 0;
                }
                else if (checkNumber >= starNumber - 1) {
                    changeCheckNumber(starNumber);
                    checkNumber = starNumber;
                }
                break;
        }
        return true;
    }

    public void setStarColor(int starColor) {
        this.starColor = starColor;
        for (int i = 0; i < stars.length; i ++) {
            updateIconColor(stars[i], starColor);
        }
    }

    public void setCheckNumber(float checkNumber) {
        this.checkNumber = checkNumber;
        changeCheckNumber(checkNumber);
    }

    private void changeCheckNumber(float number) {
        for (int i = 0; i < stars.length; i ++) {
            if (i < number) {
                if (i == (int) number) {
                    stars[i].setImageResource(R.drawable.ic_star_half_black_24dp);
                }
                else {
                    stars[i].setImageResource(R.drawable.ic_star_black_24dp);
                }
            }
            else {
                stars[i].setImageResource(R.drawable.ic_star_border_black_24dp);
            }
            updateIconColor(stars[i], starColor);
        }

        if (onStarChangeListener != null) {
            onStarChangeListener.onStarChanged(this, number);
        }
    }

    public void setOnStarChangeListener(OnStarChangeListener onStarChangeListener) {
        this.onStarChangeListener = onStarChangeListener;
    }

    public interface OnStarChangeListener {
        void onStarChanged(StarRatingView view, float checkedStar);
    }
}
