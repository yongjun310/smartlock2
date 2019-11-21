package com.smart.lock.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.smart.lock.common.SlideConstants;
import com.smart.lock.dto.ContentDTO;
import com.smart.lock.service.LockService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ContentsDataOperator {

    private static final String TAG = "ContentsDataOperator";

    // public static ContentDTO getById(Context ctx, int id) {
    // List<ContentDTO> contentList = (List<ContentDTO>) FileUtils.getObject(
    // ctx, SlideConstants.CONTENTS_FILE_NAME);
    //
    // if (contentList == null)
    // contentList = new ArrayList<ContentDTO>();
    //
    // for (ContentDTO content : contentList) {
    // if (content.getId() == id) {
    // return content;
    // }
    // }
    // return null;
    // }
    public static ContentDTO getById(Context ctx, int id) {
        ContentDTO contentDTO = null;

        SQLiteDatabase db = DBHelper.getInstance(ctx).openDatabase();
        String sql = "select * from content where id= ?";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(id)});
        try {
            if (c.moveToFirst()) {
                c.moveToPosition(0);
                int contentId = c.getInt(c.getColumnIndex("id"));
                int type = c.getInt(c.getColumnIndex("type"));
                String image = c.getString(c.getColumnIndex("image"));
                String localPath = c.getString(c.getColumnIndex("localPath"));
                int localViewCount = c.getInt(c
                        .getColumnIndex("localViewCount"));
                int isFavorite = c.getInt(c.getColumnIndex("isFavorite"));
                int isDislike = c.getInt(c.getColumnIndex("isDislike"));
                String link = c.getString(c.getColumnIndex("link"));
                String title = c.getString(c.getColumnIndex("title"));
                BigDecimal bonusAmount = new BigDecimal(c.getString(c
                        .getColumnIndex("bonusAmount")));
                String content = c.getString(c.getColumnIndex("content"));
                String startTime = c.getString(c.getColumnIndex("startTime"));
                String endTime = c.getString(c.getColumnIndex("endTime"));
                int status = c.getInt(c.getColumnIndex("status"));
                String addTime = c.getString(c.getColumnIndex("addTime"));
                String updateTime = c.getString(c.getColumnIndex("updateTime"));
                String tags = c.getString(c.getColumnIndex("tags"));
                String categoryIds = c.getString(c.getColumnIndex("categoryIds"));
                int priority = c.getInt(c.getColumnIndex("priority"));
                int hasMore = c.getInt(c.getColumnIndex("hasMore"));
                String downloadTime = c.getString(c.getColumnIndex("downloadTime"));
                contentDTO = new ContentDTO(contentId, type, image, localPath,
                        localViewCount, isFavorite, isDislike, link, title,
                        bonusAmount, content, startTime, endTime, status,
                        addTime, updateTime, tags, priority, downloadTime, hasMore, categoryIds);
            }
        } catch (Exception e) {
            Log.e("content query:", e.getStackTrace().toString());
        } finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }

        DBHelper.getInstance(ctx).closeDatabase();
        return contentDTO;
    }

    // public static List<ContentDTO> load(Context ctx) {
    // List<ContentDTO> contentList = (List<ContentDTO>) FileUtils.getObject(
    // ctx, SlideConstants.CONTENTS_FILE_NAME);
    //
    // if (contentList == null)
    // contentList = new ArrayList<ContentDTO>();
    // return contentList;
    // }
    public static List<ContentDTO> load(Context ctx) {
        return load(ctx, "SELECT * FROM content");
    }

    public static List<ContentDTO> load(Context ctx, String sql) {
        Log.d("ContentDataOperator", "load enter:"+sql);
        List<ContentDTO> contentList = new ArrayList<ContentDTO>();
        SQLiteDatabase db = DBHelper.getInstance(ctx).openDatabase();
        Cursor c = db.rawQuery(sql, null);
        Log.d("ContentDataOperator", "load excute:"+c.getCount());
        try {
            if (c.moveToFirst()) {
                for (int i = 0; i < c.getCount(); i++) {
                    c.moveToPosition(i);
                    int contentId = c.getInt(c.getColumnIndex("id"));
                    int type = c.getInt(c.getColumnIndex("type"));
                    String image = c.getString(c.getColumnIndex("image"));
                    String localPath = c.getString(c
                            .getColumnIndex("localPath"));
                    int localViewCount = c.getInt(c
                            .getColumnIndex("localViewCount"));
                    int isFavorite = c.getInt(c.getColumnIndex("isFavorite"));
                    int isDislike = c.getInt(c.getColumnIndex("isDislike"));
                    String link = c.getString(c.getColumnIndex("link"));
                    String title = c.getString(c.getColumnIndex("title"));
                    BigDecimal bonusAmount = new BigDecimal(c.getString(c
                            .getColumnIndex("bonusAmount")));
                    String content = c.getString(c.getColumnIndex("content"));
                    String startTime = c.getString(c
                            .getColumnIndex("startTime"));
                    String endTime = c.getString(c.getColumnIndex("endTime"));
                    int status = c.getInt(c.getColumnIndex("status"));
                    String addTime = c.getString(c.getColumnIndex("addTime"));
                    String updateTime = c.getString(c
                            .getColumnIndex("updateTime"));
                    String tags = c.getString(c.getColumnIndex("tags"));
                    String categoryIds = c.getString(c.getColumnIndex("categoryIds"));
                    int priority = c.getInt(c.getColumnIndex("priority"));
                    String downloadTime = c.getString(c.getColumnIndex("downloadTime"));
                    int hasMore = c.getInt(c.getColumnIndex("hasMore"));
                    contentList.add(new ContentDTO(contentId, type, image,
                            localPath, localViewCount, isFavorite, isDislike,
                            link, title, bonusAmount, content, startTime,
                            endTime, status, addTime, updateTime, tags, priority, downloadTime, hasMore, categoryIds));
                }
            }
        } catch (Exception e) {
            Log.e("content query:", e.getStackTrace().toString());
        } finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }
        DBHelper.getInstance(ctx).closeDatabase();
        Log.d("ContentDataOperator", "load success:"+contentList.size());
        return contentList;
    }

    public static List<String> loadContentsDate(Context ctx) {
        Log.d("ContentDataOperator", "loadContentsDate enter");
        List<String> contentDateList = new ArrayList<String>();

        String sql = "SELECT DISTINCT date(startTime) as st FROM content ORDER BY st DESC";
        SQLiteDatabase db = DBHelper.getInstance(ctx).openDatabase();
        Cursor c = db.rawQuery(sql, null);
        Log.d("ContentDataOperator", "loadContentsDate excute:" + c.getCount());
        String dateString = "";
        try {
            if (c.moveToFirst()) {
                for (int i = 0; i < c.getCount(); i++) {
                    c.moveToPosition(i);
                    String date = c.getString(0);
                    if(date != null) {
                        contentDateList.add(date);
                        dateString += date + " ";
                    }
                }
            }
        } catch (Exception e) {
            Log.e("content query:", e.getStackTrace().toString());
        } finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }
        DBHelper.getInstance(ctx).closeDatabase();
        Log.d("ContentDataOperator", "loadContentsDate success:" + contentDateList.size() + "/ndateSring:" + dateString);
        return contentDateList;
    }

    public static List<ContentDTO> loadContentsByDate(Context ctx, String date) {
        Log.d("ContentDataOperator", "loadNew enter");
        List<ContentDTO> contentList = new ArrayList<ContentDTO>();

        String sql = "SELECT * FROM content WHERE date(startTime) = date('" + date + "')";//AND localPath<>'' AND localPath IS NOT NULL";
        Log.d("childlsit init", "[sql]" + sql);
        SQLiteDatabase db = DBHelper.getInstance(ctx).openDatabase();
        Cursor c = db.rawQuery(sql, null);
        Log.d("childlsit init", "[Cursor Size]" + c.getCount());
        Log.d("ContentDataOperator", "load excute:"+c.getCount());
        try {
            if (c.moveToFirst()) {
                for (int i = 0; i < c.getCount(); i++) {
                    c.moveToPosition(i);
                    int contentId = c.getInt(c.getColumnIndex("id"));
                    int type = c.getInt(c.getColumnIndex("type"));
                    String image = c.getString(c.getColumnIndex("image"));
                    String localPath = c.getString(c
                            .getColumnIndex("localPath"));
                    int localViewCount = c.getInt(c
                            .getColumnIndex("localViewCount"));
                    int isFavorite = c.getInt(c.getColumnIndex("isFavorite"));
                    int isDislike = c.getInt(c.getColumnIndex("isDislike"));
                    String link = c.getString(c.getColumnIndex("link"));
                    String title = c.getString(c.getColumnIndex("title"));
                    BigDecimal bonusAmount = new BigDecimal(c.getString(c
                            .getColumnIndex("bonusAmount")));
                    String content = c.getString(c.getColumnIndex("content"));
                    String startTime = c.getString(c
                            .getColumnIndex("startTime"));
                    String endTime = c.getString(c.getColumnIndex("endTime"));
                    int status = c.getInt(c.getColumnIndex("status"));
                    String addTime = c.getString(c.getColumnIndex("addTime"));
                    String updateTime = c.getString(c
                            .getColumnIndex("updateTime"));
                    String tags = c.getString(c.getColumnIndex("tags"));
                    String categoryIds = c.getString(c.getColumnIndex("categoryIds"));
                    int priority = c.getInt(c.getColumnIndex("priority"));
                    String downloadTime = c.getString(c.getColumnIndex("downloadTime"));
                    int hasMore = c.getInt(c.getColumnIndex("hasMore"));
                    contentList.add(new ContentDTO(contentId, type, image,
                            localPath, localViewCount, isFavorite, isDislike,
                            link, title, bonusAmount, content, startTime,
                            endTime, status, addTime, updateTime, tags, priority, downloadTime, hasMore, categoryIds));
                }
            }
        } catch (Exception e) {
            Log.e("content query:", "", e);
        } finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }
        DBHelper.getInstance(ctx).closeDatabase();
        Log.d("ContentDataOperator", "load success:"+contentList.size());
        return contentList;
    }

    // public static void add(Context ctx, ContentDTO item, int position) {
    // List<ContentDTO> contentList = (List<ContentDTO>) FileUtils.getObject(
    // ctx, SlideConstants.CONTENTS_FILE_NAME);
    //
    // if (contentList == null)
    // contentList = new ArrayList<ContentDTO>();
    //
    // if (position < contentList.size()) {
    // contentList.add(position, item);
    // } else {
    // contentList.add(item);
    // }
    //
    // FileUtils.saveObject(ctx, SlideConstants.CONTENTS_FILE_NAME,
    // contentList);
    // }
    //
    // public static void add(Context ctx, ContentDTO item) {
    // List<ContentDTO> contentList = (List<ContentDTO>) FileUtils.getObject(
    // ctx, SlideConstants.CONTENTS_FILE_NAME);
    //
    // if (contentList == null)
    // contentList = new ArrayList<ContentDTO>();
    //
    // contentList.add(item);
    //
    // FileUtils.saveObject(ctx, SlideConstants.CONTENTS_FILE_NAME,
    // contentList);
    // }

    public static void add(Context ctx, ContentDTO item) {
		Log.d("ContentDataOperator", "enter add:" + item.getId());
		if(getById(ctx, item.getId()) != null){
			deleteById(ctx, item.getId());
		}
        SQLiteDatabase db = DBHelper.getInstance(ctx).openDatabase();
        ContentValues values = new ContentValues();
        values.put("id", item.getId());
        values.put("content", item.getContent());
        values.put("image", item.getImage());
        values.put("link", item.getLink());
        values.put("isFavorite", item.isFavorite() ? 1 : 0);
        values.put("isDislike", item.isDislike() ? 1 : 0);
        values.put("localPath", item.getLocalPath());
        values.put("tags", item.getTagStr());
        values.put("categoryIds", item.getCategoryStr());
        values.put("title", item.getTitle());
        values.put("accountRange", item.getAccountRange());
        values.put("localViewCount", item.getLocalViewCount());
        values.put("status", item.getStatus());
        values.put("type", item.getType());
        values.put("priority", item.getPriority());
        values.put("hasMore", item.getHasMore());
        String addTime = "1900-01-01 00:00:00";
        String startTime = "1900-01-01 00:00:00";
        String endTime = "1900-01-01 00:00:00";
        String updateTime = "1900-01-01 00:00:00";
        String downloadTime = "1900-01-01 00:00:00";
        addTime = DisplayUtils.formatDateTimeString(item.getAddTime());
        startTime = DisplayUtils.formatDateTimeString(item.getStartTime());
        endTime = DisplayUtils.formatDateTimeString(item.getEndTime());
        updateTime = DisplayUtils.formatDateTimeString(item
                .getUpdateTime());
        downloadTime = DisplayUtils.formatDateTimeString(item.getDownloadTime());
        values.put("addTime", addTime);
        values.put("bonusAmount", item.getBonusAmount().toString());
        values.put("endTime", endTime);
        values.put("startTime", startTime);
        values.put("updateTime", updateTime);
        values.put("downloadTime", downloadTime);

        try {
            long rowId = db.insert("content", null, values);
            if(rowId > 0)
                Log.d("ContentDataOperator", "add Success at row:" + rowId);
            else
                Log.e("ContentDataOperator", "add failed:" + rowId);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        
        DBHelper.getInstance(ctx).closeDatabase();
    }

    // public static void save(Context ctx, List<ContentDTO> contentList) {
    // FileUtils.saveObject(ctx, SlideConstants.CONTENTS_FILE_NAME,
    // contentList);
    // }

    public static void addAll(Context ctx, List<ContentDTO> contentList) {
        SQLiteDatabase db = DBHelper.getInstance(ctx).openDatabase();
        String sql = "insert into content (id, type, image, localPath, localViewCount, isFavorite, isDislike, link," +
                                            " title, bonusAmount, accountRange, content, startTime, endTime, downloadTime, status," +
                                            " addTime, updateTime, tags, priority, hasMore, categoryIds) values ";
        final String valueTemplate = "(%s, %s, '%s', '%s', %s, %s, %s, '%s', '%s', %s, %s, '%s', '%s', '%s', '%s', %s, '%s', '%s', '%s', %s, %s, '%s')";
        for (int i = 0; i < contentList.size(); i++) {
            ContentDTO content = contentList.get(i);
            String addTime = "1900-01-01 00:00:00";
            String startTime = "1900-01-01 00:00:00";
            String endTime = "1900-01-01 00:00:00";
            String updateTime = "1900-01-01 00:00:00";
            String downloadTime = "1900-01-01 00:00:00";
            try {
                addTime = DisplayUtils.formatDateTimeString(content
                        .getAddTime());
                startTime = DisplayUtils.formatDateTimeString(content
                        .getStartTime());
                endTime = DisplayUtils.formatDateTimeString(content
                        .getEndTime());
                updateTime = DisplayUtils.formatDateTimeString(content
                        .getUpdateTime());
                downloadTime = DisplayUtils.formatDateTimeString(content
                        .getDownloadTime());
            } catch (Exception e) {
                Log.e("add action log:", "", e);
            }
            sql += (i == 0 ? "" : ",")
                    + String.format(valueTemplate, content.getId(), content
                            .getType(), content.getImage().replace("'", "''"),
                    content.getLocalPath().replace("'", "''"), content
                            .getLocalViewCount(),
                    content.isFavorite() ? 1 : 0,
                    content.isDislike() ? 1 : 0, content.getLink()
                            .replace("'", "''"), content.getTitle()
                            .replace("'", "''"), content
                            .getBonusAmount(), content
                            .getAccountRange(), content.getContent()
                            .replace("'", "''"), startTime, endTime, downloadTime,
                    content.getStatus(), addTime, updateTime, content
                            .getTagStr().replace("'", "''"), content.getPriority(), content.getHasMore(), content
                            .getCategoryStr().replace("'", "''"));
        }
        try {
            db.execSQL(sql);
        } catch (Exception e) {
            Log.e("Contents INSERT", "", e);
        }
        DBHelper.getInstance(ctx).closeDatabase();
        Log.d("test update boardcast", "test update boardcast " + DisplayUtils.getLineInfo());
        PageChangeUtils.changeLockServiceState(ctx, LockService.STATE_UPDATE_DATE);
    }

    // public static void update(Context ctx, ContentDTO item) {
    // List<ContentDTO> contentList = (List<ContentDTO>) FileUtils.getObject(
    // ctx, SlideConstants.CONTENTS_FILE_NAME);
    //
    // if (contentList == null) {
    // contentList = new ArrayList<ContentDTO>();
    // return;
    // }
    //
    // int index = -1;
    // for (int i = 0; i < contentList.size(); i++) {
    // if (contentList.get(i).getId() == item.getId()) {
    // index = i;
    // break;
    // }
    // }
    // if (index >= 0) {
    // contentList.set(index, item);
    // }
    //
    // FileUtils.saveObject(ctx, SlideConstants.CONTENTS_FILE_NAME,
    // contentList);
    // }

    public static void update(Context ctx, ContentDTO content) {
        updateContent(ctx, content);
        PageChangeUtils.changeLockServiceState(ctx, LockService.STATE_UPDATE_DATE);
    }
    
    public static void updateWithoutNoti(Context ctx, ContentDTO content) {
        updateContent(ctx, content);
    }

	private static void updateContent(Context ctx, ContentDTO content) {
		SQLiteDatabase db = DBHelper.getInstance(ctx).openDatabase();
        final String sqlTemplate = "update content set type=%s, image='%s', link='%s', title='%s',"
                + " bonusAmount=%s, accountRange=%s, localPath='%s', localViewCount=%s, "
                + "content='%s', startTime='%s', endTime='%s', status=%s, addTime='%s', updateTime='%s', "
                + "tags='%s', isFavorite=%s, isDislike=%s, priority=%s, downloadTime='%s', hasMore=%s, categoryIds='%s' where id=%s";
        String addTime = "1900-01-01 00:00:00";
        String startTime = "1900-01-01 00:00:00";
        String endTime = "1900-01-01 00:00:00";
        String updateTime = "1900-01-01 00:00:00";
        String downloadTime = "1900-01-01 00:00:00";
        try {
            addTime = DisplayUtils.formatDateTimeString(content.getAddTime());
            startTime = DisplayUtils.formatDateTimeString(content
                    .getStartTime());
            endTime = DisplayUtils.formatDateTimeString(content.getEndTime());
            updateTime = DisplayUtils.formatDateTimeString(content
                    .getUpdateTime());
            downloadTime = DisplayUtils.formatDateTimeString(content
                    .getDownloadTime());
        } catch (Exception e) {
            Log.e("add action log:","", e);
        }
        String sql = String.format(sqlTemplate, content.getType(), content
                        .getImage().replace("'", "''"),
                content.getLink().replace("'", "''"), content.getTitle()
                        .replace("'", "''"), content.getBonusAmount(), content
                        .getAccountRange(),
                content.getLocalPath().replace("'", "''"), content
                        .getLocalViewCount(),
                content.getContent().replace("'", "''"), startTime, endTime,
                content.getStatus(), addTime, updateTime, content.getTagStr()
                        .replace("'", "''"), content.isFavorite() ? 1 : 0,
                content.isDislike() ? 1 : 0, content.getPriority(), downloadTime, content.getHasMore(), content.getCategoryStr()
                        .replace("'", "''"), content.getId());
        db.execSQL(sql);
        DBHelper.getInstance(ctx).closeDatabase();
	}

    // public static void deleteById(Context ctx, int id) {
    // List<ContentDTO> contentList = (List<ContentDTO>) FileUtils.getObject(
    // ctx, SlideConstants.CONTENTS_FILE_NAME);
    //
    // if (contentList == null) {
    // contentList = new ArrayList<ContentDTO>();
    // return;
    // }
    // int index = -1;
    // for (int i = 0; i < contentList.size(); i++) {
    // if (contentList.get(i).getId() == id) {
    // index = i;
    // }
    // }
    // if (index >= 0) {
    // contentList.remove(index);
    // }
    //
    // FileUtils.saveObject(ctx, SlideConstants.CONTENTS_FILE_NAME,
    // contentList);
    // }

    public static void deleteById(Context ctx, int id) {
        SQLiteDatabase db = DBHelper.getInstance(ctx).openDatabase();
        db.delete("content", "id = ?", new String[]{String.valueOf(id)});
        DBHelper.getInstance(ctx).closeDatabase();
    }

    public static void deleteBeforeDate(Context ctx, String time) {
        SQLiteDatabase db = DBHelper.getInstance(ctx).openDatabase();

        String sql = "DELETE FROM content WHERE date(startTime) < date('" + time + "')";
        db.execSQL(sql);
        DBHelper.getInstance(ctx).closeDatabase();

        Log.d("test update boardcast", "test update boardcast " + DisplayUtils.getLineInfo());
        PageChangeUtils.changeLockServiceState(ctx, LockService.STATE_UPDATE_DATE);
    }
}
