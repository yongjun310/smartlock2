package com.smart.lock.pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smart.lock.R;
import com.smart.lock.activity.BaseActivity;
import com.smart.lock.global.LockApplication;
import com.smart.lock.utils.DisplayUtils;
import com.smart.lock.utils.LockPatternUtils;
import com.smart.lock.view.LockPatternView;

import java.util.List;

public class EnsureGesturePasswordActivity extends BaseActivity {
	private LockPatternView mLockPatternView;
	private int mFailedPatternAttemptsSinceLastTimeout = 0;
	private CountDownTimer mCountdownTimer = null;
	private Handler mHandler = new Handler();
	private TextView mHeadTextView;
	private TextView mForgetPW;
	private Animation mShakeAnim;

	private LinearLayout rootView;

	private Toast mToast;

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
		setContentView(R.layout.gesturepassword_ensure);
		DisplayUtils.setTitleAndBackBtn(this, "设置手势密码");
		mLockPatternView = (LockPatternView) this
				.findViewById(R.id.gesturepwd_unlock_lockview);
		rootView = (LinearLayout)findViewById(R.id.gesturepwd_root);
		mLockPatternView.setOnPatternListener(mChooseNewLockPatternListener);
		mLockPatternView.setTactileFeedbackEnabled(true);
		mHeadTextView = (TextView) findViewById(R.id.gesturepwd_create_text);
		DisplayMetrics dm = getResources().getDisplayMetrics();
		mHeadTextView.setTextColor(getResources().getColor(R.color.contentText));
		rootView.setBackgroundColor(getResources().getColor(R.color.maintabbg));

		mForgetPW = (TextView) findViewById(R.id.gesturepwd_unlock_forget);
		mForgetPW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EnsureGesturePasswordActivity.this);
                builder.setCancelable(false);

				final AlertDialog show = builder
						.setTitle(
								EnsureGesturePasswordActivity.this.getResources().getString(
										R.string.forget_password))
						.setMessage(
								EnsureGesturePasswordActivity.this.getResources().getString(
										R.string.forget_touch_pw))
						.setPositiveButton(
								EnsureGesturePasswordActivity.this.getResources().getString(R.string.reloginBtn),
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
						.setNegativeButton(EnsureGesturePasswordActivity.this.getResources().getString(R.string.cancelBtn),
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
		Intent it = new Intent();
		setResult(Activity.RESULT_CANCELED, it);
        this.finish();
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
			if (LockApplication.getInstance(EnsureGesturePasswordActivity.this).getLockPatternUtils().checkPattern(pattern)) {
				mLockPatternView
						.setDisplayMode(LockPatternView.DisplayMode.Correct);
				/*Intent intent = new Intent(UnlockGesturePasswordActivity.this,
						GuideGesturePasswordActivity.class);
				// 打开新的Activity
				startActivity(intent);*/
				Intent it = new Intent();
				setResult(Activity.RESULT_OK, it);
				EnsureGesturePasswordActivity.this.finish();
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
