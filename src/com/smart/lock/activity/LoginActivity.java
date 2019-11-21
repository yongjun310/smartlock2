package com.smart.lock.activity;

import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.smart.lock.R;
import com.smart.lock.common.SSOTypeEnum;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.response.BooleanResponse;
import com.smart.lock.response.LoginResponse;
import com.smart.lock.utils.*;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners;
import com.umeng.socialize.exception.SocializeException;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class LoginActivity extends BaseActivity {

    public static final String IS_WX_LOGIN = "isWXLogin";
    private static Gson gson = new Gson();

    private boolean isLogin, isNew;

    private boolean isDoningTempLogin = false, needRealLogin = false;

    private int accountId;

    private String mobileNo;

    private String mobile, localIpAddress, code, androidId, osVersion, model,
            appVersion;

    private Button btnCode;
    private EditText mobileText;

    private String preActivity;

    private UMSocialService mController = UMServiceFactory.getUMSocialService("com.umeng.login");

    private Context mContext;

    public Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Toast.makeText(getApplicationContext(),
                            SlideConstants.TOAST_LOGIN_SMS_SENT, Toast.LENGTH_SHORT)
                            .show();
                    break;
                case 2:
                    Toast.makeText(getApplicationContext(), msg.obj.toString(),
                            Toast.LENGTH_SHORT).show();
                    btnCode.setClickable(true);
                    btnCode.setBackgroundColor(getResources().getColor(R.color.white));
                    break;
                case 3:
                    Toast.makeText(getApplicationContext(),
                            SlideConstants.TOAST_LOGIN_SERVER_ERROR,
                            Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    Toast.makeText(getApplicationContext(),
                            SlideConstants.TOAST_LOGIN_SUCCESS, Toast.LENGTH_SHORT)
                            .show();
                    LoginActivity.this.finish();
                    break;
                case 5:
                    Toast.makeText(getApplicationContext(),
                            SlideConstants.TOAST_LOGIN_SERVER_ERROR,
                            Toast.LENGTH_SHORT).show();
                    break;
                case 6:
                    btnCode.setClickable(true);
                    btnCode.setBackgroundColor(getResources().getColor(R.color.white));
                    break;
                case 7:
                    if (needRealLogin) {
                        if (DeviceUtils.isNetworkAvailable(getApplicationContext())) {
                            new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    login(mobile, code, androidId, osVersion,
                                            model, appVersion);
                                }
                            }).start();
                        } else {
                            Toast.makeText(
                                    getApplicationContext(),
                                    SlideConstants.TOAST_LOGIN_NO_AVAILABLE_NETWORK,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                    break;

                default:
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SysApplication.getInstance().addActivity(this);

        mContext = this;

        DisplayUtils.setTitleAndBackBtn(this, "登录");
        SharedPreferences commonSP = getSharedPreferences("common",
                MODE_MULTI_PROCESS);
        isLogin = commonSP.getBoolean("isLogin", false);
        isNew = SharedPreferencesUtils.getBoolSP(this, SharedPreferencesUtils.IS_NEW, true);
        accountId = commonSP.getInt("accountId", -1);
        mobileNo = commonSP.getString("mobileNo", "");
        if(this.getIntent().getExtras() != null) 
        	preActivity = this.getIntent().getExtras().getString("preActivity");
        if (!isNew && accountId >= 0) {
            boolean bindMobile = this.getIntent().getBooleanExtra("bindMobile",
                    false);

            if (!bindMobile && !"MainActivity".equals(preActivity) && !"AmountActivity".equals(preActivity)) {
                SharedPreferencesUtils.putBoolSP(this, SharedPreferencesUtils.IS_NEW, false);
                PageChangeUtils.startActivityWithParams(this, SettingWizardActivity.class);
                this.finish();
                return;
            }
        } else {
            if (DeviceUtils.isNetworkAvailable(this)) {
                isDoningTempLogin = true;
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        tempLogin(androidId, osVersion, model, appVersion);
                    }
                }).start();
            }
        }

        btnCode = (Button) findViewById(R.id.btn_code);
        mobileText = (EditText) findViewById(R.id.input_mobile);
    }

    public void resetClick(View view) {
        mobileText.setText("");
    }

    public void codeClick(View view) {
        mobile = mobileText.getText().toString();
        if (mobile == null || mobile.length() < 11) {
            Toast.makeText(this, SlideConstants.TOAST_LOGIN_INVALID_MOBILE,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!DeviceUtils.isNetworkAvailable(this)) {
            Toast.makeText(this,
                    SlideConstants.TOAST_LOGIN_NO_AVAILABLE_NETWORK,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        localIpAddress = DeviceUtils.getLocalIpAddress();
        if (localIpAddress == null || localIpAddress.length() == 0) {
            Toast.makeText(this,
                    SlideConstants.TOAST_LOGIN_NO_AVAILABLE_NETWORK,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnCode.setClickable(false);
        btnCode.setBackgroundColor(getResources().getColor(R.color.grey));

        new Thread(new Runnable() {

            @Override
            public void run() {
                sendCode(mobile, localIpAddress);
            }
        }).start();
    }

    private void sendCode(String mobile, String localIpAddress) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("mobile", mobile));
        params.add(new BasicNameValuePair("ip", localIpAddress));

        NetUtils.umengSelfEvent(this, "regist_verify_phone");
        try {
            String respStr = NetUtils.post(SlideConstants.SERVER_URL
                    + SlideConstants.SERVER_METHOD_SMSCODE, params);
            BooleanResponse resp = gson.fromJson(respStr, BooleanResponse.class);
            if (resp.getCode() == SlideConstants.SERVER_RETURN_SUCCESS) {
                Message msg = Message.obtain(messageHandler, 1);
                messageHandler.sendMessage(msg);

                msg = Message.obtain(messageHandler, 6);
                messageHandler.sendMessageDelayed(msg, 120000);
            } else {
                Message msg = Message.obtain(messageHandler, 2, resp.getMsg());
                messageHandler.sendMessage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Message msg = Message.obtain(messageHandler, 3);
            messageHandler.sendMessage(msg);
        }
    }

    public void loginClick(View view) {
        mobile = mobileText.getText().toString();
        if (mobile == null || mobile.length() < 11) {
            Toast.makeText(this, SlideConstants.TOAST_LOGIN_INVALID_MOBILE,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        EditText codeText = (EditText) findViewById(R.id.input_code);
        code = codeText.getText().toString();
        if (code == null && code.length() < SlideConstants.SMS_CODE_LENGTH) {
            Toast.makeText(this, SlideConstants.TOAST_LOGIN_INVALID_CODE,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!DeviceUtils.isNetworkAvailable(this)) {
            Toast.makeText(this,
                    SlideConstants.TOAST_LOGIN_NO_AVAILABLE_NETWORK,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (isDoningTempLogin)
            needRealLogin = true;
        else {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    login(mobile, code, androidId, osVersion, model, appVersion);
                }
            }).start();
        }
    }

    public void login(String mobile, String code, String androidId,
                      String osVersion, String model, String appVersion) {
        SharedPreferences commonSP = getSharedPreferences("common",
                MODE_MULTI_PROCESS);
        accountId = commonSP.getInt("accountId", -1);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("accountId", String
                .valueOf(accountId)));
        params.add(new BasicNameValuePair("mobile", mobile));
        params.add(new BasicNameValuePair("code", code));
        params.add(new BasicNameValuePair("androidId", androidId));
        params.add(new BasicNameValuePair("osVersion", osVersion));
        params.add(new BasicNameValuePair("model", model));
        params.add(new BasicNameValuePair("appVersion", appVersion));
        params.add(new BasicNameValuePair("isNewInstall", isNew ? "1" : "0"));
        try {
            String respStr = NetUtils.post(SlideConstants.SERVER_URL
                    + SlideConstants.SERVER_METHOD_LOGIN, params);
            LoginResponse resp = gson.fromJson(respStr, LoginResponse.class);
            SharedPreferencesUtils.removeStringSP(this, SharedPreferencesUtils.LOAD_DATA_TIME);
            if (resp.getCode() == SlideConstants.SERVER_RETURN_SUCCESS
                    && resp.getData() != null
                    && resp.getData().getAccountId() >= 0) {
                accountId = resp.getData().getAccountId();

                SharedPreferences.Editor commonEditor = commonSP.edit();
                commonEditor.putInt("accountId", accountId);
                commonEditor.putString("mobileNo", mobile);
                commonEditor.putBoolean("isLogin", true);
                commonEditor.commit();

                SharedPreferences rsaSP = getSharedPreferences("rsa"
                        + accountId, MODE_MULTI_PROCESS);
                SharedPreferences.Editor rsaEditor = rsaSP.edit();
                rsaEditor.putString("modulus", resp.getData().getModulus());
                rsaEditor.putString("publicExponent", resp.getData()
                        .getPublicExponent());
                rsaEditor.commit();
               	List<Integer> categoryIds = resp.getData().getCategoryIds();
                
                onLoginSuccess(categoryIds);

//                SharedPreferencesUtils.putBoolSP(this, SharedPreferencesUtils.IS_NEW, false);
//                PageChangeUtils.redirectMain(this);
            } else {
                Message msg = Message.obtain(messageHandler, 2, resp.getMsg());
                messageHandler.sendMessage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();

            Message msg = Message.obtain(messageHandler, 5);
            messageHandler.sendMessage(msg);
        }
    }

    /**
     * 登录成功跳转
     * @param categoryIds 已选分类信息
     */
	private void onLoginSuccess(List<Integer> categoryIds) {
		Message msg = Message.obtain(messageHandler, 4);
		messageHandler.sendMessage(msg);
		if(!"MainActivity".equals(preActivity) && !"AmountActivity".equals(preActivity)) {
		    Intent intent = new Intent();
		    Class clazz = CategorySettingActivity.class;
		    intent.putExtra("preActivity", "LoginActivity");
		    if (categoryIds != null && categoryIds.size() > 0) {
		        clazz = SettingWizardActivity.class;
		        SharedPreferencesUtils.putBoolSP(this, SharedPreferencesUtils.IS_NEW, false);
		    }
		    intent.setClass(this, clazz);
		    startActivity(intent);
		}
		this.finish();
	}

    public void skipClick(View view) {
        PageChangeUtils.startActivityWithParams(this, CategorySettingActivity.class);
        this.finish();
        Toast.makeText(this, SlideConstants.TOAST_LOGIN_SKIP_TIPS,
                Toast.LENGTH_SHORT).show();
    }

    public void backClick(View view) {
        NetUtils.umengSelfEvent(this, "regist_back");
        this.finish();
    }

    private void tempLogin(String androidId, String osVersion, String model,
                           String appVersion) {

        androidId = DeviceUtils.getAndroidId(this);
        osVersion = DeviceUtils.getOSVersion();
        model = DeviceUtils.getModel();
        appVersion = DeviceUtils.getAppVersion(this);
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
            LoginResponse resp = gson.fromJson(respStr, LoginResponse.class);
            SharedPreferencesUtils.removeStringSP(this, SharedPreferencesUtils.LOAD_DATA_TIME);
            if (resp.getCode() == SlideConstants.SERVER_RETURN_SUCCESS
                    && resp.getData() != null
                    && resp.getData().getAccountId() >= 0) {
                accountId = resp.getData().getAccountId();

                SharedPreferences commonSP = getSharedPreferences("common",
                        MODE_MULTI_PROCESS);
                SharedPreferences.Editor commonEditor = commonSP.edit();
                commonEditor.putInt("accountId", accountId);
                commonEditor.putString("mobileNo",
                        mobileNo.length() > 0 ? mobileNo : "");
                commonEditor.putBoolean("isLogin", isLogin ? true : false);
                commonEditor.commit();

                SharedPreferences rsaSP = getSharedPreferences("rsa"
                        + accountId, MODE_MULTI_PROCESS);
                SharedPreferences.Editor rsaEditor = rsaSP.edit();
                rsaEditor.putString("modulus", resp.getData().getModulus());
                rsaEditor.putString("publicExponent", resp.getData()
                        .getPublicExponent());
                rsaEditor.commit();

                isDoningTempLogin = false;

                Message msg = Message.obtain(messageHandler, 7);
                messageHandler.sendMessage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();

            isDoningTempLogin = false;

            Message msg = Message.obtain(messageHandler, 5);
            messageHandler.sendMessage(msg);
        }
    }

    public void qqLogin_test(View view) {
        String info = "is_yellow_year_vip^*#0^%$vip^*#0^%$level^*#0^%$province^*#上海^%$yellow_vip_level^*#0^%$is_yellow_vip^*#0^%$gender^*#男^%$screen_name^*#七包泡面^%$msg^*#^%$profile_image_url^*#http://q.qlogo.cn/qqapp/1104074720/067A75C74036A0250CE840A279E7A16E/100^%$city^*#浦东新区";

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("time", new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()).toString()));
        params.add(new BasicNameValuePair("androidId", DeviceUtils
                .getAndroidId(this)));
        params.add(new BasicNameValuePair("ssoInfos", info));
        params.add(new BasicNameValuePair("ssoType", String.valueOf(2)));
        params.add(new BasicNameValuePair("itemSeperator", SlideConstants.SEPERATOR_ITEM));
        params.add(new BasicNameValuePair("kvSeperator", SlideConstants.SEPERATOR_KEY_VALUE));

        SharedPreferences rsaSP = getSharedPreferences(
                "rsa" + accountId, Context.MODE_MULTI_PROCESS);
        String modulus = rsaSP.getString("modulus", "");
        String publicExponent = rsaSP.getString("publicExponent", "");

        RSAPublicKey publicKey = SecurityUtils.getPublicKey(modulus,
                publicExponent);

        String data = SecurityUtils.getInputStringNew(params);
        String sign = "";
        try {
            sign = SecurityUtils.encryptByPublicKeyNew(data, publicKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void qqLogin(View view) {
        if (!DeviceUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "网络不可用", Toast.LENGTH_SHORT).show();
            return;
        }

        //参数1为当前Activity， 参数2为开发者在QQ互联申请的APP ID，参数3为开发者在QQ互联申请的APP kEY.
        UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(LoginActivity.this, SlideConstants.QQ_APPID, SlideConstants.QQ_APPKEY);
        qqSsoHandler.addToSocialSDK();

        mController.doOauthVerify(mContext, SHARE_MEDIA.QQ, new SocializeListeners.UMAuthListener() {
            @Override
            public void onStart(SHARE_MEDIA platform) {
                Toast.makeText(mContext, "授权开始", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(SocializeException e, SHARE_MEDIA platform) {
                Toast.makeText(mContext, "授权错误", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete(Bundle value, SHARE_MEDIA platform) {
                Toast.makeText(mContext, "授权完成", Toast.LENGTH_SHORT).show();
                //获取相关授权信息
                mController.getPlatformInfo(LoginActivity.this, SHARE_MEDIA.QQ, new SocializeListeners.UMDataListener() {
                    @Override
                    public void onStart() {
                        Toast.makeText(LoginActivity.this, "获取平台数据开始...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete(int status, final Map<String, Object> info) {
                        FileUtils.addFileLog("QQ login complete [status]" + status + "[info]" + info.keySet().size());
                        if (status == 200 && info != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    postSSOInfos(SSOTypeEnum.QQ, info);
                                }
                            }).start();
                            onLoginSuccess(null);
                            SharedPreferencesUtils.putBoolSP(getApplicationContext(), "isLogin", true);
                        } else {
                            Log.d("TestData", "发生错误：" + status);
                        }
                    }
                });
            }

            @Override
            public void onCancel(SHARE_MEDIA platform) {
                Toast.makeText(mContext, "授权取消", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void wxLogin(View view) {
        if (!DeviceUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "网络不可用", Toast.LENGTH_SHORT).show();
            return;
        }

        UMWXHandler wxHandler = new UMWXHandler(this, SlideConstants.WECHAT_APPID, SlideConstants.WECHAT_APPSEC);
        wxHandler.addToSocialSDK();

        mController.doOauthVerify(mContext, SHARE_MEDIA.WEIXIN, new SocializeListeners.UMAuthListener() {
            @Override
            public void onStart(SHARE_MEDIA platform) {
//                SharedPreferencesUtils.putBoolSP(mContext, IS_WX_LOGIN, true);
                FileUtils.addFileLog("wx login start");
                Toast.makeText(mContext, "授权开始", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(SocializeException e, SHARE_MEDIA platform) {
                FileUtils.addFileLog("wx login error" + e.toString());
                Toast.makeText(mContext, "授权错误", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete(Bundle value, SHARE_MEDIA platform) {
                FileUtils.addFileLog("wx login complete");
                Toast.makeText(mContext, "授权完成", Toast.LENGTH_SHORT).show();
                //获取相关授权信息
                mController.getPlatformInfo(LoginActivity.this, SHARE_MEDIA.WEIXIN, new SocializeListeners.UMDataListener() {
                    @Override
                    public void onStart() {
                        Toast.makeText(LoginActivity.this, "获取平台数据开始...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete(int status, final Map<String, Object> info) {
                        if (status == 200 && info != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    postSSOInfos(SSOTypeEnum.WEIXIN, info);
                                }
                            }).start();
                            onLoginSuccess(null);
                            SharedPreferencesUtils.putBoolSP(getApplicationContext(), "isLogin", true);
                        } else {
                            Log.d("TestData", "发生错误：" + status);
                        }
                    }
                });
            }

            @Override
            public void onCancel(SHARE_MEDIA platform) {
                Toast.makeText(mContext, "授权取消", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postSSOInfos(SSOTypeEnum ssoType, Map<String, Object> info) {
        accountId = SharedPreferencesUtils.getIntSP(this, "accountId", -1);
        if (accountId <= 0) return;

        StringBuilder sb = new StringBuilder();
        for (String key : info.keySet()) {
            sb.append((sb.length() > 0 ? SlideConstants.SEPERATOR_ITEM : "") + key + SlideConstants.SEPERATOR_KEY_VALUE + info.get(key));

            if ("nickname".equals(key) || "screen_name".equals(key)) {
                SharedPreferencesUtils.putStringSP(this, "nickName", info.get(key).toString());
            }
            if ("headimgurl".equals(key) || "profile_image_url".equals(key)) {
                final String headPicUrl = info.get(key).toString();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String filePrefix = FileUtils.getImageDir(getApplicationContext());

                        File dirFile = new File(filePrefix);
                        if (!dirFile.exists()) {
                            dirFile.mkdirs();
                        }

                        String localPath = filePrefix + "headPic.dat";
                        if (FileUtils.downloadImage(headPicUrl, localPath)) {
                            SharedPreferencesUtils.putStringSP(getApplicationContext(), "headPic", localPath);
                        }
                    }
                }).start();
            }

        }
        FileUtils.addFileLog("postSSOInfos [ssoType]" + ssoType.getValue() + "[info]" + sb.toString());

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("time", new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()).toString()));
        params.add(new BasicNameValuePair("androidId", DeviceUtils
                .getAndroidId(this)));
        params.add(new BasicNameValuePair("ssoInfos", sb.toString()));
        params.add(new BasicNameValuePair("ssoType", String.valueOf(ssoType.getValue())));
        params.add(new BasicNameValuePair("itemSeperator", SlideConstants.SEPERATOR_ITEM));
        params.add(new BasicNameValuePair("kvSeperator", SlideConstants.SEPERATOR_KEY_VALUE));

        SharedPreferences rsaSP = getSharedPreferences(
                "rsa" + accountId, Context.MODE_MULTI_PROCESS);
        String modulus = rsaSP.getString("modulus", "");
        String publicExponent = rsaSP.getString("publicExponent", "");

        RSAPublicKey publicKey = SecurityUtils.getPublicKey(modulus,
                publicExponent);

        String data = SecurityUtils.getInputStringNew(params);
        String sign = "";
        try {
            sign = SecurityUtils.encryptByPublicKeyNew(data, publicKey);

            params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("sign", sign));
            params.add(new BasicNameValuePair("accountId", String
                    .valueOf(accountId)));

            String respStr = NetUtils.post(SlideConstants.SERVER_URL
                    + SlideConstants.SERVER_METHOD_POST_SSO_INFO, params);

            FileUtils.addFileLog("postSSOInfos server return:" + respStr);

            BooleanResponse response = gson.fromJson(respStr, BooleanResponse.class);
            if (response.getCode() == SlideConstants.SERVER_RETURN_SUCCESS) {
                //TODO
                Message msg = Message.obtain(messageHandler, 4);
                messageHandler.sendMessage(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            FileUtils.addFileLog("postSSOInfos exception:" + e.toString());
        }

    }
}
