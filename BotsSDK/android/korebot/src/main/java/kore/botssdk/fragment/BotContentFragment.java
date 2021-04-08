package kore.botssdk.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.CompositeDateValidator;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.gson.Gson;
import com.j256.ormlite.stmt.query.In;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kore.botssdk.R;
import kore.botssdk.activity.BotChatActivity;
import kore.botssdk.adapter.ChatAdapter;
import kore.botssdk.adapter.ChatAdapterMashreq;
import kore.botssdk.dialogs.CalenderActionSheetFragment;
import kore.botssdk.dialogs.DateRangeCalendarActionSheetFragment;
import kore.botssdk.dialogs.OptionsActionSheetFragment;
import kore.botssdk.listener.BotContentFragmentUpdate;
import kore.botssdk.listener.ComposeFooterInterface;
import kore.botssdk.listener.InvokeGenericWebViewInterface;
import kore.botssdk.listener.TTSUpdate;
import kore.botssdk.listener.ThemeChangeListener;
import kore.botssdk.models.BaseBotMessage;
import kore.botssdk.models.BotHistory;
import kore.botssdk.models.BotHistoryMessage;
import kore.botssdk.models.BotInfoModel;
import kore.botssdk.models.BotRequest;
import kore.botssdk.models.BotResponse;
import kore.botssdk.models.Component;
import kore.botssdk.models.ComponentModel;
import kore.botssdk.models.PayloadInner;
import kore.botssdk.models.PayloadOuter;
import kore.botssdk.models.QuickReplyTemplate;
import kore.botssdk.net.RestBuilder;
import kore.botssdk.net.RestResponse;
import kore.botssdk.net.SDKConfiguration;
import kore.botssdk.net.SDKConfiguration.Client;
import kore.botssdk.retroresponse.ServerBotMsgResponse;
import kore.botssdk.utils.BundleUtils;
import kore.botssdk.utils.DateUtils;
import kore.botssdk.utils.StringUtils;
import kore.botssdk.utils.Utils;
import kore.botssdk.view.CircularProfileView;
import kore.botssdk.view.QuickReplyView;
import kore.botssdk.views.DotsTextView;
import kore.botssdk.websocket.SocketWrapper;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by Pradeep Mahato on 31-May-16.
 * Copyright (c) 2014 Kore Inc. All rights reserved.
 */
public class BotContentFragment extends Fragment implements BotContentFragmentUpdate {
    RelativeLayout rvChatContent, header_layout;
    RecyclerView botsBubblesListView;
    ChatAdapter botsChatAdapter;
    QuickReplyView quickReplyView;
    String LOG_TAG = BotContentFragment.class.getSimpleName();
    private LinearLayout botTypingStatusRl;
    private CircularProfileView botTypingStatusIcon;
    private DotsTextView typingStatusItemDots;
    ComposeFooterInterface composeFooterInterface;
    InvokeGenericWebViewInterface invokeGenericWebViewInterface;
    ThemeChangeListener themeChangeListener;
    boolean shallShowProfilePic;
    private String mChannelIconURL;
    private String mBotNameInitials;
    private TTSUpdate ttsUpdate;
    private int mBotIconId;
    private boolean fetching = false;
    private boolean hasMore = true;
    private TextView headerView, tvTheme1, tvTheme2;
    private Gson gson = new Gson();
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayoutManager mLayoutManager;
    private int offset = 0;
    private SharedPreferences sharedPreferences;
    private String chatBgColor, chatTextColor;
    //Date Range
    private long today;
    private long nextMonth;
    private long janThisYear;
    private long decThisYear;
    private long oneYearForward;
    private Pair<Long, Long> todayPair;
    private Pair<Long, Long> nextMonthPair;
    private ImageView ivThemeSwitcher, ivChaseLogo, ivBotLogo;
    private PopupWindow popupWindow;
    private View popUpView, footer_header;
    private TextView tvChaseTitle;
    private ImageView ivChatExit;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.bot_content_layout, null);
        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        popUpView = inflater.inflate(R.layout.theme_change_layout, null);
        popupWindow = new PopupWindow(popUpView, width, height, focusable);
        findViews(view);
        findThemeViews(popUpView);
        getBundleInfo();
        initializeBotTypingStatus(view, mChannelIconURL);
        setupAdapter();
        loadChatHistory(0, limit);
        return view;
    }

    private void findViews(View view) {
        rvChatContent = (RelativeLayout)view.findViewById(R.id.rvChatContent);
        botsBubblesListView =  view.findViewById(R.id.chatContentListView);
        mLayoutManager = (LinearLayoutManager) botsBubblesListView.getLayoutManager();
        headerView = view.findViewById(R.id.filesSectionHeader);
        swipeRefreshLayout = view.findViewById(R.id.swipeContainerChat);
        quickReplyView = view.findViewById(R.id.quick_reply_view);
        ivThemeSwitcher = view.findViewById(R.id.ivThemeSwitcher);
        ivChaseLogo = view.findViewById(R.id.ivChaseLogo);
        tvChaseTitle = view.findViewById(R.id.tvChaseTitle);
        header_layout = view.findViewById(R.id.header_layout);
        footer_header = view.findViewById(R.id.footer_header);
        ivChatExit = view.findViewById(R.id.ivChatExit);
        ivBotLogo = view.findViewById(R.id.ivBotLogo);
        headerView.setVisibility(View.GONE);
//        tvChaseTitle.setText(Html.fromHtml(getActivity().getResources().getString(R.string.app_name)));
        sharedPreferences = getActivity().getSharedPreferences(BotResponse.THEME_NAME, Context.MODE_PRIVATE);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

//        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                  if (botsChatAdapter != null)
                        loadChatHistory(botsChatAdapter.getItemCount(), limit);
                    else
                        loadChatHistory(0, limit);
                }

        });

        ivThemeSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                popupWindow.showAtLocation(ivThemeSwitcher, Gravity.TOP|Gravity.RIGHT, 80, 220);
            }
        });

        ivChatExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

    }

    public void chatRefresh()
    {
        if(botsChatAdapter != null)
            botsChatAdapter.notifyDataSetChanged();
    }

    public void findThemeViews(View view)
    {
        tvTheme1 = view.findViewById(R.id.tvTheme1);
        tvTheme2 = view.findViewById(R.id.tvTheme2);

        tvTheme1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                popupWindow.dismiss();
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(BotResponse.THEME_NAME, Context.MODE_PRIVATE).edit();
                editor.putString(BotResponse.APPLY_THEME_NAME, BotResponse.THEME_NAME_1);
                editor.apply();

                themeChangeListener.onThemeChangeClicked(BotResponse.THEME_NAME_1);
            }
        });

        tvTheme2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(BotResponse.THEME_NAME, Context.MODE_PRIVATE).edit();
                editor.putString(BotResponse.APPLY_THEME_NAME, BotResponse.THEME_NAME_2);
                editor.apply();

                themeChangeListener.onThemeChangeClicked(BotResponse.THEME_NAME_2);
            }
        });
    }

    public void changeThemeAndLaunch()
    {
        Intent intent = new Intent(getActivity(), BotChatActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume()
    {
        super.onResume();
//        chatBgColor = sharedPreferences.getString(BotResponse.WIDGET_BG_COLOR, "#f3f3f5");
//        chatTextColor = sharedPreferences.getString(BotResponse.WIDGET_TXT_COLOR, SDKConfiguration.BubbleColors.leftBubbleSelected);
//        rvChatContent.setBackgroundColor(Color.parseColor(chatBgColor));
//
//        GradientDrawable gradientDrawable = (GradientDrawable) headerView.getBackground();
//        gradientDrawable.setColor(Color.parseColor(chatBgColor));
//        headerView.setTextColor(Color.parseColor(chatTextColor));

    }

    public void changeThemeBackGround(String bgColor, String textColor, String headerColor, String dividerColor, String botName, String bankLogo)
    {
//        if(!StringUtils.isNullOrEmpty(bgColor))
//        {
//            rvChatContent.setBackgroundColor(Color.parseColor(bgColor));
//            GradientDrawable gradientDrawable = (GradientDrawable) headerView.getBackground();
//            gradientDrawable.setColor(Color.parseColor(bgColor));
//        }

        if(!StringUtils.isNullOrEmpty(textColor))
        {
            headerView.setTextColor(Color.parseColor(textColor));
            tvChaseTitle.setTextColor(Color.parseColor(textColor));
        }

        if(!StringUtils.isNullOrEmpty(headerColor))
            header_layout.setBackgroundColor(Color.parseColor(headerColor));

        if(!StringUtils.isNullOrEmpty(dividerColor))
            footer_header.setBackgroundColor(Color.parseColor(dividerColor));

        if(!StringUtils.isNullOrEmpty(botName))
            tvChaseTitle.setText(botName);

        if(!StringUtils.isNullOrEmpty(bankLogo))
        {
            mChannelIconURL = bankLogo;
            botTypingStatusIcon.populateLayout(mBotNameInitials, mChannelIconURL, null, -1, Color.parseColor(SDKConfiguration.BubbleColors.quickReplyColor), true);
            Picasso.get()
                    .load(bankLogo)
                    .transform(getRoundTransformation())
                    .into(ivBotLogo);
        }
    }

    private Transformation getRoundTransformation() {
        return new RoundedTransformationBuilder()
                .oval(true)
                .build();
    }

    public void setupAdapter() {
        botsChatAdapter = new ChatAdapter(getActivity());
        botsChatAdapter.setComposeFooterInterface(composeFooterInterface);
        botsChatAdapter.setInvokeGenericWebViewInterface(invokeGenericWebViewInterface);
        botsChatAdapter.setActivityContext(getActivity());
        botsBubblesListView.setAdapter(botsChatAdapter);
//        botsChatAdapter.setShallShowProfilePic(shallShowProfilePic);
       // botsBubblesListView.setOnScrollListener(onScrollListener);
//        quickReplyView = new QuickReplyView(getContext());
        quickReplyView.setComposeFooterInterface(composeFooterInterface);
        quickReplyView.setInvokeGenericWebViewInterface(invokeGenericWebViewInterface);
    }

    public void setTtsUpdate(TTSUpdate ttsUpdate) {
        this.ttsUpdate = ttsUpdate;
    }

    public void setComposeFooterInterface(ComposeFooterInterface composeFooterInterface) {
        this.composeFooterInterface = composeFooterInterface;
    }

    public void setInvokeGenericWebViewInterface(InvokeGenericWebViewInterface invokeGenericWebViewInterface) {
        this.invokeGenericWebViewInterface = invokeGenericWebViewInterface;
    }

    public void setThemeChangeInterface(ThemeChangeListener themeChangeListener) {
        this.themeChangeListener = themeChangeListener;
    }

    private void getBundleInfo() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            shallShowProfilePic = bundle.getBoolean(BundleUtils.SHOW_PROFILE_PIC, false);
            mChannelIconURL = bundle.getString(BundleUtils.CHANNEL_ICON_URL);
            mBotNameInitials = bundle.getString(BundleUtils.BOT_NAME_INITIALS, "B");
            mBotIconId = bundle.getInt(BundleUtils.BOT_ICON_ID, -1);
        }
    }

    public void showTypingStatus(BotResponse botResponse) {
        if (botTypingStatusRl != null && botResponse.getMessage() != null && !botResponse.getMessage().isEmpty()) {
            if (typingStatusItemDots != null) {
                botTypingStatusRl.setVisibility(View.VISIBLE);
                typingStatusItemDots.start();
                Log.d("Hey", "Started animation");
            }
        }
    }

    public void setQuickRepliesIntoFooter(BotResponse botResponse) {
        ArrayList<QuickReplyTemplate> quickReplyTemplates = getQuickReplies(botResponse);
        quickReplyView.populateQuickReplyView(quickReplyTemplates);
    }

    public void showCalendarIntoFooter(BotResponse botResponse)
    {
        if (botResponse != null && botResponse.getMessage() != null && !botResponse.getMessage().isEmpty()) {
            ComponentModel compModel = botResponse.getMessage().get(0).getComponent();
            if (compModel != null) {
                String compType = compModel.getType();
                if (BotResponse.COMPONENT_TYPE_TEMPLATE.equalsIgnoreCase(compType)) {
                    PayloadOuter payOuter = compModel.getPayload();
                    PayloadInner payInner = payOuter.getPayload();
                    if (payInner != null && BotResponse.TEMPLATE_TYPE_DATE.equalsIgnoreCase(payInner.getTemplate_type()))
                    {
                        MaterialDatePicker.Builder<Long> builder =  MaterialDatePicker.Builder.datePicker();
                        builder.setTitleText(payInner.getTitle());
                        builder.setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR);
                        builder.setCalendarConstraints(minRange(payInner.getStartDate(), payInner.getEndDate(), payInner.getFormat()).build());
                        builder.setTheme(R.style.MyMaterialCalendarTheme);

//                        if(getDateTimeMills(payInner.getEndDate()) > 0)
//                            builder.setSelection(getDateTimeMills(payInner.getEndDate()));
                        try
                        {
                            MaterialDatePicker<Long> picker = builder.build();
                            picker.show(getFragmentManager(), picker.toString());

                            picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                                @Override public void onPositiveButtonClick(Long selection) {
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTimeInMillis(selection);
                                    int stYear = calendar.get(Calendar.YEAR);
                                    int stMonth = calendar.get(Calendar.MONTH);
                                    int stDay = calendar.get(Calendar.DAY_OF_MONTH);

                                    String formatedDate = "";
                                    formatedDate = DateUtils.getMonthName(stMonth)+" "+stDay+DateUtils.getDayOfMonthSuffix(stDay)+", "+stYear;

                                    if(!formatedDate.isEmpty())
                                        composeFooterInterface.onSendClick(formatedDate, false);
                                }
                            });
                        }
                        catch (IllegalArgumentException e) {}
                    }
                    else if (payInner != null && BotResponse.TEMPLATE_TYPE_DATE_RANGE.equalsIgnoreCase(payInner.getTemplate_type()))
                    {
                        initSettings();
                        MaterialDatePicker.Builder<Pair<Long, Long>> builder =  MaterialDatePicker.Builder.dateRangePicker();
                        builder.setTitleText(payInner.getTitle());
                        builder.setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR);
                        builder.setCalendarConstraints(limitRange(payInner.getEndDate(), payInner.getFormat()).build());
                        builder.setTheme(R.style.MyMaterialCalendarTheme);

                        try
                        {
                            MaterialDatePicker<Pair<Long, Long>> picker = builder.build();
                            picker.show(getFragmentManager(), picker.toString());
                            picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
                                @Override public void onPositiveButtonClick(Pair<Long,Long> selection) {
                                    Long startDate = selection.first;
                                    Long endDate = selection.second;

                                    Calendar cal = Calendar.getInstance();
                                    cal.setTimeInMillis(startDate);
                                    int strYear = cal.get(Calendar.YEAR);
                                    int strMonth = cal.get(Calendar.MONTH);
                                    int strDay = cal.get(Calendar.DAY_OF_MONTH);

                                    cal.setTimeInMillis(endDate);
                                    int endYear = cal.get(Calendar.YEAR);
                                    int endMonth = cal.get(Calendar.MONTH);
                                    int endDay = cal.get(Calendar.DAY_OF_MONTH);

                                    String formatedDate = "";
                                        formatedDate = DateUtils.getMonthName(strMonth)+" "+strDay+DateUtils.getDayOfMonthSuffix(strDay)+", "+strYear;
                                        formatedDate = formatedDate +" to "+ DateUtils.getMonthName(endMonth)+" "+endDay+DateUtils.getDayOfMonthSuffix(endDay)+", "+endYear;


                                    if(!formatedDate.isEmpty())
                                        composeFooterInterface.onSendClick(formatedDate, false);
                                }
                            });
                        }
                        catch (IllegalArgumentException e) {}
                    }
                }
            }
        }
    }

    private static int resolveOrThrow(Context context, @AttrRes int attributeResId) {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(attributeResId, typedValue, true)) {
            return typedValue.data;
        }
        throw new IllegalArgumentException(context.getResources().getResourceName(attributeResId));
    }

    private ArrayList<QuickReplyTemplate> getQuickReplies(BotResponse botResponse) {

        ArrayList<QuickReplyTemplate> quickReplyTemplates = null;

        if (botResponse != null && botResponse.getMessage() != null && !botResponse.getMessage().isEmpty()) {
            ComponentModel compModel = botResponse.getMessage().get(0).getComponent();
            if (compModel != null) {
                String compType = compModel.getType();
                if (BotResponse.COMPONENT_TYPE_TEMPLATE.equalsIgnoreCase(compType)) {
                    PayloadOuter payOuter = compModel.getPayload();
                    PayloadInner payInner = payOuter.getPayload();
                    if (payInner != null && BotResponse.TEMPLATE_TYPE_QUICK_REPLIES.equalsIgnoreCase(payInner.getTemplate_type())) {
                        quickReplyTemplates = payInner.getQuick_replies();
                    }
                }
            }
        }

        return quickReplyTemplates;
    }

    public void addMessageToBotChatAdapter(BotResponse botResponse) {
        botsChatAdapter.addBaseBotMessage(botResponse);
        botTypingStatusRl.setVisibility(View.GONE);
        botsBubblesListView.smoothScrollToPosition(botsChatAdapter.getItemCount());
    }

    protected void initializeBotTypingStatus(View view, String mChannelIconURL) {
        botTypingStatusRl = (LinearLayout) view.findViewById(R.id.botTypingStatus);
        botTypingStatusIcon = (CircularProfileView) view.findViewById(R.id.typing_status_item_cpv);
        botTypingStatusIcon.populateLayout(mBotNameInitials, mChannelIconURL, null, mBotIconId, Color.parseColor(SDKConfiguration.BubbleColors.quickReplyColor), true);
        typingStatusItemDots = (DotsTextView) view.findViewById(R.id.typing_status_item_dots);
        typingStatusItemDots.setTextColor(getResources().getColor(R.color.colorPrimary));
    }

    private void scrollToBottom() {
        final int count = botsChatAdapter.getItemCount();
        botsBubblesListView.post(new Runnable() {
            @Override
            public void run() {
                botsBubblesListView.smoothScrollToPosition(count - 1);
            }
        });
    }


    private int limit = 30;
    RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            int pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

            if (dy > 0) //check for scroll down
            {
                if (!fetching && hasMore) {
                    if (pastVisiblesItems <= 10) {
                        fetching = true;
                        loadChatHistory(botsChatAdapter.getItemCount(), limit);
                    }
                }
            }
        }
    };





    public void addMessagesToBotChatAdapter(ArrayList<BaseBotMessage> list, boolean scrollToBottom) {
        botsChatAdapter.addBaseBotMessages(list);
        if (scrollToBottom) {
            scrollToBottom();
        }
    }

    public void updateContentListOnSend(BotRequest botRequest) {
        if (botRequest.getMessage() != null) {
            if (botsChatAdapter != null) {
                botsChatAdapter.addBaseBotMessage(botRequest);
                quickReplyView.populateQuickReplyView(null);
                scrollToBottom();
            }
        }
    }


    private Observable<ServerBotMsgResponse> getHistoryRequest(final int _offset, final int limit) {
        return Observable.create(new ObservableOnSubscribe<ServerBotMsgResponse>() {
            @Override
            public void subscribe(ObservableEmitter<ServerBotMsgResponse> emitter) throws Exception {
                try {
                    ServerBotMsgResponse re = new ServerBotMsgResponse();

                    Call<BotHistory> _resp = RestBuilder.getRestAPI().getBotHistory("bearer " + SocketWrapper.getInstance(getActivity().getApplicationContext()).getAccessToken(), Client.bot_id, limit, _offset, true);
                    Response<BotHistory> rBody = _resp.execute();
                    BotHistory history = rBody.body();

                    if (rBody.isSuccessful()) {
                        List<BotHistoryMessage> messages = history.getMessages();
                        ArrayList<BaseBotMessage> msgs = null;
                        if (messages != null && messages.size() > 0) {
                            msgs = new ArrayList<>();
                            for (int index = 0; index < messages.size(); index++) {
                                BotHistoryMessage msg = messages.get(index);
                                if (msg.getType().equals(BotResponse.MESSAGE_TYPE_OUTGOING)) {
                                    List<Component> components = msg.getComponents();
                                    String data = components.get(0).getData().getText();
                                    try {
                                        PayloadOuter outer = gson.fromJson(data, PayloadOuter.class);
                                        BotResponse r = Utils.buildBotMessage(outer, msg.getBotId(), Client.bot_name, msg.getCreatedOn(), msg.getId());
                                        r.setType(msg.getType());
                                        msgs.add(r);
                                    } catch (com.google.gson.JsonSyntaxException ex) {
                                        BotResponse r = Utils.buildBotMessage(data, msg.getBotId(), Client.bot_name, msg.getCreatedOn(), msg.getId());
                                        r.setType(msg.getType());
                                        msgs.add(r);
                                    }
                                } else {
                                    try {
                                        String message = msg.getComponents().get(0).getData().getText();
                                        RestResponse.BotMessage botMessage = new RestResponse.BotMessage(message);
                                        RestResponse.BotPayLoad botPayLoad = new RestResponse.BotPayLoad();
                                        botPayLoad.setMessage(botMessage);
                                        BotInfoModel botInfo = new BotInfoModel(Client.bot_name, Client.bot_id, null);
                                        botPayLoad.setBotInfo(botInfo);
                                        Gson gson = new Gson();
                                        String jsonPayload = gson.toJson(botPayLoad);

                                        BotRequest botRequest = gson.fromJson(jsonPayload, BotRequest.class);
                                        long cTime = DateUtils.isoFormatter.parse(((BotHistoryMessage) msg).getCreatedOn()).getTime() + TimeZone.getDefault().getRawOffset();
                                        String createdTime = DateUtils.isoFormatter.format(new Date(cTime));
                                        botRequest.setCreatedOn(createdTime);
                                        msgs.add(botRequest);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            re.setBotMessages(msgs);
                            re.setOriginalSize(messages != null ? messages.size() : 0);
                        }
                    }

                    emitter.onNext(re);
                    emitter.onComplete();
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
        });
    }

    private void initSettings() {
        today = MaterialDatePicker.todayInUtcMilliseconds();
        Calendar calendar = getClearedUtc();
        calendar.setTimeInMillis(today);
        calendar.roll(Calendar.MONTH, 1);
        nextMonth = calendar.getTimeInMillis();

        calendar.setTimeInMillis(today);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        janThisYear = calendar.getTimeInMillis();
        calendar.setTimeInMillis(today);
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        decThisYear = calendar.getTimeInMillis();

        calendar.setTimeInMillis(today);
        calendar.roll(Calendar.YEAR, 1);
        oneYearForward = calendar.getTimeInMillis();

        todayPair = new Pair<>(today, today);
        nextMonthPair = new Pair<>(nextMonth, nextMonth);
    }

    private static Calendar getClearedUtc() {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.clear();
        return utc;
    }

    /*
      Limit selectable Date range
    */
    private CalendarConstraints.Builder limitRange(String date, String format) {

        /*
      Limit selectable Date range
    */
        CalendarConstraints.Builder constraintsBuilderRange = new CalendarConstraints.Builder();
        constraintsBuilderRange.setValidator(DateValidatorPointBackward.now());

        return constraintsBuilderRange;
    }

    /*
      Limit selectable Date range
    */
    private CalendarConstraints.Builder minRange(String startDate, String date, String format) {

        if(StringUtils.isNullOrEmpty(date) && StringUtils.isNullOrEmpty(startDate))
        {
            CalendarConstraints.Builder constraintsBuilderRange = new CalendarConstraints.Builder();
            constraintsBuilderRange.setValidator(DateValidatorPointForward.now());

            return constraintsBuilderRange;
        }
        else if(!StringUtils.isNullOrEmpty(startDate) && !StringUtils.isNullOrEmpty(date))
        {
            CalendarConstraints.DateValidator dateValidatorMin = DateValidatorPointForward.from(getDateTimeMills(startDate));
            CalendarConstraints.DateValidator dateValidatorMax = DateValidatorPointBackward.before(getDateTimeMills(date));

            ArrayList<CalendarConstraints.DateValidator> listValidators =
                    new ArrayList<CalendarConstraints.DateValidator>();
            listValidators.add(dateValidatorMin);
            listValidators.add(dateValidatorMax);

            CalendarConstraints.DateValidator validators = CompositeDateValidator.allOf(listValidators);
            CalendarConstraints.Builder constraintsBuilderRange = new CalendarConstraints.Builder();
            constraintsBuilderRange.setValidator(validators);

            return constraintsBuilderRange;
        }
        else if(!StringUtils.isNullOrEmpty(startDate))
        {
            CalendarConstraints.DateValidator dateValidatorMin = DateValidatorPointForward.from(getDateTimeMills(startDate));

            ArrayList<CalendarConstraints.DateValidator> listValidators =
                    new ArrayList<CalendarConstraints.DateValidator>();
            listValidators.add(dateValidatorMin);

            CalendarConstraints.DateValidator validators = CompositeDateValidator.allOf(listValidators);
            CalendarConstraints.Builder constraintsBuilderRange = new CalendarConstraints.Builder();
            constraintsBuilderRange.setValidator(validators);

            return constraintsBuilderRange;
        }
        else if(!StringUtils.isNullOrEmpty(date))
        {
            CalendarConstraints.DateValidator dateValidatorMin = DateValidatorPointForward.now();
            CalendarConstraints.DateValidator dateValidatorMax = DateValidatorPointBackward.before(getDateTimeMills(date));

            ArrayList<CalendarConstraints.DateValidator> listValidators =
                    new ArrayList<CalendarConstraints.DateValidator>();
            listValidators.add(dateValidatorMin);

            CalendarConstraints.DateValidator validators = CompositeDateValidator.allOf(listValidators);
            CalendarConstraints.Builder constraintsBuilderRange = new CalendarConstraints.Builder();
            constraintsBuilderRange.setValidator(validators);

            return constraintsBuilderRange;
        }
        else
        {
            CalendarConstraints.Builder constraintsBuilderRange = new CalendarConstraints.Builder();
            constraintsBuilderRange.setValidator(DateValidatorPointForward.now());

            return constraintsBuilderRange;
        }
    }

    private long getDateTimeMills(String date)
    {
        Calendar calendar = Calendar.getInstance();

        if(!StringUtils.isNullOrEmpty(date))
        {
            String[] endDate = date.split("-");

            if(endDate.length > 0)
            {
                calendar.set(Calendar.MONTH, (Integer.parseInt(endDate[0]) - 1));
                calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(endDate[1]));
                calendar.set(Calendar.YEAR,Integer.parseInt(endDate[2]));
            }

            return calendar.getTimeInMillis();
        }

        return calendar.getTimeInMillis();
    }



    private void loadChatHistory(final int _offset, final int limit) {
        if (fetching){
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }
        fetching = true;
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        getHistoryRequest(_offset, limit)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ServerBotMsgResponse>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ServerBotMsgResponse re) {
                        fetching = false;

                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }

                        ArrayList<BaseBotMessage> list = null;
                        if (re != null) {
                            list = re.getBotMessages();
                            offset = _offset + re.getOriginalSize();
                        }

                        if (list != null && list.size() > 0) {
                            addMessagesToBotChatAdapter(list, offset == 0);
                        }

                        if ((list == null || list.size() < limit) && offset != 0) {
                            hasMore = false;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        fetching = false;
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onComplete() {
                        fetching = false;
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                });

/*

        //accessToken, botId, limit, offset, true
        RestRequest<ServerBotMsgResponse> request = new RestRequest<ServerBotMsgResponse>(ServerBotMsgResponse.class, null, null) {
            @Override
            public ServerBotMsgResponse loadDataFromNetwork() throws Exception {
                ServerBotMsgResponse re = new ServerBotMsgResponse();
                try {
                    BotHistory history = getService().getHistory("bearer " + SocketWrapper.getInstance(getActivity().getApplicationContext()).getAccessToken(),
                            SDKConfiguration.Client.bot_id, limit, _offset, true);

                    List<BotHistoryMessage> messages = history.getMessages();
                    ArrayList<BaseBotMessage> msgs = null;
                    if (messages != null && messages.size() > 0) {
                        msgs = new ArrayList<>();
                        for (int index = 0; index < messages.size(); index++) {
                            BotHistoryMessage msg = messages.get(index);
                            if (msg.getType().equals(BotResponse.MESSAGE_TYPE_OUTGOING)) {
                                List<Component> components = msg.getComponents();
                                String data = components.get(0).getData().getText();
                                try {
                                    PayloadOuter outer = gson.fromJson(data, PayloadOuter.class);
                                    BotResponse r = Utils.buildBotMessage(outer, msg.getBotId(), Client.bot_name, msg.getCreatedOn(), msg.getId());
                                    r.setType(msg.getType());
                                    msgs.add(r);
                                } catch (com.google.gson.JsonSyntaxException ex) {
                                    BotResponse r = Utils.buildBotMessage(data, msg.getBotId(), Client.bot_name, msg.getCreatedOn(), msg.getId());
                                    r.setType(msg.getType());
                                    msgs.add(r);
                                }
                            } else {
                                try {
                                    String message = msg.getComponents().get(0).getData().getText();
                                    RestResponse.BotMessage botMessage = new RestResponse.BotMessage(message);
                                    RestResponse.BotPayLoad botPayLoad = new RestResponse.BotPayLoad();
                                    botPayLoad.setMessage(botMessage);
                                    BotInfoModel botInfo = new BotInfoModel(Client.bot_name, Client.bot_id, null);
                                    botPayLoad.setBotInfo(botInfo);
                                    Gson gson = new Gson();
                                    String jsonPayload = gson.toJson(botPayLoad);

                                    BotRequest botRequest = gson.fromJson(jsonPayload, BotRequest.class);
                                    long cTime = DateUtils.isoFormatter.parse(((BotHistoryMessage) msg).getCreatedOn()).getTime() + TimeZone.getDefault().getRawOffset();
                                    String createdTime = DateUtils.isoFormatter.format(new Date(cTime));
                                    botRequest.setCreatedOn(createdTime);
                                    msgs.add(botRequest);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        re.setBotMessages(msgs);
                        re.setOriginalSize(messages != null ? messages.size() : 0);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                return re;
            }
        };

        getSpiceManager().execute(request, new RequestListener<ServerBotMsgResponse>() {
            @Override
            public void onRequestFailure(SpiceException e) {
                //        dismissProgress();
                fetching = false;
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onRequestSuccess(ServerBotMsgResponse re) {

                fetching = false;

                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                ArrayList<BaseBotMessage> list = null;
                if (re != null) {
                    list = re.getBotMessages();
                    offset = _offset + re.getOriginalSize();
                }

                if (list != null && list.size() > 0) {
                    addMessagesToBotChatAdapter(list, offset == 0);
                }

                if ((list == null || list.size() < limit) && offset != 0) {
                    hasMore = false;
                }
            }
        });*/

    }
}