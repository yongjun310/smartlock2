package com.smart.lock.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.smart.lock.dto.CampaignDTO;
import com.smart.lock.dto.CampaignDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CampaignDataOperator {

    private static final String TAG = "CampaignDataOperator";

    // public static CampaignDTO getById(Context ctx, int id) {
    // List<CampaignDTO> campaignList = (List<CampaignDTO>) FileUtils.getObject(
    // ctx, SlideConstants.campaignS_FILE_NAME);
    //
    // if (campaignList == null)
    // campaignList = new ArrayList<CampaignDTO>();
    //
    // for (CampaignDTO campaign : campaignList) {
    // if (campaign.getId() == id) {
    // return campaign;
    // }
    // }
    // return null;
    // }
    public static CampaignDTO getById(Context ctx, int id) {
        CampaignDTO campaignDTO = null;

        SQLiteDatabase db = DBHelper.getInstance(ctx).openDatabase();
        String sql = "select * from campaign where id= ?";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(id)});
        try {
            if (c.moveToFirst()) {
                c.moveToPosition(0);
                int campaignId = c.getInt(c.getColumnIndex("id"));
                String image = c.getString(c.getColumnIndex("image"));
                String link = c.getString(c.getColumnIndex("link"));
                String title = c.getString(c.getColumnIndex("title"));
                String startTime = c.getString(c.getColumnIndex("startTime"));
                String endTime = c.getString(c.getColumnIndex("endTime"));
                int status = c.getInt(c.getColumnIndex("status"));
                String addTime = c.getString(c.getColumnIndex("addTime"));
                String updateTime = c.getString(c.getColumnIndex("updateTime"));
                int priority = c.getInt(c.getColumnIndex("priority"));
                //(int id, String image, String title, String link, String startTime, String endTime,
                //String updateTime, String addTime, int priority, int status) {
                campaignDTO = new CampaignDTO(campaignId, image,
                         title,link, startTime, endTime, updateTime, addTime, priority, status);
            }
        } catch (Exception e) {
            Log.e("campaign query:", e.getStackTrace().toString());
        } finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }

        DBHelper.getInstance(ctx).closeDatabase();
        return campaignDTO;
    }

    // public static List<CampaignDTO> load(Context ctx) {
    // List<CampaignDTO> campaignList = (List<CampaignDTO>) FileUtils.getObject(
    // ctx, SlideConstants.campaignS_FILE_NAME);
    //
    // if (campaignList == null)
    // campaignList = new ArrayList<CampaignDTO>();
    // return campaignList;
    // }
    public static List<CampaignDTO> load(Context ctx) {
        return load(ctx, "SELECT * FROM campaign");
    }

    public static List<CampaignDTO> load(Context ctx, String sql) {
        Log.d(TAG, "load enter:"+sql);
        List<CampaignDTO> campaignList = new ArrayList<CampaignDTO>();

        SQLiteDatabase db = DBHelper.getInstance(ctx).openDatabase();
        Cursor c = db.rawQuery(sql, null);
        Log.d(TAG, "load excute:"+c.getCount());
        try {
            if (c.moveToFirst()) {
                for (int i = 0; i < c.getCount(); i++) {
                    c.moveToPosition(i);
                    int campaignId = c.getInt(c.getColumnIndex("id"));
                    String image = c.getString(c.getColumnIndex("image"));
                    String link = c.getString(c.getColumnIndex("link"));
                    String title = c.getString(c.getColumnIndex("title"));
                    String startTime = c.getString(c
                            .getColumnIndex("startTime"));
                    String endTime = c.getString(c.getColumnIndex("endTime"));
                    int status = c.getInt(c.getColumnIndex("status"));
                    String addTime = c.getString(c.getColumnIndex("addTime"));
                    String updateTime = c.getString(c
                            .getColumnIndex("updateTime"));
                    int priority = c.getInt(c.getColumnIndex("priority"));
                    campaignList.add(new CampaignDTO(campaignId, image,
                            title,link, startTime, endTime, updateTime, addTime, priority, status));
                }
            }
        } catch (Exception e) {
            Log.e("campaign query:", e.toString());
        } finally {
            if (c != null) {
                c.close();
                c = null;
            }
        }
        DBHelper.getInstance(ctx).closeDatabase();
        Log.d(TAG, "load success:" + campaignList.size());
        return campaignList;
    }


    public static void add(Context ctx, CampaignDTO item) {
		Log.d(TAG, "enter add:" + item.getId());
		if(getById(ctx, item.getId()) != null){
			deleteById(ctx, item.getId());
		}
        SQLiteDatabase db = DBHelper.getInstance(ctx).openDatabase();
        ContentValues values = new ContentValues();
        values.put("id", item.getId());
        values.put("image", item.getImage());
        values.put("link", item.getLink());
        values.put("title", item.getTitle());
        values.put("status", item.getStatus());
        values.put("priority", item.getPriority());
        String addTime = "1900-01-01 00:00:00";
        String startTime = "1900-01-01 00:00:00";
        String endTime = "1900-01-01 00:00:00";
        String updateTime = "1900-01-01 00:00:00";
        addTime = DisplayUtils.formatDateTimeString(item.getAddTime());
        startTime = DisplayUtils.formatDateTimeString(item.getStartTime());
        endTime = DisplayUtils.formatDateTimeString(item.getEndTime());
        updateTime = DisplayUtils.formatDateTimeString(item
                .getUpdateTime());
        values.put("addTime", addTime);
        values.put("endTime", endTime);
        values.put("startTime", startTime);
        values.put("updateTime", updateTime);

        try {
            long rowId = db.insert("campaign", null, values);
            if(rowId > 0)
                Log.d(TAG, "add Success at row:" + rowId);
            else
                Log.e(TAG, "add failed:" + rowId);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        
        DBHelper.getInstance(ctx).closeDatabase();
    }

    // public static void save(Context ctx, List<CampaignDTO> campaignList) {
    // FileUtils.saveObject(ctx, SlideConstants.campaignS_FILE_NAME,
    // campaignList);
    // }

	public static void addAll(Context ctx, List<CampaignDTO> campaignList) {
        SQLiteDatabase db = DBHelper.getInstance(ctx).openDatabase();
        String sql = "insert into campaign (id, image,link," +
                                            " title, startTime, endTime, status," +
                                            " addTime, updateTime, priority) values ";
        final String valueTemplate = "(%s,'%s', '%s', '%s', '%s', '%s', %s, '%s', '%s', %s)";
        for (int i = 0; i < campaignList.size(); i++) {
            CampaignDTO campaign = campaignList.get(i);
            String addTime = "1900-01-01 00:00:00";
            String startTime = "1900-01-01 00:00:00";
            String endTime = "1900-01-01 00:00:00";
            String updateTime = "1900-01-01 00:00:00";
            try {
                addTime = DisplayUtils.formatDateTimeString(campaign
                        .getAddTime());
                startTime = DisplayUtils.formatDateTimeString(campaign
                        .getStartTime());
                endTime = DisplayUtils.formatDateTimeString(campaign
                        .getEndTime());
                updateTime = DisplayUtils.formatDateTimeString(campaign
                        .getUpdateTime());
            } catch (Exception e) {
                Log.e("add action log:", "", e);
            }
            sql += (i == 0 ? "" : ",")
                    + String.format(valueTemplate, campaign.getId(), campaign.getImage().replace("'", "''"),
                    campaign.getLink()
                            .replace("'", "''"), campaign.getTitle()
                            .replace("'", "''"), startTime, endTime,
                    campaign.getStatus(), addTime, updateTime, campaign.getPriority());
        }
        try {
            db.execSQL(sql);
        } catch (Exception e) {
            Log.e("campaigns INSERT", "", e);
        }
        DBHelper.getInstance(ctx).closeDatabase();
    }

    public static void update(Context ctx, CampaignDTO campaign) {
        SQLiteDatabase db = DBHelper.getInstance(ctx).openDatabase();
        final String sqlTemplate = "update campaign set image='%s', link='%s', title='%s',"
                + "startTime='%s', endTime='%s', status=%s, addTime='%s', updateTime='%s', "
                + "priority=%s, downloadTime='%s' where id=%s";
        String addTime = "1900-01-01 00:00:00";
        String startTime = "1900-01-01 00:00:00";
        String endTime = "1900-01-01 00:00:00";
        String updateTime = "1900-01-01 00:00:00";
        try {
            addTime = DisplayUtils.formatDateTimeString(campaign.getAddTime());
            startTime = DisplayUtils.formatDateTimeString(campaign
                    .getStartTime());
            endTime = DisplayUtils.formatDateTimeString(campaign.getEndTime());
            updateTime = DisplayUtils.formatDateTimeString(campaign
                    .getUpdateTime());
        } catch (Exception e) {
            Log.e("add action log:","", e);
        }
        String sql = String.format(sqlTemplate,campaign
                        .getImage().replace("'", "''"),
                campaign.getLink().replace("'", "''"), campaign.getTitle()
                        .replace("'", "''"), startTime, endTime,
                campaign.getStatus(), addTime, updateTime, campaign.getPriority(), campaign.getId());
        db.execSQL(sql);
        DBHelper.getInstance(ctx).closeDatabase();
    }

    public static void deleteById(Context ctx, int id) {
        SQLiteDatabase db = DBHelper.getInstance(ctx).openDatabase();
        db.delete("campaign", "id = ?", new String[]{String.valueOf(id)});
        DBHelper.getInstance(ctx).closeDatabase();
    }

    public static void deleteBeforeDate(Context ctx, String time) {
        SQLiteDatabase db = DBHelper.getInstance(ctx).openDatabase();

        String sql = "DELETE FROM campaign WHERE date(endTime) < date('" + time + "')";
        db.execSQL(sql);
        DBHelper.getInstance(ctx).closeDatabase();
    }
}
