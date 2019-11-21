package com.smart.lock.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

public class AccountDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int id;

	private String androidId;

	private String mobileNo;

	private BigDecimal totalAmount;

	private int referrerAccountId;

	private int registerChannel;

	private Date addTime;

	private Date updateTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAndroidId() {
		return androidId;
	}

	public void setAndroidId(String androidId) {
		this.androidId = androidId;
	}

	public String getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public int getReferrerAccountId() {
		return referrerAccountId;
	}

	public void setReferrerAccountId(int referrerAccountId) {
		this.referrerAccountId = referrerAccountId;
	}

	public int getRegisterChannel() {
		return registerChannel;
	}

	public void setRegisterChannel(int registerChannel) {
		this.registerChannel = registerChannel;
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

}
