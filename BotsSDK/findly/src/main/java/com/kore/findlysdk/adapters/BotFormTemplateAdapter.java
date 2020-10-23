package com.kore.findlysdk.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.kore.findlysdk.R;
import com.kore.findlysdk.listners.ComposeFooterInterface;
import com.kore.findlysdk.models.BotFormFieldButtonModel;
import com.kore.findlysdk.models.BotFormTemplateModel;
import com.kore.findlysdk.utils.KaFontUtils;

import java.util.ArrayList;

public class BotFormTemplateAdapter extends BaseAdapter {

    private ComposeFooterInterface composeFooterInterface;
    private LayoutInflater ownLayoutInflator;
    private Context context;
    private ArrayList<BotFormTemplateModel> arrBotFormTemplateModels;

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    private boolean isEnabled;

    public BotFormTemplateAdapter(Context context, ArrayList<BotFormTemplateModel> arrBotFormTemplateModels) {
        this.ownLayoutInflator = LayoutInflater.from(context);
        this.context = context;
        this.arrBotFormTemplateModels = arrBotFormTemplateModels;
    }

    @Override
    public int getCount() {
        if (arrBotFormTemplateModels != null) {
            return arrBotFormTemplateModels.size();
        } else {
            return 0;
        }
    }

    @Override
    public BotFormTemplateModel getItem(int position) {
        if (position == AdapterView.INVALID_POSITION) {
            return null;
        } else {
            return arrBotFormTemplateModels.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = ownLayoutInflator.inflate(R.layout.form_templete_cell_findly_view, null);
            holder.tvFormFieldTitle = convertView.findViewById(R.id.tvFormFieldTitle);
            holder.btfieldButton = convertView.findViewById(R.id.btfieldButton);
            holder.edtFormInput = convertView.findViewById(R.id.edtFormInput);
            convertView.setTag(holder);
            KaFontUtils.applyCustomFont(context, convertView);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        populateVIew(holder, position, arrBotFormTemplateModels.get(position) instanceof BotFormTemplateModel ? 0 : 1);

        return convertView;
    }

    private void populateVIew(final ViewHolder holder, int position, int type)
    {
        final BotFormTemplateModel item = getItem(position);
        holder.btfieldButton.setTag(item);
        holder.btfieldButton.setText(((BotFormFieldButtonModel) item.getFieldButton()).getTitle());
        holder.tvFormFieldTitle.setText(item.getLabel()+" : ");
        holder.edtFormInput.setHint(item.getPlaceHolder());

        holder.btfieldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (composeFooterInterface != null && isEnabled)
                {
                    StringBuffer sb = new StringBuffer();
                    sb.append(holder.edtFormInput.getText().toString());
                    composeFooterInterface.onSendClick(getDotMessage(sb.toString()), sb.toString(),false);
                }
            }
        });
    }

    private String getDotMessage(String strPassword)
    {
//        self.replacingOccurrences(of: "[A-Za-z0-9 !\"#$%&'()*+,-./:;<=>?@\\[\\\\\\]^_`{|}~]", with: "•", options: .regularExpression, range: nil)
        String strDots = "";
        for (int i = 0; i< strPassword.length(); i++)
        {
            strDots = strDots+"•";
        }
        return strDots;
    }

    public void setBotFormTemplates(ArrayList<BotFormTemplateModel> arrBotFormTemplateModels) {
        this.arrBotFormTemplateModels = arrBotFormTemplateModels;
    }

    public void setComposeFooterInterface(ComposeFooterInterface composeFooterInterface) {
        this.composeFooterInterface = composeFooterInterface;
    }

    private void initializeViewHolder(View view, int type) {
        ViewHolder holder = new ViewHolder();

        holder.tvFormFieldTitle = view.findViewById(R.id.tvFormFieldTitle);
        holder.btfieldButton = view.findViewById(R.id.btfieldButton);
        holder.edtFormInput = view.findViewById(R.id.edtFormInput);
        view.setTag(holder);
    }

    private static class ViewHolder {
        Button btfieldButton;
        TextView tvFormFieldTitle;
        EditText edtFormInput;
    }
}

