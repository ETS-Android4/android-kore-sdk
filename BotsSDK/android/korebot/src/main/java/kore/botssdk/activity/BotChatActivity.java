package kore.botssdk.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
//import com.kore.korefileuploadsdk.core.KoreWorker;
//import com.kore.korefileuploadsdk.core.UploadBulkFile;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import kore.botssdk.R;
import kore.botssdk.bot.BotClient;
import kore.botssdk.drawables.ThemeColors;
import kore.botssdk.event.KoreEventCenter;
import kore.botssdk.events.SocketDataTransferModel;
import kore.botssdk.fragment.BotContentFragment;
import kore.botssdk.fragment.CarouselFragment;
import kore.botssdk.fragment.ComposeFooterFragment;
import kore.botssdk.fragment.QuickReplyFragment;
import kore.botssdk.listener.BaseSocketConnectionManager;
import kore.botssdk.listener.BotContentFragmentUpdate;
import kore.botssdk.listener.BotSocketConnectionManager;
import kore.botssdk.listener.ComposeFooterInterface;
import kore.botssdk.listener.ComposeFooterUpdate;
import kore.botssdk.listener.InvokeGenericWebViewInterface;
import kore.botssdk.listener.SocketChatListener;
import kore.botssdk.listener.TTSUpdate;
import kore.botssdk.listener.ThemeChangeListener;
import kore.botssdk.models.BotButtonModel;
import kore.botssdk.models.BotOptionsModel;
import kore.botssdk.models.BotRequest;
import kore.botssdk.models.BotResponse;
import kore.botssdk.models.BotResponseMessage;
import kore.botssdk.models.BotResponsePayLoadText;
import kore.botssdk.models.BrandingModel;
import kore.botssdk.models.BrandingNewModel;
import kore.botssdk.models.CalEventsTemplateModel.Duration;
import kore.botssdk.models.ComponentModel;
import kore.botssdk.models.ComponentModelPayloadText;
import kore.botssdk.models.FormActionTemplate;
import kore.botssdk.models.JWTTokenResponse;
import kore.botssdk.models.KnowledgeCollectionModel;
import kore.botssdk.models.KoreComponentModel;
import kore.botssdk.models.KoreMedia;
import kore.botssdk.models.PayloadInner;
import kore.botssdk.models.PayloadOuter;
import kore.botssdk.models.limits.Attachment;
import kore.botssdk.net.BrandingRestBuilder;
import kore.botssdk.net.RestBuilder;
import kore.botssdk.net.SDKConfiguration;
import kore.botssdk.utils.BitmapUtils;
import kore.botssdk.utils.BundleConstants;
import kore.botssdk.utils.BundleUtils;
import kore.botssdk.utils.DateUtils;
import kore.botssdk.utils.SharedPreferenceUtils;
import kore.botssdk.utils.StringUtils;
import kore.botssdk.utils.TTSSynthesizer;
import kore.botssdk.websocket.SocketWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.VISIBLE;
import static kore.botssdk.utils.BitmapUtils.rotateIfNecessary;

/**
 * Created by Pradeep Mahato on 31-May-16.
 * Copyright (c) 2014 Kore Inc. All rights reserved.
 */
public class BotChatActivity extends BotAppCompactActivity implements ComposeFooterInterface,
                                        QuickReplyFragment.QuickReplyInterface,
                                        TTSUpdate, InvokeGenericWebViewInterface, ThemeChangeListener/*, PanelInterface,
                                        VerticalListViewActionHelper, UpdateRefreshItem*/
{
    String LOG_TAG = BotChatActivity.class.getSimpleName();
    FrameLayout chatLayoutFooterContainer;
    FrameLayout chatLayoutContentContainer;
    FrameLayout chatLayoutPanelContainer;
    ProgressBar taskProgressBar;
    FragmentTransaction fragmentTransaction;
    final Handler handler = new Handler();
    String chatBot, taskBotId, jwt;
    Handler actionBarTitleUpdateHandler;
    BotClient botClient;
    BotContentFragment botContentFragment;
    CarouselFragment carouselFragment;
    ComposeFooterFragment composeFooterFragment;
    TTSSynthesizer ttsSynthesizer;
    QuickReplyFragment quickReplyFragment;
    BotContentFragmentUpdate botContentFragmentUpdate;
    ComposeFooterUpdate composeFooterUpdate;
    boolean isItFirstConnect = true;
    private Gson gson = new Gson();
    //For Bottom Panel
    private ProgressBar progressBarPanel;
    private RecyclerView pannel_recycler;
    private String jwtToken;
    private TextView emptyPanelView;
    private JWTTokenResponse jwtKeyResponse;
    private SharedPreferenceUtils sharedPreferenceUtils;
    private String packageName = "com.kore.koreapp";
    private String appName = "Kore";
    private boolean keyBoardShowing = false;
//    private PanelBaseModel pModels;
    private LinearLayout perssiatentPanel, persistentSubLayout;
    private ImageView img_skill;
    private TextView closeBtnPanel, editButton;
    private RecyclerView recyclerView_panel;
    private LinearLayout single_item_container;
//    private KaWidgetBaseAdapterNew widgetBaseAdapter;
    private TextView img_icon, txtTitle;
    //Fragment Approch
    private FrameLayout composerView;
    private SharedPreferences sharedPreferences;
    private String chatBgColor, chatTextColor;
    private ImageView ivChaseBackground, ivChaseLogo;
    protected int compressQualityInt = 100;
    protected Attachment attachment;
    Handler messageHandler = new Handler();
    protected static long totalFileSize;
    private ArrayList<BrandingNewModel> arrBrandingNewDos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bot_chat_layout);
        findViews();
        getBundleInfo();
        getDataFromTxt();

        onThemeChangeClicked(sharedPreferences.getString(BotResponse.APPLY_THEME_NAME, BotResponse.THEME_NAME_1));
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        //Add Bot Content Fragment
        botContentFragment = new BotContentFragment();
        botContentFragment.setArguments(getIntent().getExtras());
        botContentFragment.setComposeFooterInterface(this);
        botContentFragment.setInvokeGenericWebViewInterface(this);
        botContentFragment.setThemeChangeInterface(this);
        fragmentTransaction.add(R.id.chatLayoutContentContainer, botContentFragment).commit();
        setBotContentFragmentUpdate(botContentFragment);

        //Add Suggestion Fragment
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        quickReplyFragment = new QuickReplyFragment();
        quickReplyFragment.setArguments(getIntent().getExtras());
        quickReplyFragment.setListener(BotChatActivity.this);
        fragmentTransaction.add(R.id.quickReplyLayoutFooterContainer, quickReplyFragment).commit();

        //Add Bot Compose Footer Fragment
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        composeFooterFragment = new ComposeFooterFragment();
        composeFooterFragment.setArguments(getIntent().getExtras());
        composeFooterFragment.setComposeFooterInterface(this);
        composeFooterFragment.setBottomOptionData(getDataFromTxt());
        fragmentTransaction.add(R.id.chatLayoutFooterContainer, composeFooterFragment).commit();
        setComposeFooterUpdate(composeFooterFragment);

        updateTitleBar();

        botClient = new BotClient(this);
        ttsSynthesizer = new TTSSynthesizer(this);
        setupTextToSpeech();
        KoreEventCenter.register(this);
        BotSocketConnectionManager.getInstance().setChatListener(sListener);
    }

    SocketChatListener sListener = new SocketChatListener() {
        @Override
        public void onMessage(BotResponse botResponse) {
            processPayload("", botResponse);
        }

        @Override
        public void onConnectionStateChanged(BaseSocketConnectionManager.CONNECTION_STATE state, boolean isReconnection) {
            if(state == BaseSocketConnectionManager.CONNECTION_STATE.CONNECTED){
                //fetchBotMessages();
            }
            updateTitleBar(state);
        }

        @Override
        public void onMessage(SocketDataTransferModel data) {
            if (data == null) return;
            if (data.getEvent_type().equals(BaseSocketConnectionManager.EVENT_TYPE.TYPE_TEXT_MESSAGE)) {
                processPayload(data.getPayLoad(), null);

            } else if (data.getEvent_type().equals(BaseSocketConnectionManager.EVENT_TYPE.TYPE_MESSAGE_UPDATE)) {
                if (botContentFragment != null) {
                    botContentFragment.updateContentListOnSend(data.getBotRequest());
                }
            }
        }
    };

    public void buttonClick(View view){
        int red= new Random().nextInt(255);
        int green= new Random().nextInt(255);
        int blue= new Random().nextInt(255);
        ThemeColors.setNewThemeColor(BotChatActivity.this, red, green, blue);
    }

    @Override
    protected void onDestroy() {

        //Close the Text to Speech Library
        if(ttsSynthesizer != null) {
            ttsSynthesizer.stopTextToSpeech();
        }

        botClient.disconnect();
        KoreEventCenter.unregister(this);
        super.onDestroy();
    }

    private void getBundleInfo() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            jwt = bundle.getString(BundleUtils.JWT_TOKEN, "");
        }
        chatBot = SDKConfiguration.Client.bot_name;
        taskBotId = SDKConfiguration.Client.bot_id;
    }

    private void findViews() {
        chatLayoutFooterContainer = (FrameLayout) findViewById(R.id.chatLayoutFooterContainer);
        chatLayoutContentContainer = (FrameLayout) findViewById(R.id.chatLayoutContentContainer);
        chatLayoutPanelContainer   = (FrameLayout) findViewById(R.id.chatLayoutPanelContainer);
        taskProgressBar = (ProgressBar) findViewById(R.id.taskProgressBar);
        ivChaseBackground = (ImageView) findViewById(R.id.ivChaseBackground);
        ivChaseLogo = (ImageView) findViewById(R.id.ivChaseLogo);
        sharedPreferences = getSharedPreferences(BotResponse.THEME_NAME, Context.MODE_PRIVATE);
    }

    private void updateTitleBar() {
//        String botName = (chatBot != null && !chatBot.isEmpty()) ? chatBot : ((SDKConfiguration.Server.IS_ANONYMOUS_USER) ? chatBot + " - anonymous" : chatBot);
//        getSupportActionBar().setSubtitle(botName);
    }

    private void updateTitleBar(BaseSocketConnectionManager.CONNECTION_STATE socketConnectionEvents) {

        String titleMsg = "";
        switch (socketConnectionEvents) {
            case CONNECTING:
                titleMsg = getString(R.string.socket_connecting);
                taskProgressBar.setVisibility(View.VISIBLE);
                updateActionBar();
                break;
            case CONNECTED:
                titleMsg = getString(R.string.socket_connected);
                taskProgressBar.setVisibility(View.GONE);
                composeFooterFragment.enableSendButton();
                updateActionBar();
                getBrandingDetails();
//                onEvent(getBrandingModel());
//                if(isItFirstConnect)
//                {
//                    botClient.sendMessage("BotNotifications");
//                    isItFirstConnect = false;
//                }
                break;
            case DISCONNECTED:
            case CONNECTED_BUT_DISCONNECTED:
                titleMsg = getString(R.string.socket_disconnected);
                taskProgressBar.setVisibility(View.VISIBLE);
                composeFooterFragment.setDisabled(true);
                composeFooterFragment.updateUI();
                updateActionBar();
                break;

            default:
                titleMsg = getString(R.string.socket_connecting);
                taskProgressBar.setVisibility(View.GONE);
                updateActionBar();

        }
    }


    private void setupTextToSpeech() {
        composeFooterFragment.setTtsUpdate(BotSocketConnectionManager.getInstance());
        botContentFragment.setTtsUpdate(BotSocketConnectionManager.getInstance());
    }



    public void onEvent(SocketDataTransferModel data) {
        if (data == null) return;
        if (data.getEvent_type().equals(BaseSocketConnectionManager.EVENT_TYPE.TYPE_TEXT_MESSAGE)) {
            processPayload(data.getPayLoad(), null);
        } else if (data.getEvent_type().equals(BaseSocketConnectionManager.EVENT_TYPE.TYPE_MESSAGE_UPDATE)) {
            if (botContentFragment != null) {
                botContentFragment.updateContentListOnSend(data.getBotRequest());
            }
        }
    }

    public void onEvent(BaseSocketConnectionManager.CONNECTION_STATE states) {
        updateTitleBar(states);
    }

    public  BrandingModel getBrandingModel()
    {
        BrandingModel brandingModel  = new BrandingModel();
        brandingModel.setBotchatBgColor("#FF5E00");
        brandingModel.setBotchatTextColor("#FFFFFF");
        brandingModel.setUserchatBgColor("#738794");
        brandingModel.setUserchatTextColor("#FFFFFF");
        brandingModel.setButtonActiveBgColor("#FF5E00");
        brandingModel.setButtonActiveTextColor("#FFFFFF");
        brandingModel.setButtonInactiveBgColor("#738794");
        brandingModel.setButtonInactiveTextColor("#738794");
        brandingModel.setWidgetBodyColor("#FFFFFF");
        brandingModel.setWidgetBorderColor("#FF5E00");
        brandingModel.setWidgetFooterColor("#E5E5E5");
        brandingModel.setWidgetHeaderColor("#FFFFFF");
        brandingModel.setWidgetTextColor("#000000");

        return brandingModel;
    }

    public void onEvent(BrandingModel brandingModel)
    {
        SharedPreferences.Editor editor = getSharedPreferences(BotResponse.THEME_NAME, Context.MODE_PRIVATE).edit();
        editor.putString(BotResponse.BUBBLE_LEFT_BG_COLOR, brandingModel.getBotchatBgColor());
        editor.putString(BotResponse.BUBBLE_LEFT_TEXT_COLOR, brandingModel.getBotchatTextColor());
        editor.putString(BotResponse.BUBBLE_RIGHT_BG_COLOR, brandingModel.getUserchatBgColor());
        editor.putString(BotResponse.BUBBLE_RIGHT_TEXT_COLOR, brandingModel.getUserchatTextColor());
        editor.putString(BotResponse.BUTTON_ACTIVE_BG_COLOR, brandingModel.getButtonActiveBgColor());
        editor.putString(BotResponse.BUTTON_ACTIVE_TXT_COLOR, brandingModel.getButtonActiveTextColor());
        editor.putString(BotResponse.BUTTON_INACTIVE_BG_COLOR, brandingModel.getButtonInactiveBgColor());
        editor.putString(BotResponse.BUTTON_INACTIVE_TXT_COLOR, brandingModel.getButtonInactiveTextColor());
        editor.putString(BotResponse.WIDGET_BG_COLOR, brandingModel.getWidgetBodyColor());
        editor.putString(BotResponse.WIDGET_TXT_COLOR, brandingModel.getWidgetTextColor());
        editor.putString(BotResponse.WIDGET_BORDER_COLOR, brandingModel.getWidgetBorderColor());
        editor.putString(BotResponse.WIDGET_DIVIDER_COLOR, brandingModel.getWidgetDividerColor());
        editor.putString(BotResponse.WIDGET_FOOTER_COLOR, brandingModel.getWidgetFooterColor());
        editor.putString(BotResponse.WIDGET_HEADER_COLOR, brandingModel.getWidgetHeaderColor());
        editor.putString(BotResponse.WIDGET_BG_IMAGE, brandingModel.getWidgetBgImage());
        editor.putString(BotResponse.BOT_NAME, brandingModel.getBotName());
        editor.putString(BotResponse.BANK_LOGO, brandingModel.getBankLogo());
        editor.apply();

        SDKConfiguration.BubbleColors.quickReplyColor = brandingModel.getWidgetBorderColor();
        SDKConfiguration.BubbleColors.quickReplyTextColor = brandingModel.getButtonActiveTextColor();

        if(!StringUtils.isNullOrEmpty(sharedPreferences.getString(BotResponse.WIDGET_BG_IMAGE, "")))
        {
            Picasso.get().load(sharedPreferences.getString(BotResponse.WIDGET_BG_IMAGE, "")).into(ivChaseLogo);
        }

        if(botContentFragment != null)
            botContentFragment.changeThemeBackGround(brandingModel.getWidgetBodyColor(), brandingModel.getWidgetTextColor(), brandingModel.getWidgetHeaderColor(), brandingModel.getWidgetDividerColor(), brandingModel.getBotName(), brandingModel.getBankLogo());

        if(composeFooterFragment != null)
            composeFooterFragment.setFooterBackground(brandingModel.getWidgetFooterColor(), brandingModel.getWidgetDividerColor(), brandingModel.getWidgetTextColor());
    }


    public void onEvent(BotResponse botResponse) {
        processPayload("", botResponse);
    }

    public void updateActionbar(boolean isSelected,String type,ArrayList<BotButtonModel> buttonModels) {

    }

    @Override
    public void lauchMeetingNotesAction(Context context, String mid, String eid) {

    }

    @Override
    public void showAfterOnboard(boolean isdiscard) {

    }

    @Override
    public void onPanelClicked(Object pModel, boolean isFirstLaunch) {

    }

    @Override
    public void knowledgeCollectionItemClick(KnowledgeCollectionModel.DataElements elements, String id) {

    }

    private void updateActionBar() {
        if (actionBarTitleUpdateHandler == null) {
            actionBarTitleUpdateHandler = new Handler();
        }

        actionBarTitleUpdateHandler.removeCallbacks(actionBarUpdateRunnable);
        actionBarTitleUpdateHandler.postDelayed(actionBarUpdateRunnable, 4000);

    }

    Runnable actionBarUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateTitleBar();
        }
    };

    @Override
    protected void onPause() {
//        ttsSynthesizer.stopTextToSpeech();
        super.onPause();
    }

    public void displayMessage(String text)
    {
        PayloadInner payloadInner = new PayloadInner();
        payloadInner.setTemplate_type("text");

        PayloadOuter payloadOuter = new PayloadOuter();
        payloadOuter.setText(text);
        payloadOuter.setType("text");
        payloadOuter.setPayload(payloadInner);

        ComponentModel componentModel = new ComponentModel();
        componentModel.setType("text");
        componentModel.setPayload(payloadOuter);

        BotResponseMessage botResponseMessage = new BotResponseMessage();
        botResponseMessage.setType("text");
        botResponseMessage.setComponent(componentModel);

        ArrayList<BotResponseMessage> arrBotResponseMessages = new ArrayList<>();
        arrBotResponseMessages.add(botResponseMessage);

        BotResponse botResponse = new BotResponse();
        botResponse.setType("text");
        botResponse.setMessage(arrBotResponseMessages);

        processPayload("", botResponse);
    }

    @Override
    public void onSendClick(String message,boolean isFromUtterance) {
        BotSocketConnectionManager.getInstance().sendMessage(message, null);
    }

    @Override
    public void onSendClick(String message, String payload, boolean isFromUtterance) {
        if(payload != null){
            BotSocketConnectionManager.getInstance().sendPayload(message, payload);
        }else{
            BotSocketConnectionManager.getInstance().sendMessage(message, payload);
        }


        toggleQuickRepliesVisiblity(false);
    }

    @Override
    public void onSendClick(String message, ArrayList<HashMap<String, String>> attachments, boolean isFromUtterance) {
        if(attachments != null && attachments.size() > 0)
            BotSocketConnectionManager.getInstance().sendAttachmentMessage(message, attachments);
    }

    @Override
    public void onFormActionButtonClicked(FormActionTemplate fTemplate) {

    }

    @Override
    public void launchActivityWithBundle(String type, Bundle payload) {

    }

    @Override
    public void sendWithSomeDelay(String message, String payload,long time,boolean isScrollupNeeded) {
        Log.e("Message", message);
    }

    @Override
    public void copyMessageToComposer(String text, boolean isForOnboard) {
        composeFooterFragment.setComposeText(text);
    }

    @Override
    public void showMentionNarratorContainer(boolean show, String natxt,String cotext, String res, boolean isEnd, boolean showOverlay,String templateType) {

    }

    @Override
    public void openFullView(String templateType, String data, Duration duration, int position) {

    }



    public void setBotContentFragmentUpdate(BotContentFragmentUpdate botContentFragmentUpdate) {
        this.botContentFragmentUpdate = botContentFragmentUpdate;
    }

    public void setComposeFooterUpdate(ComposeFooterUpdate composeFooterUpdate) {
        this.composeFooterUpdate = composeFooterUpdate;
    }


    @Override
    public void onQuickReplyItemClicked(String text) {
        onSendClick(text,false);
    }

    /**
     * payload processing
     */

    private void processPayload(String payload, BotResponse botLocalResponse) {
        if (botLocalResponse == null) BotSocketConnectionManager.getInstance().stopDelayMsgTimer();

        try
        {
            final BotResponse botResponse = botLocalResponse != null ? botLocalResponse : gson.fromJson(payload, BotResponse.class);
            if (botResponse == null || botResponse.getMessage() == null || botResponse.getMessage().isEmpty()) {
                return;
            }

            Log.d(LOG_TAG, payload);
            boolean resolved = true;
            PayloadOuter payOuter = null;
//            PayloadInner payInner = null;
            if (!botResponse.getMessage().isEmpty()) {
                ComponentModel compModel = botResponse.getMessage().get(0).getComponent();
                if (compModel != null) {
                    payOuter = compModel.getPayload();

                    if (payOuter != null) {
                        /*if (payOuter.getText() != null && payOuter.getText().contains("&quot")) {
                            payOuter.setText(payOuter.getText().replace("&quot;", "\""));
                            payOuter = gson.fromJson(payOuter.getText().replace("&quot;", "\""), PayloadOuter.class);
                            // payOuter = gson.fromJson(payOuter.getText().replace("&quot;", "\""), PayloadOuter.class);
                        }*/

                        if(payOuter.getText()!= null && payOuter.getText().equalsIgnoreCase("Login Form is successfully submitted."))
                            payOuter.setText("Thank you!");

                        if (payOuter.getText() != null && payOuter.getText().contains("&quot")) {
                            Gson gson = new Gson();
                            payOuter = gson.fromJson(payOuter.getText().replace("&quot;", "\""), PayloadOuter.class);
                        }
                    }
                }
            }
            final PayloadInner payloadInner = payOuter == null ? null : (PayloadInner) payOuter.getPayload();
            if (payloadInner != null && payloadInner.getTemplate_type() != null && "start_timer".equalsIgnoreCase(payloadInner.getTemplate_type())) {
                BotSocketConnectionManager.getInstance().startDelayMsgTimer();
            }
            botContentFragment.showTypingStatus(botResponse);
            if (payloadInner != null) {
                payloadInner.convertElementToAppropriate();
            }
            if (resolved) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        botContentFragment.addMessageToBotChatAdapter(botResponse);
                        textToSpeech(botResponse);
                        botContentFragment.setQuickRepliesIntoFooter(botResponse);
                        botContentFragment.showCalendarIntoFooter(botResponse);
                    }
                }, BundleConstants.TYPING_STATUS_TIME);
            }
        }
        catch (Exception e)
        {
            /*Toast.makeText(getApplicationContext(), "Invalid JSON", Toast.LENGTH_SHORT).show();*/
//            e.printStackTrace();
            if (e instanceof JsonSyntaxException) {
                try {
                    //This is the case Bot returning user sent message from another channel
                    if (botContentFragment != null) {
                        BotRequest botRequest = gson.fromJson(payload, BotRequest.class);
                        botRequest.setCreatedOn(DateUtils.isoFormatter.format(new Date()));
                        botContentFragment.updateContentListOnSend(botRequest);
                    }
                }
                catch (Exception e1)
                {
                    try
                    {
                        final BotResponsePayLoadText botResponse = gson.fromJson(payload, BotResponsePayLoadText.class);
                        if (botResponse == null || botResponse.getMessage() == null || botResponse.getMessage().isEmpty()) {
                            return;
                        }
                        Log.d(LOG_TAG, payload);
                        boolean resolved = true;
                        if (!botResponse.getMessage().isEmpty()) {
                            ComponentModelPayloadText compModel = botResponse.getMessage().get(0).getComponent();
                            if (compModel != null && !StringUtils.isNullOrEmpty(compModel.getPayload()))
                            {
                                displayMessage(compModel.getPayload());
                            }
                        }
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                    }
//                    e1.printStackTrace();
                }
            }
        }


    }

    @Override
    public void invokeGenericWebView(String url) {
        if (url != null && !url.isEmpty()) {
            Intent intent = new Intent(getApplicationContext(), GenericWebViewActivity.class);
            intent.putExtra("url", url);
            intent.putExtra("header", getResources().getString(R.string.app_name));
            startActivity(intent);
        }
    }

    @Override
    public void handleUserActions(String action, HashMap<String,Object > payload) {


    }
    @Override
    public void onStop() {
        BotSocketConnectionManager.getInstance().unSubscribe();
        super.onStop();
    }

    public BotOptionsModel getDataFromTxt()
    {
        BotOptionsModel botOptionsModel = null;

        try
        {
            InputStream is = getResources().openRawResource(R.raw.option);
            Reader reader = new InputStreamReader(is);
            botOptionsModel = gson.fromJson(reader, BotOptionsModel.class);
            Log.e("Options Size", botOptionsModel.getTasks().size() + "" );
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return botOptionsModel;
    }

    @Override
    public void onStart() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                BotSocketConnectionManager.getInstance().subscribe();
            }
        });
        super.onStart();
    }

    @Override
    protected void onResume() {
        BotSocketConnectionManager.getInstance().checkConnectionAndRetry(getApplicationContext(), false);
        updateTitleBar(BotSocketConnectionManager.getInstance().getConnection_state());

        if(botContentFragment != null)
            botContentFragment.chatRefresh();
        super.onResume();
    }


    @Override
    public void ttsUpdateListener(boolean isTTSEnabled) {
        stopTextToSpeech();
    }

    @Override
    public void ttsOnStop() {
        stopTextToSpeech();
    }

    public boolean isTTSEnabled() {
        if (composeFooterFragment != null) {
            return composeFooterFragment.isTTSEnabled();
        } else {
            Log.e(BotChatActivity.class.getSimpleName(), "ComposeFooterFragment not found");
            return false;
        }
    }

    private void stopTextToSpeech() {
//        try {
//            ttsSynthesizer.stopTextToSpeech();
//        }catch (IllegalArgumentException exception){
//            exception.printStackTrace();
//        }
    }

    private void textToSpeech(BotResponse botResponse) {
        if (isTTSEnabled() && botResponse.getMessage() != null && !botResponse.getMessage().isEmpty()) {
            String botResponseTextualFormat = "";
            BotResponseMessage msg = ((BotResponse) botResponse).getTempMessage();
            ComponentModel componentModel = botResponse.getMessage().get(0).getComponent();
            if (componentModel != null) {
                String compType = componentModel.getType();
                PayloadOuter payOuter = componentModel.getPayload();
                if (BotResponse.COMPONENT_TYPE_TEXT.equalsIgnoreCase(compType) || payOuter.getType() == null) {
                    botResponseTextualFormat = payOuter.getText();
                } else if (BotResponse.COMPONENT_TYPE_ERROR.equalsIgnoreCase(payOuter.getType())) {
                    botResponseTextualFormat = ((PayloadInner)payOuter.getPayload()).getText();
                } else if (BotResponse.COMPONENT_TYPE_TEMPLATE.equalsIgnoreCase(payOuter.getType()) || BotResponse.COMPONENT_TYPE_MESSAGE.equalsIgnoreCase(payOuter.getType())) {
                    PayloadInner payInner;
                    if (payOuter.getText() != null && payOuter.getText().contains("&quot")) {
                        Gson gson = new Gson();
                        payOuter = gson.fromJson(payOuter.getText().replace("&quot;", "\""), PayloadOuter.class);
                    }
                    payInner = (PayloadInner)payOuter.getPayload();

                    if (payInner.getSpeech_hint() != null) {
                        botResponseTextualFormat = payInner.getSpeech_hint();
//                        ttsSynthesizer.speak(botResponseTextualFormat);
                        } else if (BotResponse.TEMPLATE_TYPE_BUTTON.equalsIgnoreCase(payInner.getTemplate_type())) {
                            botResponseTextualFormat = payInner.getText();
                        } else if (BotResponse.TEMPLATE_TYPE_QUICK_REPLIES.equalsIgnoreCase(payInner.getTemplate_type())) {
                            botResponseTextualFormat = payInner.getText();
                        } else if (BotResponse.TEMPLATE_TYPE_CAROUSEL.equalsIgnoreCase(payInner.getTemplate_type())) {
                            botResponseTextualFormat = payInner.getText();
                        } else if (BotResponse.TEMPLATE_TYPE_CAROUSEL_ADV.equalsIgnoreCase(payInner.getTemplate_type())) {
                            botResponseTextualFormat = payInner.getText();
                        }
                        else if (BotResponse.TEMPLATE_TYPE_LIST.equalsIgnoreCase(payInner.getTemplate_type())) {
                            botResponseTextualFormat = payInner.getText();
                        }
                        else if(BotResponse.TEMPLATE_TYPE_WELCOME_QUICK_REPLIES.equalsIgnoreCase(payInner.getTemplate_type())){
                        botResponseTextualFormat = payInner.getText();
                    }
                    }
                }
            if (BotSocketConnectionManager.getInstance().isTTSEnabled()) {
                BotSocketConnectionManager.getInstance().startSpeak(botResponseTextualFormat);
            }
            }
        }


    private void toggleQuickRepliesVisiblity(boolean visible){
        if (visible) {
            quickReplyFragment.toggleQuickReplyContainer(View.VISIBLE);
        } else {
            quickReplyFragment.toggleQuickReplyContainer(View.GONE);
        }
    }


    @Override
    public void onBackPressed() {
        if (isOnline()) {
            BotSocketConnectionManager.getInstance().killInstance();
        }
            finish();
    }

    protected boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }


    @Override
    public void onThemeChangeClicked(String message)
    {
        if(message.equalsIgnoreCase(BotResponse.THEME_NAME_1))
        {
            ivChaseLogo.setVisibility(VISIBLE);
            ivChaseBackground.setVisibility(View.GONE);

            if(!StringUtils.isNullOrEmpty(sharedPreferences.getString(BotResponse.WIDGET_BG_IMAGE, "")))
            {
                Picasso.get().load(sharedPreferences.getString(BotResponse.WIDGET_BG_IMAGE, "")).into(ivChaseLogo);
            }
        }
        else
        {
            ivChaseBackground.setVisibility(VISIBLE);
            ivChaseLogo.setVisibility(View.GONE);
        }
    }

//    public void sendImage(String fP, String fN, String fPT) {
//    /*    String filePath = data.getStringExtra("filePath");
//        String fileName = data.getStringExtra("fileName");
//        String filePathThumbnail = data.getStringExtra(THUMBNAIL_FILE_PATH);*/
//        String filePath = fP;
//        String fileName = fN;
//        String filePathThumbnail = fPT;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//
//            new SaveCapturedImageTask(filePath, fileName, filePathThumbnail).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        } else {
//            new SaveCapturedImageTask(filePath, fileName, filePathThumbnail).execute();
//        }
//    }

//    protected class SaveCapturedImageTask extends AsyncTask<String, String, String> {
//
//        private String filePath;
//        private String fileName;
//        private String filePathThumbnail;
//        private String orientation;
//
//        public SaveCapturedImageTask(String filePath, String fileName, String filePathThumbnail) {
//            this.filePath = filePath;
//            this.fileName = fileName;
//            this.filePathThumbnail = filePathThumbnail;
//        }
//
//
//        @Override
//        protected String doInBackground(String... params) {
//            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);
//            String extn = null;
//            if (filePath != null) {
//                extn = filePath.substring(filePath.lastIndexOf(".") + 1);
//                Bitmap thePic = BitmapUtils.decodeBitmapFromFile(filePath, 800, 600, false);
////                    compressImage(filePath);
//                if (thePic != null) {
//                    try {
//                        // compress the image
//                        OutputStream fOut = null;
//                        File _file = new File(filePath);
//
//                        Log.d(LOG_TAG, " file.exists() ---------------------------------------- " + _file.exists());
//                        fOut = new FileOutputStream(_file);
//
//                        thePic.compress(Bitmap.CompressFormat.JPEG, compressQualityInt, fOut);
//                        thePic = rotateIfNecessary(filePath, thePic);
//                        orientation = thePic.getWidth() > thePic.getHeight() ? BitmapUtils.ORIENTATION_LS : BitmapUtils.ORIENTATION_PT;
//                        fOut.flush();
//                        fOut.close();
//                    } catch (Exception e) {
//                        Log.e(LOG_TAG, e.toString());
//                    }
//                }
//            }
//            return extn;
//        }
//
//
//        @Override
//        protected void onPostExecute(String extn) {
//            if (extn != null) {
//                //Common place for addition to composeBar
//                long fileLimit = getFileMaxSize();
//                KoreWorker.getInstance().addTask(new UploadBulkFile(fileName,
//                        filePath, "bearer " + SocketWrapper.getInstance(BotChatActivity.this).getAccessToken(),
//                        SocketWrapper.getInstance(BotChatActivity.this).getBotUserId(), "workflows", extn,
//                        KoreMedia.BUFFER_SIZE_IMAGE,
//                        new Messenger(messagesMediaUploadAcknowledgeHandler),
//                        filePathThumbnail, "AT_" + System.currentTimeMillis(),
//                        BotChatActivity.this, BitmapUtils.obtainMediaTypeOfExtn(extn), SDKConfiguration.Server.SERVER_URL, orientation, true));
//            } else {
//                showToast("Unable to attach!");
//            }
//        }
//    }

    private long getFileMaxSize() {
        long FILE_MAX_SIZE = getFileLimit();
        if (FILE_MAX_SIZE != -1) {
            FILE_MAX_SIZE = FILE_MAX_SIZE * 1024 * 1024;
        }

        return FILE_MAX_SIZE;
    }

//    Handler messagesMediaUploadAcknowledgeHandler = new Handler() {
//        @Override
//        public synchronized void handleMessage(Message msg) {
//            Bundle reply = msg.getData();
//            Log.d("shri", reply + "------------------------------");
//          /*  if (reply.getBoolean(UploadBulkFile.isFileSizeMore_key, false)) {
//                showFreemiumDialog();
//                return;
//            }
//*/
//            if (reply.getBoolean("success", true)) {
//               /* long fileSizeBytes = reply.getLong(UploadBulkFile.fileSizeBytes_key);
//                totalFileSize= totalFileSize+fileSizeBytes;*/
////                String messageId = reply.getString(Constants.MESSAGE_ID);
//                String mediaFilePath = reply.getString("filePath");
//                String MEDIA_TYPE = reply.getString("fileExtn");
//                String mediaFileId = reply.getString("fileId");
//                String mediaFileName = reply.getString("fileName");
//                String componentType = reply.getString("componentType");
//                String thumbnailURL = reply.getString("thumbnailURL");
//                String orientation = reply.getString(BundleConstants.ORIENTATION);
//                String COMPONENT_DESCRIPTION = reply.getString("componentDescription") != null ? reply.getString("componentDescription").toString() : null;
//                HashMap<String, Object> COMPONENT_DATA = reply.getSerializable("componentData") != null ? ((HashMap<String, Object>) reply.getSerializable("componentData")) : null;
//                String fileSize = reply.getString("fileSize");
//                KoreComponentModel koreMedia = new KoreComponentModel();
//                koreMedia.setMediaType(BitmapUtils.getAttachmentType(componentType));
//                HashMap<String, Object> cmpData = new HashMap<>(1);
//                cmpData.put("fileName", mediaFileName);
//
//                koreMedia.setComponentData(cmpData);
//                koreMedia.setMediaFileName(getComponentId(componentType));
//                koreMedia.setMediaFilePath(mediaFilePath);
//                koreMedia.setFileSize(fileSize);
//
//                koreMedia.setMediafileId(mediaFileId);
//                koreMedia.setMediaThumbnail(thumbnailURL);
//
//
//                composeFooterFragment.setSectionSelected(/*KoraMainComposeFragment.SECTION_TYPE.SECTION_COMPOSE_WITH_COMPOSE_BAR*/);
//                messageHandler.postDelayed(new Runnable() {
//                    public void run() {
//                        HashMap<String, String> attachmentKey = new HashMap<>();
//                        attachmentKey.put("fileName", mediaFileName + "." + MEDIA_TYPE);
//                        attachmentKey.put("fileType", componentType);
//                        attachmentKey.put("fileId", mediaFileId);
//                        attachmentKey.put("localFilePath", mediaFilePath);
//                        attachmentKey.put("fileExtn", MEDIA_TYPE);
//                        attachmentKey.put("thumbnailURL", thumbnailURL);
//                        composeFooterFragment.addAttachmentToAdapter(attachmentKey);
//                       /* KoraSocketConnectionManager.getInstance().sendMessageWithCustomDataAttchment(mediaFileName+"."+MEDIA_TYPE, attachmentKey, false);
//                        KoraSocketConnectionManager.getInstance().stopDelayMsgTimer();
//                        toggleVisibilities(false, true);*/
//                    }
//                }, 400);
//
//
//                // kaComponentModels.add(koreMedia);
//                // insertTags(koreMedia, componentType, orientation, mediaFileName);
//
//            } else {
//                String errorMsg = reply.getString(UploadBulkFile.error_msz_key);
//                if (!TextUtils.isEmpty(errorMsg)) {
//                    Log.i("File upload error", errorMsg);
//                    showToast(errorMsg);
//                }
//            }
//        }
//    };

    public void mediaAttachment(HashMap<String, String> attachmentKey)
    {
        composeFooterFragment.setSectionSelected(/*KoraMainComposeFragment.SECTION_TYPE.SECTION_COMPOSE_WITH_COMPOSE_BAR*/);
        messageHandler.postDelayed(new Runnable() {
            public void run() {

                composeFooterFragment.addAttachmentToAdapter(attachmentKey);
                       /* KoraSocketConnectionManager.getInstance().sendMessageWithCustomDataAttchment(mediaFileName+"."+MEDIA_TYPE, attachmentKey, false);
                        KoraSocketConnectionManager.getInstance().stopDelayMsgTimer();
                        toggleVisibilities(false, true);*/
            }
        }, 400);
    }

    private int getFileLimit() {
        attachment = SharedPreferenceUtils.getInstance(this).getAttachmentPref("");

        int file_limit = -1;
        if (attachment != null) {
            file_limit = attachment.getSize();
        }

        return file_limit;
    }

    private String getComponentId(String componentType) {
        if (componentType.equalsIgnoreCase(KoreMedia.MEDIA_TYPE_IMAGE)) {
            return "image_" + System.currentTimeMillis();
        } else if (componentType.equalsIgnoreCase(KoreMedia.MEDIA_TYPE_VIDEO)) {
            return "video_" + System.currentTimeMillis();
        } else {
            return "doc_" + System.currentTimeMillis();
        }
    }

    private void getBrandingDetails() {
        Call<ArrayList<BrandingNewModel>> getBankingConfigService = BrandingRestBuilder.getRestAPI().getBrandingNewDetails("bearer " + SocketWrapper.getInstance(BotChatActivity.this).getAccessToken(), SDKConfiguration.Client.tenant_id, "published", "1","en_US", SDKConfiguration.Client.bot_id);
        getBankingConfigService.enqueue(new Callback<ArrayList<BrandingNewModel>>() {
            @Override
            public void onResponse(Call<ArrayList<BrandingNewModel>> call, Response<ArrayList<BrandingNewModel>> response) {
                if (response.isSuccessful())
                {
                    arrBrandingNewDos = response.body();

                    if(arrBrandingNewDos != null && arrBrandingNewDos.size() > 0)
                    {
                        BotOptionsModel botOptionsModel = arrBrandingNewDos.get(0).getHamburgermenu();

                        if(composeFooterFragment != null)
                            composeFooterFragment.setBottomOptionData(botOptionsModel);

                        if(arrBrandingNewDos.size() > 1)
                            onEvent(arrBrandingNewDos.get(1).getBrandingwidgetdesktop());

//                        if(isItFirstConnect)
//                        {
//                            botClient.sendMessage("BotNotifications");
//                            isItFirstConnect = false;
//                        }
                    }
                }
                else
                {
                    if(isItFirstConnect)
                    {
                        botClient.sendMessage("BotNotifications");
                        isItFirstConnect = false;
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<BrandingNewModel>> call, Throwable t)
            {
                Log.e("getBrandingDetails", t.toString());

                if(isItFirstConnect)
                {
                    botClient.sendMessage("BotNotifications");
                    isItFirstConnect = false;
                }
            }
        });
    }
}