package com.smart.lock.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class NotificationService extends AccessibilityService {
@Override
public void onAccessibilityEvent(AccessibilityEvent event) {
	Log.d("onAccessibilityEvent", "onReceicer AccessibilityEvent:");
    System.out.println("onAccessibilityEvent");
    if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
        Log.d("notification: ", event.getText().toString());
    }
    
}

@Override
protected void onServiceConnected() {
    AccessibilityServiceInfo info = new AccessibilityServiceInfo();
    info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
    info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
    info.notificationTimeout = 100;
    setServiceInfo(info);
}

@Override
public void onInterrupt() {
    System.out.println("onInterrupt");
}
}