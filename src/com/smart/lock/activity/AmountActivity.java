package com.smart.lock.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.smart.lock.R;
import com.smart.lock.R.layout;
import com.smart.lock.common.FavoriteTypeEnum;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.dto.ContentDTO;
import com.smart.lock.utils.*;
import com.smart.lock.view.CustomDialog;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EncodingUtils;

import java.math.BigDecimal;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AmountActivity extends BaseActivity {

    private static Gson gson = new Gson();

    private int accountId;

    private String mobileNo, type;

    private TextView txtTitle;

    private WebView mallWebView;

    private Activity activity;

    private Handler messHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ContentDTO content = (ContentDTO) msg.obj;
                    PageChangeUtils.redirectFullImageView(activity, content, "AmountActivity", -1);
                    break;
                default:
                    break;
            }
        }
    };

//	private Handler messageHandler = new Handler() {
//		@Override
//		public void handleMessage(Message msg) {
//			switch (msg.what) {
//				case 1:
//					AccountDTO account = (AccountDTO) msg.obj;
//					if (account != null) {
//						txtAmount.setText(String.format("%.2f",
//								account.getTotalAmount()));
//
//						if (!mobileNo.equals(account.getMobileNo())) {
//							SharedPreferences commonSP = AmountActivity.this
//									.getSharedPreferences("common",
//											Context.MODE_MULTI_PROCESS );
//							Editor editor = commonSP.edit();
//							editor.putString("mobileNo", account.getMobileNo());
//							if (account.getMobileNo().length() > 0)
//								editor.putBoolean("isLogin", true);
//							editor.commit();
//
//							mobileNo = account.getMobileNo();
//						}

    //					if (account.getTotalAmount().compareTo(
//							SlideConstants.MIN_EXCHANGE_AMOUNT) >= 0) {
//						btnExchange.setClickable(true);
//						btnExchange.setBackgroundColor(this
//								.getApplicationContext().getResources()
//								.getColor(R.color.red));
//					}
//						btnExchange.setClickable(true);
//						if(this != null) {
//							btnExchange.setBackgroundColor(AmountActivity.this
//									.getResources()
//									.getColor(R.color.red));
//						}
//						if (mobileNo == null || mobileNo.length() == 0) {
//							//show bind tips
//							txtBindTips.setVisibility(View.VISIBLE);
//							txtBindTipsLink.setVisibility(View.VISIBLE);
//							txtBindTips.setText("为了保障资金安全，请");
//							txtBindTipsLink.setText("绑定您的手机号码");
//							txtBindTipsLink.setClickable(true);
//							txtBindTipsLink.setOnClickListener(new View.OnClickListener() {
//
//								@Override
//								public void onClick(View view) {
//									Intent activityIntent = new Intent();
//									activityIntent.putExtra("bindMobile", true);
//									activityIntent.setClass(AmountActivity.this, LoginActivity.class);
//									activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//									startActivity(activityIntent);
//								}
//							});
//						} else {
//							txtBindTips.setText("");
//							txtBindTipsLink.setText("");
//							txtBindTips.setVisibility(View.GONE);
//							txtBindTipsLink.setVisibility(View.GONE);
//						}
//					}
//					break;
//				case 2:
//					Toast.makeText(AmountActivity.this,
//							SlideConstants.TOAST_INCOME_SERVER_BUSY,
//							Toast.LENGTH_SHORT).show();
//					break;
//				case 3:
//					Toast.makeText(AmountActivity.this,
//							SlideConstants.TOAST_INCOME_SERVER_ERROR,
//							Toast.LENGTH_SHORT).show();
//					break;
//				default:
//					break;
//			}
//
//		}
//	};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_amount);
        SharedPreferences commonSP = this.getSharedPreferences(
                "common", Context.MODE_MULTI_PROCESS );
        accountId = commonSP.getInt("accountId", -1);
        mobileNo = commonSP.getString("mobileNo", "");

        activity = this;

        if (this.getIntent().getExtras() != null)
            type = this.getIntent().getExtras().get("type").toString();
        String titleStr = null;
        if ("amount".equals(type))
            titleStr = "兑换商城";
        if ("viewed".equals(type))
            titleStr = "我的足迹";
        DisplayUtils.setTitleAndBackBtn(this, titleStr);
//		txtAmount = (TextView) findViewById(R.id.txt_income_amount);
//		txtBindTips = (TextView) findViewById(R.id.txt_bind_tips);
//		txtBindTipsLink = (TextView) findViewById(R.id.txt_bind_tips_link);
//		btnExchange = (RelativeLayout) findViewById(R.id.layout_btn_exchange);
//		btnExchange.setClickable(false);
//		btnExchange.setBackgroundColor(this.getApplicationContext()
//				.getResources().getColor(R.color.grey));

//		inviteWebView = (WebView) findViewById(R.id.webview_invite_users);
//
//		WebSettings webSettings = inviteWebView.getSettings();
//		webSettings.setJavaScriptEnabled(true);
//
//		inviteWebView.addJavascriptInterface(new JsInterf(), "app");
//
//		inviteWebView.setWebViewClient(new WebViewClient());
//
//		NetUtils.loadEncypUrl(this, inviteWebView, SlideConstants.SERVER_METHOD_INVITE_USERS, null);

//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				initIncome();
//			}
//		}).start();
        mallWebView = (WebView) findViewById(R.id.webview_mall);

        WebSettings webSettings = mallWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        mallWebView.addJavascriptInterface(new JsInterf(), "app");

        mallWebView.setWebViewClient(new WebViewClient());

        if (!DeviceUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, SlideConstants.FAVORITE_NET_ERROR,
                    Toast.LENGTH_SHORT).show();
        } else {
            if ("amount".equals(type))
                NetUtils.loadEncypUrl(this, mallWebView, SlideConstants.SERVER_METHOD_TRANSACTION, SlideConstants.SERVER_METHOD_MALL_LIST);

            if ("viewed".equals(type))
                loadViewedUrl(this, mallWebView, SlideConstants.SERVER_METHOD_VIEWED);
        }
    }
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        backClick(null);
//        return super.onKeyDown(keyCode, event);
//    }

    public static void loadViewedUrl(Activity activity, WebView webView, String methodUrl) {
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
            params.add(new BasicNameValuePair("pageIndex", "1"));

            String postData = SecurityUtils.getInputString(params);

            webView.postUrl(SlideConstants.SERVER_URL
                            + methodUrl,
                    EncodingUtils.getBytes(postData, "base64"));
        } catch (Exception e) {
            e.printStackTrace();
            if(activity != null)
                Toast.makeText(activity,
                    SlideConstants.FAVORITE_POST_ERROR, Toast.LENGTH_SHORT)
                    .show();
        }
    }

//	private void initIncome() {
//		List<NameValuePair> params = new ArrayList<NameValuePair>();
//		params.add(new BasicNameValuePair("time", new SimpleDateFormat(
//				"yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()).toString()));
//		params.add(new BasicNameValuePair("androidId", DeviceUtils
//				.getAndroidId(this.getApplicationContext())));
//
//		SharedPreferences rsaSP = this.getSharedPreferences(
//				"rsa" + accountId, Context.MODE_MULTI_PROCESS );
//		String modulus = rsaSP.getString("modulus", "");
//		String publicExponent = rsaSP.getString("publicExponent", "");
//
//		RSAPublicKey publicKey = SecurityUtils.getPublicKey(modulus,
//				publicExponent);
//
//		String data = SecurityUtils.getInputString(params);
//		String sign = "";
//		try {
//			sign = SecurityUtils.encryptByPublicKey(data, publicKey);
//
//			params = new ArrayList<NameValuePair>();
//			params.add(new BasicNameValuePair("sign", sign));
//			params.add(new BasicNameValuePair("accountId", String
//					.valueOf(accountId)));
//
//			String respStr = NetUtils.post(SlideConstants.SERVER_URL
//					+ SlideConstants.SERVER_METHOD_INCOME, params);
//			AccountResponse resp = gson
//					.fromJson(respStr, AccountResponse.class);
//			if (resp.getCode() == SlideConstants.SERVER_RETURN_SUCCESS
//					&& resp.getData() != null) {
//				Message msg = Message.obtain(messageHandler, 1, resp.getData());
//				messageHandler.sendMessage(msg);
//			} else {
//				Message msg = Message.obtain(messageHandler, 2, resp.getMsg());
//				messageHandler.sendMessage(msg);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			Message msg = Message.obtain(messageHandler, 3);
//			messageHandler.sendMessage(msg);
//		}
//	}

    public void backClick(View view) {
        String url = mallWebView.getUrl();
        if(TextUtils.isEmpty(url)) {
        	this.finish();
        } else {
	        if (url.indexOf("lottery") >= 0)
	            NetUtils.loadEncypUrl(this, mallWebView, SlideConstants.SERVER_METHOD_TRANSACTION, SlideConstants.SERVER_METHOD_MALL_LIST);
	        else if (url.indexOf(SlideConstants.SERVER_METHOD_TRANSACTION) >= 0 || url.indexOf(SlideConstants.SERVER_METHOD_MALL_LIST) >= 0 || url.indexOf(SlideConstants.SERVER_METHOD_VIEWED) >= 0)
	            this.finish();
	        else
	            mallWebView.goBack();
        }
    }

    @Override
    public void onBackPressed() {
        backClick(null);
    }

    final class JsInterf {
        JsInterf() {
        }

        @JavascriptInterface
        public boolean viewFullImage(int contentId, String image, String link, String title, String content, int hasMore,
                                     int assnType, String bonusAmount, int bonusType) {
            ContentDTO contentDTO = new ContentDTO();
            contentDTO.setId(contentId);
            contentDTO.setBonusAmount(new BigDecimal(bonusAmount));
            contentDTO.setContent(content);
            contentDTO.setType(bonusType);
            contentDTO.setLink(link);
            contentDTO.setFavorite(assnType == FavoriteTypeEnum.FAVORITE.getValue());
            contentDTO.setTitle(title);
            contentDTO.setImage(image);
            contentDTO.setHasMore(hasMore);

//            PageChangeUtils.redirectFullImageView(getActivity().getApplicationContext(), contentDTO, "LikeFragment", -1);
            Message message = Message.obtain(messHandler, 1, contentDTO);
            messHandler.sendMessage(message);
            return true;
        }

        @JavascriptInterface
        public void share(String title, String imageUrl, String link, String desc) {
            ShareUtils.shareContent(activity, imageUrl, link, title, desc, -1);
        }

        @JavascriptInterface
        public void alert(String msg) {
            /*new AlertDialog.Builder(activity).setTitle("温馨提示")
            .setMessage(msg).setPositiveButton(R.string.go_bind, new DialogInterface.OnClickListener()
	        {
	            @Override
	            public void onClick(DialogInterface dialog, int which)
	            {
	            	PageChangeUtils.startLogin(activity);
	            	dialog.dismiss();
	            }
	        }).show();*/

            CustomDialog.Builder customBuilder = new
                    CustomDialog.Builder(activity);
            customBuilder.setTitle("温馨提示")
                    .setMessage(msg)
                    .setPositiveButton(R.string.go_bind,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    PageChangeUtils.startLogin(activity, "AmountActivity");
                                }
                            });
            CustomDialog customDialog = customBuilder.create();
            customDialog.show();
        }

        public void redirectLogin() {
            Intent activityIntent = new Intent();
            activityIntent.putExtra("bindMobile", true);
            activityIntent.setClass(AmountActivity.this, LoginActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(activityIntent);
        }
    }

}
