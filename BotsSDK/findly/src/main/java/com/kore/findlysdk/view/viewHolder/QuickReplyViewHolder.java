package com.kore.findlysdk.view.viewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.kore.findlysdk.R;

/**
 * Created by Pradeep Mahato on 28/7/17.
 * Copyright (c) 2014 Kore Inc. All rights reserved.
 */
public class QuickReplyViewHolder extends RecyclerView.ViewHolder{
    private TextView quickReplyTitle;
    private ImageView quickReplyImage;
    private LinearLayout quickReplyRoot;

    public QuickReplyViewHolder(View view) {
        super(view);
        quickReplyImage = (ImageView) view.findViewById(R.id.quick_reply_item_image);
        quickReplyTitle = (TextView) view.findViewById(R.id.quick_reply_item_text);
        quickReplyRoot = (LinearLayout) view.findViewById(R.id.quick_reply_item_root);
    }

    public TextView getQuickReplyTitle() {
        return quickReplyTitle;
    }

    public ImageView getQuickReplyImage() {
        return quickReplyImage;
    }

    public LinearLayout getQuickReplyRoot() {
        return quickReplyRoot;
    }
}
