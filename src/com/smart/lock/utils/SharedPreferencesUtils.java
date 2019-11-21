package com.smart.lock.utils;

import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.smart.lock.activity.BaseActivity;
import com.smart.lock.global.AbActivity;

/**
 * Created by john on 2015/5/28.
 */
public class SharedPreferencesUtils {

    public static String LOCK_CLOSE = "closeLock";

    public static String GESTURE_CLOSE = "closeGesture";

    public static String SERVER_TIME = "serverTime";

    public static String IS_NEW = "isNew";

    public static String LOAD_DATA_TIME = "loadDataTime";

    public static String ACCOUNT_ID = "accountId";

    public static String CURRENT_LOCK_IMGPOS = "curLockPos";

    public static String MOBILE_NO = "mobileNo";
    
    public static String IS_LOGIN = "isLogin";

    public static String CATEGORY_ID = "categoryIdS";

    public static String CATEGORY_ID_SETTING = "categoryIdSetting";

    public static String HAS_GESTURE_PW = "hasGesturePW";

    public static void putStringSP(Context ctx, String id, String value) {
        SharedPreferences.Editor editor = ctx.getSharedPreferences("common", Context.MODE_MULTI_PROCESS ).edit();
        editor.putString(id, value);
        editor.commit();
    }

    public static void putIntSP(Context ctx, String id, int value) {
        SharedPreferences.Editor editor = ctx.getSharedPreferences("common", Context.MODE_MULTI_PROCESS ).edit();
        editor.putInt(id, value);
        editor.commit();
    }

    public static void putBoolSP(Context ctx, String id, boolean value) {
        SharedPreferences.Editor editor = ctx.getSharedPreferences("common", Context.MODE_MULTI_PROCESS ).edit();
        editor.putBoolean(id, value);
        editor.commit();
    }

    public static String getStringSP(Context ctx, String id, String defaultValue) {
        return ctx.getSharedPreferences("common", Context.MODE_MULTI_PROCESS ).getString(id, defaultValue);
    }

    public static int getIntSP(Context ctx, String id, int defaultValue) {
        return ctx.getSharedPreferences("common", Context.MODE_MULTI_PROCESS ).getInt(id, defaultValue);
    }

    public static boolean getBoolSP(Context ctx, String id, boolean defaultValue) {
        return ctx.getSharedPreferences("common", Context.MODE_MULTI_PROCESS ).getBoolean(id, defaultValue);
    }

//    public static void removeSP(Context ctx, String id) {
//        SharedPreferences.Editor editor = ctx.getSharedPreferences("common", Context.MODE_MULTI_PROCESS ).edit();
//        editor.remove(id);
//        editor.commit();
//    }

    public static void storeCreateGesturePW(BaseActivity activity, boolean bool) {
        SharedPreferences.Editor editor = activity.getSharedPreferences("common", Context.MODE_MULTI_PROCESS ).edit();
        editor.putBoolean(HAS_GESTURE_PW, bool);
        editor.commit();
    }

    public static boolean hasCreateGesturePW(BaseActivity activity) {
        return activity.getSharedPreferences("common", Context.MODE_MULTI_PROCESS ).getBoolean(HAS_GESTURE_PW, false);
    }

    public static void removeCreatedGesturePW(BaseActivity activity) {
        SharedPreferences.Editor editor = activity.getSharedPreferences("common", Context.MODE_MULTI_PROCESS ).edit();
        editor.putBoolean(HAS_GESTURE_PW, false);
        editor.commit();
    }
    
    public static void removeStringSP(Context activity, String key) {
        SharedPreferences.Editor editor = activity.getSharedPreferences("common", Context.MODE_MULTI_PROCESS ).edit();
        editor.remove(key);
        editor.commit();
    }

    public static String getServerTimeStr(Context ctx) {
		String serverTime = SharedPreferencesUtils.getStringSP(ctx, SharedPreferencesUtils.SERVER_TIME, null);
        if (serverTime == null)
            serverTime = DisplayUtils.formatDateTimeString(Calendar.getInstance().getTime());
        serverTime = serverTime.substring(0,10);
		return serverTime;
	}
}
