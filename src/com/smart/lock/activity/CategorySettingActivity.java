package com.smart.lock.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.smart.lock.R;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.utils.DeviceUtils;
import com.smart.lock.utils.DisplayUtils;
import com.smart.lock.utils.NetUtils;
import com.smart.lock.utils.SharedPreferencesUtils;
import com.smart.lock.utils.SysApplication;
import com.smart.lock.view.CustomProgressDialog;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by john on 2015/6/3.
 */
public class CategorySettingActivity extends BaseActivity {
    private ImageView ecyIV, humanIV, natureIV, starIV, xqxIV, mengIV;

    private String preActivity;

    private String categoryIds = "";

    private CustomProgressDialog progressDialog;

    private ImageButton backBtn;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_setting);
        SysApplication.getInstance().addActivity(this);
        Bundle bundle = this.getIntent().getExtras();
        if(bundle != null)
            preActivity = bundle.getString("preActivity");
        ecyIV = (ImageView)this.findViewById(R.id.img_mainTab_ecy);
        humanIV = (ImageView)this.findViewById(R.id.img_mainTab_humanity);
        natureIV = (ImageView)this.findViewById(R.id.img_mainTab_nature);
        starIV = (ImageView)this.findViewById(R.id.img_mainTab_star);
        xqxIV = (ImageView)this.findViewById(R.id.img_mainTab_xqx);
        mengIV = (ImageView)this.findViewById(R.id.img_mainTab_meng);
        findViewById(R.id.txt_mainTab_homenature);
        findViewById(R.id.txt_mainTab_humanity);
        findViewById(R.id.txt_mainTab_xqx);
        findViewById(R.id.txt_mainTab_meng);
        findViewById(R.id.txt_mainTab_star);
        findViewById(R.id.txt_mainTab_ecy);
        boolean isNew = SharedPreferencesUtils.getBoolSP(this, SharedPreferencesUtils.IS_NEW, true);
        humanIV.setSelected(isNew);
        natureIV.setSelected(isNew);
        mengIV.setSelected(isNew);

        starIV.setSelected(false);
        xqxIV.setSelected(isNew);
        ecyIV.setSelected(false);
        String savedIdStr = SharedPreferencesUtils.getStringSP(this, SharedPreferencesUtils.CATEGORY_ID_SETTING,"");
        String[] savedIds = savedIdStr.split(",");
        for (int index=0; index<savedIds.length; index++) {
            String curId = savedIds[index];
            if(!TextUtils.isEmpty(curId)) {
                switch (curId.charAt(0)) {
                    case '2':
                        natureIV.setSelected(true);
                        break;
                    case '6':
                        humanIV.setSelected(true);
                        break;
                    case '3':
                        xqxIV.setSelected(true);
                        break;
                    case '1':
                        mengIV.setSelected(true);
                        break;
                    case '5':
                        starIV.setSelected(true);
                        break;
                    case '4':
                        ecyIV.setSelected(true);
                        break;
                }
            }
        }

        DisplayUtils.setTitleAndBackBtn(this, "分类订阅");

        backBtn = (ImageButton) findViewById(R.id.btn_back);

        if (!"SettingDetailActivity".equals(preActivity)) {
            backBtn.setVisibility(View.GONE);
        }

    }

    public void backClick(View view) {
        onBackPressed();
    }

    public void tabItemClick(View view) {
        String tag = (String)view.getTag();
        switch (tag.charAt(0)){
            case '0':
                NetUtils.umengSelfEvent(this, "category_id1", natureIV.isSelected()?1:0);
                natureIV.setSelected(!natureIV.isSelected());
                break;
            case '1':
                NetUtils.umengSelfEvent(this, "category_id2", natureIV.isSelected()?1:0);
                humanIV.setSelected(!humanIV.isSelected());
                break;
            case '2':
                NetUtils.umengSelfEvent(this, "category_id3", natureIV.isSelected()?1:0);
                xqxIV.setSelected(!xqxIV.isSelected());
                break;
            case '3':
                NetUtils.umengSelfEvent(this, "category_id4", natureIV.isSelected()?1:0);
                mengIV.setSelected(!mengIV.isSelected());
                break;
            case '4':
                NetUtils.umengSelfEvent(this, "category_id5", natureIV.isSelected()?1:0);
                starIV.setSelected(!starIV.isSelected());
                break;
            case '5':
                NetUtils.umengSelfEvent(this, "category_id6", natureIV.isSelected()?1:0);
                ecyIV.setSelected(!ecyIV.isSelected());
                break;

        }
    }

    public void sureClick(View view) {
        int count = 0;
        if(natureIV.isSelected()) {
            count++;
            if(!TextUtils.isEmpty(categoryIds)) {
                categoryIds += ",";
            }
            categoryIds += "2";
        }
        if(humanIV.isSelected()) {
            count++;
            if(!TextUtils.isEmpty(categoryIds)) {
                categoryIds += ",";
            }
            categoryIds += "6";
        }
        if(xqxIV.isSelected()) {
            count++;
            if(!TextUtils.isEmpty(categoryIds)) {
                categoryIds += ",";
            }
            categoryIds += "3";
        }
        if(mengIV.isSelected()) {
            count++;
            if(!TextUtils.isEmpty(categoryIds)) {
                categoryIds += ",";
            }
            categoryIds += "1";
        }
        if(starIV.isSelected()) {
            count++;
            if(!TextUtils.isEmpty(categoryIds)) {
                categoryIds += ",";
            }
            categoryIds += "5";
        }
        if(ecyIV.isSelected()) {
            count++;
            if(!TextUtils.isEmpty(categoryIds)) {
                categoryIds += ",";
            }
            categoryIds += "4";
        }
        if(count < 3) {
            NetUtils.umengSelfEvent(this, "category_ensure", "count less than 3");
            Toast.makeText(this, SlideConstants.SELECT_THREE_CATEGORYS, Toast.LENGTH_SHORT).show();
        } else {
            SharedPreferencesUtils.putBoolSP(this, SharedPreferencesUtils.IS_NEW, false);
            SharedPreferencesUtils.putStringSP(CategorySettingActivity.this,
                    SharedPreferencesUtils.CATEGORY_ID_SETTING, categoryIds);

            NetUtils.umengSelfEvent(this, "category_ensure", categoryIds);
            if(DeviceUtils.isNetworkAvailable(this)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        NetUtils.updateCategory(CategorySettingActivity.this, categoryIds);
                    }
                }).start();
            }
            CategorySettingActivity.this.finish();
            if("BoxOpenActivity".equals(preActivity) || "LoginActivity".equals(preActivity)) {
                Intent activityIntent = new Intent();
                activityIntent.setClass(CategorySettingActivity.this, SettingWizardActivity.class);
                CategorySettingActivity.this.startActivity(activityIntent);
            }
        }
    }


    @Override
    public void onStop() {
        super.onStop();
    }
}