package com.kore.findlysdk.bot;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kore.findlysdk.models.BotInfoModel;
import com.kore.findlysdk.models.BotSocketOptions;
import com.kore.findlysdk.models.LinkedBotNLModel;
import com.kore.findlysdk.models.RestResponse;
import com.kore.findlysdk.net.SDKConfiguration;
import com.kore.findlysdk.utils.BundleConstants;
import com.kore.findlysdk.utils.StringUtils;
import com.kore.findlysdk.utils.Utils;
import com.kore.findlysdk.websocket.SocketConnectionListener;
import com.kore.findlysdk.websocket.SocketWrapper;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Created by Ramachandra Pradeep on 6/13/2016.
 * Copyright (c) 2014 Kore Inc. All rights reserved.
 */

/**
 * Gateway for clients to interact with Bots.
 */
public class BotClient {
    private Context mContext;
    private String custData, linkedBotNLMeta;
    private SharedPreferences sharedPreferences;

    public RestResponse.BotCustomData getCustomData() {
        return customData;
    }

    public void setCustomData(RestResponse.BotCustomData customData) {
        this.customData = customData;
    }

    private RestResponse.BotCustomData customData;

    public BotInfoModel getBotInfoModel() {
        return botInfoModel;
    }

    public void setBotInfoModel(BotInfoModel botInfoModel) {
        this.botInfoModel = botInfoModel;
    }

    private BotInfoModel botInfoModel;
    private BotClient() {
    }

    /**
     * @param mContext
     */
    public BotClient(Context mContext) {
        this.customData = new RestResponse.BotCustomData();
        this.mContext = mContext.getApplicationContext();
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }
    public BotClient(Context mContext, RestResponse.BotCustomData customData){
        this.mContext = mContext;
        this.customData = customData;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }


    public void connectAsAnonymousUserForKora(String userAccessToken, String jwtToken, String chatBotName, String taskBotId, SocketConnectionListener socketConnectionListener,
                                              String url, String botUserId, String auth) {
//        String uuid = UUID.randomUUID().toString();//"e56dd516-5491-45b2-9ff7-ffcb7d8f2461";
        botInfoModel = new BotInfoModel(chatBotName,taskBotId,customData);
        SocketWrapper.getInstance(mContext).ConnectAnonymousForKora(userAccessToken, jwtToken,botInfoModel, socketConnectionListener,url, botUserId, auth);
    }
    /**
     * Connection for anonymous user
     *
     * @param socketConnectionListener
     */
    public void connectAsAnonymousUser(String jwtToken, String chatBotName, String taskBotId, SocketConnectionListener socketConnectionListener) {

        String uuid = UUID.randomUUID().toString();//"e56dd516-5491-45b2-9ff7-ffcb7d8f2461";
        botInfoModel = new BotInfoModel(chatBotName,taskBotId,customData);
        SocketWrapper.getInstance(mContext).connectAnonymous(jwtToken, botInfoModel,  socketConnectionListener,null);
    }


    public void shouldAttemptToReconnect(boolean value){
        SocketWrapper.getInstance(mContext).shouldAttemptToReconnect(value);
    }
    /**
     * Connection for anonymous user
     *
     * @param socketConnectionListener
     */
    public void connectAsAnonymousUserWithOptions(String jwtToken, String chatBotName,
                                                  String taskBotId, SocketConnectionListener socketConnectionListener, BotSocketOptions options) {

        String uuid = UUID.randomUUID().toString();//"e56dd516-5491-45b2-9ff7-ffcb7d8f2461";
        botInfoModel = new BotInfoModel(chatBotName,taskBotId,customData);
        SocketWrapper.getInstance(mContext).connectAnonymous(jwtToken, botInfoModel, socketConnectionListener,options);
    }



    public String generateJWT(String email,String secret,String clientId, boolean isAnonymousUser){
        long curTime = System.currentTimeMillis();
        long expTime = curTime+86400000;
//        hsh.put("clientSecret",clientSecret);

        return Jwts.builder().claim("iss", clientId).claim("iat",curTime).claim("exp",expTime)
                .claim("aud","https://idproxy.kore.com/authorize").claim("sub", email).claim("isAnonymous", isAnonymousUser).
                        signWith(SignatureAlgorithm.HS256,secret.getBytes()).compact();
    }

    public  String getAccessToken(){
        return SocketWrapper.getInstance(mContext).getAccessToken();
    }

    public  String getUserId(){
        return SocketWrapper.getInstance(mContext).getBotUserId();
    }
    /**
     * [MANDATORY] Invoke this method to disconnect the previously connected socket connection.
     */
    public void disconnect() {
        SocketWrapper.getInstance(mContext).disConnect();
    }

    /**
     * @return whether socket connection is present
     */
    public boolean isConnected() {
        return SocketWrapper.getInstance(mContext).isConnected();
    }

    /**
     * Method to send messages over socket.
     * It uses FIFO pattern to first send if any pending requests are present
     * following current request later onward.
     * <p/>
     * pass 'msg' as NULL on reconnection of the socket to empty the pool
     * by sending messages from the pool.
     *
     * @param msg
     */
    public void sendMessage(String msg) {

        if (msg != null && !msg.isEmpty()) {
            RestResponse.BotPayLoad botPayLoad = new RestResponse.BotPayLoad();
            RestResponse.BotMessage botMessage = new RestResponse.BotMessage(msg);

            if(sharedPreferences != null)
            {
                custData = sharedPreferences.getString(BundleConstants.CUSTOM_DATA, "");
                if(!StringUtils.isNullOrEmpty(custData))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(custData);
                        HashMap<String, Object> mapObj = new Gson().fromJson(jsonObject.toString(), HashMap.class);

                        botInfoModel = new BotInfoModel(SDKConfiguration.Client.bot_name, SDKConfiguration.Client.bot_id, mapObj);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    botInfoModel = new BotInfoModel(SDKConfiguration.Client.bot_name, SDKConfiguration.Client.bot_id,customData);
                }

                linkedBotNLMeta = sharedPreferences.getString(BundleConstants.LINKED_BOT_NL_META, "");
                if(!StringUtils.isNullOrEmpty(linkedBotNLMeta))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(linkedBotNLMeta);
                        LinkedBotNLModel linkedBotNLModel = new Gson().fromJson(jsonObject.toString(), LinkedBotNLModel.class);
                        botPayLoad.setLinkedBotNLMeta(linkedBotNLModel);

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(BundleConstants.LINKED_BOT_NL_META, "");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            customData.put("botToken",getAccessToken());
            botMessage.setCustomData(customData);
            botPayLoad.setMessage(botMessage);
            botPayLoad.setMyInterface("conversationalSearch");
            botPayLoad.setBotInfo(botInfoModel);

            RestResponse.Meta meta = new RestResponse.Meta(TimeZone.getDefault().getID(), Locale.getDefault().getISO3Language());
            botPayLoad.setMeta(meta);

            Gson gson = new Gson();
            String jsonPayload = gson.toJson(botPayLoad);

            Log.d("BotClient", "Payload : " + jsonPayload);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("Payload", jsonPayload);
            editor.commit();

            SocketWrapper.getInstance(mContext).sendMessage(jsonPayload);
        }
//        sendQueMessages();
    }

    public void updateAuthToken(String accessToken){
        if(customData != null){
            customData.put("kmToken",accessToken);
        }
    }

    public void sendFormData(String payLoad,String message) {

        if (payLoad != null && !payLoad.isEmpty()) {
            RestResponse.BotPayLoad botPayLoad = new RestResponse.BotPayLoad();
            RestResponse.BotMessage botMessage = new RestResponse.BotMessage(payLoad);

            if(sharedPreferences != null)
            {
                custData = sharedPreferences.getString(BundleConstants.CUSTOM_DATA, "");
                if(!StringUtils.isNullOrEmpty(custData))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(custData);
                        HashMap<String, Object> mapObj = new Gson().fromJson(jsonObject.toString(), HashMap.class);

                        botInfoModel = new BotInfoModel(SDKConfiguration.Client.bot_name, SDKConfiguration.Client.bot_id, mapObj);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                    botInfoModel = new BotInfoModel(SDKConfiguration.Client.bot_name, SDKConfiguration.Client.bot_id,customData);
                }

                linkedBotNLMeta = sharedPreferences.getString(BundleConstants.LINKED_BOT_NL_META, "");
                if(!StringUtils.isNullOrEmpty(linkedBotNLMeta))
                {
                    try
                    {
                        JSONObject jsonObject = new JSONObject(linkedBotNLMeta);
                        LinkedBotNLModel linkedBotNLModel = new Gson().fromJson(jsonObject.toString(), LinkedBotNLModel.class);
                        botPayLoad.setLinkedBotNLMeta(linkedBotNLModel);

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(BundleConstants.LINKED_BOT_NL_META, "");
                        editor.commit();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            customData.put("botToken",getAccessToken());
            botMessage.setCustomData(customData);
            botMessage.setParams(Utils.jsonToMap(payLoad));
            botPayLoad.setMessage(botMessage);
            botPayLoad.setBotInfo(botInfoModel);

            RestResponse.Meta meta = new RestResponse.Meta(TimeZone.getDefault().getID(), Locale.getDefault().getISO3Language());
            botPayLoad.setMeta(meta);

            Gson gson = new Gson();
            String jsonPayload = gson.toJson(botPayLoad);

            Log.d("BotClient", "Payload : " + jsonPayload);
            SocketWrapper.getInstance(mContext).sendMessage(jsonPayload);
//            BotRequestPool.getBotRequestStringArrayList().add(jsonPayload);
//            sendQueMessages();
        }

    }

}
