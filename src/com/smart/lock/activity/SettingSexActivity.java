package com.smart.lock.activity;

import com.smart.lock.R;
import com.smart.lock.R.layout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class SettingSexActivity extends BaseActivity {

	private ImageView maleImageView, famaleImageView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_sex);
		maleImageView = (ImageView) this.findViewById(R.id.img_male);
		famaleImageView = (ImageView) this.findViewById(R.id.img_female);
	}

	public void maleSelect(View view) {
		maleImageView.setSelected(true);
		SharedPreferences commonSP = getSharedPreferences("common", MODE_MULTI_PROCESS );
		Editor editor = commonSP.edit();
		editor.putInt("sex", 1);
		editor.commit();
		
		this.startActivity(new Intent(this, SettingWizardActivity.class));
		this.finish();
	}

	public void femaleSelect(View view) {
		famaleImageView.setSelected(true);
		SharedPreferences commonSP = getSharedPreferences("common", MODE_MULTI_PROCESS );
		Editor editor = commonSP.edit();
		editor.putInt("sex", 2);
		editor.commit();
		
		this.startActivity(new Intent(this, SettingWizardActivity.class));
		this.finish();
	}
}
