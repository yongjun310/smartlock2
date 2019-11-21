package com.smart.lock.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.smart.lock.common.SlideConstants;

import java.util.concurrent.atomic.AtomicInteger;

public class DBHelper extends SQLiteOpenHelper {

	private AtomicInteger mOpenCounter = new AtomicInteger();

	private static DBHelper instance;

	private SQLiteDatabase mDatabase;

	public synchronized static DBHelper getInstance(Context ctx) {
		if (null == instance) {
			instance = new DBHelper(ctx, SlideConstants.DB_NAME, null,
					SlideConstants.DB_VERSION);
		}
		return instance;
	}

	public DBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS content("
				+ "id INTEGER PRIMARY KEY, " + "type INTEGER, "
				+ "image VARCHAR, " + "localPath VARCHAR, "
				+ "localViewCount INTEGER, " + "isFavorite INTEGER, "
				+ "isDislike INTEGER, " + "link VARCHAR, " + "title VARCHAR, "
				+ "bonusAmount DECIMAL(10,2), " + "accountRange INTEGER, "
				+ "content VARCHAR, " + "priority INTEGER DEFAULT 0, " + "startTime VARCHAR, "
				+ "endTime VARCHAR, " + "status INTEGER, "
				+ "addTime VARCHAR, " + "updateTime VARCHAR, downloadTime VARCHAR, hasMore INTEGER, tags VARCHAR, categoryIds VARCHAR)");
		db.execSQL("CREATE TABLE IF NOT EXISTS action_log("
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT, " + "accountId INTEGER, "
				+ "actionType INTEGER, " + "contentId INTEGER, "
				+ "addTime DATETIME DEFAULT CURRENT_TIMESTAMP" + ")");
		creatCampaign(db);

	}

	private void creatCampaign(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS campaign("
				+ "id INTEGER PRIMARY KEY, "
				+ "image VARCHAR, "
				+ "link VARCHAR, "
				+ "title VARCHAR, "
				+ "priority INTEGER DEFAULT 0, "
				+ "startTime VARCHAR, "
				+ "endTime VARCHAR, "
				+ "status INTEGER, "
				+ "addTime VARCHAR, "
				+ "updateTime VARCHAR)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1 && newVersion == 2) {
			db.execSQL("ALTER TABLE content ADD COLUMN priority INTEGER DEFAULT 0");
		}
		if (oldVersion==2 && newVersion == 3) {
			try {
				db.execSQL("ALTER TABLE content ADD COLUMN downloadTime VARCHAR");
				db.execSQL("ALTER TABLE content ADD COLUMN hasMore INTEGER");
				db.execSQL("ALTER TABLE content ADD COLUMN categoryIds VARCHAR");
			} catch (Exception e) {
				Log.e("onUpgrade", e.toString());
			}
		}
		if (oldVersion==1 && newVersion == 3) {
			db.execSQL("ALTER TABLE content ADD COLUMN priority INTEGER DEFAULT 0");
			db.execSQL("ALTER TABLE content ADD COLUMN downloadTime VARCHAR");
			db.execSQL("ALTER TABLE content ADD COLUMN hasMore INTEGER");
			db.execSQL("ALTER TABLE content ADD COLUMN categoryIds VARCHAR");
		}
		if(newVersion == 4) {
			switch (oldVersion) {
				case 1:
					db.execSQL("ALTER TABLE content ADD COLUMN priority INTEGER DEFAULT 0");
				case 2:
					try {
						db.execSQL("ALTER TABLE content ADD COLUMN downloadTime VARCHAR");
						db.execSQL("ALTER TABLE content ADD COLUMN hasMore INTEGER");
						db.execSQL("ALTER TABLE content ADD COLUMN categoryIds VARCHAR");
					} catch (Exception e) {
						Log.e("onUpgrade", e.toString());
					}
				case 3:
					creatCampaign(db);
			}
		}
	}

	public synchronized SQLiteDatabase openDatabase() {
		if(mOpenCounter.incrementAndGet() == 1) {
			// Opening new database
			mDatabase = instance.getWritableDatabase();
		}
		return mDatabase;
	}

	public synchronized void closeDatabase() {
		if(mOpenCounter.decrementAndGet() == 0) {
			// Closing database
			mDatabase.close();

		}
	}
}
