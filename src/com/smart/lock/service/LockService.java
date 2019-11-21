package com.smart.lock.service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smart.lock.R;
import com.smart.lock.activity.MainActivity;
import com.smart.lock.activity.ShareActivity;
import com.smart.lock.activity.SplashActivity;
import com.smart.lock.adapter.LoopImageViewPagerAdapter;
import com.smart.lock.common.ContentTypeEnum;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.dto.AccountActionLogDTO;
import com.smart.lock.dto.ContentDTO;
import com.smart.lock.global.LockApplication;
import com.smart.lock.utils.ActionLogOperator;
import com.smart.lock.utils.ContentsDataOperator;
import com.smart.lock.utils.DaemUtil;
import com.smart.lock.utils.DeviceUtils;
import com.smart.lock.utils.DisplayUtils;
import com.smart.lock.utils.FileUtils;
import com.smart.lock.utils.LockPatternUtils;
import com.smart.lock.utils.NetUtils;
import com.smart.lock.utils.PageChangeUtils;
import com.smart.lock.utils.PollingUtils;
import com.smart.lock.utils.SharedPreferencesUtils;
import com.smart.lock.utils.SysApplication;
import com.smart.lock.utils.ThreadHelper;
import com.smart.lock.view.LockPatternView;
import com.smart.lock.view.ShimmerFrameLayout;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;

public class LockService extends NotificationListenerService implements OnTouchListener, OnPageChangeListener {
    private static String TAG = "LockService";
    private ViewPager viewPager;
    private LoopImageViewPagerAdapter adapter;
    //显示锁屏页
    public static final int STATE_SHOW=0x123;

    //隐藏锁屏页
    public static final int STATE_HIDE=0x125;

    //数据更新
    public static final int STATE_UPDATE_DATE=0x127;

    //screen on
    public static final int STATE_SCREEN_ON=0x129;

    private int lastUnLockY, lastUnLockX;

    private int currentPosition;

    private int accountId;

    private String mobileNo, lastRightSlideIncomeTime;

    private int nowSec;

    private boolean viewAdded = false, pViewAdded = false;// 透明窗体是否已经显示

    private int currentContentId;

    private View lockTab;

    private LinearLayout mainTab;
    
    private Activity webViewActivity;

    private int screenWidth;
    private int screenHeight;

    private View rootView;

    private ContentDTO content = null;

    BlurImageView blurTask;

    private boolean isBlock = false, isFavorite = false, isEnter;

    private TextView txtTime, txtDay, txtDate, txtTitle, blurText, txtUnlock, txtContent, txtWallPaper, txtLike, txtShare, txtDetail;

    private ShimmerFrameLayout shimLayout;

    private ImageView imgBlock, imgFavorite, curImageView, sharePrize, arraw;

    private View detailLayout, favoriteLayout, titleLayout, bottomLayout, dateTimeTitle, touchLayout;

    private Bitmap oldBlurBM;

    private int lockTabTop;

    /**
     * 分享标题
     */
    private String shareTitle = "遇见锁屏，一秒点亮你的视线";

    private Vibrator vibrator;

    /**
     * 分享图标
     */
    private String shareImgUrl = "";

    Timer timer = new Timer();

    /**
     * 分享详细描述
     */
    private String descContent = "遇见锁屏，一秒点亮你的视线";

    public static final String DESCRIPTOR = "com.umeng.share";

    public static final String APPID = "wx5b95be98c4f0642f";

    public static final String WXAPPSEC = "257f59414b948e4110e363a914e159f9";

    public int maintabTop, maintabBottom, bottomLeft, bottomRight,
            bottomBottom, bottomTop, maintabHeight, dateTimeTitleTop, dateTimeTitleBottom, touchTop, touchBottom, touchLeft, touchRight;

    private final UMSocialService mController = UMServiceFactory
            .getUMSocialService(DESCRIPTOR);

    private AnimationDrawable animUpArrow;

    /** 隐藏content*/
    private boolean isFold = true;

    private String preActivity = null;

    private View layoutView;

    private WindowManager windowManager;

    private WindowManager.LayoutParams layoutParams;

    private List<ContentDTO> mContents = new ArrayList<ContentDTO>();

    private LockPatternView mLockPatternView;
    private int mFailedPatternAttemptsSinceLastTimeout = 0;
    private CountDownTimer mCountdownTimer = null;
    private Handler mHandler = new Handler();
    private TextView mHeadTextView;
    private TextView mForgetPW;
    private Animation mShakeAnim;

    private FrameLayout pRootView;

    private Toast mToast;

    private View pLayoutView;

    private DisplayMetrics dm;

    private TextView toastTextView;
    
    private LockServiceReceiver receiver;

    private boolean receiveCallHide = false;

    private boolean fromLock = false;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, TAG + "onCreate");
        receiver=new LockServiceReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(SlideConstants.LOCKSERVICE_ACTION);
        filter.addAction("android.intent.action.TIME_TICK");
        filter.addAction("android.intent.action.TIME_SET");
        filter.addAction("android.intent.action.TIMEZONE_CHANGED");
        registerReceiver(receiver, filter);

        /*if (!PollingUtils.isServiceRunning(LockService.this,
                HeartBeatService.class.getName())) {
            PollingUtils.startPollingService(LockService.this,
                    SlideConstants.SEVICE_HEARTBEAT_SPAN,
                    HeartBeatService.class,
                    SlideConstants.HEARTBEAT_SERVICE_NAME);
        }*/
    	//startService(new Intent(this, HeartBeatService.class));
        pOnCreate();
        //overridePendingTransition(R.anim.anim_enter_lock, R.anim.anim_exit_lock);
        
        FileUtils.addFileLog("enter LockActivity :");
        FileUtils.addFileLog(SysApplication.getInstance().printActivityList());

        // 对电话的来电状态进行监听
        TelephonyManager telManager = (TelephonyManager) this
                .getSystemService(Context.TELEPHONY_SERVICE);
        // 注册一个监听器对电话状态进行监听
        telManager.listen(new MyPhoneStateListener(),
                PhoneStateListener.LISTEN_CALL_STATE);


        TimerTask task = new TimerTask() {
            @Override
            public void run() {
            	 PageChangeUtils.changeLockServiceState(LockService.this, STATE_UPDATE_DATE);
            }
        };
        timer.schedule(task, 20 * 60 * 1000, 1 * 60 * 1000);
        
        windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
		/*
		 * LayoutParams.TYPE_SYSTEM_ERROR：保证该悬浮窗所有View的最上层
		 * LayoutParams.FLAG_NOT_FOCUSABLE:该浮动窗不会获得焦点，但可以获得拖动
		 * PixelFormat.TRANSPARENT：悬浮窗透明
		 */
        layoutParams = new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT, LayoutParams.TYPE_SYSTEM_ERROR,
                LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
        layoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                1073741824;
        SharedPreferences commonSP = getSharedPreferences("common", 0);

        accountId = commonSP.getInt("accountId", -1);
        mobileNo = commonSP.getString("mobileNo", "");
        lastRightSlideIncomeTime = commonSP.getString(
                "lastRightSlideIncomeTime", "1900-01-01 00:00:00");
        initLayoutView();
        
        preActivity =  SharedPreferencesUtils.getStringSP(LockService.this, "firstLockActivity", null);
        
        updateDBData();

        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        DisplayUtils.setFontRoboto(this, txtTime);
        DisplayUtils.setFont(this, txtDate);
        DisplayUtils.setFont(this, txtTitle);
        DisplayUtils.setFont(this, txtWallPaper);
        DisplayUtils.setFont(this, txtShare);
        DisplayUtils.setFont(this, txtLike);
        DisplayUtils.setFont(this, txtDetail);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR_OF_DAY, -1);
        Date lastTime = Calendar.getInstance().getTime();
        try {
            lastTime = DisplayUtils.parseDatetime(lastRightSlideIncomeTime);
//            if (lastTime.before(c.getTime())) {
//                txtRightAmount.setText("￥" + SlideConstants.UNLOCK_AMOUNT);
//            } else {
//                txtRightAmount.setText("");
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


	private void initLayoutView() {
		layoutView = LayoutInflater.from(this).inflate(R.layout.service_lock, null);
        rootView = (View) layoutView.findViewById(R.id.layout_lockActivity);
        txtTime = (TextView) layoutView.findViewById(R.id.txt_time);
        txtDay = (TextView) layoutView.findViewById(R.id.txt_day);
        txtDate = (TextView) layoutView.findViewById(R.id.txt_date);
        lockTab = (View) layoutView.findViewById(R.id.layout_lockTab);

        bottomLayout = (View) layoutView.findViewById(R.id.layout_buttom);

        sharePrize = (ImageView) layoutView.findViewById(R.id.img_share_prize);
        mainTab = (LinearLayout) layoutView.findViewById(R.id.layout_mainTab);
        imgFavorite = (ImageView) layoutView.findViewById(R.id.img_mainTab_like);
        dateTimeTitle = (View) layoutView.findViewById(R.id.datatime_title);

        txtWallPaper = (TextView) layoutView.findViewById(R.id.txt_mainTab_wallpaper);
        txtShare = (TextView) layoutView.findViewById(R.id.txt_mainTab_share);
        txtDetail = (TextView) layoutView.findViewById(R.id.txt_mainTab_detail);
        txtLike = (TextView) layoutView.findViewById(R.id.txt_mainTab_like);
        txtContent = (TextView) layoutView.findViewById(R.id.txt_content);
        arraw = (ImageView) layoutView.findViewById(R.id.up_arraw);
        toastTextView = (TextView) layoutView.findViewById(R.id.toast_text);

        DisplayUtils.enlargeClickArea(arraw, 30);
        arraw.setBackgroundResource(R.anim.lock_anim_up);
        animUpArrow = (AnimationDrawable) arraw.getBackground();
        blurText = (TextView) layoutView.findViewById(R.id.text);
        detailLayout = (View) layoutView.findViewById(R.id.layout_mainTab_detail);
        favoriteLayout = (View) layoutView.findViewById(R.id.layout_mainTab_like);
        titleLayout = (View) layoutView.findViewById(R.id.title_layout);
        touchLayout = (View) layoutView.findViewById(R.id.layout_touchTab);
        lockTab.setOnTouchListener(this);
        viewPager = (ViewPager) layoutView.findViewById(R.id.viewPager);

        adapter = new LoopImageViewPagerAdapter(mContents, this);
        viewPager.setAdapter(adapter);

        shimLayout = (ShimmerFrameLayout) layoutView.findViewById(R.id.shimmer_view_container);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(DeviceUtils.isNewOS())
                    shimLayout.startShimmerAnimation();
                animUpArrow.start();
            }
        };
        messageHandler.postDelayed(runnable, 3000);
        txtUnlock = (TextView) layoutView.findViewById(R.id.txt_unlock);
	}


    private void showToast(CharSequence message) {
        toastTextView.setText(message);
        toastTextView.setVisibility(View.VISIBLE);
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                toastTextView.setVisibility(View.INVISIBLE);
            }
        }, 1500);
		/*if (null == mToast) {
			mToast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
			mToast.setGravity(Gravity.CENTER, 0, 0);
		} else {
			mToast.setText(message);
		}

		mToast.show();*/
    }

    private Runnable mClearPatternRunnable = new Runnable() {
        public void run() {
            mLockPatternView.clearPattern();
        }
    };

    public void setBackgroudImg(String imgLocalPath, int contentId) {
        if(TextUtils.isEmpty(imgLocalPath)) {
            if(contentId != 0) {
                Bitmap bm = DisplayUtils.getBitmapById(this, contentId);
                float scale = ((float)dm.widthPixels)/bm.getWidth();
                bm = DisplayUtils.small(bm, scale);
                BitmapDrawable blurBMD = DisplayUtils.blur(LockService.this, bm);
                pRootView.setBackgroundDrawable(blurBMD);
            } else {
                pRootView.setBackgroundColor(getResources().getColor(R.color.maintabbg));
            }
        } else {
            Bitmap bm = FileUtils.loadResizedBitmap(this, imgLocalPath, dm.widthPixels, dm.heightPixels, true);
            BitmapDrawable blurBMD = DisplayUtils.blur(LockService.this, bm);
            pRootView.setBackgroundDrawable(blurBMD);
        }
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
            if (LockApplication.getInstance(LockService.this).getLockPatternUtils().checkPattern(pattern)) {
                mLockPatternView
                        .setDisplayMode(LockPatternView.DisplayMode.Correct);
				/*Intent intent = new Intent(UnlockGesturePasswordActivity.this,
						GuideGesturePasswordActivity.class);
				// 打开新的Activity
				startActivity(intent);*/
	        	DisplayUtils.showNavBar(layoutView);
	        	DisplayUtils.showNavBar(pLayoutView);
                removePView();
                removeView();
                mLockPatternView.clearPattern();
                //LockService.this.stopSelf();
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
                    //showToast("输入长度不够，请重试");
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
    

    @SuppressLint({"NewApi"})
    public void onNotificationPosted(StatusBarNotification paramStatusBarNotification)
   {
  		Log.d("onNotificationPosted", "onReceicer onNotificationPosted:");
  		PollingUtils.startAppReceive(this);
  		if (!PollingUtils.isServiceRunning(getApplicationContext(),
  				LockService.class.getName())) {
  			startService(new Intent(this, LockService.class));
  		}
   }

    public void onNotificationRemoved(StatusBarNotification paramStatusBarNotification)
    {
  	  PollingUtils.startAppReceive(this);
  		if (!PollingUtils.isServiceRunning(getApplicationContext(),
  				LockService.class.getName())) {
  			startService(new Intent(this, LockService.class));
  		}
    }

    private void pOnCreate() {
        pLayoutView = LayoutInflater.from(this).inflate(R.layout.gesturepassword_unlock, null);
        /*if (!PollingUtils.isServiceRunning(LockService.this,
                HeartBeatService.class.getName())) {
            PollingUtils.startPollingService(LockService.this,
                    SlideConstants.SEVICE_HEARTBEAT_SPAN,
                    HeartBeatService.class,
                    SlideConstants.HEARTBEAT_SERVICE_NAME);
        }*/

        FileUtils.addFileLog("enter LockActivity :");
        FileUtils.addFileLog(SysApplication.getInstance().printActivityList());

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
                AlertDialog.Builder builder = new AlertDialog.Builder(LockService.this);
                builder.setCancelable(false);

                final AlertDialog show = builder
                        .setTitle(
                                LockService.this.getResources().getString(
                                        R.string.forget_password))
                        .setMessage(
                                LockService.this.getResources().getString(
                                        R.string.forget_touch_pw))
                        .setPositiveButton(
                                LockService.this.getResources().getString(R.string.reloginBtn),
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
                        .setNegativeButton(LockService.this.getResources().getString(R.string.cancelBtn),
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

    private Handler messageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    initTime();
                    break;
                case 2:
                    ImageView imageView = adapter.getView(currentPosition);
                    int contentId = imageView.getId();
                    Bitmap bitmap = adapter.getBms().get(contentId);
                    String fileName = "mshow" + contentId + ".jpg";
                    /*Toast.makeText(getApplicationContext(), SlideConstants.DOWNLOAD_WALLPAPER_SUCCESS + fileName,
                            Toast.LENGTH_SHORT).show();*/
                    showToast(SlideConstants.DOWNLOAD_WALLPAPER_SUCCESS + fileName);
                    break;
                case 3:
                    adapter.notifyDataSetChanged();

//                    adapter = new LoopImageViewPagerAdapter(mContents, getApplicationContext());
//                    viewPager.setAdapter(adapter);

                    break;
                default:
                    break;
            }
        }
    };

    public Handler getMessageHandler() {
        return messageHandler;
    }

    public void setMessageHandler(Handler messageHandler) {
        this.messageHandler = messageHandler;
    }


    private void applyBlur(final ImageView image) {
        blurText.setVisibility(View.INVISIBLE);
        image.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                image.getViewTreeObserver().removeOnPreDrawListener(this);
                image.buildDrawingCache();

                Bitmap bmp = image.getDrawingCache();
                blurTask = new BlurImageView(currentPosition);
                blurTask.execute(bmp);
                return true;
            }
        });
    }

    class BlurImageView extends AsyncTask<Bitmap, Integer, BitmapDrawable> {
        int imageViewIndex = 0;


        public int getImageViewIndex() {
            return imageViewIndex;
        }

        public BlurImageView(int imageViewIndex) {
            super();
            this.imageViewIndex = imageViewIndex;
        }

        @Override
        protected BitmapDrawable doInBackground(Bitmap... arg0) {
            // TODO Auto-generated method stub
            return DisplayUtils.blur(LockService.this.getApplication(), arg0[0]);
        }

        @Override
        protected void onPostExecute(BitmapDrawable result) {
            if (result != null && imageViewIndex == currentPosition)
                blurText.setBackgroundDrawable(result);
            blurText.setId(imageViewIndex);
        }
    }

    private void startTimeRefresh() {
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                Message msg = Message.obtain(messageHandler, 1);
                messageHandler.sendMessage(msg);
            }
        };
        timer.schedule(task, (60 - nowSec) * 1000, 60 * 1000);
    }

    private void initTouchTab() {
        ImageView view = adapter.getView(currentPosition);
        int contentId = view.getId();
        layoutView.findViewById(R.id.img_mainTab_like).setEnabled(contentId > 0);
        layoutView.findViewById(R.id.img_mainTab_detail).setEnabled(contentId > 0);
        layoutView.findViewById(R.id.img_mainTab_share).setEnabled(true);
        layoutView.findViewById(R.id.img_mainTab_wallpaper).setEnabled(true);
        if (contentId > 0) {
            favoriteLayout.setEnabled(true);
            txtLike.setTextColor(this.getResources().getColor(R.color.white));
            ContentDTO content = ContentsDataOperator.getById(this, contentId);
            if (content != null) {
                layoutView.findViewById(R.id.img_mainTab_detail).setEnabled(content.getHasMore()==1);
                if(content.getHasMore()==1)
                    txtDetail.setTextColor(this.getResources().getColor(R.color.white));
                else
                    txtDetail.setTextColor(this.getResources().getColor(R.color.unenabletextcolor));

                detailLayout.setEnabled(content.getHasMore()==1);
                if (content.getHasMore()==1 && content.getType() == ContentTypeEnum.SHARE.getValue()) {
                    if (content.getBonusAmount().compareTo(new BigDecimal(0)) > 0) {
                        sharePrize.setVisibility(View.VISIBLE);
                    } else {
                        sharePrize.setVisibility(View.INVISIBLE);
                    }
                }
//                if(content.getId() == 231 || content.getId() == 266) {
//                    adapter.getContents().remove(content);
//                    ContentsDataOperator.deleteById(this, content.getId());
//                }
            } else {
                dislikeContent(view);
            }
        } else {
            List<ContentDTO> contentDTOList = adapter.getContents();
            /*if (contentId == -5) {
                for(int i=0; i<contentDTOList.size(); i++) {
                    ContentDTO contentDTO = contentDTOList.get(i);
                    if(contentDTO.getId() == contentId) {
                        contentDTOList.remove(contentDTO);
                    }
                }
            }*/
            txtLike.setTextColor(this.getResources().getColor(R.color.unenabletextcolor));
            txtDetail.setTextColor(this.getResources().getColor(R.color.unenabletextcolor));
            detailLayout.setEnabled(false);
            imgFavorite.setImageResource(R.drawable.icon_favorite_un);
        }
        initFavoriteTab();

    }

    private void initFavoriteTab() {
        ImageView view = adapter.getView(currentPosition);
        txtTitle = (TextView) layoutView.findViewById(R.id.txt_content_Title);
        currentContentId = view.getId();

        if (view.getId() > 0) {
            ContentDTO contentItem = ContentsDataOperator.getById(this,
                    view.getId());
            if (contentItem != null) {
                if (contentItem.isFavorite()) {
                    imgFavorite.setImageResource(R.drawable.lock_favorite_actived);
                } else {
                    imgFavorite.setImageResource(R.drawable.lock_favorite);
                }
                String title = DisplayUtils.getSubTitle(contentItem.getTitle(), 20);
                txtTitle.setText(title);
                txtContent.setText(contentItem.getContent());
                if(TextUtils.isEmpty(contentItem.getContent())) {
                    txtContent.setVisibility(View.GONE);
                }
            }

            titleLayout.setVisibility(View.VISIBLE);
        } else {
            imgFavorite.setImageResource(R.drawable.icon_favorite_un);
            String strTitle = "", strContent = "";
            switch (view.getId()) {

                case -12:
                    strTitle = "这一刻，就这么一直待着";
                    strContent = "说山也高林也密月亮都怵，说进不去出不来风都糊涂，我知道这一天无法记住，因为思念的缘故。 -- 顾城";
                    break;
                case -11:
                    strTitle = "你的名字就够我爱一世了";
                    strContent = "Everything I do, I just want to be the best for you. 我做的每一件事，都是为了让你觉得我是最好的。";
                    break;
                case -10:
                    strTitle = "如果忘了，就不重要";
                    strContent = "谁不虚伪，谁不善变，到最后谁都不是谁的谁，又何必高估了自己，把一些人，一些事看得那那么重要。";
                    break;
                case -9:
                    strTitle = "我们的小缺点让我们找到对的人";
                    strContent = "喜欢一个人只喜欢她的好，爱一个人就是连她的缺点都喜欢。";
                    break;
                case -8:
                    strTitle = "没有回味是遗憾的事";
                    strContent = "我们遗憾的并不是错过了最好的人，而是遇到再好的人，却已经把最好的自己用完了。by 张晓晗";
                    break;
                case -7:
                    strTitle = "生活总会给你答案";
                    strContent = "生活总会给你答案，但不会马上把一切都告诉你。by 马德";
                    break;
                case -6:
                    strTitle = "我还年轻，我渴望发现";
                    strContent = "年轻时，觉得自己的人生是一场花开不败的演出，到最后只想围着红泥小炉煮一杯茶翻几页书。";
                    break;
                case -5:
                    strTitle = "一生只够爱一个人";
                    strContent = "爱情总是让人充满幻想，总以为可以百毒不侵。甜言蜜语像是锋利的刀子穿入心脏的快感，当爱情离开时你也体会它穿心的疼痛。";
                    break;

                case -4:
                    strTitle = "我知道，以后我会被纪念";
                    strContent = "思念，在秋天里落叶，在冬雪里凋谢，在春雨里流连。梦想在晨昏里浮现，在日月里辗转，在季节里更迭。告别，是另一种体验。";
                    break;
                case -3:
                    strTitle = "清新的早上空气像块冰淇淋";
                    strContent = "一地落叶的沧桑，秋高气爽的天气，秋风吹来的一丝丝凉意，总会让你回忆些什么。然后不知觉间，冬天来了，雪花像梦一般地缤纷";
                    break;
                case -2:
                    strTitle = "你来人间一趟，你要看看太阳";
                    strContent = "在时光的轮回处，在岁月的尘埃里，我带着我的热情，我的冷漠，我的狂暴，我的温和，以及对爱情毫无理由的相信，于千万人之中，走向你。";
                    break;
                case -1:
                    strTitle = "我曾经跨过山和大海";
                    strContent = "花下月成海，月上花梢前。谁见斯人起徘徊，风动山林待君来。待君来，此夜阑，共醉花下眠。";
                    break;

                default:
                    break;
            }
            txtTitle.setText(strTitle);
            txtContent.setText(strContent);
        }

    }

    private void initTime() {
        Calendar calendar = Calendar.getInstance();
        int nowHour = calendar.get(Calendar.HOUR_OF_DAY);
        int nowMinute = calendar.get(Calendar.MINUTE);
        nowSec = calendar.get(Calendar.SECOND);
        txtTime.setText((nowHour < 10 ? ("0" + nowHour) : nowHour) + ":"
                + (nowMinute < 10 ? ("0" + nowMinute) : nowMinute));
        DisplayUtils.setDisplayDateAndDay(txtDate, txtDay, null);
    }

    private synchronized void initPager() {
        //updateDBDate();
        adapter.setContents(mContents);
        viewPager.setCurrentItem(currentPosition);
        viewPager.setOnPageChangeListener(this);
        adapter.incViewCount(accountId, currentPosition);
        adapter.notifyDataSetChanged();
    }

	private void checkContents() {
		boolean hasRefresh = false;
    	for(int i=0; i<mContents.size(); i++) {
    		ContentDTO content = mContents.get(i);
    		if(!checkImgExistence(content)) {
    			mContents.remove(i);
    			hasRefresh = true;
    		}
    	}
    	if(hasRefresh) {
    		ThreadHelper.getInstance(this).addTask(SlideConstants.THREAD_DOWNLOAD_PIC);
    	}
    	if(mContents.size() < SlideConstants.TOTAL_DEFAULT_PAGER_SIZE) {
            addDefaultPic();
    	}
	}

    private boolean isRunning = false;
    private synchronized void updateDBData() {
		Log.d(TAG, "enter updateDBData");
        mContents.clear();
        preActivity =  SharedPreferencesUtils.getStringSP(LockService.this, "firstLockActivity", null);
        if (!"SettingWizardActivity".equals(preActivity))  {
	        long startTime = System.currentTimeMillis();

	        String serverTime = SharedPreferencesUtils.getServerTimeStr(LockService.this);
	        String sql = "SELECT * FROM content WHERE date(startTime) = date('" + serverTime + "') "
	                + " ORDER BY startTime DESC";
	        String lsql = "SELECT * FROM content WHERE isFavorite = 1 and date(startTime) <> date('" + serverTime + "') " + "ORDER BY startTime DESC";
	
	        List<ContentDTO> contentList = ContentsDataOperator.load(LockService.this, sql);
	        List<ContentDTO> lcontentList = ContentsDataOperator.load(LockService.this, lsql);
	
	        DisplayUtils.resortContent(contentList);
	        if(lcontentList.size() > 10) {
	            int count = 0;
	            while(count++<10) {
	
	                int index = (int)(Math.random() * lcontentList.size());
	                contentList.add(lcontentList.get(index));
	                lcontentList.remove(index);
	            }
	        } else {
	            contentList.addAll(lcontentList);
	        }
	        if (contentList.size() > 0) {
	            int i = 0;
	            while (i < contentList.size()) {// && contentList.get(i).getPriority() > SlideConstants.DEFAULT_CONTENT_PRIORITY) {
	                ContentDTO content = contentList.get(i);
	                contentList.remove(i);
	                addContentForDisplay(mContents, content);
	                i++;
	            }
	
	            for (int j = 0; j < contentList.size(); j++) {
	                ContentDTO content = contentList.get(j);
	                if (content.getId() == 231 || content.getId() == 266) {
	                    addContentForDisplay(mContents, content);
	                    contentList.remove(j);
	                }
	            }
	            while (!contentList.isEmpty()) {
	                if (mContents.size() > SlideConstants.VIEW_PAGER_SIZE)
	                    break;
	                /*int randomContentPos = (int) (Math.random() * contentList
	                        .size());*/
	                ContentDTO content = contentList.get(0);
	                contentList.remove(0);
	
	                addContentForDisplay(mContents, content);
	            }
	        }
        }
        addDefaultPic();
        setCurPosAndAdapter();
    }

    private void setCurPosAndAdapter() {

        currentPosition = mContents.size() * 1000;


        currentPosition = SharedPreferencesUtils.getIntSP(LockService.this, SharedPreferencesUtils.CURRENT_LOCK_IMGPOS, currentPosition);
        if (currentPosition < mContents.size()) {
            currentPosition += 1000 * mContents.size();
        }
        if(adapter != null) {
            adapter.setContents(mContents);
            messageHandler.sendMessage(Message.obtain(messageHandler, 3));
        }
    }


    private void addDefaultPic() {
        if (mContents.size() < SlideConstants.TOTAL_DEFAULT_PAGER_SIZE) {
            int maxAddImages = SlideConstants.TOTAL_DEFAULT_PAGER_SIZE;
            for (int i = -1; i >= -SlideConstants.TOTAL_DEFAULT_PAGER_SIZE; i--) {
                if (mContents.size() >= maxAddImages)
                    break;
                boolean exist = false;
                for(int j=0; j<mContents.size(); j++) {
                	if(mContents.get(j).getId() == i) {
                		exist = true;
                	}
                }
                if(exist) 
                	continue;
                ImageView item = new ImageView(LockService.this);
                int res = 0;
                ContentDTO content = new ContentDTO(i);
                item.setId(i);
                item.setTag(false);
                //item.setBackgroundDrawable(new BitmapDrawable(DisplayUtils.readBitMap(this, res)));
                mContents.add(content);
            }
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        //gotoLockView();
		PollingUtils.startAppReceive(this);
		if (!PollingUtils.isServiceRunning(getApplicationContext(),
				DataService.class.getName())) {
			startService(new Intent(SlideConstants.DATA_SERVICE_NAME));
		}
        DisplayUtils.hideNavBar(layoutView);
        DisplayUtils.hideNavBar(pLayoutView);
        layoutView.setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int i) {
                        DisplayUtils.hideNavBar(layoutView);
                    }
                });
        pLayoutView.setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int i) {
                        DisplayUtils.hideNavBar(pLayoutView);
                    }
                });
        
    }



    public void updateLockData() {
        Log.d(TAG, TAG + "onStart");
        preActivity =  SharedPreferencesUtils.getStringSP(this, "firstLockActivity", null);
        initPager();
        initFavoriteTab();

        initTouchTab();
        initTime();
    }
    private void addContentForDisplay(List<ContentDTO> mContents, ContentDTO content) {
        if (content == null)
            return;
        ImageView item = new ImageView(this);
        item.setId(content.getId());
        item.setTag(true);
        if ("preLoad".equals(content.getLocalPath())) {
            mContents.add(content);
        } else {
            if(checkImgExistence(content)) {
			    try {
			        mContents.add(content);
			    } catch (Exception e) {
			        Log.e("lock open pic:", e.toString());
			    }
            }
        }
    }


	private boolean checkImgExistence(ContentDTO content) {
		if(!TextUtils.isEmpty(content.getLocalPath())) {
			int fileSize = FileUtils.getFileSize(content
			        .getLocalPath());
			if (fileSize > 1024) {
			    return true;
			} else {
				content.setLocalPath("");
				ContentsDataOperator.updateWithoutNoti(LockService.this, content);
			}
		}
		return false;
	}

    int isSlideUp = 0;

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int dy = (int) event.getRawY() - lastUnLockY;
        int dx = (int) event.getRawX() - lastUnLockX;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setTouchDownValue(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (isSlideUp == 0 && Math.pow(dy, 2) + Math.pow(dx, 2) > 10 && (dx > 0 || dy < 0)) {
                    if (dx > 0 && dx > -dy) {
                        isSlideUp = 2;
                    } else if (dy < 0 && -dy > dx) {
                        if (isSlideUp != 1) {
                            mainTab.setVisibility(View.VISIBLE);
                            //mainTab.setAlpha(0);
                            shimLayout.setVisibility(View.GONE);
                            animUpArrow.stop();
                            arraw.setBackgroundResource(R.drawable.unfold_arrow);
                        }
                        maintabHeight = 300;
                        isSlideUp = 1;
                    }
                } else if (dy > 0 && Math.pow(dy, 2) + Math.pow(dx, 2) > 40 && dy > dx) {
                    if (isSlideUp != 3) {
                        animUpArrow.stop();
                        arraw.setBackgroundResource(R.drawable.fold_arrow);
                    }
                    isSlideUp = 3;
                }
                if (isSlideUp == 2 && isFold) {
                    touchLayout.layout(touchLeft + dx, touchLayout.getTop(), touchRight + dx, touchLayout.getBottom());
                } else if (isSlideUp == 1 || isSlideUp == 3) {
                    if (isSlideUp == 1) {
                        float mAlpha = (float) (-dy) / maintabHeight;

                        if(DeviceUtils.isNewOS())
                            mainTab.setAlpha(mAlpha);
                        touchLayout.layout(touchLeft, touchTop + dy, touchRight, touchBottom + dy);
                            /*if(-dy < maintabHeight) {
                                mainTab.layout(mainTab.getLeft(), maintabTop + dy, mainTab.getRight(), maintabBottom + dy);

                                dateTimeTitle.layout(dateTimeTitle.getLeft(), dateTimeTitleTop + dy,
                                        dateTimeTitle.getRight(), dateTimeTitleBottom + dy);
                            }*/
                    } else if (isSlideUp == 3) {

                        if(DeviceUtils.isNewOS())
                            mainTab.setAlpha((float) (-dy) / maintabHeight <= 1 ? 1 + (float) (-dy) / maintabHeight : 0);
                        if (-dy < maintabHeight) {
                            mainTab.layout(mainTab.getLeft(), maintabTop + dy, mainTab.getRight(), maintabBottom + dy);
                            dateTimeTitle.layout(dateTimeTitle.getLeft(), dateTimeTitleTop + dy,
                                    dateTimeTitle.getRight(), dateTimeTitleBottom + dy);
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_UP:

                if(DeviceUtils.isNewOS())
                    mainTab.setAlpha(1);
                if (isSlideUp == 2 && isFold) {
                    if (dx > screenWidth * 0.08) {
                        lastUnLockY = screenHeight;
                        slideRight();
                    } else {
                        touchLayout.layout(touchLeft, touchLayout.getTop(), touchRight, touchLayout.getBottom());
                    }
                } else if (isSlideUp == 1) {
                    mainTab.layout(mainTab.getLeft(), maintabTop, mainTab.getRight(), maintabBottom);
                    bottomLayout.layout(bottomLeft, bottomTop, bottomRight, bottomBottom);
                    dateTimeTitle.layout(dateTimeTitle.getLeft(), dateTimeTitleTop, dateTimeTitle.getRight(), dateTimeTitleBottom);
                    onFoldClicked(dy);
                } else if (isSlideUp == 3) {
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, DisplayUtils.dip2px(this, 160));
                    touchLayout.setLayoutParams(params);
                    mainTab.setVisibility(View.GONE);
                    shimLayout.setVisibility(View.VISIBLE);
                    arraw.setBackgroundResource(R.anim.lock_anim_up);
                    animUpArrow = (AnimationDrawable) arraw.getBackground();
                    animUpArrow.start();
                    isFold = true;
                }
        }
        return true;
    }

    private void onFoldClicked(int dy) {
        if (-dy < mainTab.getHeight() / 2) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, DisplayUtils.dip2px(this, 160));
            touchLayout.setLayoutParams(params);
            mainTab.setVisibility(View.GONE);
            shimLayout.setVisibility(View.VISIBLE);
            arraw.setBackgroundResource(R.anim.lock_anim_up);
            isFold = true;
        } else {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            touchLayout.setLayoutParams(params);
            mainTab.setVisibility(View.VISIBLE);
            shimLayout.setVisibility(View.GONE);
            arraw.setBackgroundResource(R.anim.lock_anim_down);
            int contentId = adapter.getView(currentPosition).getId();
            layoutView.findViewById(R.id.img_mainTab_like).setEnabled(contentId > 0);

            layoutView.findViewById(R.id.layout_mainTab_like).setClickable(contentId > 0);
            layoutView.findViewById(R.id.layout_mainTab_detail).setClickable(contentId > 0);
            isFold = false;

            ActionLogOperator.add(getApplicationContext(), new AccountActionLogDTO(accountId, 15, contentId));
        }
        animUpArrow = (AnimationDrawable) arraw.getBackground();
        animUpArrow.start();
    }

    private void setTouchDownValue(MotionEvent event) {
        lastUnLockY = (int) event.getRawY();
        lastUnLockX = (int) event.getRawX();
        isSlideUp = 0;
        bottomLeft = bottomLayout.getLeft();
        bottomRight = bottomLayout.getRight();
        bottomTop = bottomLayout.getTop();
        bottomBottom = bottomLayout.getBottom();
        touchTop = touchLayout.getTop();
        touchBottom = touchLayout.getBottom();
        touchLeft = touchLayout.getLeft();
        touchRight = touchLayout.getRight();
        maintabBottom = mainTab.getBottom();
        maintabTop = mainTab.getTop();
        dateTimeTitleTop = dateTimeTitle.getTop();
        dateTimeTitleBottom = dateTimeTitle.getBottom();
    }

    private void imgTouchEvents(View view, MotionEvent event) {
        ImageView tImage = (ImageView) view;

        lockTabTop = lockTab.getTop();
        int left = (int) event.getRawX() - view.getWidth() / 2;
        int right = (int) event.getRawX() + view.getWidth() / 2;
        int up = (int) event.getRawY() - view.getHeight() / 2;
        int buttom = (int) event.getRawY() + view.getHeight() / 2;

        int contentId = adapter.getView(currentPosition).getId();
        if (contentId >= 0) {
            content = ContentsDataOperator.getById(this, contentId);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                tImage.setImageResource(R.drawable.ring_active);
                hideDetail(content);
                break;

            case MotionEvent.ACTION_MOVE:
                if (left < screenWidth * SlideConstants.SLIDE_DISTANCE_COEFF && !isEnter && !isBlock) {
                    isFavorite = true;
                    tImage.setVisibility(View.INVISIBLE);
                    if (content != null && content.isFavorite())
                        imgFavorite.setImageResource(R.drawable.lock_favorite);
                    else
                        imgFavorite.setImageResource(R.drawable.lock_favorite_actived);
                } else if (right > screenWidth - screenWidth
                        * SlideConstants.SLIDE_DISTANCE_COEFF && !isFavorite && !isEnter) {
                    isBlock = true;
                    tImage.setVisibility(View.INVISIBLE);
                    imgBlock.setImageResource(R.drawable.icon_block_selected);
                } else if (up < screenHeight * SlideConstants.SLIDE_DISTANCE_UP_COEFF && !isFavorite && !isBlock) {
                    isEnter = true;
                    tImage.setVisibility(View.INVISIBLE);
                } else if (right < screenWidth - screenWidth * SlideConstants.SLIDE_DISTANCE_COEFF - 30 && isBlock ||
                        up > screenHeight * SlideConstants.SLIDE_DISTANCE_UP_COEFF + 30 && isEnter ||
                        left > screenWidth * SlideConstants.SLIDE_DISTANCE_COEFF + 30 && isFavorite) {
                    isBlock = false;
                    isFavorite = false;
                    isEnter = false;
                    tImage.setVisibility(View.VISIBLE);
                    outTouchTab(view, tImage, content);
                    tImage.setImageResource(R.drawable.ring_active);
                }
                view.layout(left, up - lockTabTop, right, buttom - lockTabTop);
                break;

            case MotionEvent.ACTION_UP:
                txtUnlock.setVisibility(View.VISIBLE);
                blurText.setVisibility(View.INVISIBLE);
                txtTitle.setVisibility(View.VISIBLE);
                endOperation(view, tImage, left, right, up, content);
                break;

            default:
                endOperation(view, tImage, left, right, up, content);
                break;
        }
    }

    private void hideDetail(ContentDTO content) {
        curImageView = adapter.getView(currentPosition);
        txtTitle.setVisibility(View.GONE);
        txtUnlock.setVisibility(View.INVISIBLE);
//                LayoutParams params = (LayoutParams) img.getLayoutParams();
//                params.bottomMargin = DisplayUtils.px_y(98, screenHeight);
//                params.height = DisplayUtils.px_y(200, screenHeight);
//                params.width = DisplayUtils.px_x(200, screenWidth);
//                img.setLayoutParams(params);
        if (blurText.getId() == currentPosition)
            blurText.setVisibility(View.VISIBLE);
        imgBlock.setImageResource(R.drawable.icon_block);
        if (content != null && content.isFavorite())
            imgFavorite.setImageResource(R.drawable.lock_favorite_actived);
        else
            imgFavorite.setImageResource(R.drawable.lock_favorite);

        imgFavorite.setVisibility(View.VISIBLE);
        imgBlock.setVisibility(View.VISIBLE);

        isBlock = false;
        isFavorite = false;
        isEnter = false;
    }

    private void endOperation(View view, ImageView tImage, int left, int right, int up, ContentDTO content) {
        imgFavorite.setVisibility(View.INVISIBLE);
        imgBlock.setVisibility(View.INVISIBLE);
        if (isBlock
                && right > screenWidth - screenWidth
                * SlideConstants.SLIDE_DISTANCE_LEFT_COEFF) {
            if (content != null) {
                dislikeContent(view);
            } else {
                Toast toast = Toast.makeText(this, SlideConstants.TOAST_CANNOT_SKIP_TIPS,
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM, 0, 19);
                toast.show();
            }
            resetTouchTab(view, tImage, content);
        } else if (isFavorite
                && left < screenWidth * SlideConstants.SLIDE_DISTANCE_LEFT_COEFF) {
            if (content != null)
                favoriteContent(view);
            else {
                resetTouchTab(view, tImage, content);
                Toast toast = Toast.makeText(this, SlideConstants.TOAST_CANNOT_LIKE_TIPS,
                        Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM, 0, 19);
                toast.show();
            }
            resetTouchTab(view, tImage, content);
        } else if (isEnter && up < screenHeight * SlideConstants.SLIDE_DISTANCE_UP_COEFF) {
            if (!adapter.isContent(currentPosition)) {
                // shareImgUrl = content.getImage();
                if (!(shareImgUrl == null || shareTitle == null || descContent == null)) {
                    int contentId = 0;
                    if (content != null)
                        contentId = content.getId();
                }
                resetTouchTab(view, tImage, content);
            } else {
//                SysApplication.getInstance().exit();
//                ActionLogOperator.add(this, new AccountActionLogDTO(accountId,
//                        ActionTypeEnum.LEFT_SLIDE, currentContentId));
                //LockService.this.stopSelf();
                removePView();
                removeView();
                PageChangeUtils.gotoDetail(getApplicationContext(), content, "LockActivity", Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

//                sendBroadcast(new Intent(
//                        SlideConstants.BROADCAST_DATA_PROCESS_LOG));
                ThreadHelper.getInstance(this).addTask(SlideConstants.THREAD_POST_LOG);

            }
            resetTouchTab(view, tImage, content);
        } else {
            resetTouchTab(view, tImage, content);
        }
        isBlock = false;
        isFavorite = false;
        isEnter = false;
    }

    private void slideRight() {
        NetUtils.umengSelfEvent(LockApplication.getInstance(this), "lock_unlock");
        ThreadHelper.getInstance(this).addTask(SlideConstants.THREAD_POST_LOG);
        if(SharedPreferencesUtils.getBoolSP(this, SharedPreferencesUtils.GESTURE_CLOSE, true)) 
        	DisplayUtils.showNavBar(layoutView);
        removeView();
        viewAdded = false;
        if ("SettingWizardActivity".equals(preActivity)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(intent);
        } else {
            ActionLogOperator.add(this, new AccountActionLogDTO(accountId, 20, currentContentId));
            
            if(!SharedPreferencesUtils.getBoolSP(this, SharedPreferencesUtils.GESTURE_CLOSE, true)) {
                gotoPattenView();
            } else {
            	SysApplication.getInstance().exit();
            }
        }
        SharedPreferencesUtils.putStringSP(LockService.this, "firstLockActivity", "LockService");
        SharedPreferencesUtils.putIntSP(this, SharedPreferencesUtils.CURRENT_LOCK_IMGPOS, (currentPosition + 1));
        currentPosition = currentPosition + 1;
    }

    private void setGestureBackgroud() {
        ImageView contentView = adapter.getView(currentPosition);

        ContentDTO content = ContentsDataOperator.getById(this,
                contentView.getId());

        if(!SharedPreferencesUtils.getBoolSP(this, SharedPreferencesUtils.GESTURE_CLOSE, true)) {
            String localPath = null;
            int contentId = 0;
            if (content != null)
                localPath = content.getLocalPath();
            else
                contentId = contentView.getId();
            setBackgroudImg(localPath, contentId);
        }
    }

    private void gotoPattenView(){
        removeView();
        mLockPatternView.clearPattern();
        windowManager.addView(pLayoutView, layoutParams);
        pViewAdded = true;
    }

    private void backHome() {
        Intent mHomeIntent = new Intent(Intent.ACTION_MAIN);

        mHomeIntent.addCategory(Intent.CATEGORY_HOME);
        mHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        startActivity(mHomeIntent);
    }

    private void resetTouchTab(View view, ImageView tImage, ContentDTO content) {
        view.layout(screenWidth - 25, view.getTop(), screenWidth + 25,
                view.getBottom());
        tImage.setVisibility(View.VISIBLE);
//        LayoutParams params = (LayoutParams) img.getLayoutParams();
//        params.bottomMargin = DisplayUtils.px_y(78, screenHeight);
//        params.height = DisplayUtils.px_y(240, screenHeight);
//        params.width = DisplayUtils.px_x(240, screenWidth);
//        img.setLayoutParams(params);
        outTouchTab(view, tImage, content);

    }

    private void outTouchTab(View view, ImageView tImage, ContentDTO content) {
        imgBlock.setImageResource(R.drawable.icon_block);
        tImage.setImageResource(R.drawable.icon_share);
        imgFavorite.setImageResource(R.drawable.favorite);
        if (content == null)
            imgFavorite.setEnabled(false);
        else if (content != null && content.isFavorite())
            imgFavorite.setImageResource(R.drawable.lock_favorite_actived);
        else if (content != null && !content.isFavorite())
            imgFavorite.setImageResource(R.drawable.lock_favorite);
        if (content != null) {
            if (content.getType() == ContentTypeEnum.SHOPPING.getValue()) {
                tImage.setImageResource(R.drawable.icon_shopping);
            }

            if (content.getType() == ContentTypeEnum.READING.getValue()) {
                tImage.setImageResource(R.drawable.icon_reading);
            }
        }
    }


    @Override
    public void onDestroy() {
        SharedPreferencesUtils.putIntSP(this, SharedPreferencesUtils.CURRENT_LOCK_IMGPOS, currentPosition + 1);
        Log.d(TAG, TAG + "onDestroy");
        if(adapter != null)
            adapter.recycleBitmap();
        timer.cancel();
        removeView();

        unregisterReceiver(receiver);
        stopForeground(true);  
        Intent intent = new Intent("com.smart.lock.lockservice.destroy");  
        sendBroadcast(intent);  
        super.onDestroy();  
    }

    public void showorHideDetail(View view) {
        NetUtils.umengSelfEvent(LockApplication.getInstance(this), "lock_show_hide_detail", isFold?"hide":"show");
        if (isFold)
            onFoldClicked(-300);
        else
            onFoldClicked(0);
    }

    public void dislikeContent(View view) {
//        ActionLogOperator.add(this, new AccountActionLogDTO(accountId,
//                ActionTypeEnum.DISLIKE, currentContentId));

        currentContentId = adapter.replaceItem(viewPager, currentPosition,
                screenWidth, screenHeight);

        viewPager.setCurrentItem(currentPosition + 1);

        initFavoriteTab();
    }

    public void favoriteContent(View view) {
        ImageView contentView = adapter.getView(currentPosition);

        NetUtils.umengSelfEvent(LockApplication.getInstance(this), "lock_favorite");
        ContentDTO content = ContentsDataOperator.getById(this,
                contentView.getId());
        if (content != null) {
//            ActionLogOperator.add(this,
//                    new AccountActionLogDTO(accountId,
//                            content.isFavorite() ? ActionTypeEnum.CANCEL_FAVOR
//                                    : ActionTypeEnum.FAVORTITE_IN_LOCK, currentContentId));
        	accountId = SharedPreferencesUtils.getIntSP(this, SharedPreferencesUtils.ACCOUNT_ID, -1);
            ActionLogOperator.add(this,
                    new AccountActionLogDTO(accountId,
                            content.isFavorite() ? 17 : 16, currentContentId));
            content.setFavorite(!content.isFavorite());
            if (content.isFavorite())
                imgFavorite.setImageResource(R.drawable.lock_favorite_actived);
            else
                imgFavorite.setImageResource(R.drawable.lock_favorite);

            ContentsDataOperator.updateWithoutNoti(this, content);
        }

//        sendBroadcast(new Intent(SlideConstants.BROADCAST_DATA_PROCESS_LOG));
        ThreadHelper.getInstance(this).addTask(SlideConstants.THREAD_POST_LOG);
    }

    public void shareContent(View view) {
        ActionLogOperator.add(this, new AccountActionLogDTO(accountId, 18, currentContentId));

        ImageView contentView = adapter.getView(currentPosition);
        ContentDTO content = ContentsDataOperator.getById(this,
                contentView.getId());
        if (!(shareImgUrl == null || shareTitle == null || descContent == null)) {
            int contentId = 0;
            String title = null, localPath = null, contentDesc = null, imageUrl = null;
            int id;
            if (content != null) {
                contentId = content.getId();
                title = content.getTitle();
                localPath = content.getLocalPath();
                contentDesc = content.getContent();
                imageUrl = content.getImage();
                id = content.getId();
            } else {
                final int tempContentId = contentView.getId();
                final String localFilePath = FileUtils.contentIdToLocalPath(tempContentId);
                final Bitmap bm = adapter.getBms().get(tempContentId);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            FileUtils.saveImage(bm, localFilePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                title = txtTitle.getText().toString();
                localPath = localFilePath;
                contentDesc = txtContent.getText().toString();
                id = tempContentId;
            }
            /*ShareUtils.shareContent(LockActivity.this, shareImgUrl,
                    SlideConstants.APP_DOWNLOAD_LINK, shareTitle,
                    descContent, contentId);*/
            NetUtils.umengSelfEvent(LockApplication.getInstance(this), "lock_share");
            SysApplication.getInstance().exit();
            Intent intent = new Intent();
            intent.setClass(this.getApplicationContext(), ShareActivity.class);
            intent.putExtra("localPath", localPath);
            intent.putExtra("shareTitle", title);
            intent.putExtra("descContent", contentDesc);
            intent.putExtra("shareImgUrl", imageUrl);
            intent.putExtra("contentId", id);
            intent.putExtra("preActivity", "LockService");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            removePView();
            removeView();
            //this.stopSelf();
            this.getApplicationContext().startActivity(intent);
        }
    }

    public void tabWallPaperClick(final View view) {
        ActionLogOperator.add(this, new AccountActionLogDTO(accountId, 19, currentContentId));

        NetUtils.umengSelfEvent(LockApplication.getInstance(this), "lock_save_img");
        ImageView imageView = adapter.getView(currentPosition);
        final int contentId = imageView.getId();
        final Bitmap bitmap = adapter.getBms().get(contentId);
        final String fileName = "mshow" + contentId + ".jpg";
        Toast.makeText(getApplicationContext(), SlideConstants.DOWNLOAD_WALLPAPER_BEGIN,
                Toast.LENGTH_SHORT).show();
        view.setClickable(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                String localFilePath = FileUtils.contentIdToLocalPath(contentId);
                try {
                    FileUtils.saveImage(bitmap, localFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                view.setClickable(true);
                messageHandler.sendMessage(Message.obtain(messageHandler, 2));
            }
        }).start();
    }

    public void tabDetailClick(View view) {
        ActionLogOperator.add(this, new AccountActionLogDTO(accountId, 20, currentContentId));

        NetUtils.umengSelfEvent(LockApplication.getInstance(this), "lock_detail");
        ImageView contentView = adapter.getView(currentPosition);
        ContentDTO content = ContentsDataOperator.getById(this,
                contentView.getId());
        if (!adapter.isContent(currentPosition)) {
            // shareImgUrl = content.getImage();
            if (!(shareImgUrl == null || shareTitle == null || descContent == null)) {
                int contentId = 0;
                if (content != null)
                    contentId = content.getId();
            }
        } else {
//                SysApplication.getInstance().exit();
//            ActionLogOperator.add(this, new AccountActionLogDTO(accountId,
//                    ActionTypeEnum.LEFT_SLIDE, currentContentId));

            removePView();
            removeView();
            //this.stopSelf();

            PageChangeUtils.gotoDetail(getApplicationContext(), content, "LockActivity", Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

//                sendBroadcast(new Intent(
//                        SlideConstants.BROADCAST_DATA_PROCESS_LOG));
            ThreadHelper.getInstance(this).addTask(SlideConstants.THREAD_POST_LOG);

        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onPageScrolled(int current, float offsetPercent,
                               int offsetPosition) {
        // TODO Auto-generated method stub
        NetUtils.umengSelfEvent(LockApplication.getInstance(this), "lock_fling");
    }

    @Override
    public void onPageSelected(int position) {
        if (currentPosition < position) {
            ActionLogOperator.add(getApplicationContext(), new AccountActionLogDTO(accountId, 13, currentContentId));
        } else if (currentPosition > position) {
            ActionLogOperator.add(getApplicationContext(), new AccountActionLogDTO(accountId, 14, currentContentId));
        }

        ImageView view = adapter.getView(position);
        applyBlur(view);

        if ((Boolean) view.getTag())
            adapter.incViewCount(accountId, position);

        currentPosition = position;

        initFavoriteTab();

        initTouchTab();

        setGestureBackgroud();
    }

    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}
    
    public void backClick(View view) {
        removePView();
        windowManager.addView(layoutView, layoutParams);
        viewAdded = true;
    }

    /**
     * 关闭悬浮窗
     */
    public void removeView() {
        synchronized(LockService.class) {
            if (viewAdded) {
                windowManager.removeView(layoutView);
                viewAdded = false;
            }
        }
    }


    private void gotoLockView() {
        synchronized(LockService.class) {
            startTimeRefresh();
            if(adapter.getContents() != null && adapter.getContents().size() > 0) {
	            if(!viewAdded) {
	                updateLockData();
	                windowManager.addView(layoutView, layoutParams);
	                viewAdded = true;
	            } else {
	                currentPosition++;
	                viewPager.setCurrentItem(currentPosition);
	            }
            }
        }
    }
    /**
     * 关闭悬浮窗
     */
    public void removePView() {
        if (pViewAdded) {
            windowManager.removeView(pLayoutView);
            pViewAdded = false;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder();
    }
    public class LocalBinder extends Binder {
        public LockService getService() {
            return LockService.this;
        }
    }

    private class MyPhoneStateListener extends PhoneStateListener {
        MediaRecorder recorder;
        File audioFile;
        String phoneNumber;

        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE: /* 无任何状态时 */
                    if(!viewAdded && receiveCallHide) {
                        handleShow();
                        receiveCallHide = false;
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK: /* 接起电话时 */
                    break;
                case TelephonyManager.CALL_STATE_RINGING: /* 电话进来时 */
                    break;
                default:
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

    //创建广播接收器用于接收前台Activity发去的广播
	class LockServiceReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			int control = intent.getIntExtra("state", -1);
			PollingUtils.checkServiceRunning(context);
			switch (control) {
			case STATE_SHOW:
				handleShow();
				break;
			case STATE_HIDE:
				Log.d(TAG, "receive STATE_HIDE");
				if (pViewAdded)
					removePView();
				if (viewAdded) {
					removeView();
		        	DisplayUtils.showNavBar(layoutView);
		        	DisplayUtils.showNavBar(pLayoutView);
		        	fromLock = true;
	                receiveCallHide = true;
				}
				break;
			case STATE_UPDATE_DATE:
				Log.d(TAG, "receive STATE_UPDATE_DATE");
				new Thread(new Runnable() {
					@Override
					public void run() {
						updateDBData();
					}
				}).start();
				break;
			case STATE_SCREEN_ON:
				if(fromLock && !viewAdded && DeviceUtils.isPhoneIdle(context)) { //for app being killed, and screen on
					handleShow();
					fromLock = false;
				}
	            Message msg = Message.obtain(messageHandler, 1);
	            messageHandler.sendMessage(msg);
				break;
			default:
				break;
			}
		}
	}

	private void handleShow() {
		if (DeviceUtils.isPhoneIdle(this)) {
			if (!isFold) {
				onFoldClicked(0);
			}
			DisplayUtils.hideNavBar(layoutView);
			Log.d(TAG, "receive STATE_SHOW");
			preActivity = SharedPreferencesUtils.getStringSP(LockService.this,
					"firstLockActivity", null);
			if (pViewAdded)
				removePView();
			gotoLockView();
			boolean isNew = SharedPreferencesUtils.getBoolSP(LockService.this,
					SharedPreferencesUtils.IS_NEW, true);
			if (isNew) {
				Intent aIntent = new Intent(LockService.this,
						SplashActivity.class);
				aIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				startActivity(aIntent);
			}
			checkContents();
			ThreadHelper.getInstance(LockService.this).addTask(
					SlideConstants.THREAD_DISTRIBUTE_CONTENT);
		}
	}
}
