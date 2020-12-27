package com.king.app.coolg_kt.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.king.app.coolg_kt.R;
import com.king.app.coolg_kt.utils.ScreenUtils;
import com.king.app.coolg_kt.view.widget.rc.RCRelativeLayout;

/**
 * Desc:
 *
 * @author：Jing Yang
 * @date: 2019/2/25 9:29
 */
public class CoverView extends RCRelativeLayout {

    private float textSize;

    private int textColor;

    private int textBackgroundColor;

    private int coverSrc;

    private int coverRadius;

    private String coverText;

    private ImageView imageView;

    private TextView coverTextView;

    public CoverView(Context context) {
        super(context);
        init(null);
    }

    public CoverView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public CoverView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CoverView);
        textSize = a.getDimension(R.styleable.CoverView_coverTextSize, ScreenUtils.dp2px(16));
        textColor = a.getColor(R.styleable.CoverView_coverTextColor, Color.WHITE);
        textBackgroundColor = a.getColor(R.styleable.CoverView_coverTextBackgroundColor, Color.parseColor("#66000000"));
        coverRadius = a.getDimensionPixelSize(R.styleable.CoverView_coverRadius, ScreenUtils.dp2px(8));
        coverSrc = a.getResourceId(R.styleable.CoverView_coverSrc, 0);
        coverText = a.getString(R.styleable.CoverView_coverText);

        setCoverRadius(coverRadius);
        inflateGroup();
    }

    private void inflateGroup() {
        imageView = new ImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        setCoverSrc(coverSrc);
        RelativeLayout.LayoutParams rParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        addView(imageView, rParams);

        coverTextView = new TextView(getContext());
        coverTextView.setGravity(Gravity.CENTER);
        setTextSize(textSize);
        setTextColor(textColor);
        setTextBackgroundColor(textBackgroundColor);
        setCoverText(coverText);
        rParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        addView(coverTextView, rParams);
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        coverTextView.setTextColor(textColor);
    }

    public void setTextSize(float textSize) {
        this.textSize = textSize;
        coverTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }

    public void setTextSize(int unit, float textSize) {
        this.textSize = textSize;
        coverTextView.setTextSize(unit, textSize);
    }

    public void setTextBackgroundColor(int textBackgroundColor) {
        this.textBackgroundColor = textBackgroundColor;
        coverTextView.setBackgroundColor(textBackgroundColor);
    }

    public void setCoverText(String coverText) {
        this.coverText = coverText;
        coverTextView.setText(coverText);
    }

    public void setCoverRadius(int coverRadius) {
        this.coverRadius = coverRadius;
        setRadius(coverRadius);
    }

    public void setCoverSrc(int coverSrc) {
        this.coverSrc = coverSrc;
        if (coverSrc != 0) {
            imageView.setImageResource(coverSrc);
        }
    }
}
