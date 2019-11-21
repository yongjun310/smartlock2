package com.smart.lock.utils;

import java.util.ArrayList;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.util.Log;

public class DaemUtil {
	static {
		System.loadLibrary("DaemUtil");
	}

	private String mMonitoredService = "";
	private volatile boolean bHeartBreak = false;
	private Context mContext;
	private boolean mRunning = true;

	public void createAppMonitor(String userId) {
		if (!createWatcher(userId)) {
			Log.e("Watcher", "<<Monitor created failed>>");
		}
	}

	public DaemUtil(Context context) {
		mContext = context;
	}

	private int isServiceRunning() {
		ActivityManager am = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		ArrayList<RunningServiceInfo> runningService = (ArrayList<RunningServiceInfo>) am
				.getRunningServices(1024);
		for (int i = 0; i < runningService.size(); ++i) {
			if (mMonitoredService.equals(runningService.get(i).service
					.getClassName().toString())) {
				return 1;
			}
		}
		return 0;
	}

	private native boolean createWatcher(String userId);

	private native boolean connectToMonitor();

	private native int sendMsgToMonitor(String msg);
}
