package com.smart.lock.dto;

import com.smart.lock.common.ActionTypeEnum;

import java.io.Serializable;
import java.util.Date;

public class AccountActionLogDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int id;

	private int accountId;

	private int actionType;

	private int contentId;

	public AccountActionLogDTO(int accountId, ActionTypeEnum actionType,
							   int contentId) {
		this.accountId = accountId;
		this.actionType = actionType.getValue();
		this.contentId = contentId;
		this.addTime = new Date(System.currentTimeMillis());
	}

	public AccountActionLogDTO(int accountId, int actionType,
							   int contentId) {
		this.accountId = accountId;
		this.actionType = actionType;
		this.contentId = contentId;
		this.addTime = new Date(System.currentTimeMillis());
	}

	public AccountActionLogDTO(int accountId, int actionType, int contentId,
			String addTime) {
		this.accountId = accountId;
		this.actionType = actionType;
		this.contentId = contentId;
		this.addTime = new Date(System.currentTimeMillis());
	}

	public int getContentId() {
		return contentId;
	}

	public void setContentId(int contentId) {
		this.contentId = contentId;
	}

	private Date addTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getAccountId() {
		return accountId;
	}

	public void setAccountId(int accountId) {
		this.accountId = accountId;
	}

	public int getActionType() {
		return actionType;
	}

	public void setActionType(int actionType) {
		this.actionType = actionType;
	}

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

}
