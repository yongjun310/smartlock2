package com.smart.lock.activity;

import java.io.File;
import java.math.BigDecimal;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.smart.lock.R;
import com.smart.lock.adapter.MainFragmentPagerAdapter;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.dto.AccountActionLogDTO;
import com.smart.lock.dto.AccountDTO;
import com.smart.lock.dto.AccountInfoDTO;
import com.smart.lock.dto.AppVersionDTO;
import com.smart.lock.dto.ContentDTO;
import com.smart.lock.fragment.ExchangeFragment;
import com.smart.lock.fragment.LikeFragment;
import com.smart.lock.fragment.MainPageFragment;
import com.smart.lock.fragment.MyFragment;
import com.smart.lock.imageloader.ImageLoader;
import com.smart.lock.response.AccountResponse;
import com.smart.lock.response.AppVersionResponse;
import com.smart.lock.response.BooleanResponse;
import com.smart.lock.utils.ActionLogOperator;
import com.smart.lock.utils.ContentsDataOperator;
import com.smart.lock.utils.DeviceUtils;
import com.smart.lock.utils.DisplayUtils;
import com.smart.lock.utils.NetUtils;
import com.smart.lock.utils.PageChangeUtils;
import com.smart.lock.utils.SecurityUtils;
import com.smart.lock.utils.SharedPreferencesUtils;
import com.smart.lock.utils.SysApplication;
import com.smart.lock.utils.WidgetUtils;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String TAG_PRE = "[" + TAG + "] ";

    private int accountId;

    private String mobileNo;

    private int screenWidth;
    private int screenHeight;

    private LikeFragment likeFragment;

    private ExchangeFragment exchangeFragment;

    private MainPageFragment mainPageFragment;

    private MyFragment myFragment;

    private ImageView mySpotImgView;

    private TextView homeTxt;

    private TextView likeTxt;

    private TextView myTxt;

    private boolean needUpdateMessage = false;

    private boolean needUpdateApp = false;

    private RelativeLayout btnExchange;

    private int currIndex = 0;// 当前页卡编号
    private int four;

    private static Gson gson = new Gson();

    private List<String> fragmentList = null;

    private ImageView mTab1, mTab2, mTab3;

    private AppVersionDTO appVersion = null;

    private ViewPager mTabPager;

    private ArrayList<Fragment> pagerItemList = null;

	private FirstPageDBUpdateReceiver dbUpdateReceiver;

    public boolean isNeedUpdateMessage() {
        return needUpdateMessage;
    }

    public boolean isNeedUpdateApp() {
        return needUpdateApp;
    }

    public AppVersionDTO getAppVersion() {
        return appVersion;
    }

    public LikeFragment getLikeFragment() {
        return likeFragment;
    }

    public ExchangeFragment getExchangeFragment() {
        return exchangeFragment;
    }

    public MainPageFragment getMainPageFragment() {
        return mainPageFragment;
    }

    public MyFragment getMyFragment() {
        return myFragment;
    }

    public ViewPager getmTabPager() {
        return mTabPager;
    }

    public ArrayList<Fragment> getPagerItemList() {
        return pagerItemList;
    }

    public void setmTabPager(ViewPager mTabPager) {
        this.mTabPager = mTabPager;
    }

    public void setPagerItemList(ArrayList<Fragment> pagerItemList) {
        this.pagerItemList = pagerItemList;
    }

    public void setExchangeFragment(ExchangeFragment exchangeFragment) {
        this.exchangeFragment = exchangeFragment;
    }

    public void setMainPageFragment(MainPageFragment mainPageFragment) {
        this.mainPageFragment = mainPageFragment;
    }

    public void setLikeFragment(LikeFragment likeFragment) {
        this.likeFragment = likeFragment;
    }

    public void setMyFragment(MyFragment myFragment) {
        this.myFragment = myFragment;
    }

    private Handler messageHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    AccountDTO account = (AccountDTO) msg.obj;
                    if (account != null) {
                        TextView txtAmount = (TextView) findViewById(R.id.txt_income_amount);

                        txtAmount.setText(String.format("%.2f",
                                account.getTotalAmount()));

                        btnExchange = (RelativeLayout) findViewById(R.id.layout_btn_exchange);
                        if (account.getTotalAmount()
                                .compareTo(new BigDecimal("30")) > 0) {
                            btnExchange.setClickable(true);
                            btnExchange.setBackgroundColor(getApplicationContext()
                                    .getResources().getColor(R.color.red));
                        } else {
                            btnExchange.setClickable(false);
                            btnExchange.setBackgroundColor(getApplicationContext()
                                    .getResources().getColor(R.color.grey));
                        }
                    }
                    break;
                case 2:
                    Toast.makeText(getApplicationContext(), msg.obj.toString(),
                            Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(getApplicationContext(),
                            SlideConstants.TOAST_INCOME_SERVER_ERROR,
                            Toast.LENGTH_SHORT).show();
                case 4:
                    Toast.makeText(getApplicationContext(),
                            SlideConstants.TOAST_VERSION_SERVER_ERROR,
                            Toast.LENGTH_SHORT).show();
                    break;
                case 5:
                    Toast.makeText(getApplicationContext(),
                            SlideConstants.TOAST_VERSION_NEWEST, Toast.LENGTH_SHORT)
                            .show();
                    break;
                case 6:
                    // Toast.makeText(getApplicationContext(),
                    // SlideConstants.TOAST_VERSION_DOWNLOADING,
                    // Toast.LENGTH_LONG).show();
                    needUpdateApp = true;
                    String content = "发现新版本，安装包大小约为6M\n";
                    if (!DeviceUtils.isWifi(getApplicationContext())) {
                        content += "\n检测到您的网络环境为非WIFI状态";
                    }
                    content += "\n是否去应用宝更新？";
                    final String url = msg.obj.toString();
                    WidgetUtils.confirm(MainActivity.this, content,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    downloadApp();
                                    // new Thread(new Runnable() {
                                    //
                                    // @Override
                                    // public void run() {
                                    // downloadAndInstall(url);
                                    // }
                                    // }).start();
                                }
                            }).show();
                    break;
                case 7:
                    Toast.makeText(getApplicationContext(),
                            SlideConstants.TOAST_VERSION_NO_WIFI,
                            Toast.LENGTH_SHORT).show();
                    break;
                case 8:
                    Toast.makeText(getApplicationContext(),
                            SlideConstants.TOAST_EXCHANGE_INPUT_ERROR,
                            Toast.LENGTH_SHORT).show();
                    break;
                case 9:
                    Toast.makeText(getApplicationContext(),
                            SlideConstants.TOAST_INCOME_EXCHANGE_SUCCESS,
                            Toast.LENGTH_SHORT).show();
                    break;
                case 10:
                    needUpdateMessage = (Boolean) msg.obj;
                    mySpotImgView.setVisibility(needUpdateMessage ? View.VISIBLE
                            : View.INVISIBLE);
                    myFragment.updateSpotImgView();
                    break;
                case 11:
                    AccountInfoDTO accountInfo = (AccountInfoDTO) msg.obj;

                    accountId = accountInfo.getAccountId();
                    SharedPreferences.Editor commonEditor = getApplicationContext().getSharedPreferences("common", 0).edit();
                    commonEditor.putInt("accountId", accountId);
                    if (accountInfo.getMobileNo()!=null && accountInfo.getMobileNo().length()>0) {
                        commonEditor.putBoolean("isLogin", true);
                        commonEditor.putString("mobileNo", accountInfo.getMobileNo());
                        //myFragment.getTxtMobile().setText(accountInfo.getMobileNo());
                    }
                    commonEditor.commit();

                    SharedPreferences rsaSP = getSharedPreferences("rsa"
                            + accountId, MODE_MULTI_PROCESS );
                    SharedPreferences.Editor rsaEditor = rsaSP.edit();
                    rsaEditor.putString("modulus", accountInfo.getModulus());
                    rsaEditor.putString("publicExponent", accountInfo.getPublicExponent());
                    rsaEditor.commit();

                    initFragments(null);
                    break;
                case 12:
                    List<String> nowGroupList = ContentsDataOperator.loadContentsDate(getApplicationContext());
                    for (String group : mainPageFragment.getGroupList()) {
                        if (nowGroupList.contains(group)) {
                            nowGroupList.remove(group);

                            int index = mainPageFragment.getGroupList().indexOf(group);
                            if(mainPageFragment.isHasRecommand())
                                index = index-1;
                            List<ContentDTO> tempList = ContentsDataOperator.loadContentsByDate(getApplicationContext(), group);
                            if (mainPageFragment.getChildList().size() > 0 && mainPageFragment.getChildList().size() > index &&
                            		tempList.size() > mainPageFragment.getChildList().get(index).size()) {
                                int tempIndex = 0;
                                while (tempList!=null && tempList.size()>0 && tempIndex<tempList.size()) {
                                    boolean hasFound = false;
                                    for (ContentDTO nowChild : mainPageFragment.getChildList().get(index)) {
                                        if (nowChild.getId() == tempList.get(tempIndex).getId()) {
                                            tempList.remove(tempIndex);
                                            hasFound = true;
                                            break;
                                        }
                                    }
                                    if (!hasFound)
                                        tempIndex++;
                                }
                                if (tempList != null && tempList.size()>0) {
                                    mainPageFragment.getChildList().get(index).addAll(tempList);
                                }
                            }
                        }
                    }
                    if (nowGroupList!=null && nowGroupList.size()>0) {
                        mainPageFragment.getGroupList().addAll(nowGroupList);
                        mainPageFragment.hasContentChange(true);

                        for (String date : nowGroupList) {
                            List<ContentDTO> tempList = ContentsDataOperator.loadContentsByDate(getApplicationContext(), date);
                            if(mainPageFragment.getChildList() != null)
                            	mainPageFragment.getChildList().add(tempList);
                        }
                    }
                    if(mainPageFragment.getAdapter() != null)
                    	mainPageFragment.getAdapter().notifyDataSetChanged();
                    /*if(mainPageFragment.getExpandableListView() != null) {
	                    for (int i = 0, count = mainPageFragment.getExpandableListView().getCount(); i < count; i++) {
	                        mainPageFragment.getExpandableListView().expandGroup(i);
	                    }
                    }*/
                    //SharedPreferencesUtils.removeSP(getApplicationContext(), SharedPreferencesUtils.HAS_NEW);
                    break;
                default:
                    break;
            }

        }
    };

    public Handler getMessageHandler() {
        return this.messageHandler;
    }

    public void downloadApp() {
        Uri uri = Uri
                .parse(SlideConstants.APP_DOWNLOAD_LINK);
        Intent viewIntent = new Intent(
                Intent.ACTION_VIEW, uri);
        startActivity(viewIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SysApplication.getInstance().addActivity(this);

//        Timer timer = new Timer();
//        TimerTask task = new TimerTask() {
//
//            @Override
//            public void run() {
//                if (!PollingUtils.isServiceRunning(MainActivity.this,
//                        HeartBeatService.class.getName())) {
//                    PollingUtils.startPollingService(MainActivity.this,
//                            SlideConstants.SEVICE_HEARTBEAT_SPAN,
//                            HeartBeatService.class,
//                            SlideConstants.HEARTBEAT_SERVICE_NAME);
//                }
//            }
//        };
//        timer.schedule(task, 10 * 1000, 10 * 1000);

//    	Log.d("ThreadHelper", "THREAD_DISTRIBUTE_CONTENT time:" + System.currentTimeMillis());
//		ThreadHelper.getInstance(this).addTask(
//				SlideConstants.THREAD_DISTRIBUTE_CONTENT);
		
        dbUpdateReceiver=new FirstPageDBUpdateReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(SlideConstants.DB_UPDATE_ACTION);
        registerReceiver(dbUpdateReceiver, filter);
        
        //startPicRefresh();
        int type = 0;
        if (this.getIntent() != null && this.getIntent().getExtras() != null)
            type = this.getIntent().getExtras().getInt("type");
        // sendBroadcast(new
        // Intent(SlideConstants.BROADCAST_DATA_PROCESS_CONTENT));
//        ThreadHelper.getInstance(this).addTask(
//                SlideConstants.THREAD_DISTRIBUTE_CONTENT);
        //
        // if (!PollingUtils.isServiceRunning(this,
        // DataService.class.getName()))
        // startService(new Intent(SlideConstants.DATA_SERVICE_NAME));

        mTabPager = (ViewPager) findViewById(R.id.vPager);
        mTabPager.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }

        });
        mTab1 = (ImageView) findViewById(R.id.img_mainTab_home);
        mTab2 = (ImageView) findViewById(R.id.img_mainTab_like);
        mTab3 = (ImageView) findViewById(R.id.img_mainTab_my);

        mySpotImgView = (ImageView) findViewById(R.id.img_my_red_spot);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        initFragments(savedInstanceState);

        checkVersion(null);
        if (type == SlideConstants.MAIN_ACTIVITY_AMOUNT) {
            mTabPager.setCurrentItem(2);
        }
    }

    public void initFragments(Bundle savedInstanceState) {
        mainPageFragment = new MainPageFragment();
        myFragment = new MyFragment();
        likeFragment = new LikeFragment();
        pagerItemList = new ArrayList<Fragment>();
        pagerItemList.add(mainPageFragment);
        pagerItemList.add(likeFragment);
        pagerItemList.add(myFragment);

        FragmentManager mFragmentManager = this.getSupportFragmentManager();
        MainFragmentPagerAdapter mFragmentPagerAdapter = new MainFragmentPagerAdapter(
                mFragmentManager, mTabPager, pagerItemList);
        mTabPager.setAdapter(mFragmentPagerAdapter);
        mTabPager.setOnPageChangeListener(new MyOnPageChangeListener());
        SharedPreferences commonSP = getSharedPreferences("common",
                MODE_MULTI_PROCESS );
        accountId = commonSP.getInt("accountId", -1);
        mobileNo = commonSP.getString("mobileNo", "");
        homeTxt = (TextView) findViewById(R.id.txt_mainTab_home);
        homeTxt.setTextColor(getResources().getColorStateList(
                R.color.maintabred));
        mTab1.setSelected(true);
        mTab2.setSelected(false);
        mTab3.setSelected(false);

        likeTxt = (TextView) findViewById(R.id.txt_mainTab_like);

        likeTxt.setTextColor(getResources().getColorStateList(R.color.maintabgrey));

        myTxt = (TextView) findViewById(R.id.txt_mainTab_my);
        myTxt.setTextColor(getResources().getColorStateList(R.color.maintabgrey));

        fragmentList = new ArrayList<String>();
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null && "income".equals(extras.getString("fragment"))) {
                mTabPager.setCurrentItem(2);
            } else if (extras != null && "like".equals(extras.getString("fragment"))) {
                mTabPager.setCurrentItem(1);
            } else {
                mTabPager.setCurrentItem(0);
            }
        }
    }

    Timer timer = new Timer();

    
  //创建广播接收器用于接收前台Activity发去的广播
    class FirstPageDBUpdateReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
        	messageHandler.sendMessage(Message.obtain(messageHandler, 12));
            mainPageFragment.dismissDialog();
        	Log.d("ThreadHelper", "FirstPageDBUpdateReceiver time:" + System.currentTimeMillis());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkMessage();
        checkRefresh(true, true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				if(myFragment != null && myFragment.getActivity() != null)
					myFragment.initIncome();
			}
		}).start();
        if(!ImageLoader.getInstance().isInited()) {
            DeviceUtils.initImageLoader(this);
        }
    }

    public void checkRefresh(boolean refreshMain, boolean refreshLike) {
        int favoriteChange = SharedPreferencesUtils.getIntSP(this, "FavoriteChange", 0);
        if (favoriteChange > 0) {
            ContentDTO changeContent = ContentsDataOperator.getById(this, favoriteChange);

            if (changeContent != null) {
                ArrayList<List<ContentDTO>> childList = mainPageFragment.getChildList();
                for (List<ContentDTO> group : childList) {
                    for (ContentDTO contentDTO : group) {
                        if (contentDTO.getId() == favoriteChange) {
                            contentDTO.setFavorite(changeContent.isFavorite());
                            if (refreshMain)
                                mainPageFragment.getAdapter().notifyDataSetChanged();

                            if (refreshLike)
                                likeFragment.refreshPage();
                        }
                    }
                }

                SharedPreferencesUtils.putIntSP(this, "FavoriteChange", 0);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(dbUpdateReceiver);
        if (mainPageFragment != null) {
            mainPageFragment.dismissDialog();
            if(mainPageFragment.getAdapter() != null) {
                DisplayUtils.recycleBMMaps(mainPageFragment.getAdapter().getBitmaps());
            }
        }
    }

    public void onBackPressed() {
        exitBy2Click(); // 调用双击退出函数
        super.onBackPressed();
    }

    public void checkMessage() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String respStr = NetUtils.get(SlideConstants.SERVER_URL
                            + SlideConstants.SERVER_METHOD_HAS_MESSAGE
                            + "?accountId=" + accountId);
                    BooleanResponse resp = gson.fromJson(respStr,
                            BooleanResponse.class);
                    if (resp.getCode() == SlideConstants.SERVER_RETURN_SUCCESS) {
                        Message msg = Message.obtain(messageHandler, 10,
                                resp.getData());
                        messageHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = Message.obtain(messageHandler, 4);
                    messageHandler.sendMessage(msg);
                }
            }
        }).start();
    }

    public class MyOnPageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageSelected(int arg0) {
            mTab1.setSelected(false);
            mTab2.setSelected(false);
            mTab3.setSelected(false);
            homeTxt.setTextColor(getResources().getColorStateList(
                    R.color.maintabgrey));
            likeTxt.setTextColor(getResources().getColorStateList(
                    R.color.maintabgrey));
            myTxt.setTextColor(getResources().getColorStateList(
                    R.color.maintabgrey));
            switch (arg0) {
                case 0:
                    ActionLogOperator.add(getApplicationContext(), new AccountActionLogDTO(accountId, 21, 0));

                    mTab1.setSelected(true);
                    homeTxt.setTextColor(getResources().getColorStateList(
                            R.color.maintabred));
                    break;
                case 1:
                    ActionLogOperator.add(getApplicationContext(), new AccountActionLogDTO(accountId, 42, 0));

                    mTab2.setSelected(true);
                    likeTxt.setTextColor(getResources().getColorStateList(
                            R.color.maintabred));
                    break;
                case 2:
                    ActionLogOperator.add(getApplicationContext(), new AccountActionLogDTO(accountId, 47, 0));

                    mTab3.setSelected(true);
                    myTxt.setTextColor(getResources().getColorStateList(
                            R.color.maintabred));
                    break;
            }
            currIndex = arg0;
        }
    }

    public void tabHomeClick(View view) {
        NetUtils.umengSelfEvent(this, "app_buttom_button", "first");
        mTabPager.setCurrentItem(0);
    }

    public void tabLikeClick(View view) {
        mTabPager.setCurrentItem(1);
        NetUtils.umengSelfEvent(this, "app_buttom_button", "like");
    }

    public void tabProfitClick(View view) {
        mTabPager.setCurrentItem(2);
        NetUtils.umengSelfEvent(this, "app_buttom_button", "profit");
    }

    public void tabMyClick(View view) {
        mTabPager.setCurrentItem(3);
        NetUtils.umengSelfEvent(this, "app_buttom_button", "my");
    }

    public void settingClick(View view) {
        ActionLogOperator.add(this, new AccountActionLogDTO(accountId, 50, 0));

        Intent intent = new Intent(this, SettingDetailActivity.class);
        startActivity(intent);
    }


    public void bindMobile(View view) {
        if (SharedPreferencesUtils.getBoolSP(this, "isLogin", false)) {
            WidgetUtils.confirm(this, "确认要修改绑定手机号？",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            PageChangeUtils.startLogin(MainActivity.this, "MainActivity");

                            NetUtils.umengSelfEvent(MainActivity.this, "change_mobile");
                            arg0.dismiss();
                        }
                    }).show();
        } else
            PageChangeUtils.startLogin(MainActivity.this, "MainActivity");
        /*((DevicePolicyManager)getSystemService("device_policy")).lockNow();
        ComponentName localComponentName = new ComponentName(this, MyDeviceAdminReceiver.class);
        Intent localIntent = new Intent("android.app.action.ADD_DEVICE_ADMIN");
        localIntent.putExtra("android.app.extra.DEVICE_ADMIN", localComponentName);
        localIntent.putExtra("android.app.extra.ADD_EXPLANATION", "explanation");
        startActivityForResult(localIntent, 0);*/
    }

    public void removeDrawer(View view) {
        RelativeLayout drawerLayout = (RelativeLayout) findViewById(R.id.layout_drawer);
        LayoutParams params = (LayoutParams) drawerLayout.getLayoutParams();
        params.width = 0;
        drawerLayout.setLayoutParams(params);

        fragmentList.remove(fragmentList.size() - 1);
    }

    public void exchangeClick(View view) {
        mTabPager.setCurrentItem(0);
    }

    public void exchangeSubmit(View view) {
        Button btnProduct = (Button) findViewById(R.id.select_amount);
        String strProduct = (String) btnProduct.getText();
        WidgetUtils.confirm(this, "确定要充值" + strProduct,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        submitExchange();
                    }
                }).show();
    }

    private void submitExchange() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                EditText txtMobile = (EditText) findViewById(R.id.input_exchange_mobile);
                String strMobile = txtMobile.getText().toString();

                Button btnProduct = (Button) findViewById(R.id.select_amount);
                String strProduct = (String) btnProduct.getText();

                int productId = 0;
                for (int i = 0; i < SlideConstants.PRODUCT_LSIT.length; i++) {
                    if (strProduct.equals(SlideConstants.PRODUCT_LSIT[i]
                            .getProductName())) {
                        productId = i + 1;
                        break;
                    }
                }

                if (productId <= 0 || strMobile.length() < 11) {
                    Message msg = Message.obtain(messageHandler, 8);
                    messageHandler.sendMessage(msg);
                    return;
                }

                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("time", new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss").format(
                        Calendar.getInstance().getTime()).toString()));
                params.add(new BasicNameValuePair("androidId", DeviceUtils
                        .getAndroidId(getApplicationContext())));
                params.add(new BasicNameValuePair("mobileNo", strMobile));
                params.add(new BasicNameValuePair("productId", String
                        .valueOf(productId)));
                params.add(new BasicNameValuePair("amount",
                        SlideConstants.PRODUCT_LSIT[productId - 1].getAmount()
                                .toString()));

                SharedPreferences rsaSP = getApplicationContext()
                        .getSharedPreferences("rsa" + accountId,
                                Context.MODE_MULTI_PROCESS );
                String modulus = rsaSP.getString("modulus", "");
                String publicExponent = rsaSP.getString("publicExponent", "");

                RSAPublicKey publicKey = SecurityUtils.getPublicKey(modulus,
                        publicExponent);

                String data = SecurityUtils.getInputString(params);
                String sign = "";
                try {
                    sign = SecurityUtils.encryptByPublicKey(data, publicKey);

                    params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("sign", sign));
                    params.add(new BasicNameValuePair("accountId", String
                            .valueOf(accountId)));

                    String respStr = NetUtils.post(SlideConstants.SERVER_URL
                            + SlideConstants.SERVER_METHOD_EXCHANGE, params);
                    AccountResponse resp = gson.fromJson(respStr,
                            AccountResponse.class);
                    if (resp.getCode() == SlideConstants.SERVER_RETURN_SUCCESS
                            && resp.getData() != null) {
                        Message msg = Message.obtain(messageHandler, 1,
                                resp.getData());
                        messageHandler.sendMessage(msg);
                        msg = Message.obtain(messageHandler, 9, resp.getData());
                        messageHandler.sendMessage(msg);
                    } else {
                        Message msg = Message.obtain(messageHandler, 2,
                                resp.getMsg());
                        messageHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = Message.obtain(messageHandler, 3);
                    messageHandler.sendMessage(msg);
                }
            }
        }).start();
    }

    public void checkVersion(final View view) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                String appVer = DeviceUtils.getAppVersion(MainActivity.this);
                try {
                    String respStr = NetUtils.get(SlideConstants.SERVER_URL
                            + SlideConstants.SERVER_METHOD_CHECK_VERSION + "?accountId=" + accountId + "&clientVersion=" + appVer);
                    appVersion = (AppVersionDTO) gson
                            .fromJson(respStr, AppVersionResponse.class)
                            .getData();
                    //if (appVersion.getVersion().equals(appVer)) {
                    if (appVersion.getVersion().compareTo(appVer) <= 0) {
                        if (view != null) {
                            Message msg = Message.obtain(messageHandler, 5);
                            messageHandler.sendMessage(msg);
                        }
                    } else {
                        Message msg = Message.obtain(messageHandler, 6,
                                appVersion.getDownloadUrl());
                        messageHandler.sendMessage(msg);
                    }
                    if (appVersion.getAccountInfo() != null) {
                        Message msg = Message.obtain(messageHandler, 11,
                                appVersion.getAccountInfo());
                        messageHandler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = Message.obtain(messageHandler, 4);
                    messageHandler.sendMessage(msg);
                }

            }
        }).start();
    }

    public void clickMessage(final View view) {
        ActionLogOperator.add(this, new AccountActionLogDTO(accountId, 48, 0));
        NetUtils.umengSelfEvent(this, "message_center");
        Intent activityIntent = new Intent();
        activityIntent.setClass(this, WebViewActivity.class);

        Bundle bundle = new Bundle();
        bundle.putInt("type", SlideConstants.WEB_VIEW_MYMESSAGE);
        bundle.putString("preActivity", "MainActivity");

        activityIntent.putExtras(bundle);
        startActivity(activityIntent);
    }

    public void clickExchange(final View view) {
        ActionLogOperator.add(this, new AccountActionLogDTO(accountId, 49, 0));
        NetUtils.umengSelfEvent(this, "exchange_center");
        
        Intent activityIntent = new Intent();
        activityIntent.putExtra("type", "amount");
        activityIntent.setClass(this, AmountActivity.class);
        startActivity(activityIntent);
    }

    public void clickViewed(final View view) {
        Intent activityIntent = new Intent();
        activityIntent.putExtra("type", "viewed");
        activityIntent.setClass(this, AmountActivity.class);
        startActivity(activityIntent);
    }

    public void backTop(View view) {
        //滑动到顶部
//        mainPageFragment.getExpandableListView().smoothScrollToPosition(0);
        //直接到顶部
        mainPageFragment.getExpandableListView().setSelection(0);
    }

    private void downloadAndInstall(String url) {
        File appFile = NetUtils.downloadFile(url, "/update", getResources()
                .getString(R.string.app_name));
        openFile(appFile);
    }

    private void openFile(File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        startActivity(intent);
    }

    public void amountSelect(View view) {
        TextView txtAmount = (TextView) findViewById(R.id.txt_income_amount);
        List<String> productNames = new ArrayList<String>();
        for (int i = 0; i < SlideConstants.PRODUCT_LSIT.length; i++) {
            if (new BigDecimal(txtAmount.getText().toString())
                    .compareTo(SlideConstants.PRODUCT_LSIT[i].getAmount()) > 0)
                productNames.add(SlideConstants.PRODUCT_LSIT[i]
                        .getProductName());
        }
        String[] productNameArr = new String[productNames.size()];
        for (int i = 0; i < productNames.size(); i++) {
            productNameArr[i] = productNames.get(i);
        }
        new AlertDialog.Builder(this)
                .setTitle("请选择充值金额")
                .setItems(productNameArr,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int index) {
                                Button amountButton = (Button) findViewById(R.id.select_amount);
                                amountButton
                                        .setText(SlideConstants.PRODUCT_LSIT[index]
                                                .getProductName());
                            }
                        }).show();
    }

    /**
     * 菜单、返回键响应
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
	        exitBy2Click(); // 调用双击退出函数
	        return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * 双击退出函数
     */
    private static Boolean isExit = false;

    private void exitBy2Click() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务

        } else {
            SysApplication.getInstance().exit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(myFragment != null) {
            myFragment.initAccountInfo();
        }
    }

}
