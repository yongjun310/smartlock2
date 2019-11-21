package com.smart.lock.response;

import java.io.Serializable;
import java.util.List;

import com.smart.lock.dto.ContentDTO;

public class ContentListResponse extends BaseResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<ContentDTO> data;

	public List<ContentDTO> getData() {
		return data;
	}

	public void setData(List<ContentDTO> data) {
		this.data = data;
	}

}
