package com.smart.lock.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.smart.lock.R;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.dto.AccountActionLogDTO;
import com.smart.lock.pattern.CreateGesturePasswordActivity;
import com.smart.lock.pattern.EnsureGesturePasswordActivity;
import com.smart.lock.utils.*;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by john on 2015/6/3.
 */
public class SettingDetailActivity extends BaseActivity {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    private static final String ENABLED_ACCESSIBILITY_SERVICES = "enabled_accessibility_services";

    private ImageView lockBtn;

    private boolean closedSysLock = false;

    private TextView categorySetting;
    
    private boolean isClosing = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_detail_layout);
        SysApplication.getInstance().addActivity(this);
        DisplayUtils.setTitleAndBackBtn(this, "设置");
        categorySetting = (TextView) this.findViewById(R.id.category_setting);
        this.findViewById(R.id.img_tablist_line);
        this.findViewById(R.id.txt_drawer_myfoot);
        this.findViewById(R.id.txt_drawer_message);
        this.findViewById(R.id.category_setting_title);
        this.findViewById(R.id.txt_drawer_faq);
        this.findViewById(R.id.txt_drawer_feedback);
        this.findViewById(R.id.lock_txt);
        this.findViewById(R.id.pw_setting);
        checkNotiOpen();
        lockBtn = (ImageView) this.findViewById(R.id.lock_btn);
        lockBtn.setSelected(!SharedPreferencesUtils.getBoolSP(this, SharedPreferencesUtils.LOCK_CLOSE, false));

        refreshCategoryTV();
    }

    private void refreshCategoryTV() {
        String savedIdStr = SharedPreferencesUtils.getStringSP(this, SharedPreferencesUtils.CATEGORY_ID_SETTING, "");
        String[] savedIds = savedIdStr.split(",");
        String categorySettingStr = "已订阅：";
        boolean hasTitle = false;
        for (int index=0; index<savedIds.length; index++) {
            String curId = savedIds[index];
            if(!TextUtils.isEmpty(curId)) {
                switch (curId.charAt(0)) {
                    case '2':
                        categorySettingStr += (hasTitle?",":"") + "自然风景";
                        hasTitle = true;
                        break;
                    case '6':
                        categorySettingStr += (hasTitle?",":"") + "人文景观";
                        hasTitle = true;
                        break;
                    case '3':
                        categorySettingStr += (hasTitle?",":"") + "小清新";
                        hasTitle = true;
                        break;
                    case '1':
                        categorySettingStr += (hasTitle?",":"") + "萌物";
                        hasTitle = true;
                        break;
                    case '5':
                        categorySettingStr += (hasTitle?",":"") + "明星";
                        hasTitle = true;
                        break;
                    case '4':
                        categorySettingStr += (hasTitle?",":"") + "二次元";
                        hasTitle = true;
                        break;
                }
            }
        }
        categorySetting.setText(DisplayUtils.getSubTitle(categorySettingStr, 20));
    }

    private void checkNotiOpen() {
        boolean isNotiOpened = DeviceUtils.isSysSettingEnabled(this, ENABLED_NOTIFICATION_LISTENERS) ||
                DeviceUtils.isSysSettingEnabled(this, ENABLED_ACCESSIBILITY_SERVICES);
        if (!isNotiOpened) {
            this.findViewById(R.id.layout_drawer_message).setVisibility(View.VISIBLE);
        } else {
            this.findViewById(R.id.layout_drawer_message).setVisibility(View.GONE);
        }
    }

//    public void wizardClick(View view) {
//        Intent intent = new Intent();
//        intent.setClass(this, WizardActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//    }

    public void feedbackClick(View view) {
        Intent activityIntent = new Intent();
        activityIntent.setClass(this, WebViewActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Bundle bundle = new Bundle();
        bundle.putInt("type", SlideConstants.WEB_VIEW_FEEDBACK);
        bundle.putString("preActivity", "SettingDetailActivity");

        activityIntent.putExtras(bundle);
        startActivity(activityIntent);
    }

    public void categoryClick(View view) {
        ActionLogOperator.add(this, new AccountActionLogDTO(SharedPreferencesUtils.getIntSP(this, "accountId", 0), 52, 0));

        NetUtils.umengSelfEvent(this, "categoryClick");
        Intent activityIntent = new Intent();
        activityIntent.setClass(this, CategorySettingActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("preActivity", "SettingDetailActivity");
        activityIntent.putExtras(bundle);
        startActivity(activityIntent);
    }

    public void faqClick(View view) {
        Intent activityIntent = new Intent();
        activityIntent.setClass(this, WebViewActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Bundle bundle = new Bundle();
        bundle.putInt("type", SlideConstants.WEB_VIEW_FAQ);
        bundle.putString("preActivity", "SettingDetailActivity");

        activityIntent.putExtras(bundle);
        startActivity(activityIntent);
    }

    public void closeLockClick(View view) {
        ActionLogOperator.add(this, new AccountActionLogDTO(SharedPreferencesUtils.getIntSP(this, "accountId", 0), 51, 0));

        if(lockBtn.isSelected() && SharedPreferencesUtils.hasCreateGesturePW(this)) {
        	Intent intent = new Intent();
            intent.setClass(this, EnsureGesturePasswordActivity.class);
            startActivityForResult(intent, 0);
            isClosing = true;
        } else {
        	changeLockStatus();
        }
    }


    public void backClick(View view) {
        onBackPressed();
    }

    public void closeSysLockClick(View view) {
        //startActivity(new Intent("android.settings.SET_OR_CHANGE_LOCK_METHOD_REQUEST"));
        ActionLogOperator.add(this, new AccountActionLogDTO(SharedPreferencesUtils.getIntSP(this, "accountId", 0), 12, 0));

        NetUtils.umengSelfEvent(this, "closeSysLockClick");
        showConfirmDialog(R.drawable.close_sys_lock_alert, "设置锁屏");
    }

    public void openSysNotiClick(View view) {
        if (!DeviceUtils.isSysSettingEnabled(this, ENABLED_NOTIFICATION_LISTENERS) ||
                !DeviceUtils.isSysSettingEnabled(this, ENABLED_ACCESSIBILITY_SERVICES)) {
            int imgResId;
            if(RomUtils.isZTE()) {
                imgResId = R.drawable.zte_sys_noti;
            } else {
                imgResId = R.drawable.read_sys_noti_alert;
            }
            showConfirmDialog(imgResId, "消息通知");

            NetUtils.umengSelfEvent(this, "openSysNotiClick");
        }
    }

    public void skipClick(View view) {
        SharedPreferencesUtils.putBoolSP(this, SharedPreferencesUtils.IS_NEW, false);
        Intent intent = new Intent(this, LockActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("preActivity", "SettingDetailActivity");
        intent.putExtras(bundle);
        this.startActivity(intent);
        this.finish();
    }

    public void setPWClick(View view) {
        if(!SharedPreferencesUtils.hasCreateGesturePW(this)) {
            PageChangeUtils.startActivityWithParams(this, CreateGesturePasswordActivity.class);
        } else {
            PageChangeUtils.startActivityWithParams(this, SettingGesturePWActivity.class);
        }
    }

    private boolean isSysSettingEnabled(String setings) {
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                setings);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    private void showConfirmDialog(final int resId, final String title) {
        final Dialog dialog = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.alert_dialog);
        ImageView image = (ImageView) dialog.findViewById(R.id.image);
        Button btn = (Button) dialog.findViewById(R.id.btn);
        image.setImageResource(resId);
        ((TextView)dialog.findViewById(R.id.title)).setText(title);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if("消息通知".equals(title)) {
                    if(RomUtils.isZTE()) {
                        Intent localIntent = new Intent("android.settings.ACCESSIBILITY_SETTINGS");
                        startActivityForResult(localIntent, 1000);
                    } else {
                        try {
                            startActivityForResult(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"), 1001);
                        } catch (Exception e) {
                            Log.e("SettingDetailActivity", e.getStackTrace().toString());
                        }
                    }
                } else {
                    try {
                        //[todo miui]
                        if(RomUtils.isMIUI()) {
                            startActivityForResult(new Intent("android.settings.APPLICATION_DEVELOPMENT_SETTINGS"), 1002);
		    				/*Intent localIntent2 = new Intent("/");
				    	    localIntent2.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$LockSettingsActivity"));*/

                        } else {
                            Intent intent = new Intent("/");
                            ComponentName cm = new ComponentName("com.android.settings","com.android.settings.ChooseLockGeneric");
                            intent.setComponent(cm);
                            startActivityForResult(intent, 0);
                            closedSysLock = true;
                        }
                    } catch(Exception e) {
                        Log.e("SettingDetailActivity", e.getStackTrace().toString());
                    }
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void onResume() {
        super.onResume();
        refreshCategoryTV();
        checkNotiOpen();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            if(isClosing) {
            	   changeLockStatus();
            } 
        }
    }

	private void changeLockStatus() {
		if (!lockBtn.isSelected()) {
			getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
		} else {
			getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		}

		lockBtn.setSelected(!lockBtn.isSelected());
		SharedPreferencesUtils.putBoolSP(this,
				SharedPreferencesUtils.LOCK_CLOSE, !lockBtn.isSelected());

		NetUtils.umengSelfEvent(this, "open_lock",
				lockBtn.isSelected() ? "open" : "close");
	}
}