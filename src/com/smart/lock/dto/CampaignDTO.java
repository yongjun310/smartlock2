package com.smart.lock.dto;

import com.smart.lock.utils.DisplayUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by 迅 on 2015/6/29.
 */
public class CampaignDTO implements Serializable {

    private int id;

    private String image;

    private String title;

    private String link;

    private int status;

    private Date startTime;

    private Date endTime;

    private Date addTime;

    private Date updateTime;

    private int priority;

    public CampaignDTO(int id, String image, String title, String link, String startTime, String endTime,
                       String updateTime, String addTime, int priority, int status) {
        this.id = id;
        this.image = image;
        this.title = title;
        this.link = link;

        this.startTime = DisplayUtils.parseDatetime(startTime);
        this.endTime = DisplayUtils.parseDatetime(endTime);
        this.addTime = DisplayUtils.parseDatetime(addTime);
        this.updateTime = DisplayUtils.parseDatetime(updateTime);
        this.status = status;
        this.priority = priority;
    }

    public CampaignDTO() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getAddTime() {
        return addTime;
    }

    public void setAddTime(Date addTime) {
        this.addTime = addTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
