package com.smart.lock.common;

public enum ActionTypeEnum {

	DISPLAY(1), LEFT_SLIDE(2), RIGHT_SLIDE(3), FAVORTITE_IN_LOCK(4), DISLIKE(5), CANCEL_FAVOR(
			6), SHARE(7), SHARE_SUCCESS(8), FAVORTITE_IN_WEBVIEW(9);

	private int actionType;

	ActionTypeEnum(int actionType) {
		this.actionType = actionType;
	}

	public int getValue() {
		return this.actionType;
	}

}
