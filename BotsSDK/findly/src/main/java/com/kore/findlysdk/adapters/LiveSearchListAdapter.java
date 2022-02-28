package com.kore.findlysdk.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.kore.findlysdk.R;
import com.kore.findlysdk.listners.ComposeFooterInterface;
import com.kore.findlysdk.listners.InvokeGenericWebViewInterface;
import com.kore.findlysdk.models.ResultsViewAppearance;
import com.kore.findlysdk.utils.BundleConstants;
import com.kore.findlysdk.utils.StringUtils;
import com.kore.findlysdk.view.RoundedCornersTransform;

import java.util.ArrayList;
import java.util.HashMap;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class LiveSearchListAdapter extends BaseAdapter
{
    private ArrayList<HashMap<String, Object>> model;
    private RoundedCornersTransform roundedCornersTransform;
    private Context context;
    private int from = 0;
    private InvokeGenericWebViewInterface invokeGenericWebViewInterface;
    private ComposeFooterInterface composeFooterInterface;
    private SharedPreferences sharedPreferences;

    public LiveSearchListAdapter(Context context, ArrayList<HashMap<String, Object>> model, int from, InvokeGenericWebViewInterface invokeGenericWebViewInterface, ComposeFooterInterface composeFooterInterface)
    {
        this.model = model;
        this.context = context;
        this.roundedCornersTransform = new RoundedCornersTransform();
        this.from = from;
        this.invokeGenericWebViewInterface = invokeGenericWebViewInterface;
        this.composeFooterInterface = composeFooterInterface;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public int getCount()
    {
        return model.size();
    }

    @Override
    public Object getItem(int i)
    {
        return model.get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.live_search_pages_findly_item,  null);
            holder = new ViewHolder();
            holder.ivPagesCell = (ImageView) convertView.findViewById(R.id.ivPagesCell);
            holder.ivPageGridCell = (ImageView) convertView.findViewById(R.id.ivPageGridCell);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            holder.tvDescription = (TextView) convertView.findViewById(R.id.tvDescription);
            holder.tvFullDescription = (TextView) convertView.findViewById(R.id.tvFullDescription);
            holder.tvPageTitle = (TextView) convertView.findViewById(R.id.tvPageTitle);
            holder.ivSuggestedPage = (ImageView) convertView.findViewById(R.id.ivSuggestedPage);
            holder.llPages = (LinearLayout) convertView.findViewById(R.id.llPages);
            holder.ivTaskCell = (ImageView) convertView.findViewById(R.id.ivTaskCell);
            holder.tvTaskName = (TextView) convertView.findViewById(R.id.tvTaskName);
            holder.llTask = (LinearLayout) convertView.findViewById(R.id.llTask);
            holder.llCentredContent = (LinearLayout) convertView.findViewById(R.id.llImageCentredContent);
            holder.llImageCentredContent = (LinearLayout) convertView.findViewById(R.id.llCentredContent);
            holder.tvTitleCentredContent = (TextView) convertView.findViewById(R.id.tvTitleCentredContent);
            holder.tvDescriptionCentredContent = (TextView) convertView.findViewById(R.id.tvDescriptionCentredContent);
            holder.tvFullDescriptionCentredContent = (TextView) convertView.findViewById(R.id.tvFullDescriptionCentredContent);
            holder.ivCenteredContent = (ImageView) convertView.findViewById(R.id.ivCenteredContent);
            holder.ibResults = (ImageButton) convertView.findViewById(R.id.ibResults);
            holder.ibResults2 = (ImageButton) convertView.findViewById(R.id.ibResults2);
            holder.rlArrows = (RelativeLayout) convertView.findViewById(R.id.rlArrows);
            convertView.setTag(holder);
        }
        else
            holder = (ViewHolder) convertView.getTag();

        final HashMap<String, Object> objectHashMap = model.get(position);

        holder.ivSuggestedPage.setVisibility(View.GONE);
        holder.llPages.setVisibility(VISIBLE);
        holder.llTask.setVisibility(GONE);
        holder.rlArrows.setVisibility(VISIBLE);

        holder.ibResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(holder.tvFullDescription.getVisibility() == GONE)
                {
                    holder.tvDescription.setVisibility(GONE);
                    holder.tvFullDescription.setVisibility(VISIBLE);
                }

                holder.ibResults.setVisibility(GONE);
                holder.ibResults2.setVisibility(VISIBLE);
            }
        });

        holder.ibResults2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(holder.tvFullDescription.getVisibility() == VISIBLE)
                {
                    holder.tvDescription.setVisibility(VISIBLE);
                    holder.tvFullDescription.setVisibility(GONE);
                }

                holder.ibResults.setVisibility(VISIBLE);
                holder.ibResults2.setVisibility(GONE);
            }
        });

        String sys_content_type = (String)objectHashMap.get(BundleConstants.SYS_CONTENT_TYPE);
        if(!StringUtils.isNullOrEmpty(sys_content_type))
        {
            holder.llPages.setVisibility(VISIBLE);
            final ResultsViewAppearance resultsViewAppearance = (ResultsViewAppearance) objectHashMap.get(BundleConstants.APPEARANCE);

            if(resultsViewAppearance != null && resultsViewAppearance.getTemplate() != null)
            {
                if(resultsViewAppearance.getTemplate().getLayout().getLayoutType().equalsIgnoreCase(BundleConstants.TILE_WITH_TEXT))
                    holder.ivPagesCell.setVisibility(View.GONE);
                else if(resultsViewAppearance.getTemplate().getLayout().getLayoutType().equalsIgnoreCase(BundleConstants.TILE_WITH_IMAGE))
                {
                    holder.ivPagesCell.setVisibility(VISIBLE);

                    if(resultsViewAppearance.getTemplate().getType().equalsIgnoreCase(BundleConstants.LAYOUT_TYPE_GRID))
                    {
                        holder.ivPagesCell.setVisibility(GONE);
                        holder.ivPageGridCell.setVisibility(VISIBLE);
                    }
                }
                else if(resultsViewAppearance.getTemplate().getLayout().getLayoutType().equalsIgnoreCase(BundleConstants.TILE_WITH_CENTERED_CONTENT))
                {
                    holder.ivPagesCell.setVisibility(GONE);
                    holder.llPages.setVisibility(GONE);
                    holder.llImageCentredContent.setVisibility(VISIBLE);
                    holder.llCentredContent.setVisibility(VISIBLE);
                }
                else
                {
                    holder.tvDescription.setVisibility(GONE);
                    holder.ivPagesCell.setVisibility(GONE);
                    holder.rlArrows.setVisibility(GONE);
                }

                holder.tvTitle.setMaxLines(1);

                if (objectHashMap.containsKey(resultsViewAppearance.getTemplate().getMapping().getHeading()))
                {
                    String heading = (String) objectHashMap.get(resultsViewAppearance.getTemplate().getMapping().getHeading());
                    String description = (String) objectHashMap.get(resultsViewAppearance.getTemplate().getMapping().getDescription());

                    if(!StringUtils.isNullOrEmpty(heading) &&
                            !StringUtils.isNullOrEmpty(description))
                    {
                        holder.tvTitle.setText(heading);
                        holder.tvDescription.setText(description);
                        holder.tvFullDescription.setText(description);

                        holder.tvTitleCentredContent.setText(heading);
                        holder.tvDescriptionCentredContent.setText(description);
                        holder.tvFullDescriptionCentredContent.setText(description);
                    }
                }

                if(resultsViewAppearance.getTemplate().getType().equalsIgnoreCase(BundleConstants.LAYOUT_TYPE_GRID))
                {
                    holder.rlArrows.setVisibility(GONE);
                    holder.tvDescription.setVisibility(GONE);
                    holder.tvFullDescription.setVisibility(VISIBLE);
                    holder.tvTitle.setMaxLines(Integer.MAX_VALUE);
                }

                if(resultsViewAppearance.getTemplate().getLayout().getIsClickable())
                    holder.rlArrows.setVisibility(GONE);

                String imageUrl = (String) objectHashMap.get(BundleConstants.FILE_IMAGE_URL);
                if (!StringUtils.isNullOrEmpty(imageUrl))
                {
                    Glide.with(context)
                            .load(imageUrl)
                            .apply(new RequestOptions()
                                    .diskCacheStrategy(DiskCacheStrategy.NONE))
                            .error(R.mipmap.imageplaceholder_left)
                            .into(new DrawableImageViewTarget(holder.ivPagesCell));
                    Glide.with(context)
                            .load(imageUrl)
                            .apply(new RequestOptions()
                                    .diskCacheStrategy(DiskCacheStrategy.NONE))
                            .error(R.mipmap.imageplaceholder_left)
                            .into(new DrawableImageViewTarget(holder.ivCenteredContent));

                    if(resultsViewAppearance.getTemplate().getType().equalsIgnoreCase(BundleConstants.LAYOUT_TYPE_GRID))
                    {
                        Glide.with(context)
                                .load(imageUrl)
                                .apply(new RequestOptions()
                                        .diskCacheStrategy(DiskCacheStrategy.NONE))
                                .error(R.mipmap.imageplaceholder_left)
                                .into(new DrawableImageViewTarget(holder.ivPageGridCell));
                    }
                }

                final String file_url = (String) objectHashMap.get(BundleConstants.FILE_URL);
                holder.llPages.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(resultsViewAppearance.getTemplate().getLayout() != null && resultsViewAppearance.getTemplate().getLayout().getIsClickable() &&
                                invokeGenericWebViewInterface != null && !StringUtils.isNullOrEmpty(file_url))
                            invokeGenericWebViewInterface.invokeGenericWebView(file_url);
                    }
                });
            }
        }

        holder.tvPageTitle.setVisibility(View.GONE);

        if(this.from == 1)
        {
            if(position == 0)
            {
                holder.tvPageTitle.setVisibility(VISIBLE);

                if(!StringUtils.isNullOrEmpty(sys_content_type))
                    holder.tvPageTitle.setText(sys_content_type);
            }

            if ((position - 1) >= 0)
            {
                if(!StringUtils.isNullOrEmpty(sys_content_type))
                {
                    if (!sys_content_type.equalsIgnoreCase((String) model.get(position - 1).get(BundleConstants.SYS_CONTENT_TYPE))) {
                        holder.tvPageTitle.setVisibility(View.VISIBLE);
                        holder.tvPageTitle.setText(sys_content_type);
                    }
                }

            }
        }

        return convertView;
    }

    public class ViewHolder  {
        TextView tvTitle, tvDescription, tvFullDescription, tvPageTitle, tvTaskName, tvTitleCentredContent, tvDescriptionCentredContent, tvFullDescriptionCentredContent;
        ImageView ivPagesCell, ivPageGridCell, ivSuggestedPage, ivTaskCell, ivCenteredContent;
        LinearLayout llPages, llTask, llImageCentredContent, llCentredContent;
        ImageButton ibResults, ibResults2;
        RelativeLayout rlArrows;
    }
}
