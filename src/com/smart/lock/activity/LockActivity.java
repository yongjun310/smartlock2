package com.smart.lock.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.*;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.View.OnTouchListener;
import android.widget.*;

import com.smart.lock.R;
import com.smart.lock.adapter.LoopImageViewPagerAdapter;
import com.smart.lock.common.ContentTypeEnum;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.dto.AccountActionLogDTO;
import com.smart.lock.dto.ContentDTO;
import com.smart.lock.pattern.UnlockGesturePasswordActivity;
import com.smart.lock.service.HeartBeatService;
import com.smart.lock.utils.*;
import com.smart.lock.view.LockLayer;
import com.smart.lock.view.ShimmerFrameLayout;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@SuppressWarnings("ALL")
public class LockActivity extends BaseActivity implements OnTouchListener, OnPageChangeListener {

    private ViewPager viewPager;
    private LoopImageViewPagerAdapter adapter;

    private int lastUnLockY, lastUnLockX;

    private int currentPosition;

    private int accountId;

    private String mobileNo, lastRightSlideIncomeTime;

    private int nowSec;

    private int currentContentId;

    private View lockTab;

    private LinearLayout mainTab;

    private int screenWidth;
    private int screenHeight;

    private View rootView;

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

    private boolean isFold = true;

    private String preActivity = null;

    private LockLayer lockLayer;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        /*if (!PollingUtils.isServiceRunning(LockActivity.this,
                HeartBeatService.class.getName())) {
            PollingUtils.startPollingService(LockActivity.this,
                    SlideConstants.SEVICE_HEARTBEAT_SPAN,
                    HeartBeatService.class,
                    SlideConstants.HEARTBEAT_SERVICE_NAME);
        }*/

    	startService(new Intent(this, HeartBeatService.class));
        overridePendingTransition(R.anim.anim_enter_lock, R.anim.anim_exit_lock);

//        FileUtils.addFileLog("enter LockActivity :");
//        FileUtils.addFileLog(SysApplication.getInstance().printActivityList());
        if (this.getIntent().getExtras() != null) {
            preActivity = this.getIntent().getExtras().getString("preActivity");
        }

        //隐藏虚拟按键以便全屏
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        View decorView = getWindow().getDecorView();
        if (currentapiVersion >= 14) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (currentapiVersion >= 19) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        lockLayer = new LockLayer();
        lockLayer.lock(this);

        ThreadHelper.getInstance(this).addTask(
                SlideConstants.THREAD_DISTRIBUTE_CONTENT);
        //锁屏在系统锁屏前面
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        SysApplication.getInstance().addActivity(this);

        SharedPreferences commonSP = getSharedPreferences("common", 0);

        accountId = commonSP.getInt("accountId", -1);
        mobileNo = commonSP.getString("mobileNo", "");
        lastRightSlideIncomeTime = commonSP.getString(
                "lastRightSlideIncomeTime", "1900-01-01 00:00:00");
        rootView = (View) findViewById(R.id.layout_lockActivity);
        txtTime = (TextView) findViewById(R.id.txt_time);
        txtDay = (TextView) findViewById(R.id.txt_day);
        txtDate = (TextView) findViewById(R.id.txt_date);
        lockTab = (View) findViewById(R.id.layout_lockTab);

        bottomLayout = (View) findViewById(R.id.layout_buttom);

        sharePrize = (ImageView) findViewById(R.id.img_share_prize);
        mainTab = (LinearLayout) findViewById(R.id.layout_mainTab);
        imgFavorite = (ImageView) findViewById(R.id.img_mainTab_like);
        dateTimeTitle = (View) findViewById(R.id.datatime_title);

        txtWallPaper = (TextView) findViewById(R.id.txt_mainTab_wallpaper);
        txtShare = (TextView) findViewById(R.id.txt_mainTab_share);
        txtDetail = (TextView) findViewById(R.id.txt_mainTab_detail);
        txtLike = (TextView) findViewById(R.id.txt_mainTab_like);
        txtContent = (TextView) findViewById(R.id.txt_content);
        arraw = (ImageView) findViewById(R.id.up_arraw);

        DisplayUtils.enlargeClickArea(arraw, 30);
        arraw.setBackgroundResource(R.anim.lock_anim_up);
        animUpArrow = (AnimationDrawable) arraw.getBackground();
        blurText = (TextView) findViewById(R.id.text);
        detailLayout = (View) findViewById(R.id.layout_mainTab_detail);
        favoriteLayout = (View) findViewById(R.id.layout_mainTab_like);
        titleLayout = (View) findViewById(R.id.title_layout);
        touchLayout = (View) findViewById(R.id.layout_touchTab);
        lockTab.setOnTouchListener(this);

        shimLayout = (ShimmerFrameLayout) findViewById(R.id.shimmer_view_container);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                shimLayout.startShimmerAnimation();
                animUpArrow.start();
            }
        };
        messageHandler.postDelayed(runnable, 3000);
        txtUnlock = (TextView) findViewById(R.id.txt_unlock);
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

        initPager();
        initFavoriteTab();

        initTouchTab();

        startTimeRefresh();
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
                    Toast.makeText(LockActivity.this, SlideConstants.DOWNLOAD_WALLPAPER_SUCCESS + fileName,
                            Toast.LENGTH_SHORT).show();
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

    @Override
    public void onAttachedToWindow() {
        // TODO Auto-generated method stub
        this.getWindow().setType(
                WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onAttachedToWindow();
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
            return DisplayUtils.blur(LockActivity.this, arg0[0]);
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
        findViewById(R.id.img_mainTab_like).setEnabled(contentId > 0);
        findViewById(R.id.img_mainTab_detail).setEnabled(contentId > 0);
        findViewById(R.id.img_mainTab_share).setEnabled(true);
        findViewById(R.id.img_mainTab_wallpaper).setEnabled(true);
        if (contentId > 0) {
            favoriteLayout.setEnabled(true);
            txtLike.setTextColor(this.getResources().getColor(R.color.white));
            ContentDTO content = ContentsDataOperator.getById(this, contentId);
            if (content != null) {
            	findViewById(R.id.img_mainTab_detail).setEnabled(content.getHasMore()==1);
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
        txtTitle = (TextView) findViewById(R.id.txt_content_Title);
        currentContentId = view.getId();

        ActionLogOperator.add(this, new AccountActionLogDTO(accountId, 63, currentContentId));

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

    private void initPager() {
        long startTime = System.currentTimeMillis();
        String serverTime = SharedPreferencesUtils.getStringSP(this, SharedPreferencesUtils.SERVER_TIME, null);
        if (serverTime == null)
            serverTime = DisplayUtils.formatDateTimeString(Calendar.getInstance().getTime());
        serverTime = serverTime.substring(0,10);
        String sql = "SELECT * FROM content WHERE date(startTime) = date('" + serverTime + "') "
        		+ " ORDER BY startTime DESC";
        String lsql = "SELECT * FROM content WHERE isFavorite = 1 and date(startTime) <> date('" + serverTime + "') " + "ORDER BY startTime DESC";

        List<ContentDTO> contentList = ContentsDataOperator.load(this, sql);
        List<ContentDTO> lcontentList = ContentsDataOperator.load(this, lsql);

        List<ContentDTO> mContents = new ArrayList<ContentDTO>();
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
            while (i < contentList.size() && contentList.get(i).getPriority() > SlideConstants.DEFAULT_CONTENT_PRIORITY) {
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

        if (mContents.size() < SlideConstants.TOTAL_DEFAULT_PAGER_SIZE) {
            for (int i = -1; i >= -SlideConstants.TOTAL_DEFAULT_PAGER_SIZE; i--) {
                if (!"SettingWizardActivity".equals(preActivity) && mContents.size() >= SlideConstants.MIN_PAGER_SIZE)
                    break;
                ImageView item = new ImageView(this);
                int res = 0;
                ContentDTO content = new ContentDTO(i);
                item.setId(i);
                item.setTag(false);
                //item.setBackgroundDrawable(new BitmapDrawable(DisplayUtils.readBitMap(this, res)));
                mContents.add(content);
            }
        }
        currentPosition = mContents.size() * 1000;


        if (!SharedPreferencesUtils.getBoolSP(this, SharedPreferencesUtils.IS_NEW, true)) {
            currentPosition = SharedPreferencesUtils.getIntSP(this, SharedPreferencesUtils.CURRENT_LOCK_IMGPOS, 0);
        }
        if (currentPosition < mContents.size()) {
            currentPosition += 1000 * mContents.size();
        }
        adapter = new LoopImageViewPagerAdapter(mContents, this);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition);
        viewPager.setOnPageChangeListener(this);
        adapter.incViewCount(accountId, currentPosition);
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
            int fileSize = FileUtils.getFileSize(content
                    .getLocalPath());
            if (fileSize > 1024) {
                try {
                    mContents.add(content);
                } catch (Exception e) {
                    Log.e("lock open pic:", e.toString());
                }
            }
        }
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
                        mainTab.setAlpha(mAlpha);
                        touchLayout.layout(touchLeft, touchTop + dy, touchRight, touchBottom + dy);
                            /*if(-dy < maintabHeight) {
                                mainTab.layout(mainTab.getLeft(), maintabTop + dy, mainTab.getRight(), maintabBottom + dy);

                                dateTimeTitle.layout(dateTimeTitle.getLeft(), dateTimeTitleTop + dy,
                                        dateTimeTitle.getRight(), dateTimeTitleBottom + dy);
                            }*/
                    } else if (isSlideUp == 3) {
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
            findViewById(R.id.img_mainTab_like).setEnabled(contentId > 0);

            findViewById(R.id.layout_mainTab_like).setClickable(contentId > 0);
            findViewById(R.id.layout_mainTab_detail).setClickable(contentId > 0);
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
        ContentDTO content = null;
        if (contentId >= 0) {
            content = ContentsDataOperator.getById(this, contentId);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                curImageView = adapter.getView(currentPosition);
                tImage.setImageResource(R.drawable.ring_active);
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
                this.finish();
                overridePendingTransition(0, 0);

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
        if(lockLayer != null) {
            lockLayer.unlock();
            lockLayer = null;
        }
        ThreadHelper.getInstance(this).addTask(SlideConstants.THREAD_POST_LOG);
        SharedPreferencesUtils.putIntSP(this, SharedPreferencesUtils.CURRENT_LOCK_IMGPOS, (currentPosition + 1));
        if ("SettingWizardActivity".equals(preActivity)) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            ActionLogOperator.add(this, new AccountActionLogDTO(accountId, 20, currentContentId));
            Intent intent = new Intent();

            ImageView contentView = adapter.getView(currentPosition);

            ContentDTO content = ContentsDataOperator.getById(this,
                    contentView.getId());

            if(!SharedPreferencesUtils.getBoolSP(this, SharedPreferencesUtils.GESTURE_CLOSE, true)) {
                if(content != null)
                    intent.putExtra("imgLocalPath", content.getLocalPath());
                else
                    intent.putExtra("contentId", contentView.getId());
                intent.setClass(this, UnlockGesturePasswordActivity.class);
                startActivityForResult(intent, 0);

            }  else {
                SysApplication.getInstance().finish(DetailActivity.class);
                this.finish();
            }
            overridePendingTransition(0, 0);
        }
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
    public void onResume() {
        super.onResume();
        lastUnLockY = screenHeight;
        ImageView view = adapter.getView(currentPosition);
        applyBlur(view);
        initTime();
    }

    @Override
    public void onStop() {
        super.onStop();
        //vibrator.cancel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.recycleBitmap();
        timer.cancel();
        if(lockLayer != null) {
            lockLayer.unlock();
            lockLayer = null;
        }
    }

    public void showorHideDetail(View view) {
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

        ContentDTO content = ContentsDataOperator.getById(this,
                contentView.getId());
        if (content != null) {
//            ActionLogOperator.add(this,
//                    new AccountActionLogDTO(accountId,
//                            content.isFavorite() ? ActionTypeEnum.CANCEL_FAVOR
//                                    : ActionTypeEnum.FAVORTITE_IN_LOCK, currentContentId));
            ActionLogOperator.add(this,
                    new AccountActionLogDTO(accountId,
                            content.isFavorite() ? 17 : 16, currentContentId));

            content.setFavorite(!content.isFavorite());
            if (content.isFavorite())
                imgFavorite.setImageResource(R.drawable.lock_favorite_actived);
            else
                imgFavorite.setImageResource(R.drawable.lock_favorite);

            ContentsDataOperator.update(this, content);
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
            Intent intent = new Intent();
            intent.setClass(this, ShareActivity.class);
            intent.putExtra("localPath", localPath);
            intent.putExtra("shareTitle", title);
            intent.putExtra("descContent", contentDesc);
            intent.putExtra("shareImgUrl", imageUrl);
            intent.putExtra("contentId", id);
            startActivity(intent);

        }
    }

    public void tabWallPaperClick(final View view) {
        ActionLogOperator.add(this, new AccountActionLogDTO(accountId, 19, currentContentId));

        ImageView imageView = adapter.getView(currentPosition);
        final int contentId = imageView.getId();
        final Bitmap bitmap = adapter.getBms().get(contentId);
        final String fileName = "mshow" + contentId + ".jpg";
        Toast.makeText(LockActivity.this, SlideConstants.DOWNLOAD_WALLPAPER_BEGIN,
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
            this.finish();
            overridePendingTransition(0, 0);

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            SysApplication.getInstance().finish(DetailActivity.class);
            this.finish();
        } else {
            lockLayer = new LockLayer();
            lockLayer.lock(this);
        }
    }
}
