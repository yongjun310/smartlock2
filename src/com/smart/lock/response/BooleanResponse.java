package com.smart.lock.response;

import java.io.Serializable;

public class BooleanResponse extends BaseResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Boolean data;

	public Boolean getData() {
		return data;
	}

	public void setData(Boolean data) {
		this.data = data;
	}

}
