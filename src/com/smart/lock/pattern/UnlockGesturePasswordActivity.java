package com.smart.lock.pattern;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smart.lock.R;
import com.smart.lock.activity.BaseActivity;
import com.smart.lock.global.LockApplication;
import com.smart.lock.service.LockService;
import com.smart.lock.utils.DisplayUtils;
import com.smart.lock.utils.FileUtils;
import com.smart.lock.utils.LockPatternUtils;
import com.smart.lock.utils.PageChangeUtils;
import com.smart.lock.utils.SysApplication;
import com.smart.lock.view.LockLayer;
import com.smart.lock.view.LockPatternView;

public class UnlockGesturePasswordActivity extends BaseActivity {
	private LockPatternView mLockPatternView;
	private int mFailedPatternAttemptsSinceLastTimeout = 0;
	private CountDownTimer mCountdownTimer = null;
	private Handler mHandler = new Handler();
	private TextView mHeadTextView;
	private TextView mForgetPW;
	private Animation mShakeAnim;

	private FrameLayout rootView;

	private Toast mToast;

	private LockLayer lockLayer;

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
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gesturepassword_unlock);
		String imgLocalPath = this.getIntent().getStringExtra("imgLocalPath");
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		lockLayer = new LockLayer();
		lockLayer.lock(this);

		SysApplication.getInstance().addActivity(this);
		mLockPatternView = (LockPatternView) this
				.findViewById(R.id.gesturepwd_unlock_lockview);
		rootView = (FrameLayout)findViewById(R.id.gesturepwd_root);
		mLockPatternView.setOnPatternListener(mChooseNewLockPatternListener);
		mLockPatternView.setTactileFeedbackEnabled(true);
		mHeadTextView = (TextView) findViewById(R.id.gesturepwd_unlock_text);
		DisplayMetrics dm = getResources().getDisplayMetrics();
		if(TextUtils.isEmpty(imgLocalPath)) {

			int contentId = this.getIntent().getIntExtra("contentId", 0);
			if(contentId != 0) {
				Bitmap bm = DisplayUtils.getBitmapById(this, contentId);
				float scale = ((float)dm.widthPixels)/bm.getWidth();
				bm = DisplayUtils.small(bm, scale);
				BitmapDrawable blurBMD = DisplayUtils.blur(UnlockGesturePasswordActivity.this, bm);
				rootView.setBackgroundDrawable(blurBMD);
			} else {
				rootView.setBackgroundColor(getResources().getColor(R.color.maintabbg));
			}
		} else {
			Bitmap bm = FileUtils.loadResizedBitmap(this, imgLocalPath, dm.widthPixels, dm.heightPixels, true);
			BitmapDrawable blurBMD = DisplayUtils.blur(UnlockGesturePasswordActivity.this, bm);
			rootView.setBackgroundDrawable(blurBMD);
		}

		mForgetPW = (TextView) findViewById(R.id.gesturepwd_unlock_forget);
		mForgetPW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UnlockGesturePasswordActivity.this); 
                builder.setCancelable(false);

				final AlertDialog show = builder
						.setTitle(
								UnlockGesturePasswordActivity.this.getResources().getString(
										R.string.forget_password))
						.setMessage(
								UnlockGesturePasswordActivity.this.getResources().getString(
										R.string.forget_touch_pw))
						.setPositiveButton(
								UnlockGesturePasswordActivity.this.getResources().getString(R.string.reloginBtn),
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
						.setNegativeButton(UnlockGesturePasswordActivity.this.getResources().getString(R.string.cancelBtn),
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
    public void onResume() {
		super.onResume();

		if (!LockApplication.getInstance(this).getLockPatternUtils().savedPatternExists()) {
			startActivity(new Intent(this, CreateGesturePasswordActivity.class));
			finish();
		}
	}

	public void backClick(View view) {
		onBackPressed();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mCountdownTimer != null)
			mCountdownTimer.cancel();
		if(lockLayer!=null) {
			lockLayer.unlock();
			lockLayer = null;
		}
	}
	private Runnable mClearPatternRunnable = new Runnable() {
		public void run() {
			mLockPatternView.clearPattern();
		}
	};

	/**
     * 描述：返回.
     *
     * @see android.support.v4.app.FragmentActivity#onBackPressed()
     */
    @Override
    public void onBackPressed() {
        this.finish();
        PageChangeUtils.changeLockServiceState(this, LockService.STATE_SHOW);
    }
    
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
			if (LockApplication.getInstance(UnlockGesturePasswordActivity.this).getLockPatternUtils().checkPattern(pattern)) {
				mLockPatternView
						.setDisplayMode(LockPatternView.DisplayMode.Correct);
				/*Intent intent = new Intent(UnlockGesturePasswordActivity.this,
						GuideGesturePasswordActivity.class);
				// 打开新的Activity
				startActivity(intent);*/
				Intent it = new Intent();
				setResult(Activity.RESULT_OK, it);
				UnlockGesturePasswordActivity.this.finish();
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

}
