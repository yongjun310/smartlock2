package com.smart.lock.dto;

import java.io.Serializable;
import java.util.Date;

public class BaseMessageDTO implements Serializable {
	
	private int type;
	
	private Date sendTime;
	
	private String version;
	
	private String data;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
	

}
