package com.smart.lock.utils;

import java.io.IOException;
import java.lang.reflect.Method;

import android.os.Build;

public class RomUtils {
	private static final String KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code";
	private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";
	private static final String KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage";

	private static final String brand = Build.BRAND;
	private static final String man = Build.MANUFACTURER;

	public static boolean isMIUI() {
		try {
			final BuildProperties prop = BuildProperties.newInstance();
			return prop.getProperty(KEY_MIUI_VERSION_CODE, null) != null
					|| prop.getProperty(KEY_MIUI_VERSION_NAME, null) != null
					|| prop.getProperty(KEY_MIUI_INTERNAL_STORAGE, null) != null;
		} catch (final IOException e) {
			return false;
		}
	}

	
	public static boolean isFlyme() {
		try {
			// Invoke Build.hasSmartBar()
			final Method method = Build.class.getMethod("hasSmartBar");
			return method != null;
		} catch (final Exception e) {
			return false;
		}
	}
	
	public static boolean isZTE() {
		if("ZTE".equalsIgnoreCase(brand) || ("ZTE".equalsIgnoreCase(man))) {
			return true;
		}
		return false;
	}

	public static boolean isNubia() {
		if("nubia".equalsIgnoreCase(brand) || ("nubia".equalsIgnoreCase(man))) {
			return true;
		}
		return false;
	}

	public static boolean isHuawei() {
		if("huawei".equalsIgnoreCase(brand) || ("huawei".equalsIgnoreCase(man))) {
			return true;
		}
		return false;
	}

	public static boolean isHTC() {
		if("htc".equalsIgnoreCase(brand) || ("htc".equalsIgnoreCase(man))) {
			return true;
		}
		return false;
	}
	
}
