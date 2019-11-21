package com.smart.lock.service;

import java.util.Timer;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import com.smart.lock.common.SlideConstants;
import com.smart.lock.receiver.AppReceiver;
import com.smart.lock.utils.PollingUtils;

public class DataService extends Service {

	private Timer timer = new Timer();
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// Notification notification = new Notification();
		// notification.flags = Notification.FLAG_ONGOING_EVENT;
		// notification.flags |= Notification.FLAG_NO_CLEAR;
		// notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
		// startForeground(1, notification);
		//
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// stopForeground(true);
		Intent intent = new Intent(
				SlideConstants.BROADCAST_DATA_SERVICE_DESTORY);
		sendBroadcast(intent);
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// if (!PollingUtils.isServiceRunning(this,
		// HeartBeatService.class.getName())) {
		// PollingUtils.startPollingService(this,
		// SlideConstants.SEVICE_HEARTBEAT_SPAN,
		// HeartBeatService.class,
		// SlideConstants.HEARTBEAT_SERVICE_NAME);
		// }

		PollingUtils.startAppReceive(this);
//		sendBroadcast(new Intent(SlideConstants.BROADCAST_DATA_PROCESS_PIC));
//		Log.d("dataSevice","add thread task:downloadPic");
//		ThreadHelper.getInstance(getApplicationContext()).addTask(SlideConstants.THREAD_DOWNLOAD_PIC);
	}

}
