package com.smart.lock.utils;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.smart.lock.common.SlideConstants;
import com.smart.lock.imageloader.FileCountLimitedDiscCache;
import com.smart.lock.imageloader.ImageLoader;
import com.smart.lock.imageloader.ImageLoaderConfiguration;
import com.smart.lock.imageloader.Md5FileNameGenerator;
import com.smart.lock.imageloader.QueueProcessingType;

public class DeviceUtils {
	private static final long ERROR = 0;
	private static final String TAG = "DeviceUtils";

	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& InetAddressUtils.isIPv4Address(inetAddress
									.getHostAddress())) {
						return inetAddress.getHostAddress();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("get ip error:", ex.toString());
		}
		return null;
	}

	public static boolean isWifi(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo.State wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		if (cm != null) {
			NetworkInfo networkINfo = cm.getActiveNetworkInfo();
			if (networkINfo != null
					&& networkINfo.getType() == ConnectivityManager.TYPE_WIFI &&
						wifi == NetworkInfo.State.CONNECTED) {
				return true;
			}
		}
		return false;
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null || cm.getActiveNetworkInfo() == null)
			return false;

		return cm.getActiveNetworkInfo().isAvailable();
	}

	public static String getAndroidId(Context context) {
		return Secure
				.getString(context.getContentResolver(), Secure.ANDROID_ID);
	}

	public static String getOSVersion() {
		return android.os.Build.VERSION.RELEASE;
	}

	public static String getModel() {
		return android.os.Build.MODEL;
	}

	public static String getAppVersion(Context context) {
		try {
			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(),
					0);
			String version = info.versionName;
			return version;
		} catch (Exception e) {
			Log.e("get app version error:", e.toString());
		}

		return null;
	}

	/**
	 * SDCARD是否存
	 */
	public static boolean externalMemoryAvailable() {
		return android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}

	/**
	 * 获取手机内部剩余存储空间
	 * 
	 * @return
	 */
	public static long getAvailableInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize;
	}

	/**
	 * 获取手机内部总的存储空间
	 * 
	 * @return
	 */
	public static long getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	/**
	 * 获取SDCARD剩余存储空间
	 * 
	 * @return
	 */
	public static long getAvailableExternalMemorySize() {
		if (externalMemoryAvailable()) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			return availableBlocks * blockSize;
		} else {
			return ERROR;
		}
	}

	/**
	 * 获取SDCARD总的存储空间
	 * 
	 * @return
	 */
	public static long getTotalExternalMemorySize() {
		if (externalMemoryAvailable()) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getBlockCount();
			return totalBlocks * blockSize;
		} else {
			return ERROR;
		}
	}
	
	/**
	 * 关闭系统锁屏
	 * 
	 * @return
	 */
	public static KeyguardManager.KeyguardLock disableSystemKeyguard(Context ctx, KeyguardManager.KeyguardLock keyguardLock) {
		KeyguardManager keyguardManager = (KeyguardManager) ctx
				.getSystemService(Context.KEYGUARD_SERVICE);
		String lockTag = "smartlock";
		if(keyguardLock != null) {
			keyguardLock.reenableKeyguard();
			keyguardLock = null;
		}
		keyguardLock = keyguardManager
				.newKeyguardLock(lockTag);
		if (!SharedPreferencesUtils.getBoolSP(ctx,SharedPreferencesUtils.LOCK_CLOSE,false)) {
			keyguardLock.disableKeyguard();
		} else {
			keyguardLock.reenableKeyguard();
		}
		return keyguardLock;
	}
	
	/**
	 * 开启系统锁屏
	 * 
	 * @return
	 */
	public static void enableSystemKeyguard(Context ctx) {
		KeyguardManager keyguardManager = (KeyguardManager) ctx
				.getSystemService(Context.KEYGUARD_SERVICE);
		String lockTag = "smartlock";
		KeyguardManager.KeyguardLock keyguardLock = keyguardManager
				.newKeyguardLock(lockTag);
		keyguardLock.reenableKeyguard();
	}

	public static boolean isSysSettingEnabled(Context ctx, String setings) {
		String pkgName = ctx.getPackageName();
		final String flat = Settings.Secure.getString(ctx.getContentResolver(),
				setings);
		if (!TextUtils.isEmpty(flat)) {
			final String[] names = flat.split(":");
			for (int i = 0; i < names.length; i++) {
				final ComponentName cn = ComponentName.unflattenFromString(names[i]);
				if (cn != null) {
					if (TextUtils.equals(pkgName, cn.getPackageName())) {
						return true;
					}
				}
			}
		}
		return false;
	}


	public static void initImageLoader(Context context)
	{
		try
		{

			ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).threadPriority(
					Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory().discCacheFileNameGenerator(
					new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO)
					.discCache(new FileCountLimitedDiscCache(new File(SlideConstants.EXTERNAL_IMAGE_LOADER_PATH),
							SlideConstants.IMAGE_MAX_COUNT))
					.build();



			ImageLoader.getInstance().init(config);

		}
		catch (Exception e)
		{
			Log.e("initImageLoader", e.getMessage());
		}
	}
	
	public static boolean isNewOS() {
    	String strVer = DeviceUtils.getOSVersion();
    	if(strVer != null && !strVer.startsWith("2") && !strVer.startsWith("4.0"))
    		return true;
    	return false;
	}
	
	public static String getUID(Context ctx) {
		String uid = null;
		ActivityManager am = (ActivityManager) ctx
				.getSystemService(Context.ACTIVITY_SERVICE);
		ApplicationInfo appinfo = ctx.getApplicationInfo();
		List<RunningAppProcessInfo> run = am.getRunningAppProcesses();
		for (RunningAppProcessInfo runningProcess : run) {
			if ((runningProcess.processName != null)
					&& runningProcess.processName.equals(appinfo.processName)) {
				uid = String.valueOf(runningProcess.uid);
				break;
			}
		}
		return uid;
	}

	public static boolean isPhoneIdle(Context context) {
		TelephonyManager telephony = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		AudioManager audioManager = (AudioManager) context.getSystemService(Service.AUDIO_SERVICE);
		boolean isIdle = !audioManager.isMusicActive() &&
				audioManager.getMode() == AudioManager.MODE_NORMAL &&
				telephony.getCallState() == TelephonyManager.CALL_STATE_IDLE &&
				!SharedPreferencesUtils.getBoolSP(context, SharedPreferencesUtils.LOCK_CLOSE, false);
		return isIdle;
	}
}
