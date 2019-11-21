package com.smart.lock.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.response.BaseResponse;
import com.smart.lock.response.LoginResponse;
import com.umeng.analytics.MobclickAgent;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NetUtils {
	private static String TAG = "NetUtils";
	
	public static String post(String url, List<NameValuePair> params)
            throws Exception {
        Log.d("NetUtilsPost:", url);
        HttpPost request = new HttpPost(url);
        request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

        HttpResponse response = new DefaultHttpClient().execute(request);

        Log.d("NetUtilsPost:", "StatusCode：" + response.getStatusLine().getStatusCode());
        if (response.getStatusLine().getStatusCode() == 200) {
            return EntityUtils.toString(response.getEntity());
        }

        return null;
    }

    public static String get(String url) throws Exception {
        HttpGet request = new HttpGet(url);

        HttpResponse response = new DefaultHttpClient().execute(request);

        if (response.getStatusLine().getStatusCode() == 200) {
            return EntityUtils.toString(response.getEntity());
        }

        return null;
    }

    public static File downloadFile(String httpUrl, String filePath,
                                    String fileName) {
        File tmpFile = new File(Environment.getExternalStorageDirectory()
                + filePath);
        if (!tmpFile.exists()) {
            tmpFile.mkdir();
        }
        final File file = new File(Environment.getExternalStorageDirectory()
                + filePath + "/" + fileName);

        try {
            URL url = new URL(httpUrl);
            try {
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                InputStream is = conn.getInputStream();
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buf = new byte[256];
                conn.connect();
                double count = 0;
                if (conn.getResponseCode() >= 400) {
                } else {
                    while (count <= 100) {
                        if (is != null) {
                            int numRead = is.read(buf);
                            if (numRead <= 0) {
                                break;
                            } else {
                                fos.write(buf, 0, numRead);
                            }

                        } else {
                            break;
                        }

                    }
                }

                conn.disconnect();
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return file;
    }


    public static void loadEncypUrl(Activity activity, WebView webView, String methodUrl, String url) {
        int accountId;
        SharedPreferences commonSP = activity.getSharedPreferences(
                "common", Context.MODE_MULTI_PROCESS );
        accountId = commonSP.getInt("accountId", -1);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("androidId", DeviceUtils
                .getAndroidId(activity)));
        params.add(new BasicNameValuePair("time", new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()).toString()));

        String data = SecurityUtils.getInputString(params);

        SharedPreferences rsaSP = activity.getSharedPreferences(
                "rsa" + accountId, Context.MODE_MULTI_PROCESS );
        String modulus = rsaSP.getString("modulus", "");
        String publicExponent = rsaSP.getString("publicExponent", "");

        RSAPublicKey publicKey = SecurityUtils.getPublicKey(modulus,
                publicExponent);
        try {
            String sign = SecurityUtils.encryptByPublicKey(data, publicKey);

            params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("accountId", String
                    .valueOf(accountId)));
            params.add(new BasicNameValuePair("sign", sign));

            if (!TextUtils.isEmpty(url)) {
                params.add(new BasicNameValuePair("url", url));
            }

            String postData = SecurityUtils.getInputString(params);

            webView.postUrl(SlideConstants.SERVER_URL
                            + methodUrl,
                    EncodingUtils.getBytes(postData, "base64"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity,
                    SlideConstants.FAVORITE_POST_ERROR, Toast.LENGTH_SHORT)
                    .show();
        }
    }


    public static String upDateGTID(Context activity, String methodUrl, String getuiId) {
        int accountId;
        SharedPreferences commonSP = activity.getSharedPreferences(
                "common", Context.MODE_MULTI_PROCESS );
        accountId = commonSP.getInt("accountId", -1);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("androidId", DeviceUtils
                .getAndroidId(activity)));
        params.add(new BasicNameValuePair("time", new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()).toString()));

        if (!TextUtils.isEmpty(getuiId)) {
            params.add(new BasicNameValuePair("getuiId", getuiId));
        }

        String data = SecurityUtils.getInputString(params);

        SharedPreferences rsaSP = activity.getSharedPreferences(
                "rsa" + accountId, Context.MODE_MULTI_PROCESS );
        String modulus = rsaSP.getString("modulus", "");
        String publicExponent = rsaSP.getString("publicExponent", "");

        RSAPublicKey publicKey = SecurityUtils.getPublicKey(modulus,
                publicExponent);
        try {
            String sign = SecurityUtils.encryptByPublicKey(data, publicKey);

            params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("accountId", String
                    .valueOf(accountId)));
            params.add(new BasicNameValuePair("sign", sign));
            String respStr = post(methodUrl, params);

            return respStr;
        } catch (Exception e) {
            e.printStackTrace();
            /*Toast.makeText(activity,
					SlideConstants.FAVORITE_POST_ERROR, Toast.LENGTH_SHORT)
					.show();*/
        }
        return null;
    }

    public static void updateCategory(Context context, String categoryIds) {
        int accountId;
        String mobileNo;
        Gson gson = new Gson();
        SharedPreferences commonSP = context.getSharedPreferences(
                "common", Context.MODE_MULTI_PROCESS );
        accountId = commonSP.getInt("accountId", -1);
        mobileNo = commonSP.getString("mobileNo", "");
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("time", new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()).toString()));
        params.add(new BasicNameValuePair("androidId", DeviceUtils
                .getAndroidId(context.getApplicationContext())));
        params.add(new BasicNameValuePair("categoryIds", categoryIds));


        SharedPreferences rsaSP = context.getSharedPreferences(
                "rsa" + accountId, Context.MODE_MULTI_PROCESS );
        String modulus = rsaSP.getString("modulus", "");
        String publicExponent = rsaSP.getString("publicExponent", "");

        RSAPublicKey publicKey = SecurityUtils.getPublicKey(modulus,
                publicExponent);

        String data = SecurityUtils.getInputString(params);
        String sign = "";
        try {
            sign = SecurityUtils.encryptByPublicKey(data, publicKey);

            params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("sign", sign));
            params.add(new BasicNameValuePair("accountId", String
                    .valueOf(accountId)));

            String respStr = NetUtils.post(SlideConstants.SERVER_URL
                    + SlideConstants.SERVER_METHOD_UPDATE_ACCOUNT_INFO, params);
            BaseResponse resp = gson
                    .fromJson(respStr, BaseResponse.class);
            if (resp.getCode() == SlideConstants.SERVER_RETURN_SUCCESS) {
                SharedPreferencesUtils.putStringSP(context, SharedPreferencesUtils.CATEGORY_ID, categoryIds);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void tempLogin(Context ctx, boolean isNew, ThreadHelper threadHelper) {
        int accountId;
        String mobileNo;
        String androidId, osVersion, model, appVersion;
        boolean isLogin;
        androidId = DeviceUtils.getAndroidId(ctx);
        osVersion = DeviceUtils.getOSVersion();
        model = DeviceUtils.getModel();
        SharedPreferences commonSP = ctx.getSharedPreferences("common", 0);
        accountId = commonSP.getInt("accountId", -1);
        mobileNo = commonSP.getString("mobileNo", "");
        isLogin = commonSP.getBoolean("isLogin", false);
        appVersion = DeviceUtils.getAppVersion(ctx);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("accountId", String
                .valueOf(accountId)));
        params.add(new BasicNameValuePair("androidId", androidId));
        params.add(new BasicNameValuePair("osVersion", osVersion));
        params.add(new BasicNameValuePair("model", model));
        params.add(new BasicNameValuePair("appVersion", appVersion));
        params.add(new BasicNameValuePair("isNewInstall", accountId < 0 ? "1" : "0"));
        try {
            String respStr = NetUtils.post(SlideConstants.SERVER_URL
                    + SlideConstants.SERVER_METHOD_TEMP_LOGIN, params);

            Log.d("tempLogin return:", respStr);
            Gson gson = new Gson();
            LoginResponse resp = gson.fromJson(respStr, LoginResponse.class);
            SharedPreferencesUtils.removeStringSP(ctx, SharedPreferencesUtils.LOAD_DATA_TIME);
            if (resp.getCode() == SlideConstants.SERVER_RETURN_SUCCESS
                    && resp.getData() != null
                    && resp.getData().getAccountId() >= 0) {
                accountId = resp.getData().getAccountId();
                mobileNo = resp.getData().getMobileNo();

                SharedPreferences.Editor commonEditor = commonSP.edit();
                commonEditor.putInt("accountId", accountId);
                commonEditor.putString("mobileNo",
                        mobileNo.length() > 0 ? mobileNo : "");
                commonEditor.putBoolean("isLogin", isLogin ? true : false);
                commonEditor.commit();

                SharedPreferences rsaSP = ctx.getSharedPreferences("rsa"
                        + resp.getData().getAccountId(), 0);
                SharedPreferences.Editor rsaEditor = rsaSP.edit();
                rsaEditor.putString("modulus", resp.getData().getModulus());
                rsaEditor.putString("publicExponent", resp.getData()
                        .getPublicExponent());
                rsaEditor.commit();

                
                
                threadHelper.addTask(SlideConstants.THREAD_DISTRIBUTE_CONTENT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void umengSelfEvent(Context context, String event, String event_detail) {
		try {
			MobclickAgent.onEvent(context, event, event_detail);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
    }

    public static void umengSelfEvent(Context context, String event, int index) {
        try {
        	MobclickAgent.onEvent(context, event, index);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static void umengSelfEvent(Context context, String event) {
        try {
        	MobclickAgent.onEvent(context, event);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
    
}
