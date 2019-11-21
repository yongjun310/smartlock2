package com.smart.lock.dto;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA. User: xun.wang Date: 15-3-22 Time: 下午7:58 To
 * change this template use File | Settings | File Templates.
 */
public class AppVersionDTO implements Serializable {

	private String version;

	private String desc;

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	private String downloadUrl;

	private AccountInfoDTO accountInfo;

	public AccountInfoDTO getAccountInfo() {
		return accountInfo;
	}

	public void setAccountInfo(AccountInfoDTO accountInfo) {
		this.accountInfo = accountInfo;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
}
