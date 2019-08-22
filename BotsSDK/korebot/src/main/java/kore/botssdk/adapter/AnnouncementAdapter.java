package kore.botssdk.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import kore.botssdk.R;
import kore.botssdk.databinding.AnnouncementCardLayoutBinding;
import kore.botssdk.listener.RecyclerViewDataAccessor;
import kore.botssdk.listener.VerticalListViewActionHelper;
import kore.botssdk.models.AnnoucementResModel;
import kore.botssdk.models.KnowledgeDetailModel;
import kore.botssdk.utils.BundleConstants;
import kore.botssdk.utils.DateUtils;
import kore.botssdk.utils.StringUtils;

public class AnnouncementAdapter extends RecyclerView.Adapter implements RecyclerViewDataAccessor {

    private static final int DATA_FOUND = 1;
    private static final int NO_DATA = 0;
    private static final int MESSAGE = 2;
    Context context;
    String msg;
    Drawable errorIcon;
//    public void setData(ArrayList<AnnoucementResModel> data) {
//        this.data = data;
//    }

    ArrayList<AnnoucementResModel> data;
    boolean isViewMore;
    private VerticalListViewActionHelper verticalListViewActionHelper;

    public AnnouncementAdapter(Context context) {
        this.context = context;
    }


    public AnnouncementAdapter(Context activity, ArrayList<AnnoucementResModel> data) {

        context = activity;
        this.data = data;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == DATA_FOUND)
            return new AnnouncementAdapter.AnnouncementViewHolder(DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.announcement_card_layout, parent, false));
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.card_empty_widget_layout, parent, false);
            return new AnnouncementAdapter.EmptyAnnocementViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (data != null && data.size() > 0) {
            return DATA_FOUND;
        }
        if (msg != null && !msg.equalsIgnoreCase("")) {
            return MESSAGE;
        }
        return NO_DATA;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AnnouncementViewHolder) {
            holder = (AnnouncementViewHolder) holder;
            AnnoucementResModel annoucementResModel = data.get(position);
            ((AnnouncementViewHolder) holder).binding.setAnnoucement(annoucementResModel);

            ((AnnouncementViewHolder) holder).binding.tvTime.setText(DateUtils.getFormattedSendDateInTimeFormatCoreFunctionality2(context, annoucementResModel.getSharedOn()));
            ((AnnouncementViewHolder) holder).binding.userProfileName.setCircle(true);
            if (annoucementResModel.getOwner() != null && annoucementResModel.getOwner().getFullName() != null) {
                ((AnnouncementViewHolder) holder).binding.userProfileName.setText(StringUtils.getInitials(annoucementResModel.getOwner().getFullName()));
            }
            try {
                ((AnnouncementViewHolder) holder).binding.userProfileName.setColor(Color.parseColor(annoucementResModel.getOwner().getColor()));
            } catch (Exception e) {

                ((AnnouncementViewHolder) holder).binding.userProfileName.setColor(context.getResources().getColor(R.color.splash_background_color));
            }
            if (position == data.size() - 1 && data.size() < 3)
                ((AnnouncementViewHolder) holder).binding.divider.setVisibility(View.GONE);

            ((AnnouncementViewHolder) holder).binding.viewAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle extras = new Bundle();
                    extras.putString(BundleConstants.KNOWLEDGE_ID, annoucementResModel.getId());
                    verticalListViewActionHelper.knowledgeItemClicked(extras, false);
                }
            });

        } else {
//            holder = (EmptyAnnocementViewHolder) holder;
            ((EmptyAnnocementViewHolder) holder).tv_message.setText(holder.getItemViewType() == NO_DATA ? "No Announcements" : msg);
            ((EmptyAnnocementViewHolder) holder).img_icon.setImageDrawable(holder.getItemViewType() == NO_DATA ? ContextCompat.getDrawable(context, R.drawable.no_meeting) : errorIcon);


        }


    }

    public void addItem(ArrayList<AnnoucementResModel> model) {
        if (model != null) {
            this.data.addAll(0, model);
            notifyItemInserted(0);
        }
    }

    @Override
    public long getItemId(int position) {
        if (data != null && data.size() > 0) {
            AnnoucementResModel model = data.get(position);
            if (model != null && model.getSharedOn() != null)
                return model.getSharedOn() + position;
            else return position;
        } else return position;
    }

    @Override
    public int getItemCount() {

        return data != null && data.size() > 0 ? (!isViewMore && data.size() > 3 ? 3 : data.size()) : 1;

    }

    public void setFullView(boolean isViewMore) {
        this.isViewMore = isViewMore;
    }

    @Override
    public ArrayList getData() {
        return data;
    }

    @Override
    public void setData(ArrayList data) {
        this.data = data;
    }

    @Override
    public void setExpanded(boolean isExpanded) {

    }

    @Override
    public void setVerticalListViewActionHelper(VerticalListViewActionHelper verticalListViewActionHelper) {
        this.verticalListViewActionHelper = verticalListViewActionHelper;
    }

    public void setMessage(String msg, Drawable errorIcon) {
        this.msg = msg;
        this.errorIcon = errorIcon;
    }

    public static class AnnouncementViewHolder extends RecyclerView.ViewHolder {
        AnnouncementCardLayoutBinding binding;

        public AnnouncementViewHolder(@NonNull AnnouncementCardLayoutBinding binding) {
            super((View) binding.getRoot());
            this.binding = binding;

        }
    }

    class EmptyAnnocementViewHolder extends RecyclerView.ViewHolder {
        TextView tv_message;
        RelativeLayout view_action;
        ImageView img_icon;

        public EmptyAnnocementViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_message = itemView.findViewById(R.id.tv_message);
            view_action = itemView.findViewById(R.id.view_action);
            img_icon = itemView.findViewById(R.id.img_icon);
        }
    }
}