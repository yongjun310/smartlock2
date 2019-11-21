package com.smart.lock.service;

import com.smart.lock.common.SlideConstants;
import com.smart.lock.receiver.AppReceiver;
import com.smart.lock.utils.PollingUtils;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NotificationListener extends NotificationListenerService
{
  protected static NotificationListener a;

  public void onCreate()
  {
    super.onCreate();
  }

  public void onDestroy()
  {
    super.onDestroy();
  }

  @SuppressLint({"NewApi"})
  public void onNotificationPosted(StatusBarNotification paramStatusBarNotification)
 {
		Log.d("onNotificationPosted", "onReceicer onNotificationPosted:");
		//PollingUtils.startAppReceive(this);
		if (!PollingUtils.isServiceRunning(getApplicationContext(),
				LockService.class.getName())) {
			startService(new Intent(this, LockService.class));
		}
		if (!PollingUtils.isServiceRunning(getApplicationContext(),
				DataService.class.getName())) {
			startService(new Intent(SlideConstants.DATA_SERVICE_NAME));
		}
 }

  public void onNotificationRemoved(StatusBarNotification paramStatusBarNotification)
  {
	    //PollingUtils.startAppReceive(this);
		if (!PollingUtils.isServiceRunning(getApplicationContext(),
				LockService.class.getName())) {
			startService(new Intent(this, LockService.class));
		}
		if (!PollingUtils.isServiceRunning(getApplicationContext(),
				DataService.class.getName())) {
			startService(new Intent(SlideConstants.DATA_SERVICE_NAME));
		}
  }
}
