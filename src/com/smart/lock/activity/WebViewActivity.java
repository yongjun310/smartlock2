package com.smart.lock.activity;

import java.security.interfaces.RSAPublicKey;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.smart.lock.R;
import com.smart.lock.common.ContentTypeEnum;
import com.smart.lock.common.FavoriteTypeEnum;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.dto.AccountActionLogDTO;
import com.smart.lock.service.LockService;
import com.smart.lock.utils.ActionLogOperator;
import com.smart.lock.utils.DisplayUtils;
import com.smart.lock.utils.FavoriteUtils;
import com.smart.lock.utils.NetUtils;
import com.smart.lock.utils.PageChangeUtils;
import com.smart.lock.utils.SecurityUtils;
import com.smart.lock.utils.ShareUtils;
import com.smart.lock.utils.SharedPreferencesUtils;
import com.smart.lock.utils.SysApplication;
import com.smart.lock.utils.*;
import com.smart.lock.view.CustomDialog;
import com.smart.lock.view.CustomProgressDialog;

public class WebViewActivity extends BaseActivity {

    private static Gson gson = new Gson();

    private int screenWidth, screenHeight, viewType, contentId, accountId,
            assnType;

    private String modulus, publicExponent;

    private String preActivity;

    private RSAPublicKey publicKey;

    private String bonusAmount;

    private static final int LOAD_START = 0;

    private static final int LOAD_OVER = 1;

    private static final int LOAD_PROGRESS = 2;

    private CustomProgressDialog progressDialog;

    private WebView webView;

    private String url = SlideConstants.FAQ_URL;

    private TextView backTextView;

    /**
     * 是否已暂停
     */
    private boolean pause;

    /**
     * 分享标题
     */
    private String shareTitle;

    /**
     * 分享图标
     */
    private String shareImgUrl;

    /**
     * 分享链接
     */
    private String shareLineLink;

    /**
     * 分享详细描述
     */
    private String descContent = "遇见锁屏，一秒点亮你的视线";

    private ImageView favoriteMenu, shareMenu, closeMenu;

    private TextView shareAmountMenu, favoriteText;

    private boolean needLoadUrl = true;

    private Dialog shareDialog = null;

    private View menuLayout, btnBack;

    private String shareAmount;

    private int bonusType;

    private TextView tvTitle;
    
    private WebSettings webSettings;
    
    private LockServiceReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        SysApplication.getInstance().addActivity(this);

        receiver=new LockServiceReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(SlideConstants.LOCKSERVICE_ACTION);
        registerReceiver(receiver, filter);
        Bundle bundle = this.getIntent().getExtras();
        viewType = bundle.getInt("type");
        preActivity = bundle.getString("preActivity");
        SharedPreferences commonSP = getSharedPreferences("common",
                MODE_MULTI_PROCESS);
        accountId = commonSP.getInt("accountId", -1);
        bonusType = bundle.getInt("bonusType");
        shareAmount = bundle.getString("bonusAmount");
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        SharedPreferences mySP = getSharedPreferences("rsa" + accountId,
                Context.MODE_MULTI_PROCESS);
        modulus = mySP.getString("modulus", "");
        publicExponent = mySP.getString("publicExponent", "");
        publicKey = SecurityUtils.getPublicKey(modulus, publicExponent);

        webView = (WebView) findViewById(R.id.webview_content);
        menuLayout = (View) findViewById(R.id.layout_mainTab);
        favoriteMenu = (ImageView) findViewById(R.id.img_mainTab_like);
        shareMenu = (ImageView) findViewById(R.id.img_mainTab_share);
        shareAmountMenu = (TextView) findViewById(R.id.txt_mainTab_share);
        closeMenu = (ImageButton) findViewById(R.id.btn_webview_close);
        favoriteText = (TextView) findViewById(R.id.txt_mainTab_like);
        tvTitle = (TextView) findViewById(R.id.title);
        btnBack = (View) findViewById(R.id.btn_back);
        DisplayUtils.enlargeClickArea(btnBack, 100);

        backTextView = (TextView) findViewById(R.id.back_text);
        if (preActivity != null && preActivity.equals("LockActivity")) {
            closeMenu.setVisibility(View.VISIBLE);
            closeMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SysApplication.getInstance().exit();
                }
            });
            backTextView.setText("锁屏");
        }
        if (bonusType == ContentTypeEnum.SHARE.getValue()) {
            if (!TextUtils.isEmpty(shareAmount) && new Float(shareAmount) > 0) {
                shareAmountMenu.setVisibility(View.VISIBLE);
                shareAmountMenu.setText("奖" + shareAmount + "元");
                /*menuLayout.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
                        LayoutParams.WRAP_CONTENT));*/
            } else {
                shareAmountMenu.setText("分享");
                /*menuLayout.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
                        150));*/
            }
        }
        progressDialog = CustomProgressDialog.createDialog(WebViewActivity.this);

        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUserAgentString(webSettings.getUserAgentString() + "imiaoxiu");
        webSettings.setBlockNetworkImage(true);

        webView.setDownloadListener(new MyWebViewDownLoadListener());

        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        //webView.addJavascriptInterface(new JsInterf(), "app");
        initData(bundle);
        webView.setWebViewClient(webViewClient);
        webView.setWebChromeClient(webChromeClient);
        if (needLoadUrl)
            webView.loadUrl(url);
        // Toast.makeText(this, SlideConstants.TOAST_WEB_VIEW_LOADING,
        // Toast.LENGTH_SHORT).show();
    }


	private void initData(Bundle bundle) {
		SharedPreferences commonSP;
		switch (viewType) {
            case SlideConstants.WEB_VIEW_CONTENT:
                menuLayout.setVisibility(View.VISIBLE);
                contentId = bundle.getInt("contentId");
                shareImgUrl = bundle.getString("image");
                shareLineLink = bundle.getString("link");
                bonusAmount = bundle.getString("bonusAmount");
                shareLineLink += "&headpic=" + shareImgUrl;
                String tempTitle = bundle.getString("title");
                if (tempTitle != null) {
                    descContent = tempTitle;
                }

                url = (shareLineLink == null || shareLineLink.length() == 0) ? SlideConstants.FAQ_URL
                        : shareLineLink;

                assnType = bundle.getInt("assnType");
                if (assnType == FavoriteTypeEnum.FAVORITE.getValue()) {
                    favoriteMenu.setSelected(true);
                    favoriteText.setTextColor(this.getResources().getColor(R.color.maintabred));
                }
                //
                // if (contentId >= 0) {
                // ContentDTO content = ContentsDataOperator.getById(this,
                // contentId);
                // if (content != null && content.getLink() != null
                // && content.getLink().length() > 0)
                // url = content.getLink();
                // }
                break;

            case SlideConstants.WEB_VIEW_FAQ:
                contentId = bundle.getInt("contentId");
                menuLayout.setVisibility(View.GONE);

                tvTitle.setText("常见问题");
                url = SlideConstants.FAQ_URL;
                break;

            case SlideConstants.WEB_VIEW_FEEDBACK:
                contentId = bundle.getInt("contentId");
                menuLayout.setVisibility(View.GONE);
                tvTitle.setText("意见反馈");
                // menuLayout.removeView(shareMenu);

                url = SlideConstants.SERVER_URL
                        + SlideConstants.SERVER_METHOD_FEEDBACK + "?accountId="
                        + accountId;
                break;


            case SlideConstants.WEB_VIEW_RECOMMAND:
//                contentId = bundle.getInt("contentId");
                menuLayout.setVisibility(View.GONE);
                tvTitle.setText(DisplayUtils.getSubTitle(bundle.getString("title"), 10));

                webView.addJavascriptInterface(new JsInterf(), "app");
                url = bundle.getString("link");
                break;

            case SlideConstants.WEB_VIEW_MYMESSAGE:
                tvTitle.setText("消息中心");
                menuLayout.setVisibility(View.GONE);
                NetUtils.loadEncypUrl(this, webView, SlideConstants.SERVER_METHOD_TRANSACTION, SlideConstants.SERVER_METHOD_MESSAGE_LIST);
                needLoadUrl = false;
                webView.addJavascriptInterface(new JsInterf(), "app");
//    			backTextView.setVisibility(View.VISIBLE);
                break;

            case SlideConstants.WEB_VIEW_MYMESSAGE_DETAIL:
                menuLayout.setVisibility(View.GONE);
                int messageId = 0;
                //for avoid taoke page displays in pc style
                webView.addJavascriptInterface(new JsInterf(), "app");
                tvTitle.setText("消息中心");
                commonSP = getSharedPreferences("common", 0);
                messageId = commonSP.getInt("messageId", 0);
                preActivity = commonSP.getString("preActivity", null);
                NetUtils.loadEncypUrl(this, webView, SlideConstants.SERVER_METHOD_TRANSACTION,
                        SlideConstants.SERVER_METHOD_MESSAGE_DETAIL + "?messageId=" + messageId);
                needLoadUrl = false;
                backTextView.setVisibility(View.VISIBLE);
                break;

            default:
                break;
        }
	}

    private class MyWebViewDownLoadListener implements DownloadListener {

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                                    long contentLength) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }

    }

    @Override
    public void onBackPressed() {
        webBack();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView != null && webView.getUrl() != null && webView.getUrl().contains(SlideConstants.SERVER_METHOD_MESSAGE_LIST)) {
                this.finish();
            } else {
                webBack();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void webBack() {
        if ("LockActivity".equals(preActivity)) {
            //this.startActivity(new Intent(this, LockActivity.class));
        	this.finish();
            PageChangeUtils.changeLockServiceState(this, LockService.STATE_SHOW);
        }
        if ("PushReceiver".equals(preActivity)) {
            if (SlideConstants.WEB_VIEW_RECOMMAND == viewType) {
                this.finish();
            } else if (SlideConstants.WEB_VIEW_MYMESSAGE == viewType) {
                Intent activityIntent = new Intent();
                activityIntent.setClass(this, MainActivity.class);
                activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(activityIntent);
                this.finish();
            } else {
                viewType = SlideConstants.WEB_VIEW_MYMESSAGE;
                NetUtils.loadEncypUrl(this, webView, SlideConstants.SERVER_METHOD_TRANSACTION, SlideConstants.SERVER_METHOD_MESSAGE_LIST);
            }
        } else {
        	//for taoke redirect link
        	if(!TextUtils.isEmpty(url) && !url.equals(webView.getUrl()) && needLoadUrl) {
        		webView.clearHistory();
        		webView.loadUrl(url);
        	} else if (webView != null && webView.canGoBack() && !webView.getUrl().equals(url)) {
                webView.goBack();
            } else {
                this.finish();
            }
        }
    }

    private void backHome() {
        Intent mHomeIntent = new Intent(Intent.ACTION_MAIN);

        mHomeIntent.addCategory(Intent.CATEGORY_HOME);
        mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivity(mHomeIntent);
    }

    private WebViewClient webViewClient = new WebViewClient() {

        /** 重写点击动作,用webview载入 */
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view,
                                                final String url) {
            webView.loadUrl(url);
            return true;
        }

        public void onReceivedSslError(WebView view, SslErrorHandler handler,
                                       SslError error) {
            handler.proceed();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            showProgressDialog();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            // 如果全部载入,隐藏进度对话框
            ActionLogOperator.add(getApplicationContext(), new AccountActionLogDTO(accountId, 35, contentId));

            dismissProgressDialog();
            webView.getSettings().setBlockNetworkImage(false);
            WebViewActivity.this.onPageFinished();
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            Toast.makeText(WebViewActivity.this, description,
                    Toast.LENGTH_SHORT).show();
        }

        /** 按键事件 */
        @Override
        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            return super.shouldOverrideKeyEvent(view, event);
        }
    };

    /* 定义一个Handler，用于处理下载线程与UI间通讯 */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_START:
                    // 开始加载
                    if (!pause) {
                        progressDialog.show();// 显示进度对话框
                    }

                    break;

                case LOAD_OVER:
                    // 加载完毕
                    if (!pause) {
                        progressDialog.dismiss();
                    }


                    break;

                case LOAD_PROGRESS:
                    // 进度
                    break;
            }
        }
    };

    /**
     * 显示对话框
     *
     * @see [类、类#方法、类#成员]
     */
    public void showProgressDialog() {
        handler.sendEmptyMessage(LOAD_START);
    }

    /**
     * 关闭对话框
     *
     * @see [类、类#方法、类#成员]
     */
    protected void dismissProgressDialog() {
        handler.sendEmptyMessage(LOAD_OVER);
    }

    private WebChromeClient webChromeClient = new WebChromeClient() {

        @Override
        public void onProgressChanged(WebView view, int progress) {

            super.onProgressChanged(view, progress);
        }

        @Override
        public boolean onJsBeforeUnload(WebView view, String url,
                                        String message, JsResult result) {
            return super.onJsBeforeUnload(view, url, message, result);
        }

        /**
         * 覆盖默认的window.alert展示界面，避免title里显示为“：来自file:////”
         */
        @Override
        public boolean onJsAlert(WebView view, String url, String message,
                                 final JsResult result) {
            return super.onJsAlert(view, url, message, result);
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message,
                                   final JsResult result) {
            return super.onJsConfirm(view, url, message, result);
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message,
                                  String defaultValue, final JsPromptResult result) {
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            Log.d("ANDROID_LAB", "TITLE=" + title);
            shareTitle = title;
            if (shareTitle.substring(shareTitle.length() - 1).equals("|")) {
                shareTitle = shareTitle.substring(0, shareTitle.length() - 2);
            }
        }
    };

    public void backClick(View view) {
        onBackPressed();
        // if (preActivity.equals("LockActivity")) {
        // this.finish();
        // backHome();
        // }
        // if (preActivity.equals("MainActivity")) {
        // Intent activityIntent = new Intent();
        // activityIntent.setClass(this, MainActivity.class);
        // activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //
        // startActivity(activityIntent);
        //
        // this.finish();
        // }
    }

    public void refreshClick(View view) {
        webView.loadUrl(url);
        // Toast.makeText(this, SlideConstants.TOAST_WEB_VIEW_REFRESHING,
        // Toast.LENGTH_SHORT).show();
    }

    public void shareClick(View view) {
        shareMenu.setSelected(true);
        NetUtils.umengSelfEvent(this, "webview_share");
        shareAmountMenu.setTextColor(this.getResources().getColor(R.color.maintabred));
        /*Intent intent = new Intent();
        intent.setClass(this, ShareActivity.class);
        intent.putExtra("preActivity", "WebViewActivity");
        intent.putExtra("bonusAmount", bonusAmount);
        intent.putExtra("bonusType", bonusType);
        intent.putExtra("link", shareLineLink);
        intent.putExtra("type", SlideConstants.WEB_VIEW_CONTENT);
        String localPath = this.getIntent().getExtras().getString("localPath");
        intent.putExtra("localPath", localPath);
        intent.putExtra("shareTitle", shareTitle);
        intent.putExtra("shareImgUrl", shareImgUrl);
        intent.putExtra("content", this.getIntent().getExtras().getString("content"));
        intent.putExtra("contentId", contentId);
        startActivity(intent);*/
        ShareUtils.shareContent(WebViewActivity.this, shareImgUrl, shareLineLink, shareTitle, this.getIntent().getExtras().getString("content"), contentId);
        shareMenu.setSelected(false);
        shareAmountMenu.setTextColor(this.getResources().getColor(R.color.maintabgrey));
    }

    public void tabHomeClick(View view) {
        NetUtils.umengSelfEvent(this, "webview_first_page");
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        startActivity(intent);
    }

    public void favoriteClick(View view) {
    	webView.loadUrl("http://192.168.0.110:8081/userAgent.html");
        NetUtils.umengSelfEvent(this, "webview_favorite");
        ActionLogOperator.add(getApplicationContext(), new AccountActionLogDTO(accountId, assnType == FavoriteTypeEnum.FAVORITE.getValue() ? 37 : 36, contentId));
        // ContentDTO content = ContentsDataOperator.getById(this, contentId);
        // content.setFavorite(true);
        // ContentsDataOperator.update(this, content);
//        ActionLogOperator.add(this, new AccountActionLogDTO(accountId,
//                ActionTypeEnum.FAVORTITE_IN_WEBVIEW, contentId));
        if (assnType == FavoriteTypeEnum.FAVORITE.getValue()) {
            favoriteMenu.setSelected(false);
            favoriteText.setTextColor(this.getResources().getColor(R.color.black));
            assnType = FavoriteTypeEnum.NO_FEEL.getValue();

            FavoriteUtils.cancelFavoriteContent(this, accountId, contentId);
        } else {
            favoriteMenu.setSelected(true);
            favoriteText.setTextColor(this.getResources().getColor(R.color.maintabred));
            assnType = FavoriteTypeEnum.FAVORITE.getValue();

            FavoriteUtils.favoriteContent(this, accountId, contentId);
        }

        SharedPreferencesUtils.putIntSP(this, "FavoriteChange", contentId);

//        FavoriteUtils.updateAssnType(this, accountId, contentId, assnType);
        // Toast.makeText(this, SlideConstants.TOAST_WEB_VIEW_FAVORITE_SUCCESS,
        // Toast.LENGTH_SHORT).show();
    }

    protected void onPageFinished() {
        shareLineLink = webView.getUrl();
    }

    @Override
    public void onResume() {
        super.onResume();
        shareMenu.setSelected(false);
        shareAmountMenu.setTextColor(this.getResources().getColor(R.color.maintabgrey));
        pause = false;
        if (shareDialog != null) {
            shareDialog.dismiss();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        pause = true;
        progressDialog.dismiss();
        if (isFinishing()) {
            // 载入空白
            webView.loadUrl("about:blank");
        }
    }

    public void hideShareAmount() {
        shareAmountMenu.setVisibility(View.GONE);
        shareAmountMenu.setText("");
        menuLayout.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
                60));
    }
    
    final class JsInterf {
        JsInterf() {
        }

        @JavascriptInterface
        public void redirectLogin() {
            Intent activityIntent = new Intent();
            activityIntent.putExtra("bindMobile", true);
            activityIntent.setClass(WebViewActivity.this, LoginActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(activityIntent);
        }

        @JavascriptInterface
        public void share(String title, String imageUrl, String link, String desc) {
            shareDialog = ShareUtils.shareContent(WebViewActivity.this, imageUrl, link, title, desc, 0);
            NetUtils.umengSelfEvent(WebViewActivity.this,"dispatch_gprs_package");
        }

        @JavascriptInterface
        public void shareNew(String title, String imageUrl, String link, String desc) {
            link += accountId;
            shareDialog = ShareUtils.shareContent(WebViewActivity.this, imageUrl, link, title, desc, 0);
            NetUtils.umengSelfEvent(WebViewActivity.this, "dispatch_gprs_package", 0);
        }
    }
    
    class LockServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			int control = intent.getIntExtra("state", -1);
			switch (control) {
			case LockService.STATE_SHOW:
				WebViewActivity.this.finish();
				break;
			default:
				break;
			}
		}
    }
}
