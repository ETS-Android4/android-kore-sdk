package com.kore.findlysdk.view.tableview.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kore.findlysdk.R;
import com.kore.findlysdk.models.PayloadInner;
import com.kore.findlysdk.utils.AppControl;
import com.kore.findlysdk.view.tableview.TableRespExpandDataAdapter;
import com.kore.findlysdk.view.tableview.model.MiniTableModel;

import java.util.List;

public class BotRespExpandTableAdapter extends TableRespExpandDataAdapter<MiniTableModel> {

    private static final int TEXT_SIZE = 14;
    private static final int HEADER_TEXT_SIZE = 12;
    private String[] alignment;
    private String[]  headers;
    int dp1;
    private PayloadInner payloadInner;

    public BotRespExpandTableAdapter(final Context context, final List<MiniTableModel> data, String[] alignment, String[]  headers, PayloadInner payloadInner) {
        super(context, data, payloadInner);
        this.alignment = alignment;
        this.headers = headers;
        this.dp1 = (int) AppControl.getInstance().getDimensionUtil().dp1;
        this.payloadInner = payloadInner;
    }

    @Override
    public View getCellView(int rowIndex, int columnIndex, ViewGroup parentView, boolean showDivider) {
        String  str;
        if(getRowData(rowIndex).getElements().get(columnIndex) instanceof Double){
            str = Double.toString((Double) getRowData(rowIndex).getElements().get(columnIndex));
        }else if(getRowData(rowIndex).getElements().get(columnIndex) instanceof String){
            str = (String) getRowData(rowIndex).getElements().get(columnIndex);
        }else{
            str = "";
        }

        View renderedView = null;
        renderedView = renderString(columnIndex,str, headers[columnIndex], showDivider);

        return renderedView;
    }

    @Override
    public View getGroupedView(int rowIndex, int columnIndex, ViewGroup parentView)
    {
        String  str;
        if(getRowData(rowIndex).getElements().get(columnIndex) instanceof Double){
            str = Double.toString((Double) getRowData(rowIndex).getElements().get(columnIndex));
        }else if(getRowData(rowIndex).getElements().get(columnIndex) instanceof String){
            str = (String) getRowData(rowIndex).getElements().get(columnIndex);
        }else{
            str = "";
        }

        View renderedView = null;
        renderedView = renderGroupString(columnIndex,str);

        return renderedView;
    }

    private int getGravity(int columnIndex){
        if(alignment[columnIndex].equals("left") || alignment[columnIndex].equals("default"))
            return Gravity.LEFT;
        else if(alignment[columnIndex].equals("right"))
            return Gravity.RIGHT;
        else return Gravity.CENTER;
    }

//    private View renderString(int columnIndex, final String value, String header) {
//        LinearLayout renderView = new LinearLayout(getContext());
//        renderView.setOrientation(LinearLayout.VERTICAL);
//        renderView.setGravity(Gravity.CENTER_VERTICAL);
//
//        final TextView headerTextView = new TextView(getContext());
//        headerTextView.setText(header);
//        headerTextView.setTextSize(HEADER_TEXT_SIZE);
//        headerTextView.setTextColor(Color.LTGRAY);
//        headerTextView.setGravity(Gravity.CENTER);
//        renderView.addView(headerTextView);
//
//        final TextView textView = new TextView(getContext());
//        textView.setText(value);
//        textView.setTextSize(TEXT_SIZE);
//        textView.setTextColor(Color.BLACK);
//        textView.setGravity(Gravity.CENTER);
//        renderView.addView(textView);
//
//        renderView.setPadding(0 , 30, 0, 30);
//        return renderView;
//    }

    private View renderString(int columnIndex, final String value, String header, boolean showDivider) {
        LinearLayout renderView = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.table_responsive_child_findly_layout, null);
        TextView tvColumnName1 = (TextView)renderView.findViewById(R.id.tvColumnName1);
        TextView tvValue1 = (TextView)renderView.findViewById(R.id.tvValue1);
        ImageView ivChildDivider = (ImageView)renderView.findViewById(R.id.ivChildDivider);

        tvColumnName1.setText(header);
        tvValue1.setText(value);

        ivChildDivider.setVisibility(View.VISIBLE);

        if(!showDivider)
            ivChildDivider.setVisibility(View.GONE);

        return renderView;
    }

//    private View renderGroupString(int columnIndex, final String value) {
//        final TextView textView = new TextView(getContext());
//        textView.setText(value);
//        textView.setPadding(0, 30, 0, 30);
//        textView.setTextSize(TEXT_SIZE);
//        textView.setTextColor(Color.BLACK);
//        textView.setGravity(Gravity.CENTER);
//        return textView;
//    }

    private View renderGroupString(int columnIndex, final String value) {
        LinearLayout renderView = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.table_responsive_group_findly_layout, null);
        TextView tvViewGroup1 = (TextView)renderView.findViewById(R.id.tvViewGroup1);

//        final TextView textView = new TextView(getContext());
        tvViewGroup1.setText(value);
//        textView.setPadding(0, 30, 0, 30);
//        textView.setTextSize(TEXT_SIZE);
//        textView.setTextColor(Color.BLACK);
//        textView.setGravity(Gravity.CENTER);
        return renderView;
    }
}
