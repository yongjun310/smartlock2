package com.smart.lock.common;

public enum ContentTypeEnum {

	SHOPPING(1), READING(2), SHARE(3);

	private int contentType;

	ContentTypeEnum(int contentType) {
		this.contentType = contentType;
	}

	public int getValue() {
		return this.contentType;
	}

}
