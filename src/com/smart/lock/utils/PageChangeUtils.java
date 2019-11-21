package com.smart.lock.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.smart.lock.activity.*;
import com.smart.lock.common.FavoriteTypeEnum;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.dto.ContentDTO;
import com.smart.lock.service.LockService;

public class PageChangeUtils {

    public static void redirectMain(Activity activity) {
        activity.finish();
        boolean isNew;
        SharedPreferences commonSP = activity.getSharedPreferences("common",
                0);
		isNew = SharedPreferencesUtils.getBoolSP(activity, SharedPreferencesUtils.IS_NEW, true);
        Intent activityIntent = new Intent();
        if (isNew) {
            activityIntent.putExtra("preActivity", "BoxOpenActivity");
            activityIntent.setClass(activity, CategorySettingActivity.class);
        } else {
            activityIntent.setClass(activity, MainActivity.class);
        }
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(activityIntent);
    }

    public static void startLogin(Activity activity, String preActivity) {
        Intent activityIntent = new Intent();
        activityIntent.putExtra("bindMobile", true);
        activityIntent.putExtra("preActivity", preActivity);
        activityIntent.setClass(activity, LoginActivity.class);
        activity.startActivityForResult(activityIntent, 0);
    }


    public static void gotoDetail(Context ctx, ContentDTO content, String preActivity, int flags) {
        Intent activityIntent = new Intent();
        activityIntent.setClass(ctx,
                WebViewActivity.class);
        if (flags != -1)
            activityIntent.setFlags(flags);

        Bundle bundle = new Bundle();
        bundle.putString("preActivity", preActivity);
        if (content != null) {
            bundle.putInt("contentId", content.getId());
            bundle.putString("image", content.getImage());
            bundle.putString("link", content.getLink());
            bundle.putInt("bonusType", content.getType());

            String bonusAmount = String.format("%.2f", content.getBonusAmount());
            bundle.putString("bonusAmount", bonusAmount);
            bundle.putInt(
                    "assnType",
                    content.isFavorite() ? FavoriteTypeEnum.FAVORITE
                            .getValue() : FavoriteTypeEnum.VIEWED
                            .getValue());
            bundle.putString("localPath", content.getLocalPath());
            bundle.putString("title", content.getTitle());
            bundle.putString("content", content.getContent());
//            ContentsDataOperator.deleteById(ctx, content.getId());
        }
        bundle.putInt("type", SlideConstants.WEB_VIEW_CONTENT);

        activityIntent.putExtras(bundle);
        ctx.startActivity(activityIntent);
    }

    public static void redirectFullImageView(Activity activity, ContentDTO content, String preActivity, int flags) {
        Intent activityIntent = new Intent();
        activityIntent.setClass(activity,
                DetailActivity.class);
        if (flags != -1)
            activityIntent.setFlags(flags);

        Bundle bundle = new Bundle();
        bundle.putString("preActivity", preActivity);
        if (content != null) {
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
//            ContentsDataOperator.deleteById(ctx, content.getId());
        }

        activityIntent.putExtras(bundle);
        activity.startActivity(activityIntent);
    }

    public static void startActivityWithParams(Activity arg0, Class<?> clazz) {
        Intent intent = new Intent();
        intent.setClass(arg0, clazz);
        arg0.startActivity(intent);
    }

    public static void changeLockServiceState(Context context, int state) {
		Intent bintent=new Intent();
		bintent.setAction(SlideConstants.LOCKSERVICE_ACTION);
		bintent.putExtra("state", state);
        //向锁屏Service发送隐藏锁屏控制的广播
		context.sendBroadcast(bintent);
	}

    /**
     * notify first page update page
     * @param context
     */
    public static void sendDBUpdateBroadcast(Context context) {
		Intent bintent=new Intent();
		bintent.setAction(SlideConstants.DB_UPDATE_ACTION);
		context.sendBroadcast(bintent);
	}
    
    
}
