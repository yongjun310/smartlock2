package com.smart.lock.wxapi;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.Toast;
import com.google.gson.Gson;
import com.smart.lock.R;
import com.smart.lock.activity.LoginActivity;
import com.smart.lock.common.SSOTypeEnum;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.response.BooleanResponse;
import com.smart.lock.utils.*;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.umeng.socialize.weixin.view.WXCallbackActivity;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class WXEntryActivity extends WXCallbackActivity {
    private IWXAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        api = WXAPIFactory.createWXAPI(this, SlideConstants.WECHAT_APPID, false);
        api.registerApp(SlideConstants.WECHAT_APPID);
        api.handleIntent(getIntent(), this);
    }

//    @Override
//    public void onReq(BaseReq req) {
//    }
//
//    @Override
//    public void onResp(BaseResp resp) {
//        int result = 0;
//        switch (resp.errCode) {
//            case BaseResp.ErrCode.ERR_OK:
//                SharedPreferences commSP = getSharedPreferences("common", MODE_MULTI_PROCESS);
//                int accountId = commSP.getInt("accountId", -1);
//                int contentId = commSP.getInt("shareContentId", -1);
//                int viewType = commSP.getInt("shareType", -1);
//                String shareAmount = commSP.getString("shareAmount", null);
////			ActionLogOperator.add(this, new AccountActionLogDTO(accountId,
////	                ActionTypeEnum.SHARE_SUCCESS, contentId));
////			ThreadHelper.getInstance(this).addTask(SlideConstants.THREAD_POST_LOG);
//                Editor edit = commSP.edit();
//                edit.putString("shareAmount", null);
//                edit.commit();
//
////                boolean isWXLogin = SharedPreferencesUtils.getBoolSP(this, LoginActivity.IS_WX_LOGIN, false);
////                if (isWXLogin) {
////                    SharedPreferencesUtils.putBoolSP(this, LoginActivity.IS_WX_LOGIN, false);
////                    new Thread(new Runnable() {
////                        @Override
////                        public void run() {
////                            postSSOInfos(SSOTypeEnum.WEIXIN, info);
////                        }
////                    }).start();
////                }
//                result = R.string.errcode_success;
//                break;
//            case BaseResp.ErrCode.ERR_USER_CANCEL:
//                result = R.string.errcode_cancel;
//                break;
//            case BaseResp.ErrCode.ERR_AUTH_DENIED:
//                result = R.string.errcode_deny;
//                break;
//            default:
//                result = R.string.errcode_unknown;
//                break;
//        }
//
//        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
//        finish();
//        overridePendingTransition(R.anim.change_in, R.anim.change_out);
//    }

}
