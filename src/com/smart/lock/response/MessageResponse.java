package com.smart.lock.response;

import java.io.Serializable;

import com.smart.lock.dto.MessageDTO;

public class MessageResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int code;

	private String msg;
	
	private MessageDTO data;

	public MessageDTO getData() {
		return data;
	}

	public void setData(MessageDTO data) {
		this.data = data;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
