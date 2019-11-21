package com.smart.lock.response;

import com.smart.lock.dto.CampaignDTO;

import java.io.Serializable;
import java.util.List;

public class CampaignListResponse extends BaseResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<CampaignDTO> data;

	public List<CampaignDTO> getData() {
		return data;
	}

	public void setData(List<CampaignDTO> data) {
		this.data = data;
	}

}
