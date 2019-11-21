package com.smart.lock.response;

import java.io.Serializable;

import com.smart.lock.dto.AccountInfoDTO;

public class LoginResponse extends BaseResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private AccountInfoDTO data;

	public AccountInfoDTO getData() {
		return data;
	}

	public void setData(AccountInfoDTO data) {
		this.data = data;
	}

}
