package com.smart.lock.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.igexin.sdk.PushManager;
import com.smart.lock.R;
import com.smart.lock.common.ContentTypeEnum;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.dto.ContentDTO;
import com.smart.lock.service.HeartBeatService;
import com.smart.lock.service.LockService;
import com.smart.lock.utils.ContentsDataOperator;
import com.smart.lock.utils.DeviceUtils;
import com.smart.lock.utils.NetUtils;
import com.smart.lock.utils.PollingUtils;
import com.smart.lock.utils.SharedPreferencesUtils;
import com.smart.lock.utils.ThreadHelper;

public class SplashActivity extends BaseActivity {

	private Bitmap bm;

	private ImageView animImageView;
	
	private int accountId;
	
	private boolean isNew;
	
	private boolean hasGo = false;

	private Handler handler = new Handler();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		DeviceUtils.initImageLoader(this);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏

		setContentView(R.layout.activity_splash);

		SharedPreferences commonSP = getSharedPreferences("common",
				MODE_MULTI_PROCESS );
		accountId = commonSP.getInt("accountId", -1);
//		new Thread(new Runnable(){
//			@Override
//			public void run() {
//				if (isNew || accountId < 0) {
//					initPreloadContents();
//				}
//			}
//		}).start();
		
		
		animImageView = (ImageView) this.findViewById(R.id.anim_img);

		Message msg = new Message();
		Runnable mRunnable = new Runnable(){

			@Override
			public void run() {
				SplashActivity.this.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						if(!hasGo) {
							gotoSplash();
							hasGo = true;
						}
					}
				});
				}
			};
		handler.postDelayed(mRunnable, 1900);

		Boolean iswifi = DeviceUtils.isWifi(this);
		Log.d("Splash", "iswifi:" + (iswifi ? "true" : "false"));
		final Animation mFadeInScale;
		/** 设置缩放动画 */
		mFadeInScale = AnimationUtils.loadAnimation(this,R.anim.fade_in_scale);
		mFadeInScale.setDuration(2000);
		animImageView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				/** 结束动画 */
				if(!hasGo) {
					gotoSplash();
					hasGo = true;
				}
			}
		});
		animImageView.startAnimation(mFadeInScale);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		/** 初始化个推 */
		PushManager.getInstance().initialize(this.getApplicationContext());
		// start heartbeat
		/*if (!PollingUtils.isServiceRunning(this,
				HeartBeatService.class.getName())) {
			PollingUtils.startPollingService(this,
					SlideConstants.SEVICE_HEARTBEAT_SPAN,
					HeartBeatService.class,
					SlideConstants.HEARTBEAT_SERVICE_NAME);

		}*/

    	startService(new Intent(this, HeartBeatService.class));
    	startService(new Intent(this, LockService.class));
/*
		int index = (int) (Math.random() * 4);
		View bgView = findViewById(R.id.layout_splash);
		switch (index) {
		case 0:
			bgView.setBackgroundResource(R.drawable.default_1);
			break;
		case 1:
			bgView.setBackgroundResource(R.drawable.default_2);
			break;
		case 2:
			bgView.setBackgroundResource(R.drawable.default_3);
			break;
		case 3:
			bgView.setBackgroundResource(R.drawable.default_4);
			break;
		default:
			break;
		}
*/
		
		mFadeInScale.setAnimationListener(new AnimationListener() {

			public void onAnimationStart(Animation animation) {

			}

			public void onAnimationRepeat(Animation animation) {

			}

			public void onAnimationEnd(Animation animation) {
				
			}
		});
		if (!PollingUtils.isServiceRunning(this,
				SlideConstants.DATA_SERVICE_NAME))
			startService(new Intent(SlideConstants.DATA_SERVICE_NAME));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (bm != null && !bm.isRecycled()) {
			bm.recycle();
			bm = null;
			System.gc();
		}
	}

	private void gotoSplash() {
		Intent mainIntent = null;

		isNew = SharedPreferencesUtils.getBoolSP(this, SharedPreferencesUtils.IS_NEW, true);
		if (isNew) {
			mainIntent = new Intent(SplashActivity.this,
					BoxOpenActivity.class);
			mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		} else {
			mainIntent = new Intent(SplashActivity.this,
					MainActivity.class);
			mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		}
		if(accountId < 0 && !isNew) {
			final ThreadHelper threadHelper = ThreadHelper.getInstance(this);
			new Thread(new Runnable() {
				@Override
				public void run() {
					NetUtils.tempLogin(SplashActivity.this, isNew, threadHelper);
				}
			}).start();
		}
		SplashActivity.this.startActivity(mainIntent);
		SplashActivity.this.finish();
	}

	private void initPreloadContents() {
		List<ContentDTO> preLoadContents = new ArrayList<ContentDTO>();
		ContentDTO contentDTO0 = new ContentDTO(231, ContentTypeEnum.READING
				.getValue(), "http://7x00e0.com1.z0.glb.clouddn.com/jasmine_duola.jpg",
				"http://www.imiaoxiu.com/?p=1476", "Name-liking？喜欢自己的名字你会更幸福！");
		ContentDTO contentDTO1 = new ContentDTO(266, ContentTypeEnum.READING
				.getValue(), "http://7x00e0.com1.z0.glb.clouddn.com/jasmine_Emma.jpg",
				"http://www.imiaoxiu.com/?p=1860", "这才是最棒的姑娘——女神，心灵美，学霸，有思想");
		ContentDTO contentDTO2 = new ContentDTO(268, ContentTypeEnum.READING
				.getValue(), "http://7x00e0.com1.z0.glb.clouddn.com/jasmine_coffee.jpg",
				"http://www.imiaoxiu.com/?p=1954", "5 种「吃」咖啡的方法，为速溶咖啡找到新出路");
		ContentDTO contentDTO3 = new ContentDTO(362, ContentTypeEnum.READING
				.getValue(), "http://7xiuhd.com1.z0.glb.clouddn.com/sport.png",
				"http://www.imiaoxiu.com/?p=684", "燃烧吧，我的卡路里！");
		preLoadContents.add(contentDTO0);
		preLoadContents.add(contentDTO1);
		preLoadContents.add(contentDTO2);
		preLoadContents.add(contentDTO3);

		ContentsDataOperator.addAll(this, preLoadContents);
	}
}
