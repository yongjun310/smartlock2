package com.smart.lock.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;

import android.content.ComponentName;
import android.content.Context;
import com.smart.lock.activity.LockActivity;

import java.util.LinkedList;
import java.util.List;

public class SysApplication extends Application {
    // 运用list来保存们每一个activity是关键
    private List<Activity> mList = new LinkedList<Activity>();
    // 为了实现每次使用该类时不创建新的对象而创建的静态对象
    private static SysApplication instance;

    // 构造方法
    private SysApplication() {
    }

    // 实例化一次
    public synchronized static SysApplication getInstance() {
        if (null == instance) {
            instance = new SysApplication();
        }
        return instance;
    }

    // add Activity
    public void addActivity(Activity activity) {
        if (!mList.contains(activity))
            mList.add(activity);
    }

    // 关闭每一个list内的activity
    public void exit() {
        super.onTerminate();
        try {
            while (mList.size() > 0) {
                Activity activity = mList.get(0);
                if (activity != null)
                    activity.finish();
                mList.remove(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void finish(Class<?> clz) {
        try {
            int index = 0;
            while (index < mList.size()) {
                Activity activity = mList.get(index);
                if (activity.getClass().equals(clz)) {
                    if (activity != null)
                        activity.finish();
                    mList.remove(index);
                } else {
                    index++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 杀进程
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }

    public String printActivityList() {
        String result = "";
        for (Activity activity : mList) {
            result += (result.length() == 0 ? "" : ",") + activity.toString();
        }

        return result;
    }

    public boolean checkLockActivityExists() {
        for (int i = mList.size() - 1; i >= 0; i--) {
            if (mList.get(i) != null) {
                if (mList.get(i).getClass().equals(LockActivity.class))
                    return true;
                else
                    return false;
            }
        }
        return false;
    }

    public static String getTopActivity(Context context) {
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
