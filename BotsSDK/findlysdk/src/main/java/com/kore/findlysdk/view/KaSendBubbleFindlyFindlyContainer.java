package com.kore.findlysdk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.kore.findlysdk.R;
import com.kore.findlysdk.utils.AppControl;
import com.kore.findlysdk.view.viewUtils.LayoutUtils;
import com.kore.findlysdk.view.viewUtils.MeasureUtils;

/**
 * Created by Pradeep Mahato on 01-Jun-16.
 * Copyright (c) 2014 Kore Inc. All rights reserved.
 */
public class KaSendBubbleFindlyFindlyContainer extends KaBaseBubbleFindlyContainer {

    KaSendBubbleFindlyLayout sendBubbleLayout;
    View headerLayout;
    public KaSendBubbleFindlyFindlyContainer(Context context) {
        super(context);
    }

    public KaSendBubbleFindlyFindlyContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KaSendBubbleFindlyFindlyContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*public KaSendBubbleContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }*/

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        //Find views...
        sendBubbleLayout = (KaSendBubbleFindlyLayout) findViewById(R.id.sendBubbleLayout);
        headerLayout = findViewById(R.id.headerLayout);
        dp1 = (int) AppControl.getInstance().getDimensionUtil().dp1;

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = View.MeasureSpec.getSize(widthMeasureSpec);
        int wrapSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

        int totalHeight = getPaddingTop();

        int childWidthSpec;

        int childWidthSpec1 = View.MeasureSpec.makeMeasureSpec(parentWidth, View.MeasureSpec.EXACTLY);

        totalHeight += dp1;

        /*
         * For Send Bubble Layout
         */
        childWidthSpec = View.MeasureSpec.makeMeasureSpec(BUBBLE_CONTENT_LAYOUT_WIDTH, View.MeasureSpec.AT_MOST);
        MeasureUtils.measure(sendBubbleLayout, childWidthSpec, wrapSpec);

        MeasureUtils.measure(headerLayout, childWidthSpec1, wrapSpec);

        totalHeight += sendBubbleLayout.getMeasuredHeight();
        totalHeight += headerLayout.getMeasuredHeight();

        int parentHeightSpec = View.MeasureSpec.makeMeasureSpec(totalHeight, View.MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, parentHeightSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int top = getPaddingTop();
        int left = getPaddingLeft();
        int parentWidth = getMeasuredWidth();

        /*
         * For Received Bubble Layout
         */
        LayoutUtils.layoutChild(headerLayout,left,top);
        top = headerLayout.getBottom();
        int viewLeft = (parentWidth - getPaddingRight()) - sendBubbleLayout.getMeasuredWidth();
        LayoutUtils.layoutChild(sendBubbleLayout, viewLeft, top);
    }
}
