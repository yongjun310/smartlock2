package com.smart.lock.activity;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.*;
import com.smart.lock.R;
import com.smart.lock.service.LockService;
import com.smart.lock.utils.*;
import com.umeng.analytics.MobclickAgent;

public class SettingWizardActivity extends BaseActivity {
    private static final String TAG = SettingWizardActivity.class.getSimpleName();

    private static final String TAG_PRE = "[" + TAG + "] ";

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    private static final String ENABLED_ACCESSIBILITY_SERVICES = "enabled_accessibility_services";

    private ImageButton openSysNotiBtn;

    private ImageButton closeSysLockBtn, openFloatBtn, selfStartBtn;

    private boolean closedSysLock = false, openFloatBtnClicked = false, selfStartBtnClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏

        setContentView(R.layout.activity_setting_wizard);
        findViewById(R.id.title);
        SysApplication.getInstance().addActivity(this);

        SharedPreferencesUtils.putStringSP(this, "firstLockActivity", "SettingWizardActivity");
        PageChangeUtils.changeLockServiceState(this, LockService.STATE_UPDATE_DATE);
        
        openSysNotiBtn = (ImageButton) findViewById(R.id.btn_wizard_open_sys_noti);
        closeSysLockBtn = (ImageButton) findViewById(R.id.btn_wizard_close_sys_lock);
        openFloatBtn = (ImageButton) findViewById(R.id.btn_wizard_open_float);
        selfStartBtn = (ImageButton) findViewById(R.id.btn_wizard_self_start);
        if (RomUtils.isFlyme()) {
            openFloatBtn.setBackgroundResource(R.drawable.open_float_selector_meizu);
            openFloatBtn.setVisibility(View.VISIBLE);
            closeSysLockBtn.setVisibility(View.GONE);
        }
        if (RomUtils.isMIUI()) {
            openFloatBtn.setVisibility(View.VISIBLE);
            selfStartBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        openSysNotiBtn.setSelected(DeviceUtils.isSysSettingEnabled(this, ENABLED_NOTIFICATION_LISTENERS) ||
                DeviceUtils.isSysSettingEnabled(this, ENABLED_ACCESSIBILITY_SERVICES));
//        if (RomUtils.isFlyme()) {
//            closeSysLockBtn.setVisibility(View.GONE);
//        }
        closeSysLockBtn.setSelected(closedSysLock);
        openFloatBtn.setSelected(openFloatBtnClicked);
        selfStartBtn.setSelected(selfStartBtnClicked);
    }

    public void openSysNotiClick(View view) {
        if (!DeviceUtils.isSysSettingEnabled(this, ENABLED_NOTIFICATION_LISTENERS) ||
                !DeviceUtils.isSysSettingEnabled(this, ENABLED_ACCESSIBILITY_SERVICES)) {
            int imgResId;
            if (RomUtils.isZTE()) {
                imgResId = R.drawable.zte_sys_noti;
            } else if (RomUtils.isFlyme()) {
                imgResId = R.drawable.flyme_noti;
            } else if (RomUtils.isHTC()) {
                imgResId = R.drawable.htc_noti;
            } else if (RomUtils.isHuawei()) {
                imgResId = R.drawable.huawei_noti;
            } else if (RomUtils.isMIUI()) {
                imgResId = R.drawable.mi_noti;
            } else if (RomUtils.isNubia()) {
                imgResId = R.drawable.nubia_noti;
            } else {
                imgResId = R.drawable.read_sys_noti_alert;
            }
            showConfirmDialog(imgResId, "消息通知");
        }

        NetUtils.umengSelfEvent(this, "init_step1");
    }

    public void closeSysLockClick(View view) {
        closedSysLock = true;
        //startActivity(new Intent("android.settings.SET_OR_CHANGE_LOCK_METHOD_REQUEST"));
        int imgResId;
        if (RomUtils.isZTE()) {
            imgResId = R.drawable.zte_close_lock;
        } else if (RomUtils.isFlyme()) {
            imgResId = R.drawable.flyme_other;//flyme_other;
        } else if (RomUtils.isHTC()) {
            imgResId = R.drawable.htc_close_lock;
        } else if (RomUtils.isHuawei()) {
            imgResId = R.drawable.huawei_clock_lock;
        } else if (RomUtils.isMIUI()) {
            imgResId = R.drawable.mi_close_lock;
        } else if (RomUtils.isNubia()) {
            imgResId = R.drawable.nubia_close_lock;
        } else {
            imgResId = R.drawable.close_sys_lock_alert;
        }
        NetUtils.umengSelfEvent(this, "init_step2");
        showConfirmDialog(imgResId, "设置锁屏");
    }

    public void openFloatClick(View view) {
        openFloatBtnClicked = true;
        int imgResId;
        if (RomUtils.isFlyme()) {
            imgResId = R.drawable.flyme_other;
            showConfirmDialog(imgResId, "开启以下权限");
        } else {
            imgResId = R.drawable.mi_float;
            showConfirmDialog(imgResId, "设置悬浮窗");
        }
        NetUtils.umengSelfEvent(this, "init_step3");
    }

    public void selfStartClick(View view) {
        selfStartBtnClicked = true;
        int imgResId;
        if (RomUtils.isFlyme()) {
            imgResId = R.drawable.flyme_other;
        } else {
            imgResId = R.drawable.mi_self_start;
        }
        showConfirmDialog(imgResId, "设置自启动");
        NetUtils.umengSelfEvent(this, "init_step4");
    }


    public void skipClick(View view) {
        SharedPreferencesUtils.putBoolSP(this, SharedPreferencesUtils.IS_NEW, false);
        /*Intent intent = new Intent(this, LockActivity.class);
        Bundle bundle = new Bundle();
		bundle.putString("preActivity", "SettingWizardActivity");
		intent.putExtras(bundle);
		this.startActivity(intent);
		this.finish();*/
        NetUtils.umengSelfEvent(this, "init_start_now");
        this.finish();
        PageChangeUtils.changeLockServiceState(this, LockService.STATE_SHOW);
    }

    private void logNLS(Object object) {
        Log.i(TAG, TAG_PRE + object);
    }

    private void showConfirmDialog(final int resId, final String title) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.alert_dialog);
        TextView titleTV = (TextView) dialog.findViewById(R.id.title);
        titleTV.setText(title);
        ImageView image = (ImageView) dialog.findViewById(R.id.image);
        Button btn = (Button) dialog.findViewById(R.id.btn);
        image.setImageResource(resId);
        btn.setOnClickListener(new View.OnClickListener() {

                                   @Override
                                   public void onClick(View arg0) {
                                       if ("消息通知".equals(title)) {
                                           if (RomUtils.isZTE()) {
                                               Intent localIntent = new Intent("android.settings.ACCESSIBILITY_SETTINGS");
                                               startActivityForResult(localIntent, 1000);
                                           } else {
                                               try {
                                                   startActivityForResult(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"), 1001);
                                               } catch (Exception e) {
                                                   Log.e(TAG, e.getStackTrace().toString());
                                               }
                                           }
                                       } else if ("设置锁屏".equals(title)) {
                                           try {
                                               //[todo miui]
                                               if (RomUtils.isMIUI()) {
                                                   startActivityForResult(new Intent("android.settings.APPLICATION_DEVELOPMENT_SETTINGS"), 1002);
		/*Intent localIntent2 = new Intent("/");
		localIntent2.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$LockSettingsActivity"));*/

                                               } else {
                                                   Intent intent = new Intent("/");
                                                   ComponentName cm = new ComponentName("com.android.settings", "com.android.settings.ChooseLockGeneric");
                                                   intent.setComponent(cm);
                                                   startActivityForResult(intent, 0);
//                                                   closedSysLock = true;
                                               }
                                           } catch (Exception e) {
                                               Log.e(TAG, e.getStackTrace().toString());
                                           }
                                       } else if ("设置悬浮窗".equals(title) || "开启以下权限".equals(title)) {
                                           if (RomUtils.isMIUI()) {
                                               PackageManager pm = SettingWizardActivity.this.getPackageManager();
                                               PackageInfo info = null;
                                               try {
                                                   info = pm.getPackageInfo(SettingWizardActivity.this.getPackageName(), 0);
                                               } catch (PackageManager.NameNotFoundException e) {
                                                   e.printStackTrace();
                                               }
                                               Intent i = new Intent("miui.intent.action.APP_PERM_EDITOR");
                                               i.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                                               i.putExtra("extra_pkgname", SettingWizardActivity.this.getPackageName());
                                               try {
                                                   SettingWizardActivity.this.startActivity(i);
                                               } catch (Exception e) {
                                                   Toast.makeText(SettingWizardActivity.this, "只有MIUI才可以设置哦", Toast.LENGTH_SHORT).show();
                                               }
                                           } else {
                                               Intent localIntent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS",
                                                       Uri.parse("package:" + SettingWizardActivity.this.getPackageName()));

                                               localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                               //localIntent.setAction("ACTION_APPLICATION_DETAILS_SETTINGS");
                                               //localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                                               //localIntent.putExtra("com.android.settings.ApplicationPkgName", SettingWizardActivity.this.getPackageName());
                                               //localIntent.setData(Uri.parse("package:" + SettingWizardActivity.this.getPackageName()));
                                               try {
                                                   SettingWizardActivity.this.startActivity(localIntent);
                                               } catch (Exception e) {
                                                   e.printStackTrace();
                                               }
                                           }
                                       } else if ("设置自启动".equals(title)) {
                                           if (RomUtils.isMIUI()) {
                                               PackageManager pm = SettingWizardActivity.this.getPackageManager();
                                               PackageInfo info = null;
                                               try {
                                                   info = pm.getPackageInfo(SettingWizardActivity.this.getPackageName(), 0);
                                               } catch (PackageManager.NameNotFoundException e) {
                                                   e.printStackTrace();
                                               }
                                               Intent i = new Intent("miui.intent.action.APP_PERM_EDITOR");
                                               i.setClassName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");
                                               i.putExtra("extra_package_uid", info.applicationInfo.uid);
                                               try {
                                                   SettingWizardActivity.this.startActivity(i);
                                               } catch (Exception e) {
                                                   Toast.makeText(SettingWizardActivity.this, "只有MIUI才可以设置哦", Toast.LENGTH_SHORT).show();
                                               }
                                           } else {
                                               Intent localIntent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS",
                                                       Uri.parse("package:" + SettingWizardActivity.this.getPackageName()));

                                               localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                               //localIntent.setData(Uri.parse("package:" + SettingWizardActivity.this.getPackageName()));
                                               try {
                                                   SettingWizardActivity.this.startActivity(localIntent);
                                               } catch (Exception e) {
                                                   e.printStackTrace();
                                               }
                                           }
                                       }
                                       dialog.dismiss();
                                   }
                               }

        );
        dialog.show();
    }

}
