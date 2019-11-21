package com.smart.lock.utils;

import java.util.List;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.util.Log;

import com.smart.lock.activity.LockActivity;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.receiver.AppReceiver;
import com.smart.lock.service.DataService;
import com.smart.lock.service.HeartBeatService;
import com.smart.lock.service.LockService;

public class PollingUtils {

	// 开启轮询服务
	public static PendingIntent startPollingService(Context context, long span,
			Class<?> cls, String action) {
		// 获取AlarmManager系统服务
		AlarmManager manager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		// 包装需要执行Service的Intent
		Intent intent = new Intent(context, cls);
		intent.setAction(action);
		PendingIntent pendingIntent = PendingIntent.getService(context,
				SlideConstants.POLLING_CODE, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// 触发服务的起始时间
		long triggerAtTime = SystemClock.elapsedRealtime();

		// 使用AlarmManger的setRepeating方法设置定期执行的时间间隔（seconds秒）和需要执行的Service
		manager.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtTime,
				span, pendingIntent);

		return pendingIntent;
	}
	
	// 停止轮询服务
	public static void stopPollingService(Context context, Class<?> cls,
			String action) {
		AlarmManager manager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, cls);
		intent.setAction(action);
		PendingIntent pendingIntent = PendingIntent.getService(context,
				SlideConstants.POLLING_CODE, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		// 取消正在执行的服务
		manager.cancel(pendingIntent);
	}

	// 开启轮询广播
	public static PendingIntent startPollingBroadcast(Context context,
			long span, String action) {
		AlarmManager manager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		Intent intent = new Intent(action);
		intent.setAction(action);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
				SlideConstants.POLLING_BROADCAST_CODE, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		long triggerAtTime = SystemClock.elapsedRealtime();

		manager.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtTime,
				span, pendingIntent);

		return pendingIntent;
	}

	// 停止轮询广播
	public static void stopPollingBroadcast(Context context, String action) {
		AlarmManager manager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(action);
		intent.setAction(action);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
				SlideConstants.POLLING_BROADCAST_CODE, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		// 取消正在执行的服务
		manager.cancel(pendingIntent);
	}

	// 停止轮询服务
	public static void stopPollingService(Context context,
			PendingIntent pendingIntent) {
		AlarmManager manager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		manager.cancel(pendingIntent);
	}

	/**
	 * 用来判断服务是否运行.
	 * 
	 * @param mContext
	 * @param className
	 *            判断的服务名字
	 * @return true 在运行 false 不在运行
	 */
	public static boolean isServiceRunning(Context mContext, String className) {
		boolean isRunning = false;
		ActivityManager activityManager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo> serviceList = activityManager
				.getRunningServices(Integer.MAX_VALUE);
		if (!(serviceList.size() > 0)) {
			return false;
		}
		for (int i = 0; i < serviceList.size(); i++) {
			if (serviceList.get(i).service.getClassName().equals(className) == true) {
				isRunning = true;
				break;
			}
		}
		Log.d("isServiceRunning", "isService:" + className + " running:" + isRunning);
		return isRunning;
	}

	public static void startAppReceive(Context ctx) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_TIME_TICK);
		filter.addAction(SlideConstants.BROADCAST_DATA_SERVICE_DESTORY);
		filter.addAction(SlideConstants.BROADCAST_HEARTBEAT_SERVICE_DESTORY);
		filter.addAction(SlideConstants.BROADCAST_DATA_PROCESS_LOG);
		filter.addAction(SlideConstants.BROADCAST_DATA_PROCESS_CONTENT);
		filter.addAction(SlideConstants.BROADCAST_DATA_PROCESS_PIC);
		// filter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
		// filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);

//		AppReceiver receiver = new AppReceiver(); // 用于侦听
//		ctx.registerReceiver(receiver, filter);
		ctx.registerReceiver(AppReceiver.getInstance(ctx), filter);
	}

	public static void checkServiceRunning(Context context) {
        /*if (!PollingUtils.isServiceRunning(context,
        		LockService.class.getName())) {*/
        	context.startService(new Intent(context, LockService.class));
        //}
        if (!PollingUtils.isServiceRunning(context,
        		DataService.class.getName())) {
        	context.startService(new Intent(context, DataService.class));
        }
        /*if (!PollingUtils.isServiceRunning(context,
        		HeartBeatService.class.getName())) {
        	context.startService(new Intent(context, HeartBeatService.class));
        }*/
	}
	
	public static void registSystemReceiver(Context context, BroadcastReceiver receiver) {
	    IntentFilter localIntentFilter = new IntentFilter();
	    localIntentFilter.addAction("android.intent.action.TIME_TICK");
	    localIntentFilter.addAction("android.intent.action.TIME_SET");
	    localIntentFilter.addAction("android.intent.action.TIMEZONE_CHANGED");
	    context.registerReceiver(receiver, localIntentFilter, null, null);
	}
}
