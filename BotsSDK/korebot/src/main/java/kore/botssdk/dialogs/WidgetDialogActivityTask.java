package kore.botssdk.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import kore.botssdk.R;
import kore.botssdk.adapter.WidgetCancelActionsAdapter;
import kore.botssdk.adapter.WidgetSelectActionsAdapter;
import kore.botssdk.databinding.WidgetFilesItemSelectionDialogBinding;
import kore.botssdk.models.CalEventsTemplateModel;
import kore.botssdk.models.TaskTemplateModel;
import kore.botssdk.models.WidgetDialogModel;


public class WidgetDialogActivityTask extends Dialog {

    private ImageView img_cancel;
    TaskTemplateModel widgetDialogModel;
    ImageView checkbox;

    RecyclerView recycler_actions;
    Context mContext;
    List<CalEventsTemplateModel.Action> actionList;


    /* public WidgetDialogActivity(@NonNull Context context) {
         super(context);
     }
 */
    public WidgetDialogActivityTask(Context mContext, TaskTemplateModel widgetDialogModel, List<CalEventsTemplateModel.Action> actionList) {
        super(mContext, R.style.WidgetDialog);
        this.widgetDialogModel = widgetDialogModel;
        this.mContext = mContext;
        this.actionList = actionList;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);


        setCanceledOnTouchOutside(false);

        getWindow().setBackgroundDrawableResource(R.color.transparent_card);
       // ViewDataBinding mBinding = DataBindingUtil.setContentView(this,R.layout.widget_files_item_selection_dialog);

        WidgetFilesItemSelectionDialogBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.widget_files_item_selection_dialog, null, false);
        setContentView(binding.getRoot());

        binding.setTaskData(widgetDialogModel);

       // WidgetFilesItemSelectionDialogBinding binding = DataBindingUtil.setContentView(mContext, R.layout.activity_main);


        // setContentView(R.layout.widget_files_item_selection_dialog);
        initViews();

        WidgetSelectActionsAdapter adapter = new WidgetSelectActionsAdapter(WidgetDialogActivityTask.this, actionList);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        recycler_actions.setLayoutManager(layoutManager);
        recycler_actions.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    private void initViews() {

        img_cancel = findViewById(R.id.img_cancel);
        checkbox = findViewById(R.id.checkbox);
        checkbox.setVisibility(View.GONE);


        recycler_actions = findViewById(R.id.recycler_actions);

    }

}