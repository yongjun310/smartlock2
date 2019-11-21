package com.smart.lock.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.smart.lock.R;
import com.smart.lock.utils.DisplayUtils;

/**
 * ** @ClassName: gallery下面点点
	* @Description: 用一句话描述这个类的作用
	* @author john
	* @date 2015-5-8 上午9:27:37
 */
public class GalleryPoint extends LinearLayout
{

    public GalleryPoint(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
    }

    public void addSwitchBtn(int size)
    {
        addSwitchBtn(size, R.drawable.gallery_switcherbtn_selector, 12, 12);
    }

    public void addSwitchBtn(int size, int imageId, int width, int height)
    {
        removeAllViews();

        if (size <= 0)
        {
            return;
        }

        ImageView imageView;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(DisplayUtils.dip2px(getContext(), width),
                DisplayUtils.dip2px(getContext(), height));
        params.leftMargin = 24;
        for (int i = 0; i < size; i++)
        {
            imageView = new ImageView(getContext());

            imageView.setLayoutParams(params);
            imageView.setBackgroundResource(imageId);
            // localSwitchBtnClickListener = this.mSwitchClickListener;
            // localImageView.setOnClickListener(localSwitchBtnClickListener);
            imageView.setEnabled(false);
            addView(imageView);
        }
    }

    /** 设置当前点点 */
    public void setSelectedSwitchBtn(int paramInt)
    {
        int count = getChildCount();
        for (int i = 0; i < count; i++)
        {
            getChildAt(i).setEnabled(i == paramInt);
        }
    }
}
