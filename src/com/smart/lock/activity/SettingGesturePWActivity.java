package com.smart.lock.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.smart.lock.R;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.global.LockApplication;
import com.smart.lock.pattern.CreateGesturePasswordActivity;
import com.smart.lock.pattern.EnsureGesturePasswordActivity;
import com.smart.lock.utils.DisplayUtils;
import com.smart.lock.utils.SharedPreferencesUtils;
import com.smart.lock.utils.SysApplication;

/**
 * Created by john on 2015/6/3.
 */
public class SettingGesturePWActivity extends BaseActivity {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    private static final String ENABLED_ACCESSIBILITY_SERVICES = "enabled_accessibility_services";

    private ImageView gestureBtn;

    private boolean closedSysLock = false;

    private TextView categorySetting;

    private View settingLayout;

    private boolean isClosing = false;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_gesture_layout);
        SysApplication.getInstance().addActivity(this);
        DisplayUtils.setTitleAndBackBtn(this, "设置手势密码");
        categorySetting = (TextView) this.findViewById(R.id.category_setting);
        this.findViewById(R.id.geture_txt);
        this.findViewById(R.id.gesture_setting);
        settingLayout = this.findViewById(R.id.gesture_setting_layout);
        gestureBtn = (ImageView) this.findViewById(R.id.gesture_btn);
        boolean opened = !SharedPreferencesUtils.getBoolSP(this, SharedPreferencesUtils.GESTURE_CLOSE, false);
        gestureBtn.setSelected(opened);
        settingLayout.setVisibility(opened?View.VISIBLE:View.GONE);

    }

    public void closeGestureClick(View view) {
        isClosing = true;
        setPWClick(null);
    }


    public void backClick(View view) {
        onBackPressed();
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
        Intent intent = new Intent();
        intent.setClass(this, EnsureGesturePasswordActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
            if(isClosing) {
                gestureBtn.setSelected(!gestureBtn.isSelected());
                settingLayout.setVisibility(gestureBtn.isSelected() ? View.VISIBLE : View.GONE);
                SharedPreferencesUtils.putBoolSP(this, SharedPreferencesUtils.GESTURE_CLOSE, !gestureBtn.isSelected());
                SharedPreferencesUtils.removeCreatedGesturePW(this);
                Toast.makeText(this, SlideConstants.CLOSE_GESTURE_PW_SUCCESS,
                        Toast.LENGTH_SHORT).show();
                this.finish();
            } else {
                Intent intent = new Intent();
                intent.setClass(this, CreateGesturePasswordActivity.class);
                startActivityForResult(intent, 0);
            }
        } else if(resultCode == Activity.RESULT_CANCELED) {
            isClosing = false;
            LockApplication.getInstance(this).getLockPatternUtils().getsHaveNonZeroPatternFile().set(true);
        } else {
            isClosing = false;
            this.finish();
        }
    }
}