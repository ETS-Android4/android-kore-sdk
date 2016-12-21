package kore.botssdk.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;

import kore.botssdk.R;
import kore.botssdk.models.BotCustomListModel;
import kore.botssdk.models.ButtonTemplate;

/**
 * Created by Anil Kumar on 12/20/2016.
 */
public class BotListCustomAdapterNew extends BaseAdapter /*implements View.OnClickListener*/ {
    private Context mContext;

    private ArrayList<BotCustomListModel> optionsList;

    private LayoutInflater inflater = null;
    BotCustomListModel tempValues;
    private static final int OPTIONS_LIST_LIMIT = 3;
    public static boolean isInExpandedMode;
    private MoreSelectionListener moreSelectionListener;

    public BotListCustomAdapterNew(Context a) {
        mContext = a;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        if (isInExpandedMode || !showMore())
            return optionsList.size();
        else
            return (OPTIONS_LIST_LIMIT + 1);
    }

    public int getOptionsSize(){
        return optionsList.size();
    }

    @Override
    public BotCustomListModel getItem(int position) {
        return optionsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    public static class ViewHolder {
        public TextView title;
        public TextView subtitle;
        public ImageView imageUrl;
        public TextView buy;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        BotCustomListModel option= getItem(position);
        if(option != null) {
            if (!isInExpandedMode && showMore()) {
                if (position == OPTIONS_LIST_LIMIT) {
                    convertView = getShowMoreView(convertView, parent);
                } else {
                    convertView = getOptionsView(convertView, position);
                }
            } else {
                convertView = getOptionsView(convertView, position);
            }
        }

        return convertView;
    }

    private View getOptionsView(View convertView,int position){
        View vi = convertView;
        ViewHolder holder;
        if (convertView == null || convertView.getTag() ==null) {
            vi = inflater.inflate(R.layout.bot_custom_list_row_new, null);
            holder = new ViewHolder();
            holder.title= (TextView) vi.findViewById(R.id.title);
            holder.subtitle= (TextView) vi.findViewById(R.id.subtitle);
            holder.imageUrl= (ImageView) vi.findViewById(R.id.imageUrl);
            holder.buy = (TextView) vi.findViewById(R.id.productDetails);
            vi.setTag(holder);
        } else
            holder = (ViewHolder) vi.getTag();

        if (optionsList.size() <= 0) {
            holder.title.setText("No Data");
        } else {
            tempValues = null;
            tempValues = (BotCustomListModel) optionsList.get(position);
            holder.title.setText(tempValues.getTitle());
            holder.subtitle.setText(tempValues.getSubtitle());
            downlodImage(tempValues.getImageUrl(),holder.imageUrl);
            holder.buy.setClickable(true);
            holder.buy.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Toast.makeText(mContext,"Clicked on buy",Toast.LENGTH_SHORT).show();
                }
            });
        }
        return vi;
    }

    private View getShowMoreView(View convertView, ViewGroup parent) {

        convertView = inflater.inflate(R.layout.bot_options_more_list_item, parent, false);
        LinearLayout container = (LinearLayout) convertView.findViewById(R.id.bot_options_more);
        TextView moreTextView = (TextView) convertView.findViewById(R.id.more_txt_view);
        moreTextView.setText("More");

        // LinearLayout membersMoreOption = (LinearLayout) convertView.findViewById(R.id.space_members_more);
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isInExpandedMode = true;
                //notifyDataSetChanged();

                if(moreSelectionListener !=null)
                    moreSelectionListener.onMoreSelected();
            }
        });
        convertView.setTag(null);
        return convertView;
    }

    /*@Override
    public void onClick(View v) {
        Log.v("BotListCustomAdapter", "=====Row button clicked=====");
    }*/

    private boolean showMore() {
        if (optionsList != null && !optionsList.isEmpty()) {
            return (optionsList.size() > OPTIONS_LIST_LIMIT) ? true : false;
        }
        return false;
    }

    public interface MoreSelectionListener {
        void onMoreSelected();
    }

    public void setMoreSelectionListener(MoreSelectionListener moreSelectionListener) {
        this.moreSelectionListener = moreSelectionListener;
    }

    public ArrayList<BotCustomListModel> getOptionsList() {
        return optionsList;
    }

    public void setOptionsList(ArrayList<BotCustomListModel> optionsList) {
        this.optionsList = optionsList;
    }

    private void downlodImage(String imageUrl, ImageView iv){
        Picasso.with(mContext)
                .load(imageUrl)
                .into(iv);
    }


}