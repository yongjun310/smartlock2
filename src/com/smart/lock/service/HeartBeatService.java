package com.smart.lock.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import com.smart.lock.common.SlideConstants;
import com.smart.lock.utils.DaemUtil;
import com.smart.lock.utils.DeviceUtils;
import com.smart.lock.utils.FileUtils;
import com.smart.lock.utils.PollingUtils;
import com.yyh.fork.NativeRuntime;

public class HeartBeatService extends Service {
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.d("HeartBeatService", "onCreate");
		PollingUtils.checkServiceRunning(this);
		PollingUtils.startAppReceive(this);
		DaemUtil du = new DaemUtil(this);
		du.createAppMonitor(DeviceUtils.getUID(this));
		String executable = "libhelper.so";
		String aliasfile = "helper";
		String parafind = "/data/data/" + getPackageName() + "/" + aliasfile;
		String retx = "false";
		NativeRuntime.getInstance().RunExecutable(getPackageName(), executable, aliasfile, getPackageName() + "/com.smart.lock.service.HostMonitor");
		NativeRuntime.getInstance().startService(getPackageName() + "/com.smart.lock.service.HeartBeatService", FileUtils.createRootPath());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		/*Notification notification = new Notification(R.drawable.icon_notify,
				 getString(R.string.app_name), System.currentTimeMillis());
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;

		PendingIntent pendingintent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
		notification.setLatestEventInfo(this, "秒秀锁屏正在运行", "秒秀锁屏正在持续为您赚钱",
				pendingintent);
		startForeground(1, notification);*/
		
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		stopForeground(true);
		Intent intent = new Intent(SlideConstants.BROADCAST_HEARTBEAT_SERVICE_DESTORY);
		sendBroadcast(intent);
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		PollingUtils.checkServiceRunning(this);
	}
}
