package com.smart.lock.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;
import com.smart.lock.R;
import com.smart.lock.activity.MainActivity;
import com.smart.lock.activity.ShareActivity;
import com.smart.lock.common.FavoriteTypeEnum;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.dto.AccountActionLogDTO;
import com.smart.lock.dto.ContentDTO;
import com.smart.lock.utils.*;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EncodingUtils;

import java.math.BigDecimal;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LikeFragment extends Fragment {

    private WebView webView;

    private int accountId, screenWidth, screenHeight;

    private TextView likeTitle;

    private Context ctx;

    public LikeFragment() {
    }

    private Handler messHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ContentDTO content = (ContentDTO) msg.obj;
                    PageChangeUtils.redirectFullImageView(getActivity(), content, "LikeFragment", -1);
                    break;
                case 2:
                    String localPath = (String) msg.obj;
                    Toast.makeText(getActivity(), "图片已下载到" + localPath, Toast.LENGTH_LONG).show();
                    break;
                case 3:
                    ContentDTO contentDTO = (ContentDTO) msg.obj;
                    ContentsDataOperator.update(getActivity(), contentDTO);
//                    ((MainActivity) getActivity()).checkRefresh(true, false);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_like, container,
                false);
        webView = (WebView) rootView.findViewById(R.id.webview_favorite);

        this.ctx = getActivity();

        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        SharedPreferences commonSP = getActivity().getSharedPreferences(
                "common", Context.MODE_MULTI_PROCESS);
        boolean isLogin = commonSP.getBoolean("isLogin", false);
        accountId = commonSP.getInt("accountId", -1);
        // if (!isLogin || accountId < 0) {
        // Intent activityIntent = new Intent();
        // activityIntent.setClass(getActivity(), LoginActivity.class);
        // activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // startActivity(activityIntent);
        // return rootView;
        // }
        //
        if (!DeviceUtils.isNetworkAvailable(getActivity())) {
            Toast.makeText(getActivity(), SlideConstants.FAVORITE_NET_ERROR,
                    Toast.LENGTH_SHORT).show();
        } else {
            refreshPage();
        }

        likeTitle = (TextView) rootView.findViewById(R.id.like_page_title);
        DisplayUtils.setFont(getActivity(), likeTitle);

        return rootView;
    }

    public void refreshPage() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        webView.setWebViewClient(new WebViewClient());

        webView.addJavascriptInterface(new JsInterf(), "app");

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("androidId", DeviceUtils
                .getAndroidId(getActivity())));
        params.add(new BasicNameValuePair("time", new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()).toString()));

        String data = SecurityUtils.getInputString(params);

        SharedPreferences rsaSP = getActivity().getSharedPreferences(
                "rsa" + accountId, Context.MODE_MULTI_PROCESS);
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

            String postData = SecurityUtils.getInputString(params);

            webView.postUrl(SlideConstants.SERVER_URL
                            + SlideConstants.SERVER_METHOD_FAVORITE_NEW,
                    EncodingUtils.getBytes(postData, "base64"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(),
                    SlideConstants.FAVORITE_POST_ERROR, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    final class JsInterf {
        JsInterf() {
        }

        @JavascriptInterface
        public boolean setFavorite(int contentId, boolean isFavorite) {
            ActionLogOperator.add(getActivity(), new AccountActionLogDTO(accountId, 44, contentId));

            ContentDTO contentDTO = ContentsDataOperator.getById(getActivity(), contentId);
            if (contentDTO != null) {
                contentDTO.setFavorite(isFavorite);
                Message message = Message.obtain(messHandler, 3, contentDTO);
                messHandler.sendMessage(message);

                SharedPreferencesUtils.putIntSP(ctx, "FavoriteChange", contentId);
            }
            return true;
        }

        @JavascriptInterface
        public boolean viewFullImage(int contentId, String image, String link, String title, String content, int hasMore,
                                     int assnType, String bonusAmount, int bonusType) {
            ActionLogOperator.add(getActivity(), new AccountActionLogDTO(accountId, 43, contentId));

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
        public boolean downloadPic(final int contentId, final String image) {
            ActionLogOperator.add(getActivity(), new AccountActionLogDTO(accountId, 45, contentId));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    String localPath = FileUtils.contentIdToLocalPath(contentId);
                    if (FileUtils.downloadImage(image
                            + String.format(
                            SlideConstants.QINIU_PIC_PARAM,
                            screenHeight, screenWidth), localPath)) {
                        Message message = Message.obtain(messHandler, 2, localPath);
                        messHandler.sendMessage(message);
                    }
                }
            }).start();
            return true;
        }

        @JavascriptInterface
        public boolean shareContent(final int contentId, final String image, String link, String title, String content, int hasMore) {
            ActionLogOperator.add(getActivity(), new AccountActionLogDTO(accountId, 46, contentId));

            final String localPath = FileUtils.contentIdToLocalPath(contentId) + ".dat";

            Intent intent = new Intent();

            intent.setClass(ctx, ShareActivity.class);
            intent.putExtra("preActivity", "WebViewActivity");
            intent.putExtra("link", link);
            intent.putExtra("type", SlideConstants.WEB_VIEW_CONTENT);
            intent.putExtra("localPath", localPath);
            intent.putExtra("shareTitle", title);
            intent.putExtra("shareImgUrl", image);
            intent.putExtra("content", content);
            intent.putExtra("contentId", contentId);
            startActivity(intent);
            return true;
        }
    }

}
