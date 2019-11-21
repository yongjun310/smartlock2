package com.smart.lock.global;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;

// TODO: Auto-generated Javadoc
/**
 * The Class AbViewUtil.
 */
public class AbViewUtil {
	
	/**
	 * 描述：重置AbsListView的高度.
	 *
	 * @param absListView the abs list view
	 * @param lineNumber 每行几个  ListView一行一个item
	 * @param verticalSpace the vertical space
	 */
	public static void setAbsListViewHeight(AbsListView absListView,int lineNumber,int verticalSpace) {
		int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED); 
	    int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
		ListAdapter mListAdapter = absListView.getAdapter();
		if (mListAdapter == null) {
			return;
		}
		int totalHeight = 0;
		int count = mListAdapter.getCount();
		ViewGroup.LayoutParams params = absListView.getLayoutParams();
		if(absListView instanceof ListView){
			for (int i = 0; i < count; i++) {
				View listItem = mListAdapter.getView(i, null, absListView);
				listItem.measure(w, h);
				totalHeight += listItem.getMeasuredHeight();
			}
			if (count == 0) {
				View listItem = mListAdapter.getView(0, null, absListView);
				listItem.measure(w, h);
				totalHeight += listItem.getMeasuredHeight();
				params.height = totalHeight;
			} else {
				params.height = totalHeight + (((ListView)absListView).getDividerHeight() * (count - 1));
			}
			
		}else if(absListView instanceof GridView){
			int remain = count % lineNumber;
			if(remain>0){
				remain = 1;
			}
			View listItem = mListAdapter.getView(0, null, absListView);
			listItem.measure(w, h);
			int line = count/lineNumber + remain;
			params.height = line*listItem.getMeasuredHeight()+(line+1)*verticalSpace;
		}
		
		((MarginLayoutParams) params).setMargins(0, 0, 0, 0);
		absListView.setLayoutParams(params);

	}
	
	/**
	 * 测量这个view，最后通过getMeasuredWidth()获取宽度和高度.
	 *
	 * @param v 要测量的view
	 * @return 测量过的view
	 */
	public static View measureView(View v){
		int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED); 
	    int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
		v.measure(w, h);
		return v;
	}
	
	/**
	 * 描述：根据分辨率获得字体大小.
	 *
	 * @param screenWidth the screen width
	 * @param screenHeight the screen height
	 * @param textSize the text size
	 * @return the int
	 */
	public static int resizeTextSize(int screenWidth,int screenHeight,int textSize){
		float ratio =  1;
		try {
			float ratioWidth = (float)screenWidth / 480; 
			float ratioHeight = (float)screenHeight / 800; 
			ratio = Math.min(ratioWidth, ratioHeight); 
		} catch (Exception e) {
		}
		return Math.round(textSize * ratio);
	}
	
	
	
}