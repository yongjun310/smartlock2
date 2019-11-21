package com.smart.lock.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.gson.Gson;
import com.smart.lock.common.FavoriteTypeEnum;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.dto.CampaignDTO;
import com.smart.lock.dto.AccountActionLogDTO;
import com.smart.lock.dto.ContentDTO;
import com.smart.lock.response.BaseResponse;
import com.smart.lock.response.CampaignListResponse;
import com.smart.lock.response.ContentListResponse;
import com.smart.lock.service.LockService;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.*;

public class ThreadHelper {

    private static Gson gson = new Gson();

    private static ThreadHelper instance;

    private Map<String, Date> threadTask = new HashMap<String, Date>();

    private Context ctx;
    
    private Thread picThread;
    public ThreadHelper(Context ctx) {
        this.ctx = ctx;
    }

    public synchronized static ThreadHelper getInstance(Context ctx) {
        if (null == instance) {
            instance = new ThreadHelper(ctx);
        }
        return instance;
    }

    private Handler messHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    instance.threadTask.remove(msg.obj.toString());
                    picThread = null;
                    break;
                default:
                    break;
            }
        }
    };

    public synchronized void addTask(String taskName) {
        Log.d("addTask", "enter:" + taskName);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, -5);

        Date lastTaskTime = threadTask.get(taskName);
        if (lastTaskTime == null || lastTaskTime.before(c.getTime())) {
            threadTask.put(taskName, Calendar.getInstance().getTime());
            // TODO 反射方法，这里就简单的先判断
            if ("postLogs".equals(taskName)) {
                if (DeviceUtils.isNetworkAvailable(ctx)) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            postLogs();
                            messHandler.sendMessage(Message.obtain(
                                    instance.messHandler, 1, "postLogs"));
                        }
                    }).start();
                }
            }

            if (SlideConstants.THREAD_DISTRIBUTE_CONTENT.equals(taskName)) {
            	if (DeviceUtils.isNetworkAvailable(ctx)) {
	                new Thread(new Runnable() {
	                    @Override
	                    public void run() {
	                    	updateCategorySetting(ctx);
	                        updateCampaign();
	                    }
	                }).start();
            	}
                ThreadHelper.getInstance(ctx).addTask(SlideConstants.THREAD_DOWNLOAD_PIC);
                String lastLoadTime = SharedPreferencesUtils.getStringSP(ctx, SharedPreferencesUtils.LOAD_DATA_TIME, null);
                Date lastLoadDate = null;
                Calendar calendar = Calendar.getInstance();
                if (lastLoadTime != null) {
                    lastLoadDate = DisplayUtils.parseDatetime(lastLoadTime);
                    calendar.setTime(lastLoadDate);
                }

                if (DeviceUtils.isNetworkAvailable(ctx) &&
                        (lastLoadDate == null || Math.abs(System.currentTimeMillis() - calendar.getTimeInMillis()) >
                                SlideConstants.UPDATE_CONTENT_SPAN * 60 * 1000)) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            loadContentFromServer();
                            messHandler.sendMessage(Message.obtain(
                                    instance.messHandler, 1,
                                    "loadContentFromServer"));
                        }
                    }).start();
                }
            }

            if (SlideConstants.THREAD_DOWNLOAD_PIC.equals(taskName)) {
                if(picThread == null) {
                	picThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            downloadPic();
                            messHandler.sendMessage(Message.obtain(
                                    instance.messHandler, 1, "downloadPic"));
                        }
                    });
                	picThread.start();
                }
            }
        }
    }

    private static int getAccountId(Context ctx) {
        SharedPreferences commonSP = ctx.getSharedPreferences("common",
                Context.MODE_MULTI_PROCESS );
        return commonSP.getInt("accountId", -1);
    }

    private static int getSex(Context ctx) {
        SharedPreferences commonSP = ctx.getSharedPreferences("common",
                Context.MODE_MULTI_PROCESS );
        return commonSP.getInt("sex", 0);
    }

    private static RSAPublicKey getPublicKey(Context ctx, int accountId) {
        SharedPreferences mySP = ctx.getSharedPreferences("rsa" + accountId,
                Context.MODE_MULTI_PROCESS );
        String modulus = mySP.getString("modulus", "");
        String publicExponent = mySP.getString("publicExponent", "");
        return SecurityUtils.getPublicKey(modulus, publicExponent);
    }

    public void postLogs() {
        String postData = ActionLogOperator.getPostData(ctx);

        if (postData == null || postData.length() == 0)
            return;

        int accountId = getAccountId(ctx);

        try {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("androidId", DeviceUtils
                    .getAndroidId(ctx)));
            params.add(new BasicNameValuePair("time",
                    DisplayUtils.formatDateTimeString(Calendar.getInstance()
                            .getTime())));
            params.add(new BasicNameValuePair("data", postData));

            String inputStr = SecurityUtils.getInputString(params);

            RSAPublicKey publicKey = getPublicKey(ctx, accountId);
            String sign = "";

            sign = SecurityUtils.encryptByPublicKey(inputStr, publicKey);
            params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("accountId", String
                    .valueOf(accountId)));
            params.add(new BasicNameValuePair("sign", sign));

            String respStr = NetUtils.post(SlideConstants.SERVER_URL
                    + SlideConstants.SERVER_METHOD_POST_LOG, params);

            BaseResponse resp = gson.fromJson(respStr, BaseResponse.class);
            if (resp.getCode() == SlideConstants.SERVER_RETURN_SUCCESS) {
                ActionLogOperator.deleteAll(ctx);
            }
        } catch (Exception e) {
            Log.e("postLogs", e.toString());
        }

    }

    private void loadContentFromServer() {
        /*List<ContentDTO> contentList = ContentsDataOperator.load(ctx);

        if (contentList.size() >= SlideConstants.MAX_LOCAL_CONTENT_SIZE)
            return;
        */
    	long start = System.currentTimeMillis();
    	Log.d("ThreadHelper", "enterLoadContentFromServer time:" + start);
        int accountId = getAccountId(ctx);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("androidId", DeviceUtils
                .getAndroidId(ctx)));
        params.add(new BasicNameValuePair("quantity", String
                .valueOf(SlideConstants.MAX_LOAD_CONTENT_SIZE)));
        params.add(new BasicNameValuePair("time", DisplayUtils.formatDateTimeString(Calendar.getInstance().getTime())));
        params.add(new BasicNameValuePair("sex", String.valueOf(getSex(ctx))));

        RSAPublicKey publicKey = getPublicKey(ctx, accountId);

        String data = SecurityUtils.getInputString(params);
        String sign = "";

        try {
            sign = SecurityUtils.encryptByPublicKey(data, publicKey);

            params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("accountId", String
                    .valueOf(accountId)));
            params.add(new BasicNameValuePair("sign", sign));

            String resp = NetUtils.post(SlideConstants.SERVER_URL
                    + SlideConstants.SERVER_METHOD_DISTRIBUTE, params);

        	Log.d("ThreadHelper", "server response time:" + (System.currentTimeMillis() - start));
            Log.d("loadContentFromServer", "server response:" + resp);

            ContentListResponse respObj = gson.fromJson(resp,
                    ContentListResponse.class);

            Date serverTimeStr = respObj.getServerTime();

            //1435197119000 serverTime:2015-06-24 19:14:14

            String serverTime = DisplayUtils.formatDateTimeString(serverTimeStr);
            Log.d("loadContentFromServer", "serverDate:" + serverTimeStr.getTime() + " serverTime:" + serverTime);
            SharedPreferencesUtils.putStringSP(ctx, SharedPreferencesUtils.SERVER_TIME, serverTime);

            if (respObj.getCode() == SlideConstants.SERVER_RETURN_SUCCESS
                    && respObj.getData() != null
                    && respObj.getData().size() > 0) {
                Log.d("loadContentFromServer", "gson converted contentList size:" + respObj.getData().size());
                // local initial
                for (ContentDTO content : respObj.getData()) {
                    content.setLocalPath("");

                    String imgFilePath = FileUtils.getImageDir(ctx) + content.getId() + ".dat";
                    if (FileUtils.getFileSize(imgFilePath) > 0) {
                        content.setLocalPath(imgFilePath);
                    }
                    content.setLocalViewCount(0);
                    content.setDislike(false);
                    content.setFavorite(content.getAccountAssnType() == FavoriteTypeEnum.FAVORITE.getValue());
                    content.setDownloadTime(serverTimeStr);
                    ContentsDataOperator.add(ctx, content);
                }

                Log.d("test update boardcast", "test update boardcast " + DisplayUtils.getLineInfo());
                PageChangeUtils.changeLockServiceState(ctx, LockService.STATE_UPDATE_DATE);

            	Log.d("ThreadHelper", "addAll contents time:" + (System.currentTimeMillis() - start) +
            							"/n sendDBUpdateBroadcast time:" + System.currentTimeMillis());
				PageChangeUtils.sendDBUpdateBroadcast(ctx);
                
                SharedPreferencesUtils.putStringSP(ctx, SharedPreferencesUtils.LOAD_DATA_TIME,
                        DisplayUtils.formatDateTimeString(new Date(System.currentTimeMillis())));
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(serverTimeStr);
                calendar.add(Calendar.DATE, -10);
                String sql = "SELECT * FROM content WHERE date(startTime) <= date('" + DisplayUtils.formatDateTimeString(calendar.getTime()) + "')";

                List<ContentDTO> contentDTOList = ContentsDataOperator.load(ctx, sql);
                Log.d("ThreadHelper", "delete Old Pics:" + contentDTOList.size());
                for (ContentDTO contentDTO : contentDTOList) {
                    FileUtils.deleteFile(contentDTO.getLocalPath());
                }
                ContentsDataOperator.deleteBeforeDate(ctx, DisplayUtils.formatDateTimeString(calendar.getTime()));

                contentDTOList = ContentsDataOperator.load(ctx);
                File filedir = new File(SlideConstants.EXTERNAL_IMAGE_PATH);
                File[] files = filedir.listFiles();
                if (files.length > 0) {
                    for (int j = 0; j < files.length; j++) {
                        if (!files[j].isDirectory()) {
                            boolean exist = false;
                            for(ContentDTO contentDTO:contentDTOList) {
                                if (contentDTO.getLocalPath() != null &&
                                        contentDTO.getLocalPath().contains(files[j].getName())) {
                                    exist = true;
                                }
                            }
                            if(!exist) {
                                files[j].delete();
                            }

                        }
                    }
                }
                Log.d("ThreadHelper", "add thread task:downloadPic");
            }
        } catch (Exception e) {
            Log.e("content:", e.toString());
        }
    }

    private void updateCampaign(){
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("androidId", DeviceUtils
                .getAndroidId(ctx)));
        params.add(new BasicNameValuePair("quantity", String
                .valueOf(SlideConstants.MAX_LOAD_CONTENT_SIZE)));
        params.add(new BasicNameValuePair("time", DisplayUtils.formatDateTimeString(Calendar.getInstance().getTime())));
        params.add(new BasicNameValuePair("sex", String.valueOf(getSex(ctx))));
        try {
            String resp1 = NetUtils.post(SlideConstants.SERVER_URL
                    + SlideConstants.SERVER_METHOD_LOAD_CAMPAIGNS, params);

            Log.d("loadCampaignFromServer", "loadCampaign server response:" + resp1);

            CampaignListResponse respObj1 = gson.fromJson(resp1,
                    CampaignListResponse.class);

            if (respObj1.getCode() == SlideConstants.SERVER_RETURN_SUCCESS
                    && respObj1.getData() != null
                    && respObj1.getData().size() > 0) {
                Log.d("loadCampaignFromServer", "gson converted CampaignList size:" + respObj1.getData().size());
                // local initial
                CampaignDataOperator.addAll(ctx, respObj1.getData());
                Date serverTimeStr = respObj1.getServerTime();

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(serverTimeStr);
                CampaignDataOperator.deleteBeforeDate(ctx, DisplayUtils.formatDateTimeString(calendar.getTime()));
            }

			PageChangeUtils.sendDBUpdateBroadcast(ctx);
        } catch (Exception e) {
            Log.e("content:", e.toString());
        }
    }

    public void downloadPic() {
        Log.d("downloadPic", "enter");
        DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        boolean hasNew = false;
        String serverTime = SharedPreferencesUtils.getServerTimeStr(ctx);
        String sql = "SELECT * FROM content WHERE date(startTime) = date('" + serverTime + "') "
                + " ORDER BY startTime DESC";
        
        List<ContentDTO> contentList = ContentsDataOperator.load(ctx, "SELECT * FROM content WHERE"
        		+ " (localPath = '' or localPath is NUll) AND (date(startTime) = date('" + 
        		serverTime + "') or isFavorite = 1) ORDER BY startTime ASC");

        String filePrefix = FileUtils.getImageDir(ctx);

        File dirFile = new File(filePrefix);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        int downloadPicCount = 0;
        for (int i = contentList.size() - 1; i >= 0; i--) {
            Log.d("downloadPic", "enter for loop contentList size:" + contentList.size());
            boolean isWifi = DeviceUtils.isNetworkAvailable(ctx)
                    && DeviceUtils.isWifi(ctx);
            if (downloadPicCount > SlideConstants.DOWNLOAD_PIC_BATCH_COUNT || !isWifi)
                break;
            ContentDTO content = contentList.get(i);

            if ("preLoad".equals(content.getLocalPath())) {
                Log.d("downloadPic", "enter preLoad");
                continue;
            }

            try {
                String imgFilePath = filePrefix + content.getId() + ".dat";
                if (FileUtils.getFileSize(imgFilePath) > 0) {
                    Log.d("downloadPic", "enter getFileSize");
                    if (content.getLocalPath() == null
                            || content.getLocalPath().length() == 0) {
                        content.setLocalPath(imgFilePath);
                        ContentsDataOperator.updateWithoutNoti(ctx, content);
                        hasNew = true;
                        //SharedPreferencesUtils.putBoolSP(ctx, SharedPreferencesUtils.HAS_NEW, true);
                    }
                } else {
                    if (FileUtils.downloadImage(
                            content.getImage()
                                    + String.format(
                                    SlideConstants.QINIU_PIC_PARAM,
                                    screenHeight, screenWidth),
                            imgFilePath)) {
                        content.setLocalPath(imgFilePath);
                        ContentsDataOperator.updateWithoutNoti(ctx, content);
                        hasNew = true;
                        downloadPicCount++;
                        //SharedPreferencesUtils.putBoolSP(ctx, SharedPreferencesUtils.HAS_NEW, true);

                        ActionLogOperator.add(ctx, new AccountActionLogDTO(SharedPreferencesUtils.getIntSP(ctx, "accountId", 0), 64, content.getId()));
                    }
                }
            } catch (Exception e) {
                Log.e("image:", e.toString());
            }
        }
        if(hasNew) {
        	PageChangeUtils.changeLockServiceState(ctx, LockService.STATE_UPDATE_DATE);
        }
    }


    private void updateCategorySetting(Context ctx) {
        String settingIds = SharedPreferencesUtils.getStringSP(ctx, SharedPreferencesUtils.CATEGORY_ID_SETTING, null);
        String categoryIds = SharedPreferencesUtils.getStringSP(ctx, SharedPreferencesUtils.CATEGORY_ID, null);
        if(DeviceUtils.isNetworkAvailable(ctx) && settingIds != null && !settingIds.equals(categoryIds)) {
            NetUtils.updateCategory(ctx, categoryIds);
        }
    }
}
