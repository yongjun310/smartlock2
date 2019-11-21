package com.smart.lock.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by 迅 on 2015/5/5.
 */
public class CommonMessageDTO implements Serializable {

    private String title;

    private String desc;//暂时不用，保留字段

    private int target;//1-首页 2-商城（含收益） 3-大图 4-注册 5-webview

	private int contentId; //内容ID

	public int getContentId() {
		return contentId;
	}

	public void setContentId(int contentId) {
		this.contentId = contentId;
	}

    private String url;//webview的url

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public int getTarget() {
		return target;
	}

	public void setTarget(int target) {
		this.target = target;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}


}
