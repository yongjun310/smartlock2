package com.smart.lock.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.smart.lock.common.SlideConstants;
import com.smart.lock.dto.AccountActionLogDTO;

public class ActionLogOperator {

	public static List<AccountActionLogDTO> load(Context ctx) {
		List<AccountActionLogDTO> logList = new ArrayList<AccountActionLogDTO>();
		SQLiteDatabase db = DBHelper.getInstance(ctx).openDatabase();

		Cursor c = db.query("action_log", null, null, null, null, null, null);
		try {
			if (c.moveToFirst()) {
				for (int i = 0; i < c.getCount(); i++) {
					c.moveToPosition(i);
					int id = c.getInt(c.getColumnIndex("id"));
					int accountId = c.getInt(c.getColumnIndex("accountId"));
					int actionType = c.getInt(c.getColumnIndex("actionType"));
					int contentId = c.getInt(c.getColumnIndex("contentId"));
					String addTime = c.getString(c.getColumnIndex("addTime"));
					logList.add(new AccountActionLogDTO(accountId, actionType,
							contentId, addTime));
				}
			}
		} catch (Exception e) {
			Log.e("content query:", e.toString());
		} finally {
			if (c != null) {
				c.close();
				c = null;
			}
		}

		DBHelper.getInstance(ctx).closeDatabase();
		return logList;
	}

	public static void add(Context ctx, AccountActionLogDTO item) {
		SQLiteDatabase db = DBHelper.getInstance(ctx).openDatabase();
		String sql = "insert into action_log (accountId, actionType, contentId) values ";
		final String valueTemplate = "(%s, %s, %s)";
//		String addTime = "1900-01-01 00:00:00";
//		try {
//			addTime = SlideConstants.TIME_FORMART.format(item.getAddTime());
//		} catch (Exception e) {
//			Log.e("add action log:", e.toString());
//		}
		sql += String.format(valueTemplate, item.getAccountId(),
				item.getActionType(), item.getContentId());
		db.execSQL(sql);
		DBHelper.getInstance(ctx).closeDatabase();
	}

	public static void deleteAll(Context ctx) {
		SQLiteDatabase db = DBHelper.getInstance(ctx).openDatabase();
		db.delete("action_log", null, null);
		DBHelper.getInstance(ctx).closeDatabase();
	}

	public static String getPostData(Context ctx) {
		String data = "";

		SQLiteDatabase db = DBHelper.getInstance(ctx).openDatabase();
		Cursor c = db.query("action_log", null, null, null, null, null, null);
		try {
			if (c.moveToFirst()) {
				for (int i = 0; i < c.getCount(); i++) {
					c.moveToPosition(i);
					int accountId = c.getInt(c.getColumnIndex("accountId"));
					int actionType = c.getInt(c.getColumnIndex("actionType"));
					int contentId = c.getInt(c.getColumnIndex("contentId"));
					String addTime = c.getString(c.getColumnIndex("addTime"));

					data += (data.length() == 0 ? "" : "|")
							+ String.format("%s,%s,%s,%s", accountId,
									actionType, contentId, addTime);
				}
			}
		} catch (Exception e) {
			Log.e("content query:", e.toString());
		} finally {
			if (c != null) {
				c.close();
				c = null;
			}
		}

		DBHelper.getInstance(ctx).closeDatabase();

		return data;
	}

}
