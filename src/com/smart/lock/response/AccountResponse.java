package com.smart.lock.response;

import java.io.Serializable;

import com.smart.lock.dto.AccountDTO;

public class AccountResponse extends BaseResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private AccountDTO data;

	public AccountDTO getData() {
		return data;
	}

	public void setData(AccountDTO data) {
		this.data = data;
	}

}
