package kore.botssdk.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import kore.botssdk.models.ContentModel;
import kore.botssdk.utils.KaFontUtils;
import kore.botssdk.utils.StringUtils;
import kore.botssdk.view.viewUtils.RoundedCornersTransform;

public class ListWidgetDetailsAdapter extends BaseAdapter
{
    private Context context;
    private ArrayList<ContentModel> contentModels;
    private LayoutInflater layoutInflater;

    protected ListWidgetDetailsAdapter(Context context, ArrayList<ContentModel> contentModels)
    {
        this.context = context;
        this.contentModels = contentModels;
        this.layoutInflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount()
    {
        return contentModels.size();
    }

    @Override
    public Object getItem(int i)
    {
        return contentModels.get(i);
    }

    @Override
    public long getItemId(int i)
    {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        DetailsViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(com.kora.ai.widgetsdk.R.layout.listwidget_details_item, null);
            KaFontUtils.applyCustomFont(context, convertView);
            holder = new DetailsViewHolder();
            holder.tvBtnText = convertView.findViewById(com.kora.ai.widgetsdk.R.id.tvBtnText);
            holder.ivListBtnIcon = convertView.findViewById(com.kora.ai.widgetsdk.R.id.ivListBtnIcon);
            convertView.setTag(holder);
        } else {
            holder = (DetailsViewHolder) convertView.getTag();
        }

        populateData(holder, position);

        return convertView;
    }

    private void populateData(DetailsViewHolder holder, int position) {
        ContentModel dataObj = (ContentModel) getItem(position);
        holder.tvBtnText.setText(dataObj.getDescription());

        if(holder.ivListBtnIcon != null && !StringUtils.isNullOrEmpty(dataObj.getImage().getImage_src()))
        {
            holder.ivListBtnIcon.setVisibility(View.VISIBLE);
            String url = dataObj.getImage().getImage_src().trim();
            url = url.replace("http://","https://");
            Picasso.get().load(url).transform(new RoundedCornersTransform()).into(holder.ivListBtnIcon);
        }
    }

    private class DetailsViewHolder {
        private TextView tvBtnText;
        private ImageView ivListBtnIcon;

    }
}
