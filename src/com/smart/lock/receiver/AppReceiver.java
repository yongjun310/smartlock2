package com.smart.lock.receiver;

import java.util.List;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

import com.google.gson.Gson;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.service.DataService;
import com.smart.lock.service.HeartBeatService;
import com.smart.lock.service.LockService;
import com.smart.lock.utils.DeviceUtils;
import com.smart.lock.utils.PageChangeUtils;
import com.smart.lock.utils.PollingUtils;
import com.smart.lock.utils.SharedPreferencesUtils;

public class AppReceiver extends BroadcastReceiver {
	private static String TAG = "AppReceiver";
	private Context ctx = null;

	private static Gson gson = new Gson();

	private int accountId, sex;

	private int screenWidth = 480;
	private int screenHeight = 640;

	private String modulus, publicExponent;

	private KeyguardManager.KeyguardLock keyguardLock;

	private static AppReceiver instance;

	private int homeCount = 0;
	
	public static synchronized AppReceiver getInstance(Context ctx) {
		if (instance == null) {
			instance = new AppReceiver();
		}
		return instance;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceicer onAppReceive:" + intent.getAction());
		PollingUtils.checkServiceRunning(context);
		
		if (ctx == null) {
			ctx = context;
			SharedPreferences commonSP = ctx.getSharedPreferences("common",
					Context.MODE_MULTI_PROCESS );
			accountId = commonSP.getInt("accountId", -1);
			sex = commonSP.getInt("sex", 0);

			DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
			screenWidth = dm.widthPixels;
			screenHeight = dm.heightPixels;

			SharedPreferences mySP = ctx.getSharedPreferences(
					"rsa" + accountId, Context.MODE_MULTI_PROCESS );
			modulus = mySP.getString("modulus", "");
			publicExponent = mySP.getString("publicExponent", "");
		}

		if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
			keyguardLock = DeviceUtils.disableSystemKeyguard(context,
					keyguardLock);
			TelephonyManager telephony = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			AudioManager audioManager =  (AudioManager)context.getSystemService(Service.AUDIO_SERVICE);
			if (telephony.getCallState() != TelephonyManager.CALL_STATE_IDLE) {
			        PageChangeUtils.changeLockServiceState(context, LockService.STATE_HIDE);
			}
			PageChangeUtils.changeLockServiceState(context, LockService.STATE_SCREEN_ON);
	        Log.d(TAG, "lockscreen" + System.currentTimeMillis());
	        
//	        ThreadHelper.getInstance(context).addTask(
//	                SlideConstants.THREAD_DOWNLOAD_PIC);
		}

		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
			keyguardLock = DeviceUtils.disableSystemKeyguard(context,
					keyguardLock);

			//gcLocalData();
			if (DeviceUtils.isPhoneIdle(context)) {
//				if (getTopActivity(context).equals("com.smart.lock.LockActivity"))
//					return;

				/*Intent activityIntent = new Intent();
				activityIntent.setClass(context, LockActivity.class);
				activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_CLEAR_TASK);
				context.startActivity(activityIntent);*/

		        PageChangeUtils.changeLockServiceState(context, LockService.STATE_SHOW);
			}
		}

		if (intent.getAction().equals(
				SlideConstants.BROADCAST_DATA_SERVICE_DESTORY)) {
			context.startService(new Intent(SlideConstants.DATA_SERVICE_NAME));
		}

		/*if (intent.getAction().equals(
				SlideConstants.BROADCAST_HEARTBEAT_SERVICE_DESTORY)) {
			PollingUtils.startPollingService(context,
					SlideConstants.SEVICE_HEARTBEAT_SPAN,
					HeartBeatService.class,
					SlideConstants.HEARTBEAT_SERVICE_NAME);
			context.startService(new Intent(context, HeartBeatService.class));
		}*/
	}

	private String getTopActivity(Context context) {
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> taskInfos = am.getRunningTasks(1);
		if (taskInfos != null && taskInfos.size() > 0) {
			ComponentName cn = taskInfos.get(0).topActivity;

			return cn.getClassName();
		}
		return "";
	}
}
