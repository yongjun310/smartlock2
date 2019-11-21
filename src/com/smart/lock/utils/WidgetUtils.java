package com.smart.lock.utils;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.smart.lock.view.CustomDialog;

public class WidgetUtils {

	public static Dialog confirm(Context ctx, String title, String message,
			DialogInterface.OnClickListener confirmClick) {
		Dialog dialog = null;
		CustomDialog.Builder customBuilder = new
				CustomDialog.Builder(ctx);
		customBuilder.setTitle(title)
				.setMessage(message)
				.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
				.setPositiveButton("确定",
						confirmClick);
		dialog = customBuilder.create();
		return dialog;
	}

	public static Dialog confirm(Context ctx, String message,
			DialogInterface.OnClickListener confirmClick) {

		Dialog dialog = null;
		CustomDialog.Builder customBuilder = new
				CustomDialog.Builder(ctx);
		customBuilder.setTitle("温馨提示")
				.setMessage(message)
				.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
				.setPositiveButton("确定",
						confirmClick);
		dialog = customBuilder.create();
		return dialog;
	}
}
