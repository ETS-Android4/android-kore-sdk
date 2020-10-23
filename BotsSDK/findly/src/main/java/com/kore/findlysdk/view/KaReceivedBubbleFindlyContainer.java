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
public class KaReceivedBubbleFindlyContainer extends KaBaseBubbleFindlyContainer {

    KaReceivedBubbleFindlyLayout receivedBubbleLayout;
    View headerLayout;

    public KaReceivedBubbleFindlyContainer(Context context) {
        super(context);
    }

    public KaReceivedBubbleFindlyContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KaReceivedBubbleFindlyContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /*public KaReceivedBubbleContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }*/

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        receivedBubbleLayout = (KaReceivedBubbleFindlyLayout) findViewById(R.id.receivedBubbleLayout);
        headerLayout = findViewById(R.id.headerLayout);
        headerLayout.setVisibility(VISIBLE);
        dp1 = (int) AppControl.getInstance().getDimensionUtil().dp1;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int wrapSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

        int totalHeight = getPaddingTop();

        int childWidthSpec;
       int childWidthSpec1 = MeasureSpec.makeMeasureSpec(parentWidth, MeasureSpec.EXACTLY);

        totalHeight += dp1;

        /*
         * For Received Bubble Layout
         */
        childWidthSpec = MeasureSpec.makeMeasureSpec(BUBBLE_CONTENT_LAYOUT_WIDTH, MeasureSpec.AT_MOST);
        MeasureUtils.measure(receivedBubbleLayout, childWidthSpec, wrapSpec);

        MeasureUtils.measure(headerLayout, childWidthSpec1, wrapSpec);

        totalHeight += receivedBubbleLayout.getMeasuredHeight();
        totalHeight += headerLayout.getMeasuredHeight();

        int parentHeightSpec = MeasureSpec.makeMeasureSpec(totalHeight, MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, parentHeightSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        //Consider the paddings and manupulate them it first..
        l += getPaddingLeft();
        t += getPaddingTop();
        r -= getPaddingRight();
        b -= getPaddingBottom();

        int top = getPaddingTop();
        int left = getPaddingLeft();

        /*
         * For Received Bubble Layout
         */
        top = dp1;
        LayoutUtils.layoutChild(headerLayout,left,top);
        top = headerLayout.getBottom();
        LayoutUtils.layoutChild(receivedBubbleLayout, left, top);
    }
}
