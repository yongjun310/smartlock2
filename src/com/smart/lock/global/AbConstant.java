package com.smart.lock.global;

// TODO: Auto-generated Javadoc
/**
 * ** @ClassName: AbConstant
 * 
 * @Description: 常量.
 * @author yongjunxie
 * @date 2013-8-16 上午10:41:03
 */
public class AbConstant {

	/** SharePreferences文件名. */
	public static final String SHAREPATH = "app_share";

	/** 数据状态：可用. */
	public static final int STATEYES = 1;

	/** 数据状态：不可用. */
	public static final int STATENO = 0;

	/** 数据状态：全部. */
	public static final int STATEALL = 9;

	/** 数据状态：有. */
	public static final int HAVE = 1;

	/** 数据状态：没有. */
	public static final int NOTHAVE = 0;

	/** 图片处理：裁剪. */
	public static final int CUTIMG = 0;

	/** 图片处理：缩放. */
	public static final int SCALEIMG = 1;

	/** 返回码：成功. */
	public static final int RESULRCODE_OK = 0;

	/** 返回码：失败. */
	public static final int RESULRCODE_ERROR = -1;

	/** 显示Toast. */
	public static final int SHOW_TOAST = 0;

	/** 显示进度框. */
	public static final int SHOW_PROGRESS = 1;

	/** 删除进度框. */
	public static final int REMOVE_PROGRESS = 2;

	/** 删除底部进度框. */
	public static final int REMOVE_DIALOGBOTTOM = 3;

	/** 删除中间进度框. */
	public static final int REMOVE_DIALOGCENTER = 4;

	/** 标题栏透明标记. */
	public static final String TITLE_TRANSPARENT_FLAG = "TITLE_TRANSPARENT_FLAG";

	/** 标题栏透明. */
	public static final int TITLE_TRANSPARENT = 0;

	/** 标题栏不透明. */
	public static final int TITLE_NOTRANSPARENT = 1;

	/** View的类型. */
	public static final int LISTVIEW = 1;

	/** The Constant GRIDVIEW. */
	public static final int GRIDVIEW = 1;

	/** The Constant GALLERYVIEW. */
	public static final int GALLERYVIEW = 2;

	/** The Constant RELATIVELAYOUTVIEW. */
	public static final int RELATIVELAYOUTVIEW = 3;

	/** Dialog的类型. */
	public static final int DIALOGPROGRESS = 0;

	/** The Constant DIALOGBOTTOM. */
	public static final int DIALOGBOTTOM = 1;

	/** The Constant DIALOGCENTER. */
	public static final int DIALOGCENTER = 2;

	public static final String DESCRIPTOR = "com.umeng.share";

	private static final String TIPS = "请移步官方网站 ";
	private static final String END_TIPS = ", 查看相关说明.";
	public static final String TENCENT_OPEN_URL = TIPS
			+ "http://wiki.connect.qq.com/android_sdk使用说明" + END_TIPS;
	public static final String PERMISSION_URL = TIPS
			+ "http://wiki.connect.qq.com/openapi权限申请" + END_TIPS;

	public static String SOCIAL_LINK = "";
	public static final String SOCIAL_IMAGE = "http://www.umeng.com/images/pic/banner_module_social.png";

}