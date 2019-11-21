package com.smart.lock.dto;

import java.io.Serializable;

public class ImageInfoDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String format;
	
	private int width;
	
	private int height;
	
	private String colorModel;

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getColorModel() {
		return colorModel;
	}

	public void setColorModel(String colorModel) {
		this.colorModel = colorModel;
	}

}
