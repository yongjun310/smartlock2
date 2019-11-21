package com.smart.lock.common;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;

import android.content.Intent;
import android.os.Environment;

import com.smart.lock.dto.ProductDTO;

public interface SlideConstants {
	public final static String EXTERNAL_IMAGE_PATH = Environment
			.getExternalStorageDirectory() + "/data/mshow/";

	public final static String EXTERNAL_IMAGE_LOADER_PATH = Environment
			.getExternalStorageDirectory() + "/data/mshow/imageloader";

	public final static String EXTERNAL_PHOTO_IMAGE_PATH = Environment
			.getExternalStorageDirectory() + "/遇见锁屏/";
	

    /** 当前 DEMO 应用的 APP_KEY，第三方应用应该使用自己的 APP_KEY 替换该 APP_KEY */
    public static final String APP_KEY = "2753758244";

    /** 
     * 当前 DEMO 应用的回调页，第三方应用可以使用自己的回调页。
     * 
     * <p>
     * 注：关于授权回调页对移动客户端应用来说对用户是不可见的，所以定义为何种形式都将不影响，
     * 但是没有定义将无法使用 SDK 认证登录。
     * 建议使用默认回调页：https://api.weibo.com/oauth2/default.html
     * </p>
     */
    public static final String REDIRECT_URL = "http://open.weibo.com/apps/2753758244/info/advanced";

    /**
     * Scope 是 OAuth2.0 授权机制中 authorize 接口的一个参数。通过 Scope，平台将开放更多的微博
     * 核心功能给开发者，同时也加强用户隐私保护，提升了用户体验，用户在新 OAuth2.0 授权页中有权利
     * 选择赋予应用的功能。
     * 
     * 我们通过新浪微博开放平台-->管理中心-->我的应用-->接口管理处，能看到我们目前已有哪些接口的
     * 使用权限，高级权限需要进行申请。
     * 
     * 目前 Scope 支持传入多个 Scope 权限，用逗号分隔。
     * 
     * 有关哪些 OpenAPI 需要权限申请，请查看：http://open.weibo.com/wiki/%E5%BE%AE%E5%8D%9AAPI
     * 关于 Scope 概念及注意事项，请查看：http://open.weibo.com/wiki/Scope
     */
    public static final String SCOPE = 
            "email,direct_messages_read,direct_messages_write,"
            + "friendships_groups_read,friendships_groups_write,statuses_to_me_read,"
            + "follow_app_official_microblog," + "invitation_write";
    
    public static final int IMAGE_MAX_COUNT = 500;
	public static final int MAX_VIEW_COUNT = 6;
	public static final int VIEW_PAGER_SIZE = 50;
	public static final int MAX_LOCAL_CONTENT_SIZE = 60;
	public static final int MAX_LOAD_CONTENT_SIZE = 1000;
	public static final int NOTIFICATION_ID = 100;
	public static final int OFFSET_COEFFICIENT = 3;

	public static final int UPDATE_CONTENT_SPAN = 12*60;


	public static final String DATE_FORMART = "%s月%s号 星期%s";

	public static final String NEW_DATE_FORMART = "%s月%s号";

	// home
	//public static final String SERVER_URL = "http://192.168.1.9:8080/slide-server";
	// office
	//public static final String SERVER_URL = "http://192.168.0.100:8080/slide-server";
	// product
	public static final String SERVER_URL = "http://120.26.43.29:8081/slide-server";

	public static final String SERVER_METHOD_DISTRIBUTE = "/distributeNew";

	public static final String SERVER_METHOD_LOAD_CAMPAIGNS = "/loadCampaigns";

	public static final String SERVER_METHOD_SMSCODE = "/sms/code";

	public static final String SERVER_METHOD_LOGIN = "/login";

	public static final String SERVER_METHOD_TEMP_LOGIN = "/tempLogin";

	public static final String SERVER_METHOD_UPDATE_ACCOUNT_INFO = "/updateAccountInfo";

	public static final String SERVER_METHOD_INCOME = "/income/overview";

	public static final String SERVER_METHOD_FAVORITE = "/viewed/favorite";

	public static final String SERVER_METHOD_FAVORITE_NEW = "/viewed/favoriteNew";

	public static final String SERVER_METHOD_VIEWED = "/viewed/all";

	public static final String SERVER_METHOD_UPDATE_FAVORITE = "/viewed/update";

	public static final String SERVER_METHOD_EXCHANGE = "/exchange";

	public static final String SERVER_METHOD_CHECK_VERSION = "/version/checkNew";

	public static final String SERVER_METHOD_FEEDBACK = "/feedback/init";

	public static final String SERVER_METHOD_POST_LOG = "/log/post";

	public static final String SERVER_METHOD_MALL = "/mall/init";

	public static final String SERVER_METHOD_MALL_LIST = "mall/listNew";

	public static final String SERVER_METHOD_HAS_MESSAGE = "/message/hasNew";
	
	public static final String SERVER_METHOD_TRANSACTION = "/transaction";

	public static final String SERVER_METHOD_MESSAGE_LIST = "message/list";

	public static final String SERVER_METHOD_UPDATE_GETUI = "/updateGTId";

	public static final String SERVER_METHOD_MESSAGE_DETAIL = "message/detail";

	public static final String SERVER_METHOD_INVITE_USERS = "/inviteNewUser";

	public static final String SERVER_METHOD_POST_SSO_INFO = "/postSSOInfo";


	public static final String FAQ_URL = "http://www.imiaoxiu.com/?page_id=783";

	public static final String CONTENTS_FILE_NAME = "contentList.dat";

	public static final int MIN_PAGER_SIZE = 4;

	public static final int TOTAL_DEFAULT_PAGER_SIZE = 12;

	public static final String UNLOCK_AMOUNT = "0.02";

	public static final String TOAST_LOGIN_SMS_SENT = "验证码已发送";

	public static final String TOAST_LOGIN_SKIP_TIPS = "绑定手机后才能兑话费哦~";

	public static final String TOAST_EXCHANGE_SKIP_TIPS = "绑定手机或登录后才能兑话费哦~";

	public static final String TOAST_LOGIN_SERVER_ERROR = "服务器异常，发送失败";

	public static final String TOAST_LOGIN_INVALID_MOBILE = "请输入正确的手机号";

	public static final String TOAST_LOGIN_NO_AVAILABLE_NETWORK = "没有网络连接";

	public static final String TOAST_LOGIN_INVALID_CODE = "请输入正确的验证码";

	public static final String TOAST_CANNOT_SKIP_TIPS = "该图不能屏蔽，小编在奋力加新内容~";

	public static final String TOAST_CANNOT_LIKE_TIPS = "默认图片无法喜欢，分享给小伙伴吧~";

	public static final int SMS_CODE_LENGTH = 6;

	public static final int SERVER_RETURN_SUCCESS = 200;

	public static final String TOAST_LOGIN_SUCCESS = "登陆成功";

	public static final String TOAST_INCOME_SERVER_BUSY = "服务器忙，请稍候重试";

	public static final String TOAST_INCOME_SERVER_ERROR = "服务器异常，获取账户信息失败";

	public static final String TOAST_INCOME_EXCHANGE_SUCCESS = "充值已提交，订单将在一个工作日内完成";

	public static final String TOAST_VERSION_SERVER_ERROR = "服务器异常，获取最新版本失败";

	public static final int WEB_VIEW_CONTENT = 1;

	public static final int WEB_VIEW_FEEDBACK = 2;

	public static final int WEB_VIEW_FAQ = 3;

	public static final int WEB_VIEW_MYMESSAGE = 4;

	public static final int WEB_VIEW_MYMESSAGE_DETAIL = 5;

	public static final int WEB_VIEW_RECOMMAND = 6;

	public static final int MAIN_ACTIVITY_AMOUNT = 6;

	public static final String FAVORITE_POST_ERROR = "加载失败";

	public static final String FAVORITE_NET_ERROR = "没有网络连接";

	public static final String TOAST_VERSION_NEWEST = "已是最新版本";

	public static final String TOAST_VERSION_DOWNLOADING = "发现新版本，正在后台为您下载";

	public static final String TOAST_VERSION_NO_WIFI = "发现新版本，系统检测到您未在WIFI环境下，取消下载";

	public static final ProductDTO[] PRODUCT_LSIT = new ProductDTO[] {
			new ProductDTO(1, new BigDecimal(30), "30元话费"),
			new ProductDTO(1, new BigDecimal(50), "50元话费") };

	public static final String TOAST_EXCHANGE_INPUT_ERROR = "请输入正确的充值手机和金额";

	public static final String TOAST_WEB_VIEW_LOADING = "页面加载中，请稍候";

	public static final String TOAST_WEB_VIEW_REFRESHING = "页面刷新中，请稍候";

	public static final String TOAST_WEB_VIEW_FAVORITE_SUCCESS = "添加收藏成功";

	public static final String DOWNLOAD_WALLPAPER_BEGIN = "壁纸已开始下载，请稍候";

	public static final String CLOSE_GESTURE_PW_SUCCESS = "手势密码已关闭";

	public static final String DOWNLOAD_WALLPAPER_SUCCESS = "图片已下载到" + EXTERNAL_PHOTO_IMAGE_PATH;

	public static final String SELECT_THREE_CATEGORYS = "至少选择3个分类";

	public static final double SLIDE_DISTANCE_COEFF = 0.250;

	public static final double SLIDE_DISTANCE_UP_COEFF = 0.71;

	public static final double SLIDE_DISTANCE_LEFT_COEFF = 0.410;

	public static final String LOGS_FILE_NAME = "logList.dat";

	public static final String PROCESS_REMOTE_NAME = ":remote";

	public static final String DATA_SERVICE_NAME = "com.smart.lock.service.DataService";

	
    public static final String LOCKSERVICE_ACTION="com.smart.lock.service.LOCKSERVICE_ACTION";
	
    public static final String DB_UPDATE_ACTION="com.smart.lock.DB_UPDATE_ACTION";

	public static final String BROADCAST_DATA_SERVICE_DESTORY = "com.smart.lock.service.DataService.destory";

	public static final String BROADCAST_HEARTBEAT_SERVICE_DESTORY = "com.smart.lock.service.HeartBeatService.destory";

	public static final String BROADCAST_DATA_PROCESS_LOG = "com.smart.lock.service.DataService.log";

	public static final String BROADCAST_DATA_PROCESS_CONTENT = "com.smart.lock.service.DataService.content";

	public static final String BROADCAST_DATA_PROCESS_PIC = "com.smart.lock.service.DataService.pic";

	public static final long SEVICE_HEARTBEAT_SPAN = 1 * 60 * 1000;

	public static final long SEVICE_DATA_PROCESS_SPAN = 3 * 60 * 1000;

	public static final long SEVICE_Lock_SPAN =  6 * 1000;

	public static final String HEARTBEAT_SERVICE_NAME = "com.smart.lock.service.HeartBeatService";

	public static final int POLLING_CODE = 1107;

	public static final int POLLING_BROADCAST_CODE = 1108;

	public static final double NORMAL_FILE_SIZE = 204800.0;

	public static final int MAX_IMAGE_SIZE = 300 * 1024;

	public static final String QINIU_PIC_PARAM = "?imageMogr2/thumbnail/x%s/gravity/Center/crop/%sx";

	public static final BigDecimal MIN_EXCHANGE_AMOUNT = new BigDecimal("30");

	public static final String DB_NAME = "slide.db";

	/** 本地图片content*/
	public static final String LOCAL_CONTENT_PATH = "preLoad";

//	public static final int DB_VERSION = 1;
	//加入优先级
//	public static final int DB_VERSION = 2;
	//加入downloadTime，hasMore，tags，categoryIds
	//public static final int DB_VERSION = 3;
	//加入campaign
	public static final int DB_VERSION = 4;

	public static final String IMAGE_STYLE_SHARE_ICON = "?imageMogr2/thumbnail/128x/gravity/North/crop/x128";

	public static final int DOWNLOAD_PIC_BATCH_COUNT = 10;

	public static final String EXCHANGE_TIPS_NO_MOBILE_LESS = "为保障您的账户安全，请用手机注册登陆";

	public static final String EXCHANGE_TIPS_NO_MOBILE_MORE = "先用手机注册，才能兑换话费哦";

	public static final long SPLASH_DISPLAY_DURA = 2000;

	public static final String QQ_APPID = "1104074720";

	public static final String QQ_APPKEY = "H1Za1T4Q1ttP6Q5g";

	public static final String WECHAT_APPID = "wx5b95be98c4f0642f";

	public static final String WECHAT_APPSEC = "257f59414b948e4110e363a914e159f9";

	/** 分享应用宝链接 */
	public static final String APP_DOWNLOAD_LINK = "http://a.app.qq.com/o/simple.jsp?pkgname=com.smart.lock";

	public static final int DEFAULT_CONTENT_PRIORITY = 0;

	public static final String THREAD_DISTRIBUTE_CONTENT = "loadContentFromServer";

	public static final String THREAD_POST_LOG = "postLogs";

	public static final String THREAD_DOWNLOAD_PIC = "downloadPic";

	public static final int REQUEST_CODE_DETAIL = 0;
	public static final int RESULT_CODE_REFRESH_MAIN = 5;

	public static final String SEPERATOR_ITEM = "^%$";

	public static final String SEPERATOR_KEY_VALUE = "^*#";

	public static final String SEPERATOR_PARAM = "^@~";

	public static final String SEPERATOR_PARAM_VALUE = "^&!";
}
