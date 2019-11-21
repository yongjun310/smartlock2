package com.smart.lock.activity;

import com.smart.lock.R;
import com.smart.lock.R.drawable;
import com.smart.lock.R.id;
import com.smart.lock.R.layout;
import com.smart.lock.utils.DisplayUtils;
import com.smart.lock.utils.SharedPreferencesUtils;
import com.smart.lock.utils.SysApplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.smart.lock.view.GalleryPoint;

public class WizardActivity extends BaseActivity {

	private int step;

	private ImageButton btn;
	
	private ImageButton skip_btn;
	
	private RelativeLayout bg;

    private GalleryPoint mSwithBtnContainer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏
		
		setContentView(R.layout.activity_wizard);
        mSwithBtnContainer = (GalleryPoint) findViewById(R.id.switcherbtn_container);
		
		SysApplication.getInstance().addActivity(this);
		
		step = 1;
		btn = (ImageButton) findViewById(R.id.btn_wizard_next);
		skip_btn = (ImageButton) findViewById(R.id.btn_wizard_next_up);
		
		DisplayUtils.enlargeClickArea(skip_btn, 50);
		
		btn.setVisibility(View.INVISIBLE);
		skip_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				skipNext();
			}
		});
		bg = (RelativeLayout) findViewById(R.id.layout_wizard_bg);
		for(int i=0; i<7; i++) {
			if (mSwithBtnContainer != null)
	        {
	            mSwithBtnContainer.addSwitchBtn(7);
	        }
		}
		mSwithBtnContainer.setSelectedSwitchBtn(0);
	}

	public void nextClick(View view) {
		step++;
		if (step >= 0)
        {
            if (mSwithBtnContainer != null)
            {
                mSwithBtnContainer.setSelectedSwitchBtn(step-1);
            }
        }
		switch (step) {
		case 1:
			bg.setBackgroundResource(R.drawable.guide_1);
			break;

		case 2:
			bg.setBackgroundResource(R.drawable.guide_2);
			break;

		case 3:
			bg.setBackgroundResource(R.drawable.guide_3);
			break;

		case 4:
			bg.setBackgroundResource(R.drawable.guide_4);
			break;

		case 5:
			bg.setBackgroundResource(R.drawable.guide_5);
			break;

		case 6:
			bg.setBackgroundResource(R.drawable.guide_6);
			break;

		case 7:
			bg.setBackgroundResource(R.drawable.guide_7);
			bg.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					
				}
			});
			skip_btn.setVisibility(View.INVISIBLE);
			btn.setVisibility(View.VISIBLE);
			break;

		default:
			skipNext();
			break;
		}
	}

	private void skipNext() {
		this.finish();
		SharedPreferencesUtils.putBoolSP(this, SharedPreferencesUtils.IS_NEW, false);
		step = 1;
		Intent activityIntent = new Intent();
		activityIntent.setClass(this, LockActivity.class);
		activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(activityIntent);
	}
}
