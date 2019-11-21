package com.smart.lock.common;

public enum SSOTypeEnum {

	WEIBO(1), QQ(2), WEIXIN(3);

	private int ssoType;

	SSOTypeEnum(int ssoType) {
		this.ssoType = ssoType;
	}

	public int getValue() {
		return this.ssoType;
	}

}
