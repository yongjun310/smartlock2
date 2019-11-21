package com.smart.lock.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.smart.lock.common.SlideConstants;
import com.smart.lock.service.DataService;
import com.smart.lock.service.LockService;
import com.smart.lock.utils.PollingUtils;

public class SystemReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		Log.d("SystemReceiver", "onReceicer onSystemReceiver:" + intent.getAction());
		if (!PollingUtils.isServiceRunning(context,
				DataService.class.getName()))
			context.startService(new Intent(SlideConstants.DATA_SERVICE_NAME));

        if (intent.getAction().equals("com.smart.lock.lockservice.destroy")) {  
        	context.startService(new Intent(context, LockService.class));
        }
        
        if (intent.getAction().equals(SlideConstants.BROADCAST_DATA_SERVICE_DESTORY)) {  
        	context.startService(new Intent(context, DataService.class));
        }
	}
}
