package com.smart.lock.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.igexin.sdk.PushConsts;
import com.igexin.sdk.PushManager;
import com.smart.lock.R;
import com.smart.lock.activity.*;
import com.smart.lock.common.FavoriteTypeEnum;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.dto.*;
import com.smart.lock.response.BaseResponse;
import com.smart.lock.response.CampaignListResponse;
import com.smart.lock.response.ContentListResponse;
import com.smart.lock.service.LockService;
import com.smart.lock.utils.CampaignDataOperator;
import com.smart.lock.utils.ContentsDataOperator;
import com.smart.lock.utils.DisplayUtils;
import com.smart.lock.utils.FileUtils;
import com.smart.lock.utils.NetUtils;
import com.smart.lock.utils.PageChangeUtils;
import com.smart.lock.utils.PollingUtils;
import com.smart.lock.utils.*;

import java.util.Calendar;
import java.util.Date;

public class PushReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Log.d("GetuiSdkDemo", "onReceive() action=" + bundle.getInt("action"));
        switch (bundle.getInt(PushConsts.CMD_ACTION)) {

            case PushConsts.GET_MSG_DATA:
                // 获取透传数据
                // String appid = bundle.getString("appid");
                byte[] payload = bundle.getByteArray("payload");

                String taskid = bundle.getString("taskid");
                String messageid = bundle.getString("messageid");

                // smartPush第三方回执调用接口，actionid范围为90000-90999，可根据业务场景执行
                boolean result = PushManager.getInstance().sendFeedbackMessage(
                        context, taskid, messageid, 90001);
                System.out.println("第三方回执接口调用" + (result ? "成功" : "失败"));

                if (payload != null) {
                    String data = new String(payload);
                    Log.d("GetuiSdkDemo", "Got Payload:" + data);
                    Gson gson = new Gson();
                    ContentListResponse respObj = null;
                    try {
                        respObj = gson.fromJson(data, ContentListResponse.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (respObj != null && respObj.getData() != null
                            && respObj.getData().size() > 0) {
                        // local initial
                        Date nowTimeStr = Calendar.getInstance().getTime();
                        for (ContentDTO content : respObj.getData()) {
                            content.setLocalPath("");
                            content.setLocalViewCount(0);
                            content.setDislike(false);
                            content.setFavorite(false);
                            content.setDownloadTime(nowTimeStr);
                            ContentsDataOperator.add(context, content);
                        }
                        Log.d("test update boardcast", "test update boardcast " + DisplayUtils.getLineInfo());
                        PageChangeUtils.changeLockServiceState(context, LockService.STATE_UPDATE_DATE);
                    } else {
                        BaseMessageDTO baseMessageDTO = gson.fromJson(data,
                                BaseMessageDTO.class);
                        switch (baseMessageDTO.getType()) {
                            case 1:
                                MessageDTO respMessageObj = gson.fromJson(
                                        baseMessageDTO.getData(), MessageDTO.class);
                                sendnotify(context, respMessageObj);
                                break;
                            case 2:
                                CommonMessageDTO respCommonMessageObj = gson.fromJson(
                                        baseMessageDTO.getData(),
                                        CommonMessageDTO.class);
                                sendCommonNotify(context, respCommonMessageObj);
                                break;
                            case 3:
                                CampaignListResponse respCampaignMessageObj = null;
                                try {
                                    respCampaignMessageObj = gson.fromJson(
                                            baseMessageDTO.getData(),
                                            CampaignListResponse.class);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                for (CampaignDTO campaignDTO : respCampaignMessageObj.getData()) {
                                    CampaignDataOperator.add(context, campaignDTO);
                                }
                                PageChangeUtils.sendDBUpdateBroadcast(context);
                                break;
                        }
                    }
                }
                break;
            case PushConsts.GET_CLIENTID:
                // 获取ClientID(CID)
                // 第三方应用需要将CID上传到第三方服务器，并且将当前用户帐号和CID进行关联，以便日后通过用户帐号查找CID进行消息推送
                final String cid = bundle.getString("clientid");
                Log.d("PushGeTuiClientId", cid);
                final SharedPreferences commonSP = context.getSharedPreferences(
                        "common", 0);
                String sClientId = commonSP.getString("clientid", null);
                if (!TextUtils.isEmpty(cid) && !cid.equals(sClientId)) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Gson gson = new Gson();
                            String respStr;
                            respStr = NetUtils
                                    .upDateGTID(
                                            context,
                                            SlideConstants.SERVER_URL
                                                    + SlideConstants.SERVER_METHOD_UPDATE_GETUI,
                                            cid);
                            BaseResponse resp = gson.fromJson(respStr,
                                    BaseResponse.class);
                            if (resp != null) {
                                if (resp.getCode() == SlideConstants.SERVER_RETURN_SUCCESS) {
                                    Editor editor = commonSP.edit();
                                    editor.putString("clientid", cid);
                                    editor.commit();
                                } else {
                                    FileUtils.addFileLog("update clientid:" + cid
                                            + " error. Error Code:"
                                            + resp.getCode());
                                }
                            }
                        }
                    }).start();

                }

                break;
            case PushConsts.THIRDPART_FEEDBACK:
            /*
             * String appid = bundle.getString("appid"); String taskid =
			 * bundle.getString("taskid"); String actionid =
			 * bundle.getString("actionid"); String result =
			 * bundle.getString("result"); long timestamp =
			 * bundle.getLong("timestamp");
			 * 
			 * Log.d("GetuiSdkDemo", "appid = " + appid); Log.d("GetuiSdkDemo",
			 * "taskid = " + taskid); Log.d("GetuiSdkDemo", "actionid = " +
			 * actionid); Log.d("GetuiSdkDemo", "result = " + result);
			 * Log.d("GetuiSdkDemo", "timestamp = " + timestamp);
			 */
                break;
            default:
                break;
        }
    }

    void sendnotify(Context context, MessageDTO respMessageObj) {
        // Notification notification = new
        // Notification.Builder(context).setTicker(respMessageObj.getTitle()).setSmallIcon(R.drawable.icon_notify).build();
        Notification notification = new Notification(R.drawable.logo,
                respMessageObj.getTitle(), System.currentTimeMillis());

        notification.flags = Notification.FLAG_AUTO_CANCEL;

        Intent activityIntent = new Intent();
        activityIntent.setClass(context, WebViewActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putInt("type", SlideConstants.WEB_VIEW_MYMESSAGE_DETAIL);
        bundle.putString("preActivity", "PushReceiver");
        int id = respMessageObj.getId();

        SharedPreferences commonSP = context.getSharedPreferences("common", 0);
        Editor editor = commonSP.edit();
        editor.putInt("messageId", id);
        editor.putString("preActivity", "PushReceiver");
        editor.commit();
        activityIntent.putExtras(bundle);

        PendingIntent contentIntent = PendingIntent.getActivity(context, 106,
                activityIntent, 0);

        notification.setLatestEventInfo(context, "遇见锁屏",
                respMessageObj.getTitle(), contentIntent);

        NotificationManager nm = (NotificationManager) context
                .getSystemService(context.NOTIFICATION_SERVICE);
        nm.cancel(SlideConstants.NOTIFICATION_ID + 6);
        nm.notify(SlideConstants.NOTIFICATION_ID + 6, notification);
    }

    void sendCommonNotify(Context context, CommonMessageDTO respMessageObj) {
        Notification notification = new Notification(R.drawable.logo,
                respMessageObj.getTitle(), System.currentTimeMillis());
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        Intent activityIntent = new Intent();
        Bundle bundle = new Bundle();

        switch (respMessageObj.getTarget()) {
            case 1:
                //target=1 跳转至首页
                activityIntent.setClass(context, MainActivity.class);
                break;
            case 2:
                //target=2 跳转至收益页
                activityIntent.setClass(context, AmountActivity.class);
                bundle.putString("type", "amount");
                bundle.putString("preActivity", "PushReceiver");
//            SharedPreferences commonSP = context.getSharedPreferences("common",
//                    0);
//            Editor editor = commonSP.edit();
//            editor.putBoolean("hasShared", true);
//            editor.commit();
                activityIntent.putExtras(bundle);
                break;
            case 3:
                //target=3 跳转至大图页
                ContentDTO content = ContentsDataOperator.getById(context, respMessageObj.getContentId());
                if (content != null) {
                    activityIntent.setClass(context, DetailActivity.class);

                    bundle.putString("preActivity", "PushReceiver");
                    bundle.putInt("contentId", content.getId());
                    bundle.putInt("hasMore", content.getHasMore());
                    bundle.putString("image", content.getImage());
                    bundle.putString("localPath", content.getLocalPath());
                    bundle.putString("link", content.getLink());
                    bundle.putInt("bonusType", content.getType());

                    String bonusAmount = String.format("%.2f", content.getBonusAmount());
                    bundle.putString("bonusAmount", bonusAmount);

                    bundle.putInt(
                            "assnType",
                            content.isFavorite() ? FavoriteTypeEnum.FAVORITE
                                    .getValue() : FavoriteTypeEnum.VIEWED
                                    .getValue());
                    bundle.putString("title", content.getTitle());
                    bundle.putString("content", content.getContent());
                    activityIntent.putExtras(bundle);
                } else {
                    activityIntent.setClass(context, MainActivity.class);
                }
                break;
            case 4:
                //target=4 跳转至登录页
                activityIntent.setClass(context, LoginActivity.class);
                activityIntent.putExtra("bindMobile", true);
                break;
            case 5:
                //target=5 跳转至webview
                activityIntent.setClass(context, WebViewActivity.class);

                bundle.putInt("type", SlideConstants.WEB_VIEW_RECOMMAND);
                bundle.putString("link", respMessageObj.getUrl());
                bundle.putString("title", respMessageObj.getTitle());
                bundle.putString("preActivity", "PushReceiver");
                activityIntent.putExtras(bundle);
                break;
            default:
                //其他跳转至首页
                activityIntent.setClass(context, MainActivity.class);
                break;
        }

        PendingIntent contentIntent = PendingIntent.getActivity(context, 100,
                activityIntent, 0);

        notification.setLatestEventInfo(context, "遇见锁屏",
                respMessageObj.getTitle(), contentIntent);

        NotificationManager nm = (NotificationManager) context
                .getSystemService(context.NOTIFICATION_SERVICE);
        nm.cancel(SlideConstants.NOTIFICATION_ID + 1);
        nm.notify(SlideConstants.NOTIFICATION_ID + 1, notification);
    }
}
