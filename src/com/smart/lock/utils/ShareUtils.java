package com.smart.lock.utils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.smart.lock.R;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.dto.AccountActionLogDTO;
import com.smart.lock.dto.ContentDTO;
import com.smart.lock.sina.AccessTokenKeeper;
import com.smart.lock.sina.Constants;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.SnsPostListener;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.TencentWBSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

public class ShareUtils {
    private static final String DESCRIPTOR = "com.umeng.share";

    private static UMSocialService mController = UMServiceFactory
            .getUMSocialService(DESCRIPTOR);

    /**
     * 根据不同的平台设置不同的分享内容
     *
     * @param activity      当前activity
     * @param shareImgUrl   分享图标
     * @param shareLineLink 分享链接
     * @param shareTitle    分享标题
     * @param descContent   分享详细描述
     */
    public static Dialog shareContent(final Activity activity, final String shareImgUrl, final String shareLineLink, String shareTitle,
                                      final String descContent, final int contentId) {
        /*Intent intent = new Intent(activity, CustomDialogActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("shareImgUrl", shareImgUrl);
		bundle.putString("shareLineLink", shareLineLink);
		bundle.putString("shareTitle", shareTitle);
		bundle.putString("descContent", descContent);
		bundle.putInt("contentId", contentId);
		intent.putExtras(bundle);
		activity.startActivity(intent);*/
		DisplayMetrics dp = activity.getResources().getDisplayMetrics();
		final Dialog dialog = new Dialog(activity, R.style.ShareDialog);
		dialog.setContentView(R.layout.share_list);
		Window window = dialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		window.setGravity(Gravity.BOTTOM);  //此处可以设置dialog显示的位置
		lp.x = 0; // 新位置X坐标
		lp.y = 0; // 新位置Y坐标
		lp.width = dp.widthPixels; // 宽度
		lp.gravity=Gravity.BOTTOM;
		// 当Window的Attributes改变时系统会调用此函数,可以直接调用以应用上面对窗口参数的更改,也可以用setAttributes
		// dialog.onWindowAttributesChanged(lp);
		window.setAttributes(lp);
		window.setWindowAnimations(R.style.ShareDialog);  //添加动画
		final Map<String, SHARE_MEDIA> mPlatformsMap = new HashMap<String, SHARE_MEDIA>();
		//mPlatformsMap.put("新浪微博", SHARE_MEDIA.SINA);
		mPlatformsMap.put("QQ", SHARE_MEDIA.QQ);
		mPlatformsMap.put("QQ空间", SHARE_MEDIA.QZONE);
		mPlatformsMap.put("微信", SHARE_MEDIA.WEIXIN);
		mPlatformsMap.put("朋友圈", SHARE_MEDIA.WEIXIN_CIRCLE);

		UMImage urlImage = null;
		if (shareImgUrl == null || shareImgUrl.length() == 0) {
			urlImage = new UMImage(activity, R.drawable.logo);
		} else {
			urlImage = new UMImage(activity, shareImgUrl
					+ SlideConstants.IMAGE_STYLE_SHARE_ICON);
		}
		// 配置新浪微博
		/*mController.getConfig().setSsoHandler(new SinaSsoHandler());*/
        // 配置腾讯微博
        mController.getConfig().setSsoHandler(new TencentWBSsoHandler());

        QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(activity,
                SlideConstants.QQ_APPID, SlideConstants.QQ_APPKEY);
        qZoneSsoHandler.setTargetUrl(shareLineLink);
        qZoneSsoHandler.addToSocialSDK();

        UMQQSsoHandler umQQSsoHandler = new UMQQSsoHandler(activity,
                SlideConstants.QQ_APPID, SlideConstants.QQ_APPKEY);
        umQQSsoHandler.setTargetUrl(shareLineLink);
        umQQSsoHandler.addToSocialSDK();

        // 添加微信平台
        UMWXHandler wxHandler = new UMWXHandler(activity,
                SlideConstants.WECHAT_APPID, SlideConstants.WECHAT_APPSEC);
        wxHandler.addToSocialSDK();

        // 支持微信朋友圈
        UMWXHandler wxCircleHandler = new UMWXHandler(activity,
                SlideConstants.WECHAT_APPID, SlideConstants.WECHAT_APPSEC);
        wxCircleHandler.setToCircle(true);
        wxCircleHandler.addToSocialSDK();

        WeiXinShareContent weixinContent = new WeiXinShareContent();
        weixinContent.setShareContent(descContent);
        weixinContent.setTitle(shareTitle);
        weixinContent.setTargetUrl(shareLineLink);
        weixinContent.setShareMedia(urlImage);
        mController.setShareMedia(weixinContent);

        // 设置朋友圈分享的内容
        CircleShareContent circleMedia = new CircleShareContent();
        if (contentId >= 0)
            circleMedia.setTitle(shareTitle);
        else
            circleMedia.setTitle(descContent);
        circleMedia.setShareContent(descContent);
        circleMedia.setShareMedia(urlImage);
        circleMedia.setTargetUrl(shareLineLink);
        mController.setShareMedia(circleMedia);

        // 设置QQ空间分享内容
        QZoneShareContent qzone = new QZoneShareContent();
        if (contentId >= 0)
            qzone.setTitle(shareTitle);
        else
            qzone.setTitle(" ");
        qzone.setShareContent(descContent);
        qzone.setTargetUrl(shareLineLink);
        qzone.setShareMedia(urlImage);
        mController.setShareMedia(qzone);

		/*SinaShareContent sinaContent = new SinaShareContent();
		sinaContent.setShareContent(descContent+"#秒秀锁屏看段子还能赚钱# @秒秀在线"+shareLineLink);
		sinaContent.setShareMedia(urlImage);
		sinaContent.setTargetUrl(shareLineLink);
		sinaContent.setTitle(shareTitle);
		sinaContent.setAppWebSite("");
		sinaContent.setShareImage(urlImage);
		mController.setShareMedia(sinaContent);*/

        QQShareContent qqShareContent = new QQShareContent();
        qqShareContent.setShareContent(descContent);
        qqShareContent.setTitle(shareTitle);
        qqShareContent.setShareMedia(urlImage);
        qqShareContent.setTargetUrl(shareLineLink);
        mController.setShareMedia(qqShareContent);

//		TencentWbShareContent tencent = new TencentWbShareContent();
//		tencent.setShareContent(descContent);
//		tencent.setShareMedia(urlImage);
//		tencent.setTargetUrl(shareLineLink);
//		tencent.setTitle(shareTitle);
//		mController.setShareMedia(tencent);
        SharedPreferences commonSP = activity.getSharedPreferences("common",
                activity.MODE_MULTI_PROCESS );
        final int accountId = commonSP.getInt("accountId", -1);

        // 新浪、QQ、QQ空间、易信、来往、豆瓣、人人平台
        final CharSequence[] items = {"微信", "朋友圈", "QQ空间", "QQ"};
        /**
         * 分享监听器
         */
        final SnsPostListener mShareListener = new SnsPostListener() {

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(SHARE_MEDIA platform, int stCode,
                                   SocializeEntity entity) {
                if (stCode == 200) {
                    Toast.makeText(activity, "分享成功", Toast.LENGTH_SHORT)
                            .show();
//					if(contentId >= 0)
//						ActionLogOperator.add(activity, new AccountActionLogDTO(accountId,
//								ActionTypeEnum.SHARE_SUCCESS, contentId));
                    ThreadHelper.getInstance(activity).addTask(SlideConstants.THREAD_POST_LOG);
                    //((WebViewActivity) activity).hideShareAmount();
                } /*else {
					Toast.makeText(activity,
							"分享失败 : error code : " + stCode, Toast.LENGTH_SHORT)
							.show();
				}*/
            }
        };

        final Handler handler = new Handler();
        View.OnClickListener listener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//				if(contentId >= 0)
//					ActionLogOperator.add(activity, new AccountActionLogDTO(accountId,
//							ActionTypeEnum.SHARE, contentId));
                // 获取用户点击的平台
                final SHARE_MEDIA platform = mPlatformsMap.get(items[Integer.parseInt((String) v.getTag())]);
                boolean isShared = false;
                if (SHARE_MEDIA.WEIXIN.equals(platform)) {
                    ActionLogOperator.add(activity, new AccountActionLogDTO(accountId, 38, contentId));
                    isShared = true;
                }
                if (SHARE_MEDIA.WEIXIN_CIRCLE.equals(platform)) {
                    ActionLogOperator.add(activity, new AccountActionLogDTO(accountId, 39, contentId));
                    isShared = true;
                }
                if (SHARE_MEDIA.QQ.equals(platform)) {
                    ActionLogOperator.add(activity, new AccountActionLogDTO(accountId, 40, contentId));
                    isShared = true;
                }
                if (SHARE_MEDIA.QZONE.equals(platform)) {
                    ActionLogOperator.add(activity, new AccountActionLogDTO(accountId, 41, contentId));
                    isShared = true;
                }
                /*if (SHARE_MEDIA.SINA.equals(platform)) {
                    ActionLogOperator.add(activity, new AccountActionLogDTO(accountId, 60, contentId));
                    isShared = true;
                }*/
                TimerTask task = new TimerTask() {

                    @Override
                    public void run() {
                    	activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								ThreadHelper.getInstance(activity).addTask(SlideConstants.THREAD_POST_LOG);
							}
                    	});
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 10 * 1000);

                if (isShared) {
                    ContentDTO contentDTO = ContentsDataOperator.getById(activity, contentId);
                    if (contentDTO != null) {
                        contentDTO.setBonusAmount(new BigDecimal(0));
                        ContentsDataOperator.update(activity, contentDTO);
                    }
                }
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        if (dialog != null)
                            dialog.dismiss();

                        if ("SINA".equals(platform.name())) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Bitmap bm = null;
                                    String localPath = SlideConstants.EXTERNAL_IMAGE_PATH + "shareImg.dat";
                                    FileUtils.downloadImage(shareImgUrl + SlideConstants.IMAGE_STYLE_SHARE_ICON, localPath);
                                    try {
                                        bm = FileUtils.loadResizedBitmap(activity, localPath,
                                                128, 128, true);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    final Bitmap fbm = bm;
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            sendMutiMessage(activity, descContent + "  #全世界的美图都在这# @遇见锁屏"
                                                    + shareLineLink, fbm);
                                        }

                                    });
                                }

                            }).start();

                        } else {
                            mController.postShare(activity, platform,
                                    mShareListener);
                        }
                        if ("QQ".equals(platform.name()) || "QZONE".equals(platform.name())) {
                            Runnable mRunnable = new Runnable() {

                                @Override
                                public void run() {
                                    if (contentId >= 0) {
//										ActionLogOperator.add(activity, new AccountActionLogDTO(accountId,
//												ActionTypeEnum.SHARE_SUCCESS, contentId));
                                        ThreadHelper.getInstance(activity).addTask(SlideConstants.THREAD_POST_LOG);
                                    }
                                }
                            };

                            handler.postDelayed(mRunnable, 10000);
                        }
                    }
                });

            }
        };

        window.findViewById(R.id.layout_0).setTag("0");
        window.findViewById(R.id.layout_1).setTag("1");
        window.findViewById(R.id.layout_2).setTag("2");
        window.findViewById(R.id.layout_3).setTag("3");
        window.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null)
                    dialog.dismiss();
            }
        });

        //window.findViewById(R.id.layout_4).setTag("4");
        window.findViewById(R.id.layout_0).setOnClickListener(listener);
        window.findViewById(R.id.layout_1).setOnClickListener(listener);
        window.findViewById(R.id.layout_2).setOnClickListener(listener);
        window.findViewById(R.id.layout_3).setOnClickListener(listener);
        //window.findViewById(R.id.layout_4).setOnClickListener(listener);

		/*dialogBuilder.setItems(items, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// 获取用户点击的平台
				SHARE_MEDIA platform = mPlatformsMap.get(items[which]);

				mController.postShare(activity, platform,
						mShareListener);
			} // end of onClick
		});*/

        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        return dialog;
    }


    Handler handler = new Handler();

    public static void sendMutiMessage(final Context ctx, String title, Bitmap bm) {

        Oauth2AccessToken mAccessToken;

        AuthInfo mAuthInfo;
        /** 注意：SsoHandler 仅当 SDK 支持 SSO 时有效 */
        SsoHandler mSsoHandler;

        /** 微博微博分享接口实例 */
        IWeiboShareAPI mWeiboShareAPI = null;
        mAccessToken = new Oauth2AccessToken();
        AuthListener authListener = new AuthListener((Activity) ctx);
        // 创建微博分享接口实例
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(ctx, SlideConstants.APP_KEY);

        // 注册第三方应用到微博客户端中，注册成功后该应用将显示在微博的应用列表中。
        // 但该附件栏集成分享权限需要合作申请，详情请查看 Demo 提示
        // NOTE：请务必提前注册，即界面初始化的时候或是应用程序初始化时，进行注册
        mWeiboShareAPI.registerApp();
        // 1. 初始化微博的分享消息
        // 用户可以分享文本、图片、网页、音乐、视频中的一种

        // 1. 初始化微博的分享消息
        // 用户可以分享文本、图片、网页、音乐、视频中的一种
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        TextObject textObject = new TextObject();
        textObject.text = title;
        weiboMessage.textObject = textObject;
        ImageObject imageObject = new ImageObject();

        imageObject.setImageObject(bm);
        weiboMessage.imageObject = imageObject;
        // 2. 初始化从第三方到微博的消息请求
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;


        // 3. 发送请求消息到微博，唤起微博分享界面

        AuthInfo authInfo = new AuthInfo(ctx, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(ctx);
        String token = "";
        if (accessToken != null) {
            token = accessToken.getToken();
        } else {
            mAuthInfo = new AuthInfo(ctx, SlideConstants.APP_KEY, SlideConstants.REDIRECT_URL, SlideConstants.SCOPE);
            mSsoHandler = new SsoHandler((Activity) ctx, mAuthInfo);
            mSsoHandler.authorizeWeb(authListener);
            accessToken = AccessTokenKeeper.readAccessToken(ctx);
            if (accessToken != null)
                token = accessToken.getToken();
        }

        DisplayUtils.recycleBitmap(bm);
        mWeiboShareAPI.sendRequest((Activity) ctx, request, authInfo, token, new WeiboAuthListener() {

            @Override
            public void onWeiboException(WeiboException arg0) {
            }

            @Override
            public void onComplete(Bundle bundle) {
                // TODO Auto-generated method stub
                Oauth2AccessToken newToken = Oauth2AccessToken.parseAccessToken(bundle);
                AccessTokenKeeper.writeAccessToken(ctx, newToken);
            }

            @Override
            public void onCancel() {
            }
        });
    }

    /**
     * 微博认证授权回调类。
     * 1. SSO 授权时，需要在 {@link #onActivityResult} 中调用 {@link SsoHandler#authorizeCallBack} 后，
     * 该回调才会被执行。
     * 2. 非 SSO 授权时，当授权结束后，该回调就会被执行。
     * 当授权成功后，请保存该 access_token、expires_in、uid 等信息到 SharedPreferences 中。
     */
    static class AuthListener implements WeiboAuthListener {

        Oauth2AccessToken mAccessToken;

        Activity activity;


        public AuthListener(Activity activity) {
            super();
            this.activity = activity;
        }

        @Override
        public void onComplete(Bundle values) {
            // 从 Bundle 中解析 Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {

                AccessTokenKeeper.writeAccessToken(activity, mAccessToken);
                Toast.makeText(activity,
                        R.string.weibosdk_demo_toast_auth_success, Toast.LENGTH_SHORT).show();
            } else {
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                String code = values.getString("code");
                String message = activity.getResources().getString(R.string.weibosdk_demo_toast_auth_failed);
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCancel() {
            Toast.makeText(activity,
                    R.string.weibosdk_demo_toast_auth_canceled, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
        }
    }
}
