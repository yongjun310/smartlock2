package com.smart.lock.fragment;

import java.math.BigDecimal;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.smart.lock.R;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.dto.AccountDTO;
import com.smart.lock.response.AccountResponse;
import com.smart.lock.utils.DeviceUtils;
import com.smart.lock.utils.NetUtils;
import com.smart.lock.utils.SecurityUtils;

public class ExchangeFragment extends Fragment {

	private static Gson gson = new Gson();

	private int accountId;

	private String mobileNo;

	private TextView txtAmount;

	private EditText mobileText;

	private RelativeLayout btnExchange;

	private Handler messageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				AccountDTO account = (AccountDTO) msg.obj;
				if (account != null) {
					txtAmount.setText(String.format("%.2f",
							account.getTotalAmount()));

					if (account.getTotalAmount()
							.compareTo(new BigDecimal("30")) >= 0) {
						btnExchange.setClickable(true);
						btnExchange.setBackgroundColor(getActivity()
								.getApplicationContext().getResources()
								.getColor(R.color.red));
					} else {
						btnExchange.setClickable(false);
						btnExchange.setBackgroundColor(getActivity()
								.getApplicationContext().getResources()
								.getColor(R.color.grey));
					}
				}
				break;
			case 2:
				Toast.makeText(getActivity(),
						SlideConstants.TOAST_INCOME_SERVER_BUSY,
						Toast.LENGTH_SHORT).show();
				break;
			case 3:
				Toast.makeText(getActivity(),
						SlideConstants.TOAST_INCOME_SERVER_ERROR,
						Toast.LENGTH_SHORT).show();
				break;
			case 4:
				Toast.makeText(getActivity(),
						SlideConstants.TOAST_INCOME_EXCHANGE_SUCCESS,
						Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}

		}
	};

	public ExchangeFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		SharedPreferences commonSP = getActivity().getSharedPreferences(
				"common", Context.MODE_MULTI_PROCESS );
		accountId = commonSP.getInt("accountId", -1);
		mobileNo = commonSP.getString("mobileNo", "");

		View rootView = inflater.inflate(R.layout.fragment_exchange, container,
				false);

		txtAmount = (TextView) rootView.findViewById(R.id.txt_income_amount);
		txtAmount.setText(SlideConstants.PRODUCT_LSIT[0].getProductName());
		mobileText = (EditText) rootView
				.findViewById(R.id.input_exchange_mobile);
		mobileText.setText(mobileNo);
		btnExchange = (RelativeLayout) rootView
				.findViewById(R.id.layout_btn_exchange);
		btnExchange.setClickable(false);
		btnExchange.setBackgroundColor(getActivity().getApplicationContext()
				.getResources().getColor(R.color.grey));

		new Thread(new Runnable() {

			@Override
			public void run() {
				initIncome();
			}
		}).start();

		return rootView;
	}

	private void initIncome() {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("time", new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()).toString()));
		params.add(new BasicNameValuePair("androidId", DeviceUtils
				.getAndroidId(getActivity().getApplicationContext())));

		SharedPreferences rsaSP = getActivity().getSharedPreferences(
				"rsa" + accountId, Context.MODE_MULTI_PROCESS );
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
					+ SlideConstants.SERVER_METHOD_INCOME, params);
			AccountResponse resp = gson
					.fromJson(respStr, AccountResponse.class);
			if (resp.getCode() == SlideConstants.SERVER_RETURN_SUCCESS
					&& resp.getData() != null) {
				Message msg = Message.obtain(messageHandler, 1, resp.getData());
				messageHandler.sendMessage(msg);
			} else {
				Message msg = Message.obtain(messageHandler, 2, resp.getMsg());
				messageHandler.sendMessage(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Message msg = Message.obtain(messageHandler, 3);
			messageHandler.sendMessage(msg);
		}
	}
}
