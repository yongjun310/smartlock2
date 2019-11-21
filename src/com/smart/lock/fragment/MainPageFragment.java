package com.smart.lock.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.*;

import com.smart.lock.R;
import com.smart.lock.activity.LoginActivity;
import com.smart.lock.activity.WebViewActivity;
import com.smart.lock.adapter.CommonListViewAdapter;
import com.smart.lock.adapter.MyExpandableListAdapter;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.dto.AccountActionLogDTO;
import com.smart.lock.dto.CampaignDTO;
import com.smart.lock.dto.ContentDTO;
import com.smart.lock.handler.ListEventHandler;
import com.smart.lock.utils.*;
import com.smart.lock.view.CustomProgressDialog;
import com.smart.lock.view.PinnedHeaderExpandableListView;
import com.umeng.analytics.MobclickAgent;

import java.math.BigDecimal;
import java.util.*;

public class MainPageFragment extends Fragment implements
        ExpandableListView.OnChildClickListener,
        ExpandableListView.OnGroupClickListener,
        ExpandableListView.OnScrollListener,
        PinnedHeaderExpandableListView.OnHeaderUpdateListener {

    private WebView webView;

    private Activity ctx;

    private PinnedHeaderExpandableListView expandableListView;

    public PinnedHeaderExpandableListView getExpandableListView() {
        return this.expandableListView;
    }

    private ListEventHandler onItemClickHandler;

    private CommonListViewAdapter commonListViewAdapter;

    private int curIndex = 0;

    private List<Map<String, String>> scriptData;

    private List<ContentDTO> contentDTOList;

    private int EACH_LOAD_COUNTS = 10;

    private int size;

    private static final int LOAD_START = 0;

    private static final int LOAD_OVER = 1;

    private CustomProgressDialog progressDialog;
    
    private boolean hasRecommand = false;

    private String sql;
    private List<String> groupList = new ArrayList<String>();

    public List<String> getGroupList() {
        return groupList;
    }

    private ArrayList<List<ContentDTO>> childList = new ArrayList<List<ContentDTO>>();

    public ArrayList<List<ContentDTO>> getChildList() {
        return this.childList;
    }

    private MyExpandableListAdapter adapter;

    private ImageView noContentImg, btnTop;

    private TextView noContentTV;

    public MainPageFragment() {
    }

    public boolean isHasRecommand() {
        return hasRecommand;
    }

    public void setHasRecommand(boolean hasRecommand) {
        this.hasRecommand = hasRecommand;
    }

    public MyExpandableListAdapter getAdapter() {
        return adapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.main_page, container,
                false);
        ctx = (Activity) inflater.getContext();
        expandableListView = (PinnedHeaderExpandableListView) rootView.findViewById(R.id.expandablelist);

        noContentImg = (ImageView) rootView.findViewById(R.id.no_content_img);
        btnTop = (ImageView) rootView.findViewById(R.id.btn_main_top);
        noContentTV = (TextView) rootView.findViewById(R.id.no_content_hint);
        DisplayUtils.setFont(ctx, (TextView) rootView.findViewById(R.id.page_title));
        DisplayUtils.setFont(ctx, noContentTV);
        progressDialog = CustomProgressDialog.createDialog(ctx);
        // Set a listener to be invoked when the list should be refreshed.
        new Thread(new Runnable() {
            @Override
            public void run() {
                initData();
                //keep progress until new contents come
                if(groupList.size() > 0)
                	dismissProgressDialog();
                ctx.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						initListView();
					}
				});
            }
        }).start();
        showProgressDialog();
        return rootView;
    }


    /* 定义一个Handler，用于处理下载线程与UI间通讯 */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case LOAD_START:
                	if(progressDialog != null)
                		progressDialog.show();// 显示进度对话框
                    break;

                case LOAD_OVER:
                	if(progressDialog != null)
                		progressDialog.dismiss();// 显示进度对话框
                    break;
            }
        }
    };

    public void initListView() {
        adapter = new MyExpandableListAdapter(ctx, groupList, childList);

        expandableListView.setAdapter(adapter);// 展开所有group
        for (int i = 0, count = expandableListView.getCount(); i < count; i++) {
            expandableListView.expandGroup(i);
        }
        expandableListView.setOnHeaderUpdateListener(MainPageFragment.this);
        expandableListView.setOnChildClickListener(MainPageFragment.this);
        expandableListView.setOnGroupClickListener(MainPageFragment.this);
        expandableListView.setOnScrollListener(MainPageFragment.this);
    }

    /**
     * 显示对话框
     *
     * @see [类、类#方法、类#成员]
     */
    public void showProgressDialog() {
        handler.sendEmptyMessage(LOAD_START);
    }

    /**
     * 关闭对话框
     *
     * @see [类、类#方法、类#成员]
     */
    protected void dismissProgressDialog() {
        handler.sendEmptyMessage(LOAD_OVER);
    }

    /**
     * InitData
     */
    public void initData() {
        boolean hasContent = false;
        groupList = ContentsDataOperator.loadContentsDate(ctx);
        //不改变排序下的去重
        List<String> ids = new ArrayList<String>();
        Set<String> uniqueIdSet = new HashSet<String>();
        uniqueIdSet.addAll(groupList);
        ids.addAll(groupList);
        groupList.clear();
        
        String serverTime = SharedPreferencesUtils.getStringSP(ctx, SharedPreferencesUtils.SERVER_TIME, null);
        if (serverTime == null)
            serverTime = DisplayUtils.formatDateTimeString(Calendar.getInstance().getTime());
        serverTime = serverTime.substring(0,10);
        String sql = "SELECT * FROM campaign WHERE date(startTime) <= date('" + serverTime + "') and date(endTime) >= date('" + serverTime + "')"
                + "and status = 1 ORDER BY priority,startTime, id DESC";
        List<CampaignDTO> campaignDTOList = CampaignDataOperator.load(ctx, sql);
        if(campaignDTOList.size() > 0) {
        	groupList.add("热门推荐");
        	hasRecommand = true;
        }
        for (String group : ids) {
            if (uniqueIdSet.contains(group)) {
                groupList.add(group);
                uniqueIdSet.remove(group);
            }
        }
        for (String date : groupList) {
            if ("热门推荐".equals(date))
                continue;
            List<ContentDTO> tempList = ContentsDataOperator.loadContentsByDate(ctx, date);
            Log.d("childlsit init", "[date]" + date + "[size]" + tempList.size());
            DisplayUtils.resortContent(tempList);
            if (tempList != null && tempList.size() > 0) {
                hasContent = true;
            }
            childList.add(tempList);
        }

        hasContentChange(hasContent);
    }

    public void hasContentChange(final boolean hasContent) {
        ctx.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                expandableListView.setVisibility(hasContent ? View.VISIBLE : View.GONE);
                noContentTV.setVisibility(hasContent ? View.GONE : View.VISIBLE);
                noContentImg.setVisibility(hasContent ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void reload() {
        size = contentDTOList.size();
        for (int index = 0; index < curIndex; index++) {
            loadListData(index);
        }
    }

	/*private void loadMore() {
        size = contentDTOList.size();
		for(int index = 0;index<EACH_LOAD_COUNTS &&  curIndex < size; index++, curIndex++) {
			loadListData(curIndex);
		}
		if(curIndex == size) {
			expandableListView.setHasLoadAll(true);
		}
	}*/

    public void dismissDialog() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void loadListData(int cusor) {
        Map<String, String> scriptRow = new HashMap<String, String>();
        if (cusor >= contentDTOList.size())
            return;
        ContentDTO contentDTO = contentDTOList.get(cusor);
        if (FileUtils.getFileSize(contentDTO.getLocalPath()) <= 0) {
            return;
        }
        String rewardType = "0";
        if (contentDTO.getBonusAmount().compareTo(new BigDecimal(0)) > 0) {
            rewardType = "" + contentDTO.getType();
        }
        scriptRow.put("favorite", contentDTO.isFavorite() ? "1" : "0");
        scriptRow.put("title", contentDTO.getTitle());
        scriptRow.put("rewardType", rewardType);
        scriptRow.put("imgUri", contentDTO.getLocalPath());
        scriptData.add(scriptRow);
    }

	/*private class GetDataTask extends AsyncTask<Void, Void, String[]> {

		@Override
		protected String[] doInBackground(Void... params) {
			// Simulates a background job.
			try {
				Thread.sleep(800);
			} catch (InterruptedException e) {
				Log.e("threadException", e.toString());
			}
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {
			// Call onRefreshComplete when the list has been refreshed.
			expandableListView.onRefreshComplete();
			super.onPostExecute(result);
		}
	}*/

    /**
     * 数据源
     *
     * @author Administrator
     */

    @Override
    public boolean onGroupClick(final ExpandableListView parent, final View v,
                                int groupPosition, final long id) {

        return false;
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v,
                                int groupPosition, int childPosition, long id) {
        if (groupPosition == 0 && hasRecommand) {
            CampaignDTO campaignDTO = adapter.getCampaignDTOList().get(childPosition);
            Intent activityIntent = new Intent();
            activityIntent.setClass(ctx, WebViewActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            Bundle bundle = new Bundle();
            bundle.putInt("type", SlideConstants.WEB_VIEW_RECOMMAND);
            bundle.putString("link", campaignDTO.getLink());
            bundle.putString("title", campaignDTO.getTitle());
            bundle.putString("preActivity", "MainActivity");
            activityIntent.putExtras(bundle);
            startActivity(activityIntent);

            NetUtils.umengSelfEvent(ctx, "first_page_click_image", "campaign");
        } else {
            int position = groupPosition;
        	if(hasRecommand)
        		position = groupPosition-1;
            ContentDTO content = childList.get(position).get(childPosition);

            ActionLogOperator.add(getActivity(), new AccountActionLogDTO(SharedPreferencesUtils.getIntSP(getActivity(), "accountId", 0), 62, content.getId()));

            PageChangeUtils.redirectFullImageView(getActivity(), content, "MainPageFragment", -1);

            if (content.getLocalPath() == null || content.getLocalPath().length() == 0) {
                content.setLocalPath(ContentsDataOperator.getById(getActivity(), content.getId()).getLocalPath());
                //Toast.makeText(getActivity(), "图片下载中，请耐心等待", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            }

            NetUtils.umengSelfEvent(ctx, "first_page_click_image", "content");

        }
        return false;
    }

    @Override
    public View getPinnedHeader() {
        View headerView = (ViewGroup) ((Activity) ctx).getLayoutInflater().inflate(R.layout.group, null);
        headerView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, DisplayUtils.dip2px(ctx, 44)));
        TextView headText = (TextView) headerView.findViewById(R.id.group);

        if (groupList.size() > 0) {
            String text = groupList.get(0);
            headText.setText(text);
        }
        return headerView;
    }

    @Override
    public void updatePinnedHeader(View headerView, int firstVisibleGroupPos) {
        String firstVisibleGroup = (String) adapter.getGroup(firstVisibleGroupPos);
        TextView textView = (TextView) headerView.findViewById(R.id.group);
        ImageView imageView = (ImageView) headerView.findViewById(R.id.image);
        Date date = null;
        String text = null;

        if (firstVisibleGroupPos == 0 && hasRecommand) {
            text = firstVisibleGroup;
            imageView.setImageResource(R.drawable.icon_recommend);
        } else {
            imageView.setImageResource(R.drawable.icon_calendar);
            if (firstVisibleGroup != null) {
                date = DisplayUtils.parseDate(firstVisibleGroup);
            }
            text = DisplayUtils.setDisplayDate(date);
        }
        textView.setText(text);
        DisplayUtils.setFont(ctx, textView);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int scrollState) {
        if(scrollState == SCROLL_STATE_FLING){
            NetUtils.umengSelfEvent(ctx, "first_page_scroll");
        }
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem >= 2)
            btnTop.setVisibility(View.VISIBLE);
        else
            btnTop.setVisibility(View.GONE);

    }


    final class JsInterf {
        JsInterf() {
        }

        @JavascriptInterface
        public void redirectLogin() {
            Intent activityIntent = new Intent();
            activityIntent.putExtra("bindMobile", true);
            activityIntent.setClass(getActivity(), LoginActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(activityIntent);
        }
    }
}
