package com.smart.lock.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.gson.Gson;
import com.smart.lock.common.FavoriteTypeEnum;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.dto.ContentDTO;
import com.smart.lock.response.BooleanResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ѹ on 2015/6/3.
 */
public class FavoriteUtils {

    private static Gson gson = new Gson();

    public static void updateAssnType(final Context ctx,final int accountId, final int contentId, final int assnType) {
        SharedPreferences mySP = ctx.getSharedPreferences("rsa" + accountId,
                Context.MODE_MULTI_PROCESS );
        String modulus = mySP.getString("modulus", "");
        String publicExponent = mySP.getString("publicExponent", "");
        final RSAPublicKey publicKey = SecurityUtils.getPublicKey(modulus, publicExponent);

        new Thread(new Runnable() {

            @Override
            public void run() {
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("contentId", String
                        .valueOf(contentId)));
                params.add(new BasicNameValuePair("type", String
                        .valueOf(assnType)));
                params.add(new BasicNameValuePair("androidId", DeviceUtils
                        .getAndroidId(ctx)));

                String data = SecurityUtils.getInputString(params);
                String sign = "";
                try {
                    sign = SecurityUtils.encryptByPublicKey(data, publicKey);

                    params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("accountId", String
                            .valueOf(accountId)));
                    params.add(new BasicNameValuePair("sign", sign));

                    String resp = NetUtils.post(SlideConstants.SERVER_URL
                                    + SlideConstants.SERVER_METHOD_UPDATE_FAVORITE,
                            params);

                    BooleanResponse respObj = gson.fromJson(resp,
                            BooleanResponse.class);

                    if (respObj.getCode() != SlideConstants.SERVER_RETURN_SUCCESS) {
                        throw new Exception("server update failed");
                    }
                } catch (Exception e) {
                    Log.e("update assn type", e.toString());
                }
            }
        }).start();
    }

    public static void favoriteContent(final Context ctx,final int accountId, final int contentId) {
        ContentDTO contentDTO = ContentsDataOperator.getById(ctx, contentId);
        if (contentDTO != null) {
            contentDTO.setFavorite(true);
            ContentsDataOperator.update(ctx, contentDTO);
        }

        updateAssnType(ctx, accountId, contentId, FavoriteTypeEnum.FAVORITE.getValue());
    }

    public static void cancelFavoriteContent(final Context ctx,final int accountId, final int contentId) {
        ContentDTO contentDTO = ContentsDataOperator.getById(ctx, contentId);
        if (contentDTO != null) {
            contentDTO.setFavorite(false);
            ContentsDataOperator.update(ctx, contentDTO);
        }

        updateAssnType(ctx, accountId, contentId, FavoriteTypeEnum.NO_FEEL.getValue());
    }

    public static void dislikeContent(final Context ctx,final int accountId, final int contentId) {
        ContentDTO contentDTO = ContentsDataOperator.getById(ctx, contentId);
        if (contentDTO != null) {
            contentDTO.setDislike(true);
            ContentsDataOperator.update(ctx, contentDTO);
        }

        updateAssnType(ctx, accountId, contentId, FavoriteTypeEnum.DISLIKE.getValue());
    }
}
