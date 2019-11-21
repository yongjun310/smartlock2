package com.smart.lock.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.smart.lock.R;
import com.smart.lock.dto.AccountActionLogDTO;
import com.smart.lock.utils.*;
import com.umeng.analytics.MobclickAgent;

public class BoxOpenActivity extends BaseActivity implements OnPageChangeListener {
    /**
     * ViewPager
     */
    private ViewPager viewPager;

    /**
     * 装点点的ImageView数组
     */
    private ImageView[] tips;

    /**
     * 装ImageView数组
     */
    private ImageView[] mImageViews;

    /**
     * 图片资源id
     */
    private int[] imgIdArray;

    private boolean isLogin, isNew;

    private boolean isDoningTempLogin = false, needRealLogin = false;

    private int accountId;

    private String mobileNo;

    public void loginClick(View view) {
        NetUtils.umengSelfEvent(this, "box_open_regist");
        ActionLogOperator.add(this, new AccountActionLogDTO(accountId, 10, 0));
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("preActivity", "BoxOpenActivity");
        startActivity(intent);
        this.finish();
    }

    public void skipClick() {
        NetUtils.umengSelfEvent(this, "box_open_later_regist");
        ActionLogOperator.add(this, new AccountActionLogDTO(accountId, 11, 0));
        PageChangeUtils.redirectMain(this);
        this.finish();
//        Toast.makeText(this, SlideConstants.TOAST_LOGIN_SKIP_TIPS,
//                Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_open);

        SysApplication.getInstance().addActivity(this);

        LinearLayout group = (LinearLayout) findViewById(R.id.viewGroup);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        TextView idleTextView = (TextView) findViewById(R.id.idle_textview);
        DisplayUtils.enlargeClickArea(idleTextView, 10);
        //group.setBackground(this.getResources().getDrawable(R.drawable.bg));



		imgIdArray = new int[]{R.drawable.guide_1, R.drawable.guide_2, R.drawable.guide_3, R.drawable.guide_4, R.drawable.guide_5};


		tips = new ImageView[imgIdArray.length];
		for(int i=0; i<tips.length; i++){
			ImageView imageView = new ImageView(this);
	    	imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
	    	tips[i] = imageView;
	    	if(i == 0){
	    		tips[i].setBackgroundResource(R.drawable.dot_current);
	    	}else{
	    		tips[i].setBackgroundResource(R.drawable.dot);
	    	}

	    	LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(new ViewGroup.LayoutParams(15,
                    15));
	    	layoutParams.leftMargin = DisplayUtils.dip2px(this, 2);
	    	layoutParams.rightMargin = DisplayUtils.dip2px(this, 2);
	    	group.addView(imageView, layoutParams);
		}


		mImageViews = new ImageView[imgIdArray.length];
		for(int i=0; i<mImageViews.length; i++){
			ImageView imageView = new ImageView(this);
			mImageViews[i] = imageView;
			imageView.setBackgroundResource(imgIdArray[i]);
		}

		viewPager.setAdapter(new MyAdapter());
		viewPager.setOnPageChangeListener(this);
		viewPager.setCurrentItem((mImageViews.length) * 100);

        SharedPreferences commonSP = getSharedPreferences("common",
                MODE_MULTI_PROCESS );
        isLogin = commonSP.getBoolean("isLogin", false);
        isNew = SharedPreferencesUtils.getBoolSP(this, SharedPreferencesUtils.IS_NEW, true);
        accountId = commonSP.getInt("accountId", -1);
        mobileNo = commonSP.getString("mobileNo", "");

        if (!isNew && accountId >= 0) {
            boolean bindMobile = this.getIntent().getBooleanExtra("bindMobile",
                    false);

            if (!bindMobile) {
                PageChangeUtils.redirectMain(this);
                return;
            }
        } else {
            if (DeviceUtils.isNetworkAvailable(this)) {
                isDoningTempLogin = true;
                final ThreadHelper threadHelper = ThreadHelper.getInstance(this);
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        NetUtils.tempLogin(BoxOpenActivity.this, isNew, threadHelper);
                    }
                }).start();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SharedPreferencesUtils.getBoolSP(this, "isLogin", false)) {
            PageChangeUtils.redirectMain(this);
        }
    }

    public class MyAdapter extends PagerAdapter{

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(View container, int position, Object object) {
            ((ViewPager)container).removeView(mImageViews[position % mImageViews.length]);

        }

        /**
         */
        @Override
        public Object instantiateItem(View container, int position) {
            ((ViewPager)container).addView(mImageViews[position % mImageViews.length], 0);
            return mImageViews[position % mImageViews.length];
        }



    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageSelected(int arg0) {
        NetUtils.umengSelfEvent(this, "box_open_fling");
        setImageBackground(arg0 % mImageViews.length);
    }

    /**
     * @param selectItems
     */
    private void setImageBackground(int selectItems){
        for(int i=0; i<tips.length; i++){
            if(i == selectItems){
                tips[i].setBackgroundResource(R.drawable.dot_current);
            }else{
                tips[i].setBackgroundResource(R.drawable.dot);
            }
        }
    }


    public void temploginClick(View view) {
        skipClick();
    }


}
