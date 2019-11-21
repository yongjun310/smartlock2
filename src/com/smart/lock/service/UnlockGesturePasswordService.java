package com.smart.lock.service;

import java.util.List;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smart.lock.R;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.global.LockApplication;
import com.smart.lock.utils.DisplayUtils;
import com.smart.lock.utils.FileUtils;
import com.smart.lock.utils.LockPatternUtils;
import com.smart.lock.utils.PageChangeUtils;
import com.smart.lock.utils.PollingUtils;
import com.smart.lock.utils.SysApplication;
import com.smart.lock.view.LockLayer;
import com.smart.lock.view.LockPatternView;

public class UnlockGesturePasswordService extends Service {
	private LockPatternView mLockPatternView;
	private int mFailedPatternAttemptsSinceLastTimeout = 0;
	private CountDownTimer mCountdownTimer = null;
	private Handler mHandler = new Handler();
	private TextView mHeadTextView;
	private TextView mForgetPW;
	private Animation mShakeAnim;

	private FrameLayout pRootView;

	private Toast mToast;

	private LockLayer lockLayer;
	
	private View pLayoutView;
    
	private WindowManager windowManager;
	
	private WindowManager.LayoutParams pLayoutParams;
	
	private boolean viewAdded = false;
	
	private DisplayMetrics dm;
	
	private void showToast(CharSequence message) {
		if (null == mToast) {
			mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
			mToast.setGravity(Gravity.CENTER, 0, 0);
		} else {
			mToast.setText(message);
		}

		mToast.show();
	}

	@Override
	public void onCreate() {
        pLayoutView = LayoutInflater.from(this).inflate(R.layout.gesturepassword_unlock, null);
        /*if (!PollingUtils.isServiceRunning(UnlockGesturePasswordService.this,
                HeartBeatService.class.getName())) {
            PollingUtils.startPollingService(UnlockGesturePasswordService.this,
                    SlideConstants.SEVICE_HEARTBEAT_SPAN,
                    HeartBeatService.class,
                    SlideConstants.HEARTBEAT_SERVICE_NAME);
        }*/

    	//startService(new Intent(this, HeartBeatService.class));
        //overridePendingTransition(R.anim.anim_enter_lock, R.anim.anim_exit_lock);

        FileUtils.addFileLog("enter LockActivity :");
        FileUtils.addFileLog(SysApplication.getInstance().printActivityList());
        /*if (this.getIntent().getExtras() != null) {
            preActivity = this.getIntent().getExtras().getString("preActivity");
        }*/
        

		windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
		/*
		 * LayoutParams.TYPE_SYSTEM_ERROR：保证该悬浮窗所有View的最上层
		 * LayoutParams.FLAG_NOT_FOCUSABLE:该浮动窗不会获得焦点，但可以获得拖动
		 * PixelFormat.TRANSPARENT：悬浮窗透明
		 */
		pLayoutParams = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT, LayoutParams.TYPE_SYSTEM_ERROR,
				LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);


		mLockPatternView = (LockPatternView) pLayoutView.findViewById(R.id.gesturepwd_unlock_lockview);
		pRootView = (FrameLayout)pLayoutView.findViewById(R.id.gesturepwd_root);
		mLockPatternView.setOnPatternListener(mChooseNewLockPatternListener);
		mLockPatternView.setTactileFeedbackEnabled(true);
		mHeadTextView = (TextView) pLayoutView.findViewById(R.id.gesturepwd_unlock_text);
		dm = getResources().getDisplayMetrics();

		mForgetPW = (TextView) pLayoutView.findViewById(R.id.gesturepwd_unlock_forget);
		mForgetPW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UnlockGesturePasswordService.this); 
                builder.setCancelable(false);

				final AlertDialog show = builder
						.setTitle(
								UnlockGesturePasswordService.this.getResources().getString(
										R.string.forget_password))
						.setMessage(
								UnlockGesturePasswordService.this.getResources().getString(
										R.string.forget_touch_pw))
						.setPositiveButton(
								UnlockGesturePasswordService.this.getResources().getString(R.string.reloginBtn),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0,
														int arg1) {
                                /*UtilMethod.cleanLogin(UnlockGesturePasswordActivity.this);
                                Intent intent = new Intent();
                                intent.setClass(UnlockGesturePasswordActivity.this, LoginActivity.class);
                                intent.putExtra("returnable", false);
                                UnlockGesturePasswordActivity.this.startActivityForResult(intent, 0);*/
									}
								})
						.setNegativeButton(UnlockGesturePasswordService.this.getResources().getString(R.string.cancelBtn),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0,
														int arg1) {
									}
								}).show();
			}
        });
		mShakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake_x);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		windowManager.addView(pLayoutView, pLayoutParams);
		viewAdded = true;
	}

	public void backClick(View view) {
        this.stopSelf();
        PageChangeUtils.changeLockServiceState(this, LockService.STATE_SHOW);
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
		removeView();
    }
    
	/**
	 * 关闭悬浮窗
	 */
	public void removeView() {
		if (viewAdded) {
			windowManager.removeView(pLayoutView);
			viewAdded = false;
		}
	}
	
	private Runnable mClearPatternRunnable = new Runnable() {
		public void run() {
			mLockPatternView.clearPattern();
		}
	};

    
	protected LockPatternView.OnPatternListener mChooseNewLockPatternListener = new LockPatternView.OnPatternListener() {

		public void onPatternStart() {
			mLockPatternView.removeCallbacks(mClearPatternRunnable);
			patternInProgress();
		}

		public void onPatternCleared() {
			mLockPatternView.removeCallbacks(mClearPatternRunnable);
		}

		public void onPatternDetected(List<LockPatternView.Cell> pattern) {
			if (pattern == null)
				return;
			if (LockApplication.getInstance(UnlockGesturePasswordService.this).getLockPatternUtils().checkPattern(pattern)) {
				mLockPatternView
						.setDisplayMode(LockPatternView.DisplayMode.Correct);
				/*Intent intent = new Intent(UnlockGesturePasswordActivity.this,
						GuideGesturePasswordActivity.class);
				// 打开新的Activity
				startActivity(intent);*/
				UnlockGesturePasswordService.this.stopSelf();
			} else {
				mLockPatternView
						.setDisplayMode(LockPatternView.DisplayMode.Wrong);
				if (pattern.size() >= LockPatternUtils.MIN_PATTERN_REGISTER_FAIL) {
					/*mFailedPatternAttemptsSinceLastTimeout++;
					int retry = LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT
							- mFailedPatternAttemptsSinceLastTimeout;
					if (retry >= 0) {
						if (retry == 0)
							showToast("您已5次输错密码，请30秒后再试");
						mHeadTextView.setText("密码错误，还可以再输入" + retry + "次");
						mHeadTextView.setTextColor(Color.RED);
						mHeadTextView.startAnimation(mShakeAnim);
					}*/

				}else{
					showToast("输入长度不够，请重试");
				}

				if (mFailedPatternAttemptsSinceLastTimeout >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT) {
					//mHandler.postDelayed(attemptLockout, 2000);
				} else {
					mLockPatternView.postDelayed(mClearPatternRunnable, 2000);
				}
			}
		}

		public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {

		}

		private void patternInProgress() {
		}
	};
	Runnable attemptLockout = new Runnable() {

		@Override
		public void run() {
			mLockPatternView.clearPattern();
			mLockPatternView.setEnabled(false);
			mCountdownTimer = new CountDownTimer(
					LockPatternUtils.FAILED_ATTEMPT_TIMEOUT_MS + 1, 1000) {

				@Override
				public void onTick(long millisUntilFinished) {
					int secondsRemaining = (int) (millisUntilFinished / 1000) - 1;
					if (secondsRemaining > 0) {
						mHeadTextView.setText(secondsRemaining + " 秒后重试");
					} else {
						mHeadTextView.setText("请绘制手势密码");
						mHeadTextView.setTextColor(Color.WHITE);
					}

				}

				@Override
				public void onFinish() {
					mLockPatternView.setEnabled(true);
					mFailedPatternAttemptsSinceLastTimeout = 0;
				}
			}.start();
		}
	};

	public void setBackgroudImg(String imgLocalPath, int contentId) {
		if(TextUtils.isEmpty(imgLocalPath)) {
			if(contentId != 0) {
				Bitmap bm = DisplayUtils.getBitmapById(this, contentId);
				float scale = ((float)dm.widthPixels)/bm.getWidth();
				bm = DisplayUtils.small(bm, scale);
				BitmapDrawable blurBMD = DisplayUtils.blur(UnlockGesturePasswordService.this, bm);
				pRootView.setBackgroundDrawable(blurBMD);
			} else {
				pRootView.setBackgroundColor(getResources().getColor(R.color.maintabbg));
			}
		} else {
			Bitmap bm = FileUtils.loadResizedBitmap(this, imgLocalPath, dm.widthPixels, dm.heightPixels, true);
			BitmapDrawable blurBMD = DisplayUtils.blur(UnlockGesturePasswordService.this, bm);
			pRootView.setBackgroundDrawable(blurBMD);
		}
	}
	@Override
	public IBinder onBind(Intent intent) {
    	windowManager.addView(pLayoutView, pLayoutParams);
		viewAdded = true;
		return myBinder;
	}
	
	 private MyBinder myBinder = new MyBinder();  
	 
	 public class MyBinder extends Binder {  
         
	        public UnlockGesturePasswordService getService1(){  
	            return UnlockGesturePasswordService.this;  
	        }  
	    }  

}
