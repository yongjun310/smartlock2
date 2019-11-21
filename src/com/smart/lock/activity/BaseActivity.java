package com.smart.lock.activity;

import com.smart.lock.utils.DisplayUtils;
import com.umeng.analytics.MobclickAgent;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;

public abstract class BaseActivity extends ActionBarActivity {
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	}

    @Override
    public View findViewById(int id){
        View v = super.findViewById(id);
        //这样会同时将EditText、Button等一起改变字体了。如果想要只改变TextView，可以用v.getClass().getName().equals(TextView.class.getName())
        if(v != null && v instanceof TextView){
            //在这里设置TextView的字体，设置字体代码可以百度
            DisplayUtils.setFont(this, (TextView) v);
        }
        return v;
    }

    @Override
    public void onResume() {
    	super.onResume();
    	MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
    	super.onPause();
    	MobclickAgent.onPause(this);
    }
    
    @Override
    public void onStop() {
        super.onStop();
    }
}