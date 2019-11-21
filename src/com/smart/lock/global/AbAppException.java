package com.smart.lock.global;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.text.TextUtils;

// TODO: Auto-generated Javadoc
/**
 * ** @ClassName: AbAppException
	* @Description: 公共异常类.
	* @author yongjunxie
	* @date 2013-8-16 上午10:40:40
 */
public class AbAppException extends Exception {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1;

	
	/** The msg. */
	private String msg = null;

	/**
	 * Instantiates a new ab app exception.
	 *
	 * @param e the e
	 */
	public AbAppException(Exception e) {
		super();

		try {
			if (e instanceof ConnectException) {
				msg = "无法连接网络，请检查网络配置";
			} 
			else if (e instanceof UnknownHostException) {
				msg = "不能解析的服务地址";
			}else if (e instanceof SocketException) {
				msg = "网络有错误，请重试";
			}else if (e instanceof SocketTimeoutException) {
				msg = "连接超时，请重试";
			} else {
				if (e == null || TextUtils.isEmpty(e.getMessage())) {
					msg = "抱歉，程序出错了，请联系我们";
				}
				msg = " " + e.getMessage();
			}
		} catch (Exception e1) {
		}

	}

	/**
	 * Instantiates a new ab app exception.
	 *
	 * @param detailMessage the detail message
	 */
	public AbAppException(String detailMessage) {
		super(detailMessage);
		msg = detailMessage;
	}

	/**
	 * 描述：获取异常信息.
	 *
	 * @return the message
	 * @see Throwable#getMessage()
	 */
	@Override
	public String getMessage() {
		return msg;
	}

}