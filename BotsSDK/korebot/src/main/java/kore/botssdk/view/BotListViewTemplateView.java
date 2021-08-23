package kore.botssdk.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;

import kore.botssdk.R;
import kore.botssdk.adapter.BotListViewTemplateAdapter;
import kore.botssdk.application.AppControl;
import kore.botssdk.dialogs.ListActionSheetFragment;
import kore.botssdk.dialogs.ListMoreActionSheetFragment;
import kore.botssdk.listener.ComposeFooterInterface;
import kore.botssdk.listener.InvokeGenericWebViewInterface;
import kore.botssdk.listener.ListClickableListner;
import kore.botssdk.listener.VerticalListViewActionHelper;
import kore.botssdk.models.BotButtonModel;
import kore.botssdk.models.BotListModel;
import kore.botssdk.models.BotListViewMoreDataModel;
import kore.botssdk.models.BotResponse;
import kore.botssdk.models.PayloadInner;
import kore.botssdk.net.SDKConfiguration;
import kore.botssdk.utils.KaFontUtils;
import kore.botssdk.utils.StringUtils;
import kore.botssdk.view.viewUtils.LayoutUtils;
import kore.botssdk.view.viewUtils.MeasureUtils;

public class BotListViewTemplateView extends ViewGroup implements ListClickableListner {

    private float dp1, layoutItemHeight = 0;
    private AutoExpandListView autoExpandListView;
    private TextView botCustomListViewButton;
    private TextView workBenchListViewButton;
    private LinearLayout botCustomListRoot;
    private float restrictedMaxWidth, restrictedMaxHeight;
    private ComposeFooterInterface composeFooterInterface;
    private InvokeGenericWebViewInterface invokeGenericWebViewInterface;
    private VerticalListViewActionHelper verticalListViewActionHelper;
    private String title;
    private SharedPreferences sharedPreferences;
    private String quickWidgetColor,fillColor,quickReplyFontColor;
    private BotListViewTemplateAdapter botListTemplateAdapter = null;
    private PayloadInner payloadInner;
    private View view;

    public BotListViewTemplateView(Context context) {
        super(context);
        init(context);
    }

    public BotListViewTemplateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BotListViewTemplateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        view =  LayoutInflater.from(getContext()).inflate(R.layout.bot_custom_list_view_template, this, true);
        botCustomListRoot = (LinearLayout) findViewById(R.id.botCustomListRoot);
        autoExpandListView = (AutoExpandListView) findViewById(R.id.botCustomListView);
        botCustomListViewButton = (TextView) findViewById(R.id.botCustomListViewButton);
        workBenchListViewButton = (TextView) findViewById(R.id.workBenchListViewButton);
        dp1 = (int) AppControl.getInstance().getDimensionUtil().dp1;
        KaFontUtils.applyCustomFont(getContext(), view);
        layoutItemHeight = getResources().getDimension(R.dimen.list_item_view_height);
        sharedPreferences = context.getSharedPreferences(BotResponse.THEME_NAME, Context.MODE_PRIVATE);

        quickWidgetColor = SDKConfiguration.BubbleColors.quickReplyColor;
        quickReplyFontColor = SDKConfiguration.BubbleColors.quickReplyTextColor;
        fillColor = SDKConfiguration.BubbleColors.quickReplyColor;

        fillColor = sharedPreferences.getString(BotResponse.BUTTON_ACTIVE_BG_COLOR, fillColor);
        quickWidgetColor = sharedPreferences.getString(BotResponse.BUTTON_ACTIVE_TXT_COLOR, quickWidgetColor);
        quickReplyFontColor = sharedPreferences.getString(BotResponse.BUTTON_INACTIVE_TXT_COLOR, quickReplyFontColor);

        botCustomListViewButton.setTextColor(Color.parseColor(quickWidgetColor));
        GradientDrawable gradientDrawable = (GradientDrawable)view.findViewById(R.id.botCustomListViewButton).getBackground();
        gradientDrawable.setStroke((int) (2 * dp1), Color.parseColor(quickWidgetColor));
        gradientDrawable.setColor(Color.parseColor(fillColor));

    }

    public void populateListTemplateView(String title, BotListViewMoreDataModel botListViewMoreDataModel, ArrayList<BotListModel> botListModelArrayList, final ArrayList<BotButtonModel> botButtonModelArrayList, int moreCount, String seeMore, PayloadInner payloadInner) {

        if(botListViewMoreDataModel != null)
            Log.e("More Data", botListViewMoreDataModel.getTab1().toString());

        if (botListModelArrayList != null && botListModelArrayList.size() > 0)
        {
            this.payloadInner = payloadInner;

            if(payloadInner != null)
                view.setAlpha((payloadInner.isIs_end() ? 0.4f : 1.0f));

            if(!StringUtils.isNullOrEmpty(seeMore))
            {
                if(moreCount != 0 && botListModelArrayList.size() > moreCount)
                    botListTemplateAdapter = new BotListViewTemplateAdapter(getContext(), autoExpandListView, moreCount);
                else
                    botListTemplateAdapter = new BotListViewTemplateAdapter(getContext(), autoExpandListView, botListModelArrayList.size());
            }
            else
            {
                if(moreCount != 0 && botListModelArrayList.size() > moreCount)
                    botListTemplateAdapter = new BotListViewTemplateAdapter(getContext(), autoExpandListView, moreCount);
                else
                    botListTemplateAdapter = new BotListViewTemplateAdapter(getContext(), autoExpandListView, botListModelArrayList.size());
            }

            autoExpandListView.setAdapter(botListTemplateAdapter);
            botListTemplateAdapter.setComposeFooterInterface(composeFooterInterface);
            botListTemplateAdapter.setInvokeGenericWebViewInterface(invokeGenericWebViewInterface);
            botListTemplateAdapter.setListClickableInterface(BotListViewTemplateView.this);
            botListTemplateAdapter.setBotListModelArrayList(botListModelArrayList);
            botListTemplateAdapter.setListClickable(payloadInner.isIs_end());
            botListTemplateAdapter.notifyDataSetChanged();

            if(payloadInner.isIs_end() && view != null)
                    view.setAlpha((payloadInner.isIs_end() ? 0.4f : 1.0f));


            botCustomListRoot.setVisibility(VISIBLE);
            if(botButtonModelArrayList != null && botButtonModelArrayList.size() > 0)
            {
                botCustomListViewButton.setText(Html.fromHtml(botButtonModelArrayList.get(0).getTitle()));
                botCustomListViewButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListActionSheetFragment bottomSheetDialog = new ListActionSheetFragment();
                        bottomSheetDialog.setisFromFullView(false);
                        bottomSheetDialog.setSkillName("skillName","trigger");
                        bottomSheetDialog.setData(botListViewMoreDataModel);
                        bottomSheetDialog.setHeaderVisible(true);
                        bottomSheetDialog.setComposeFooterInterface(composeFooterInterface);
                        bottomSheetDialog.setInvokeGenericWebViewInterface(invokeGenericWebViewInterface);
                        bottomSheetDialog.show(((FragmentActivity) getContext()).getSupportFragmentManager(), "add_tags");
                    }
                });


                botCustomListViewButton.setVisibility(botListModelArrayList.size() > moreCount ? VISIBLE : GONE);
            }
            else
            {
                if(moreCount != 0)
                {
                    botCustomListViewButton.setVisibility(botListModelArrayList.size() > moreCount ? VISIBLE : GONE);
                    botCustomListViewButton.setText(Html.fromHtml(getResources().getString(R.string.show_more)));
                }


                botCustomListViewButton.setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        ListMoreActionSheetFragment bottomSheetDialog = new ListMoreActionSheetFragment();
                        bottomSheetDialog.setisFromFullView(false);
                        bottomSheetDialog.setSkillName("skillName","trigger");
                        bottomSheetDialog.setData(title, botListModelArrayList);
                        bottomSheetDialog.setComposeFooterInterface(composeFooterInterface);
                        bottomSheetDialog.setInvokeGenericWebViewInterface(invokeGenericWebViewInterface);
                        bottomSheetDialog.show(((FragmentActivity) getContext()).getSupportFragmentManager(), "add_tags");
                    }
                });
            }
        }
        else
        {
            botCustomListRoot.setVisibility(GONE);
            botCustomListViewButton.setVisibility(GONE);
        }
    }


    public void setRestrictedMaxHeight(float restrictedMaxHeight) {
        this.restrictedMaxHeight = restrictedMaxHeight;
    }

    public void setRestrictedMaxWidth(float restrictedMaxWidth) {
        this.restrictedMaxWidth = restrictedMaxWidth;
    }

    public void setComposeFooterInterface(ComposeFooterInterface composeFooterInterface) {
        this.composeFooterInterface = composeFooterInterface;
    }

    public void setInvokeGenericWebViewInterface(InvokeGenericWebViewInterface invokeGenericWebViewInterface) {
        this.invokeGenericWebViewInterface = invokeGenericWebViewInterface;
    }

    public int getViewHeight() {
        int viewHeight = 0;
        if (autoExpandListView != null) {
            int count = 0;
            if (autoExpandListView.getAdapter() != null) {
                count = autoExpandListView.getAdapter().getCount();
            }
            viewHeight = (int) (layoutItemHeight * count);
        }
        return viewHeight;
    }

    public int getViewWidth() {
        int viewHeight = 0;
        if (autoExpandListView != null) {
            int count = 0;
            if (autoExpandListView.getAdapter() != null) {
                count = autoExpandListView.getAdapter().getCount();
            }
            viewHeight = (count > 0) ? (int) (restrictedMaxWidth -2*dp1) : 0;
        }
        return viewHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int wrapSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

        int totalHeight = getPaddingTop();
        int childWidthSpec;
        int totalWidth = getPaddingLeft();

        childWidthSpec = MeasureSpec.makeMeasureSpec((int) restrictedMaxWidth, MeasureSpec.EXACTLY);
        MeasureUtils.measure(botCustomListRoot, childWidthSpec, wrapSpec);

        totalHeight += botCustomListRoot.getMeasuredHeight() + getPaddingBottom() + getPaddingTop();
        totalWidth += botCustomListRoot.getMeasuredWidth() + getPaddingLeft()+getPaddingRight();
        if(totalHeight != 0){
            totalWidth = totalWidth + (int)(3 * dp1);
        }

        int parentHeightSpec = MeasureSpec.makeMeasureSpec(totalHeight, MeasureSpec.EXACTLY);
        int parentWidthSpec = MeasureSpec.makeMeasureSpec(totalWidth, MeasureSpec.AT_MOST);
        setMeasuredDimension(parentWidthSpec, parentHeightSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        final int count = getChildCount();
        int parentWidth = getMeasuredWidth();

        //get the available size of child view
        int childLeft = 0;
        int childTop = 0;

        int itemWidth = (r - l) / getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                LayoutUtils.layoutChild(child, childLeft, childTop);
                childTop += child.getMeasuredHeight();
            }
        }
    }

    @Override
    public void listClicked(boolean isListClicked)
    {
        if(payloadInner != null)
            payloadInner.setIs_end(true);
    }
}

