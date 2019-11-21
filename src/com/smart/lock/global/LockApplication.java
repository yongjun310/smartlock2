package com.smart.lock.global;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.smart.lock.activity.SplashActivity;
import com.smart.lock.service.LockService;
import com.smart.lock.utils.DisplayUtils;
import com.smart.lock.utils.LockPatternUtils;
import com.smart.lock.utils.PollingUtils;
import com.smart.lock.utils.SharedPreferencesUtils;


public class LockApplication extends Application {

    private static boolean isRunning;

    public boolean userPasswordRemember = false;
    public boolean ad = false;

    private String mData;

    private static LockApplication mInstance = null;

    private static LockPatternUtils mLockPatternUtils;
    

    public static boolean isRunning() {
        return isRunning;
    }
    
	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
        mLockPatternUtils = new LockPatternUtils(this);
		TimeTickReceiver receiver = new TimeTickReceiver();
		PollingUtils.registSystemReceiver(this, receiver);
	}
	
	class TimeTickReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			PollingUtils.checkServiceRunning(context);
		}
	}

    public LockPatternUtils getLockPatternUtils() {
        return mLockPatternUtils;
    }
	

    public static LockApplication getInstance(Context activity) {
        if(mInstance == null) {
            mInstance = new LockApplication();
        }
        if(mLockPatternUtils == null) {
            mLockPatternUtils = new LockPatternUtils(activity);
        }
        return mInstance;
    }
}
