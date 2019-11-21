package com.smart.lock.activity;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;
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
import com.smart.lock.service.LockService;
import com.smart.lock.sina.AccessTokenKeeper;
import com.smart.lock.sina.Constants;
import com.smart.lock.utils.ActionLogOperator;
import com.smart.lock.utils.DisplayUtils;
import com.smart.lock.utils.FileUtils;
import com.smart.lock.utils.NetUtils;
import com.smart.lock.utils.PageChangeUtils;
import com.smart.lock.utils.SharedPreferencesUtils;
import com.smart.lock.utils.SysApplication;
import com.smart.lock.utils.ThreadHelper;
import com.smart.lock.view.CustomProgressDialog;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SocializeEntity;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

public class ShareActivity extends BaseActivity {

	private static Gson gson = new Gson();

	private EditText shareText;

	private ImageView imageView;

	private Bitmap bm, mbm;

	private int width, height;

	private final String DESCRIPTOR = "com.umeng.share";

	private int contentId, accountId;
    private Oauth2AccessToken mAccessToken;
    
    private AuthInfo mAuthInfo;

    /** 注意：SsoHandler 仅当 SDK 支持 SSO 时有效 */
    private SsoHandler mSsoHandler;
	
    /** 微博微博分享接口实例 */
    private IWeiboShareAPI  mWeiboShareAPI = null;
    
    private CustomProgressDialog progressDialog;

	private String shareTitle, shareLink, shareContent, shareImage, imgLocalPath;
	private UMSocialService mController = UMServiceFactory
			.getUMSocialService(DESCRIPTOR);
	
	private String preActivity;

	public ShareActivity() {
	}


	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 1:
					ContentDTO content = (ContentDTO) msg.obj;
					PageChangeUtils.redirectFullImageView(ShareActivity.this, content, "LikeFragment", -1);
					break;
				case 2:
					progressDialog.dismiss();
					bm = BitmapFactory.decodeFile(imgLocalPath);
					setImageBG();
					shareContent(ShareActivity.this, imgLocalPath, shareLink, shareTitle, shareContent, contentId);
					break;
				default:
					break;
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_share);
		SysApplication.getInstance().addActivity(this);
		imageView = (ImageView) findViewById(R.id.share_image);
		shareText = (EditText) findViewById(R.id.share_text);
		DisplayUtils.setTitleAndBackBtn(this, "图文分享");
		contentId = getIntent().getExtras().getInt("contentId");
		shareTitle = getIntent().getExtras().getString("shareTitle");
		shareLink = getIntent().getExtras().getString("link");
		shareContent = getIntent().getExtras().getString("content");
		shareImage = getIntent().getExtras().getString("shareImgUrl");
		imgLocalPath = getIntent().getExtras().getString("localPath");
		preActivity = this.getIntent().getExtras().getString("preActivity");

        progressDialog = CustomProgressDialog.createDialog(this);
        
        mAccessToken = new Oauth2AccessToken();
        // 创建微博分享接口实例
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, SlideConstants.APP_KEY);
        
        // 注册第三方应用到微博客户端中，注册成功后该应用将显示在微博的应用列表中。
        // 但该附件栏集成分享权限需要合作申请，详情请查看 Demo 提示
        // NOTE：请务必提前注册，即界面初始化的时候或是应用程序初始化时，进行注册
        mWeiboShareAPI.registerApp();
        
		shareText.setText(shareTitle);

		DisplayMetrics dm = getResources().getDisplayMetrics();
		width = dm.widthPixels;
		height = dm.heightPixels;
		accountId = SharedPreferencesUtils.getIntSP(this, SharedPreferencesUtils.ACCOUNT_ID, -1);
		File file = null;
		if(imgLocalPath != null) 
			file = new File(imgLocalPath);
		if (imgLocalPath == null || !file.exists()) {
			final String filePrefix = SlideConstants.EXTERNAL_PHOTO_IMAGE_PATH;
			File dirFile = new File(filePrefix);
			if (!dirFile.exists()) {
				dirFile.mkdirs();
			}
			progressDialog.show();
			new Thread(new Runnable() {
				@Override
				public void run() {
					if (FileUtils.downloadImage(shareImage
							+ String.format(
							SlideConstants.QINIU_PIC_PARAM,
							height, width), imgLocalPath)) {
						Message message = Message.obtain(handler, 2);
						handler.sendMessage(message);
					}
				}
			}).start();
		} else {
			bm = BitmapFactory.decodeFile(imgLocalPath);
			setImageBG();
			shareContent(this, imgLocalPath, shareLink, shareTitle, shareContent, contentId);
		}

		DisplayUtils.setFont(this, shareText);
		final Map<String, SHARE_MEDIA> mPlatformsMap = new HashMap<String, SHARE_MEDIA>();
		mPlatformsMap.put("新浪微博", SHARE_MEDIA.SINA);

		mPlatformsMap.put("QQ", SHARE_MEDIA.QQ);
		mPlatformsMap.put("QQ空间", SHARE_MEDIA.QZONE);
		mPlatformsMap.put("微信", SHARE_MEDIA.WEIXIN);
		mPlatformsMap.put("朋友圈", SHARE_MEDIA.WEIXIN_CIRCLE);
		final CharSequence[] items = {"微信", "朋友圈", "QQ空间", "QQ", "新浪微博"};
		final SocializeListeners.SnsPostListener mShareListener = new SocializeListeners.SnsPostListener() {

			@Override
			public void onStart() {

			}

			@Override
			public void onComplete(SHARE_MEDIA platform, int stCode,
								   SocializeEntity entity) {
				if (stCode == 200) {
					Toast.makeText(ShareActivity.this, "分享成功", Toast.LENGTH_SHORT)
							.show();
//					if (contentId >= 0)
//						ActionLogOperator.add(ShareActivity.this, new AccountActionLogDTO(accountId,
//								ActionTypeEnum.SHARE_SUCCESS, contentId));
					ThreadHelper.getInstance(ShareActivity.this).addTask(SlideConstants.THREAD_POST_LOG);
				} /*else {
					Toast.makeText(activity,
							"分享失败 : error code : " + stCode, Toast.LENGTH_SHORT)
							.show();
				}*/
			}
		};
		View.OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
//				if(contentId >= 0)
//					ActionLogOperator.add(ShareActivity.this, new AccountActionLogDTO(accountId,
//							ActionTypeEnum.SHARE, contentId));
				// 获取用户点击的平台
				final SHARE_MEDIA platform = mPlatformsMap.get(items[Integer.parseInt((String)v.getTag())]);
				if ("WEIXIN".equals(platform)) {
					ActionLogOperator.add(getApplicationContext(), new AccountActionLogDTO(accountId, 31, contentId));
				}
				if ("WEIXIN_CIRCLE".equals(platform)) {
					ActionLogOperator.add(getApplicationContext(), new AccountActionLogDTO(accountId, 32, contentId));
				}
				if ("QQ".equals(platform)) {
					ActionLogOperator.add(getApplicationContext(), new AccountActionLogDTO(accountId, 33, contentId));
				}
				if ("SINA".equals(platform)) {
					ActionLogOperator.add(getApplicationContext(), new AccountActionLogDTO(accountId, 34, contentId));
				}

				shareContent(ShareActivity.this, imgLocalPath, shareLink, shareTitle,shareText.getEditableText().toString(),contentId);
				ShareActivity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						String editStr = shareText.getEditableText().toString();
						if (!TextUtils.isEmpty(editStr) && editStr.equals(shareTitle)) {
							NetUtils.umengSelfEvent(ShareActivity.this, "share_edited", "not edit");
						} else {
							NetUtils.umengSelfEvent(ShareActivity.this, "share_edited", "edit");
						}

						NetUtils.umengSelfEvent(ShareActivity.this, "share_share_to", platform.name());
						if ("SINA".equals(platform.name())) {
							sendMutiMessage(shareContent + "  #全世界的美图都在这# @遇见锁屏" + shareLink);
						} else {
							mController.postShare(ShareActivity.this, platform,
									mShareListener);
						}
						if ("QQ".equals(platform.name()) || "QZONE".equals(platform.name())) {
							Runnable mRunnable = new Runnable() {

								@Override
								public void run() {
									if (contentId >= 0) {
//										ActionLogOperator.add(ShareActivity.this, new AccountActionLogDTO(accountId,
//												ActionTypeEnum.SHARE_SUCCESS, contentId));
										ThreadHelper.getInstance(ShareActivity.this).addTask(SlideConstants.THREAD_POST_LOG);
									}
								}
							};

							handler.postDelayed(mRunnable, 10000);
						}
					}
				});

			}
		};
		findViewById(R.id.layout_0).setTag("0");
		findViewById(R.id.layout_1).setTag("1");
		findViewById(R.id.layout_2).setTag("2");
		findViewById(R.id.layout_3).setTag("3");
		findViewById(R.id.layout_4).setTag("4");
		findViewById(R.id.layout_0).setOnClickListener(listener);
		findViewById(R.id.layout_1).setOnClickListener(listener);
		findViewById(R.id.layout_2).setOnClickListener(listener);
		findViewById(R.id.layout_3).setOnClickListener(listener);
		findViewById(R.id.layout_4).setOnClickListener(listener);
	}

	private Bitmap convertToBitmap(String path) {
		int w = width;
		int h = height;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		// 设置为ture只获取图片大小
		opts.inJustDecodeBounds = true;
		opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
		// 返回为空
		BitmapFactory.decodeFile(path, opts);
		int width = opts.outWidth;
		int height = opts.outHeight;
		float scaleWidth = 0.f, scaleHeight = 0.f;
		if (width > w || height > h) {
			// 缩放
			scaleWidth = ((float) width) / w;
			scaleHeight = ((float) height) / h;
		}
		opts.inJustDecodeBounds = false;
		float scale = Math.max(scaleWidth, scaleHeight);
		opts.inSampleSize = (int) scale;
		WeakReference<Bitmap> weak = new WeakReference<Bitmap>(BitmapFactory.decodeFile(path, opts));
		return Bitmap.createScaledBitmap(weak.get(), w, h, true);
	}

	private void setImageBG() {
		if(bm != null) {
			mbm = bm.copy(Bitmap.Config.ARGB_8888, true);
			Canvas canvas = new Canvas(mbm);
			Bitmap markBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.icon_mark);
			Bitmap sbmp = DisplayUtils.small(markBitmap, 0.3f);
			canvas.drawBitmap(sbmp, width - sbmp.getWidth() - 130, height - sbmp.getHeight() - 100, null);
			canvas.save(Canvas.ALL_SAVE_FLAG);
			canvas.restore();
			DisplayUtils.recycleBitmap(markBitmap);
			DisplayUtils.recycleBitmap(sbmp);
	
			imageView.setBackgroundDrawable(new BitmapDrawable(mbm));
		}
	}

	public void shareContent(final Activity activity, String shareImgUrl, String shareLineLink, String shareTitle,
									  String descContent, final int contentId) {

		UMImage urlImage = null;
		if (shareImgUrl == null || shareImgUrl.length() == 0) {
			urlImage = new UMImage(activity, R.drawable.logo);
		} else {
			urlImage = new UMImage(activity, createDrawable(shareText.getEditableText().toString()));
		}
		// 配置腾讯微博
		//mController.getConfig().setSsoHandler(new TencentWBSsoHandler());



		// 添加微信平台
		UMWXHandler wxHandler = new UMWXHandler(activity,
				SlideConstants.WECHAT_APPID, SlideConstants.WECHAT_APPSEC);
		wxHandler.addToSocialSDK();

		WeiXinShareContent weixinContent = new WeiXinShareContent();
		//weixinContent.setShareContent(descContent);
		//weixinContent.setTitle(shareTitle);
		weixinContent.setShareImage(urlImage);
		//weixinContent.setTargetUrl(shareLineLink);
		weixinContent.setShareMedia(urlImage);
		mController.setShareMedia(weixinContent);

		// 支持微信朋友圈
		UMWXHandler wxCircleHandler = new UMWXHandler(activity,
				SlideConstants.WECHAT_APPID, SlideConstants.WECHAT_APPSEC);
		wxCircleHandler.setToCircle(true);
		wxCircleHandler.addToSocialSDK();

		CircleShareContent circleMedia = new CircleShareContent();
		/*if (contentId >= 0)
			circleMedia.setTitle(shareTitle);
		else
			circleMedia.setTitle(descContent);*/
		//circleMedia.setShareContent(descContent);
		circleMedia.setShareMedia(urlImage);
		circleMedia.setShareImage(urlImage);
		circleMedia.setTargetUrl(shareLineLink);
		mController.setShareMedia(circleMedia);

		// 设置QQ空间分享内容
		QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(activity,
				SlideConstants.QQ_APPID, SlideConstants.QQ_APPKEY);
		qZoneSsoHandler.setTargetUrl(shareLineLink);
		qZoneSsoHandler.addToSocialSDK();
		QZoneShareContent qzone = new QZoneShareContent();
		if (contentId >= 0)
			qzone.setTitle(shareTitle);
		else
			qzone.setTitle(" ");
		qzone.setShareContent(descContent);
		qzone.setTargetUrl(shareLineLink);
		qzone.setShareImage(urlImage);
		qzone.setShareMedia(urlImage);
		mController.setShareMedia(qzone);

		// QQ好友
		UMQQSsoHandler umQQSsoHandler = new UMQQSsoHandler(activity,
				SlideConstants.QQ_APPID, SlideConstants.QQ_APPKEY);
		//umQQSsoHandler.setTargetUrl(shareLineLink);
		umQQSsoHandler.addToSocialSDK();

		QQShareContent qqShareContent = new QQShareContent();
		//qqShareContent.setShareContent(descContent);
		//qqShareContent.setTitle(shareTitle);
		qqShareContent.setShareImage(urlImage);
		qqShareContent.setShareMedia(urlImage);
		//qqShareContent.setTargetUrl(shareLineLink);
		mController.setShareMedia(qqShareContent);

		int accountId = SharedPreferencesUtils.getIntSP(activity, SharedPreferencesUtils.ACCOUNT_ID, -1);
//		TencentWbShareContent tencent = new TencentWbShareContent();
//		tencent.setShareContent(descContent);
//		tencent.setShareMedia(urlImage);
//		tencent.setTargetUrl(shareLineLink);
//		tencent.setTitle(shareTitle);
//		mController.setShareMedia(tencent);

		// 新浪微博
		/*SinaSsoHandler handler = new SinaSsoHandler();
		handler.setShareAfterAuthorize(true);
		mController.getConfig().setSsoHandler(handler);
		SinaShareContent sinaContent = new SinaShareContent();
		sinaContent.setShareContent(descContent + "#秒秀锁屏看段子还能赚钱# @秒秀在线" + shareLineLink);
		sinaContent.setShareMedia(urlImage);
		//sinaContent.setTargetUrl(shareLineLink);
		sinaContent.setTitle(shareTitle);
		sinaContent.setAppWebSite("");
		sinaContent.setShareImage(urlImage);
		mController.setShareMedia(sinaContent);*/

		// 新浪、QQ、QQ空间、易信、来往、豆瓣、人人平台

		/**
		 * 分享监听器
		 */

	}

	public void backClick(View view) {
		onBackPressed();
		NetUtils.umengSelfEvent(this, "share_back");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		DisplayUtils.recycleBitmap(bm);
		DisplayUtils.recycleBitmap(mbm);
		if(!TextUtils.isEmpty(preActivity) && preActivity.equals("LockService")) {
	        PageChangeUtils.changeLockServiceState(this, LockService.STATE_SHOW);
		}
	}

	private Bitmap createDrawable(String str) {
		mbm = bm.copy(Bitmap.Config.ARGB_8888, true);
		Paint paint = new Paint(); // 建立画笔
		paint.setDither(true);
		paint.setFilterBitmap(true);
		Bitmap retBM = DisplayUtils.smallToShareSize(mbm);
		Canvas canvas = new Canvas(retBM);

		Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG
				| Paint.DEV_KERN_TEXT_FLAG);
		textPaint.setTextSize(2*DisplayUtils.dip2px(ShareActivity.this, 18)/5);
		String familyName = "宋体";
		Typeface font = Typeface.create(familyName, Typeface.ITALIC);
		textPaint.setTypeface(DisplayUtils.getTypeface(this)); // 采用默认的宽度
		textPaint.setColor(Color.WHITE);
		textPaint.setShadowLayer(2, 2, 4, 0x66000000);
		int width = retBM.getWidth();
		int height = retBM.getHeight();


		String shareEditText = shareText.getEditableText().toString();

		int paddingLeftRight = 30;
		ArrayList<String> char_list = staticLayout((int) Math.abs(width - 2*paddingLeftRight), shareEditText, textPaint);

		Path mPath = new Path();
		int lineH = paddingLeftRight;
		Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		bgPaint.setColor(0x4C000000);
		if (char_list != null && char_list.size() == 1)
		{
			canvas.drawRect(paddingLeftRight - 10, height/4 -105 , width - (paddingLeftRight - 10), height / 4  -15, bgPaint);
			mPath.moveTo(paddingLeftRight, height / 4 - 55);
			mPath.lineTo(width - paddingLeftRight, height / 4 - 55);
			canvas.drawTextOnPath(char_list.get(0), mPath, 0, 5.5f, textPaint);
		}
		else
		{
			if(char_list.size() > 0) {
				canvas.drawRect(paddingLeftRight - 10, height /4 - 85, width - (paddingLeftRight - 10), height /4 + char_list.size() * lineH , bgPaint);
			}
			for (int i = 0; i < char_list.size(); i++)
			{
				// 换行时文字沿路径居中显示
				mPath.moveTo(paddingLeftRight, height/4 +i*lineH);
				mPath.lineTo(width - paddingLeftRight, height/4+i*lineH);

				canvas.drawTextOnPath(char_list.get(char_list.size()-1-i), mPath, 0,  char_list.size()*lineH-(i + 1)*(lineH+5), textPaint);
			}

		}
		/*canvas.drawText(contentDTO.getTitle(), 25, height-15-(char_list.size()+1)*(lineH + 9),
				textPaint);*/
		Bitmap bmp = BitmapFactory.decodeResource(this.getResources(), R.drawable.icon_mark);
		Bitmap sbmp = DisplayUtils.small(bmp, 0.12f);
		canvas.drawBitmap(sbmp, width - sbmp.getWidth() - 40, height - sbmp.getHeight() - 40, null);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();

		DisplayUtils.recycleBitmap(bmp);
		DisplayUtils.recycleBitmap(sbmp);
		DisplayUtils.recycleBitmap(mbm);

		return retBM;
	}

	/**
	 * <一句话功能简述>得到换行显示的字符串集合<BR>
	 * <功能详细描述>
	 *
	 * @param weidth 每行的宽度
	 * @param text 总共要显示的文字
	 * @return
	 * @see [类、类#方法、类#成员]
	 */
	public ArrayList<String> staticLayout(int weidth, String text, Paint paint_char)
	{
		ArrayList<String> al = new ArrayList<String>();
		CharSequence toMeasure = text;
		int end = toMeasure.length();
		int next = 0;
		float[] measuredWidth = {0};
		while (next < end)
		{
			int bPoint = paint_char.breakText(toMeasure, next, end, true,
					weidth, measuredWidth);
			int spacePosition = 0;
			int enterPosition = 0;
			enterPosition = text.substring(next, next + bPoint).indexOf('\n');
			spacePosition = text.substring(next, next + bPoint).lastIndexOf(' ');
			if (enterPosition <= 0)
			{// 没有找到回车
				/*if (spacePosition <= 0)
				{// 没有空格*/
					al.add(text.substring(next, next + bPoint));
					next += bPoint;
				/*}
				else
				{// 有空格，最后一个空格处换行
					al.add(text.substring(next, next + spacePosition + 1));
					next += spacePosition + 1;
				}*/
			}
			else
			{// 有回车，第一个回车处换行
				al.add(text.substring(next, next + enterPosition));// 空格不打印出来，因为现实方框
				next += enterPosition + 1;
			}
		}
		return al;
	}
	
	private void sendMutiMessage(String str) {
        
        // 1. 初始化微博的分享消息
        // 用户可以分享文本、图片、网页、音乐、视频中的一种

        // 1. 初始化微博的分享消息
        // 用户可以分享文本、图片、网页、音乐、视频中的一种
		WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
		TextObject textObject = new TextObject();
		textObject.text = shareText.getEditableText().toString();
		if(TextUtils.isEmpty(textObject.text)) {
			textObject.text = " ";
		}
		weiboMessage.textObject = textObject;
		ImageObject imageObject = new ImageObject();
		imageObject.setImageObject(createDrawable(shareText.getEditableText().toString()));
        weiboMessage.imageObject = imageObject;
        // 2. 初始化从第三方到微博的消息请求
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;
        
        
        // 3. 发送请求消息到微博，唤起微博分享界面
        
        AuthInfo authInfo = new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(getApplicationContext());
        String token = "";
        if (accessToken != null) {
            token = accessToken.getToken();
        } else {
        	mAuthInfo = new AuthInfo(this, SlideConstants.APP_KEY, SlideConstants.REDIRECT_URL, SlideConstants.SCOPE);
            mSsoHandler = new SsoHandler(this, mAuthInfo);
            mSsoHandler.authorizeWeb(new AuthListener());
            accessToken = AccessTokenKeeper.readAccessToken(getApplicationContext());
            if (accessToken != null) 
                token = accessToken.getToken();
        }
        mWeiboShareAPI.sendRequest(this, request, authInfo, token, new WeiboAuthListener() {
            
            @Override
            public void onWeiboException( WeiboException arg0 ) {
            }
            
            @Override
            public void onComplete( Bundle bundle ) {
                // TODO Auto-generated method stub
                Oauth2AccessToken newToken = Oauth2AccessToken.parseAccessToken(bundle);
                AccessTokenKeeper.writeAccessToken(getApplicationContext(), newToken);
            }
            
            @Override
            public void onCancel() {
            }
        });
    }
	/**
     * 微博认证授权回调类。
     * 1. SSO 授权时，需要在 {@link #onActivityResult} 中调用 {@link SsoHandler#authorizeCallBack} 后，
     *    该回调才会被执行。
     * 2. 非 SSO 授权时，当授权结束后，该回调就会被执行。
     * 当授权成功后，请保存该 access_token、expires_in、uid 等信息到 SharedPreferences 中。
     */
    class AuthListener implements WeiboAuthListener {
        
        @Override
        public void onComplete(Bundle values) {
            // 从 Bundle 中解析 Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {
                
                // 保存 Token 到 SharedPreferences
                AccessTokenKeeper.writeAccessToken(ShareActivity.this, mAccessToken);
                Toast.makeText(ShareActivity.this, 
                        R.string.weibosdk_demo_toast_auth_success, Toast.LENGTH_SHORT).show();
            } else {
                // 以下几种情况，您会收到 Code：
                // 1. 当您未在平台上注册的应用程序的包名与签名时；
                // 2. 当您注册的应用程序包名与签名不正确时；
                // 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
                String code = values.getString("code");
                String message = getString(R.string.weibosdk_demo_toast_auth_failed);
                if (!TextUtils.isEmpty(code)) {
                    message = message + "\nObtained the code: " + code;
                }
                Toast.makeText(ShareActivity.this, message, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onCancel() {
            Toast.makeText(ShareActivity.this, 
                    R.string.weibosdk_demo_toast_auth_canceled, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onWeiboException(WeiboException e) {
            Toast.makeText(ShareActivity.this, 
                    "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
