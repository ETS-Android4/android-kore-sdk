package kore.botssdk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import kore.botssdk.R;
import kore.botssdk.models.BotResponse;
import kore.botssdk.models.PayloadInner;
import kore.botssdk.view.tableview.TableView;
import kore.botssdk.view.tableview.adapters.BotTableAdapter;
import kore.botssdk.view.tableview.model.MiniTableModel;
import kore.botssdk.view.tableview.model.TableColumnWeightModel;
import kore.botssdk.view.tableview.toolkit.SimpleTableHeaderAdapter;
import kore.botssdk.view.tableview.toolkit.TableDataRowBackgroundProviders;
import kore.botssdk.view.viewUtils.LayoutUtils;

import static kore.botssdk.view.viewUtils.DimensionUtil.dp1;

/**
 * Extension of the {@link TableView} that gives the possibility to sort the table by every single
 * column. For this purpose implementations of {@link Comparator} are used. If there is a comparator
 * set for a column the  will automatically display an ImageView at the start
 * of the header indicating to the user, that this column is sortable.
 * If the user clicks this header the given comparator will used to sort the table ascending by the
 * content of this column. If the user clicks this header again, the table is sorted descending
 * by the content of this column.
 *
 * @author ISchwarz
 */
public class BotTableView extends TableView<MiniTableModel> {


    private Context context;
    public BotTableView(final Context context) {
        this(context, null);
        this.context = context;
//        setBackgroundColor(0xffff0000);

    }

    public BotTableView(final Context context, final AttributeSet attributes) {
        this(context, attributes, android.R.attr.listViewStyle);
//        setBackgroundColor(0xffff0000);

    }

    public BotTableView(final Context context, final AttributeSet attributes, final int styleAttributes){
        super(context,attributes,styleAttributes);
//        setBackgroundColor(0xffff0000);

    }
    public String[] addHeaderAdapter( List<List<String>> primary){
        String[]  headers = new String[primary.size()];
        String[] alignment = new String[primary.size()];
        String defaultAlign = "left";
        for(int i=0; i<primary.size();i++){
            headers[i] = primary.get(i).get(0);
            if(primary.get(i).size() > 1)
                alignment[i] = primary.get(i).get(1);
            else
                alignment[i] = defaultAlign;
        }
        final SimpleTableHeaderAdapter simpleTableHeaderAdapter = new SimpleTableHeaderAdapter(context, headers,alignment);
        simpleTableHeaderAdapter.setTextColor(context.getResources().getColor(R.color.primaryDark));
        setHeaderAdapter(simpleTableHeaderAdapter);

        final int rowColorEven =context.getResources().getColor(R.color.table_data_row_even);
        //final int rowColorOdd = context.getResources().getColor(R.color.table_data_row_odd);
        setDataRowBackgroundProvider(TableDataRowBackgroundProviders.alternatingRowColors(rowColorEven, rowColorEven));
//        setHeaderSortStateViewProvider(SortStateViewProviders.brightArrows());

        final TableColumnWeightModel tableColumnWeightModel = new TableColumnWeightModel(headers.length);
        for(int index=0;index<headers.length;index++) {
            tableColumnWeightModel.setColumnWeight(index, 3);
        }
        setColumnCount(headers.length);
        setColumnModel(tableColumnWeightModel);
        return alignment;
    }

    public void addDataAdapter(String template_type, List<List<Object>> additional, String[] alignment){
        if(BotResponse.TEMPLATE_TYPE_MINITABLE.equals(template_type) || BotResponse.TEMPLATE_TYPE_TABLE.equals(template_type)) {
            List<MiniTableModel> lists = new ArrayList<>();
            for(int j=0; j<additional.size();j++) {
                MiniTableModel model = new MiniTableModel();
                model.setElements(additional.get(j));
                lists.add(model);
            }
            BotTableAdapter tableAdapter = new BotTableAdapter(context,lists, alignment);
            setDataAdapter(tableAdapter);
        }

    }

    public void setData(PayloadInner payloadInner){
        if(payloadInner != null) {
            String[] alignment = addHeaderAdapter(payloadInner.getColumns());
            addDataAdapterForTable(payloadInner, alignment);
        }else{
            setDataAdapter(null);
        }
    }

    public void addDataAdapterForTable(PayloadInner data, String[] alignment) {

        List<MiniTableModel> lists = new ArrayList<>();
        int size = ((ArrayList) data.getElements()).size();
        for (int j = 0; j < size; j++) {
            MiniTableModel model = new MiniTableModel();
            model.setElements(((ArrayList)(((LinkedTreeMap)((ArrayList) data.getElements()).get(j))).get("Values")));
            lists.add(model);
        }
        BotTableAdapter tableAdapter = new BotTableAdapter(context, lists,alignment);
        setDataAdapter(tableAdapter);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        int parentWidth = getMeasuredWidth();

        //get the available size of child view
        int childLeft = 0;//this.getPaddingLeft();
        int childTop = 0;//this.getPaddingTop();

        //walk through each child, and arrange it from left to right
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                LayoutUtils.layoutChild(child, childLeft, childTop);
                childTop += child.getMeasuredHeight();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)  {
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        // Unspecified means that the ViewPager is in a ScrollView WRAP_CONTENT.
        // At Most means that the ViewPager is not in a ScrollView WRAP_CONTENT.
        if (mode == MeasureSpec.UNSPECIFIED || mode == MeasureSpec.AT_MOST) {
            // super has to be called in the beginning so the child views can be initialized.
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int height = 0;
            height = getListViewHeightBasedOnChildren(tableDataView);
            height += tableHeaderView.getMeasuredHeight();

            if (height != 0 ) {
                height = height + (int) (25 * dp1);
            }
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height+getPaddingTop(), MeasureSpec.EXACTLY);
    /*        for(int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                child.getLayoutParams().height = height;
                child.requestLayout();
            }*/
        }


        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public int getListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return  0;
        }

        int totalHeight = 0;
        int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(),
                MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }
        return totalHeight;

        /*ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();*/
    }

   /* @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)  {
        *//*int mode = MeasureSpec.getMode(heightMeasureSpec);
        // Unspecified means that the ViewPager is in a ScrollView WRAP_CONTENT.
        // At Most means that the ViewPager is not in a ScrollView WRAP_CONTENT.
        if (mode == MeasureSpec.UNSPECIFIED || mode == MeasureSpec.AT_MOST) {
            // super has to be called in the beginning so the child views can be initialized.
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int height = 0;
//            for (int i = 0; i < getChildCount(); i++) {
                tableHeaderView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                height += tableHeaderView.getMeasuredHeight();
                tableDataView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                height += tableDataView.getMeasuredHeight();
//            }
            *//**//*if (height != 0) {
                height = height + (int) (25 * dp1);
            }*//**//*
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height+getPaddingTop()+getPaddingBottom(), MeasureSpec.EXACTLY);
    *//**//*        for(int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                child.getLayoutParams().height = height;
                child.requestLayout();
            }*//**//*
        }


        super.onMeasure(widthMeasureSpec, heightMeasureSpec);*//*

        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int maxAllowedWidth = parentWidth;
        int wrapSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

        int totalHeight = getPaddingTop();
        int totalWidth = getPaddingLeft();

        int childWidthSpec;
        int childHeightSpec;
        int contentWidth = 0;
        int childHeight;

        *//*
         * For Carousel ViewPager Layout
         *//*


        childWidthSpec = MeasureSpec.makeMeasureSpec(maxAllowedWidth, MeasureSpec.UNSPECIFIED);
        // childHeightSpec = MeasureSpec.makeMeasureSpec( childHeight , MeasureSpec.EXACTLY);
        MeasureUtils.measure(tableHeaderView, childWidthSpec, wrapSpec);

        totalHeight += tableHeaderView.getMeasuredHeight();

        childWidthSpec = MeasureSpec.makeMeasureSpec(maxAllowedWidth, MeasureSpec.UNSPECIFIED);
        // childHeightSpec = MeasureSpec.makeMeasureSpec( childHeight , MeasureSpec.EXACTLY);
        MeasureUtils.measure(tableDataView, childWidthSpec, wrapSpec);

        totalHeight += tableDataView.getMeasuredHeight();


        *//*if(carousalView.getMeasuredHeight() !=0 ){
            totalHeight+=1*dp1;
        }*//*
        int parentHeightSpec = MeasureSpec.makeMeasureSpec( totalHeight, MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, parentHeightSpec);
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        int parentWidth = getMeasuredWidth();

        //get the available size of child view
        int childLeft = 0;//this.getPaddingLeft();
        int childTop = 0;//this.getPaddingTop();

        //walk through each child, and arrange it from left to right
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                LayoutUtils.layoutChild(child, childLeft, childTop);
                childTop += child.getMeasuredHeight();
            }
        }
    }*/
}