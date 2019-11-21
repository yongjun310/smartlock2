package com.smart.lock.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.smart.lock.common.SlideConstants;
import com.smart.lock.dto.ContentDTO;
import com.smart.lock.service.LockService;
import com.smart.lock.utils.ContentsDataOperator;
import com.smart.lock.utils.DisplayUtils;
import com.smart.lock.utils.FileUtils;
import com.smart.lock.utils.PageChangeUtils;

public class LoopImageViewPagerAdapter extends PagerAdapter {

    private String contentIdSet = "";

    private Set<Integer> viewedIdSet = new HashSet<Integer>();

    private List<ContentDTO> contents;

    private Context ctx;

    private int updateId;

    private Map<Integer, Bitmap> bms;

    public List<ContentDTO> getContents() {
        return contents;
    }

    public void setContents(List<ContentDTO> contents) {
    	List<ContentDTO> lContents = new ArrayList<ContentDTO>(contents);
    	if(lContents != null && !lContents.isEmpty()) {
    		this.contents.clear();
	        for (int j = 0; j < lContents.size(); j++) {
	            ContentDTO content = lContents.get(j);
	            if (content != null && content.getId() == -12) {
	                lContents.remove(content);
	                this.contents.add(content);
	                contentIdSet += (contentIdSet.length() == 0 ? "" : ",") + content.getId();
	            }
	        }
	        while (!lContents.isEmpty() && lContents.size() > 0) {
	            ContentDTO content = lContents.get(0);
	            if(content != null) {
		            this.contents.add(content);
		            lContents.remove(0);
		
		            if (content.getId() >= 0) {
		                contentIdSet += (contentIdSet.length() == 0 ? "" : ",") + content.getId();
		            }
	            }
	        }
    	}
	}

	public Map<Integer, Bitmap> getBms() {
        return bms;
    }

    public void setBms(Map<Integer, Bitmap> bms) {
        this.bms = bms;
    }

    public LoopImageViewPagerAdapter(List<ContentDTO> contents, Context ctx) {
        this.ctx = ctx;
        this.contents = new ArrayList<ContentDTO>();

        for (int j = 0; j < contents.size(); j++) {
            ContentDTO content = contents.get(j);
            if (content.getId() == 231 || content.getId() == 266) {
                contents.remove(content);
                this.contents.add(content);
                contentIdSet += (contentIdSet.length() == 0 ? "" : ",") + content.getId();
            }
            //put the guide page in first page
            if (content.getId() == -12) {
                contents.remove(content);
                this.contents.add(content);
                contentIdSet += (contentIdSet.length() == 0 ? "" : ",") + content.getId();
            }
        }
        while (!contents.isEmpty()) {
            ContentDTO content = contents.get(0);
            this.contents.add(content);
            contents.remove(0);

            if (content.getId() >= 0) {
                contentIdSet += (contentIdSet.length() == 0 ? "" : ",") + content.getId();
            }
        }
        bms = new HashMap<Integer, Bitmap>();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public int getCount() {
        if (contents.size() > 3)
            return Integer.MAX_VALUE;
        else
            return contents.size();
    }

    @Override
    public Object instantiateItem(View container, int position) {
        ContentDTO content = contents.get(position % contents.size());
        ViewPager viewPager = (ViewPager) container;
        ImageView currentView = (ImageView) viewPager.findViewById(content.getId());
        if (currentView == null) {
            currentView = new ImageView(ctx);
            currentView.setTag(isContent(position));
            currentView.setId(content.getId());
        }
        if (currentView.getParent() != null)
            ((ViewPager) container).removeView(currentView);

        Bitmap bm = loadBitmapFromFile(currentView, content);
        
        bms.put(content.getId(), bm);
        Log.d("add content id:", "" + currentView.getId());
        currentView.setBackgroundDrawable(new BitmapDrawable(bm));
        viewPager.addView(currentView);

        return currentView;
    }

    private Bitmap loadBitmapFromFile(ImageView currentView, ContentDTO content) {
        Bitmap bm = null;
        if (!(Boolean) currentView.getTag()) {
            bm = DisplayUtils.getBitmapById(ctx, content.getId());
        } else {
            bm = DisplayUtils.loadBitmapfromContent(ctx, content);
        }
        return bm;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ContentDTO content = contents.get(position % contents.size());
        ViewPager viewPager = (ViewPager) container;
        ImageView currentView = (ImageView) viewPager.findViewById(content.getId());

        if (currentView != null) {
            viewPager.removeView(currentView);
            if (currentView.getId() >= 0) {
                Bitmap bm = bms.get(currentView.getId());
                Log.d("remove content id:", "" + currentView.getId());
                bms.remove(currentView.getId());
                DisplayUtils.recycleBitmap(bm);
            }
        }
    }

    @Override
    public int getItemPosition(Object object) {
        if (((View) object).getId() == updateId)
            return POSITION_NONE;
        return POSITION_UNCHANGED;
    }

    public int replaceItem(ViewPager viewPager, int position, int screenWidth, int screenHeight) {
        int absPos = contents.size() > 1 ? (position % contents.size()) : 0;
        ContentDTO currentContent = contents.get(absPos);
        ImageView currentView = (ImageView) viewPager.findViewById(currentContent.getId());
        if (currentView == null) {
            currentView = new ImageView(ctx);
            currentView.setId(currentContent.getId());
            currentView.setTag(isContent(position));
        }
        currentView.setBackgroundDrawable(null);

        updateId = currentView.getId();
        if (updateId >= 0) {
            ContentsDataOperator.deleteById(ctx, updateId);
            Log.d("test update boardcast", "test update boardcast " + DisplayUtils.getLineInfo());
            PageChangeUtils.changeLockServiceState(ctx, LockService.STATE_UPDATE_DATE);
        }
        String nowTimeStr = DisplayUtils.formatDateTimeString(new Date(System.currentTimeMillis()));
        String sql = "SELECT * FROM content WHERE id NOT IN (" + contentIdSet
                + ") AND startTime < '" + nowTimeStr + "' AND endTime > '"
                + nowTimeStr + "' AND localViewCount < " + SlideConstants.MAX_VIEW_COUNT
                + " AND isDislike = 0 ORDER BY priority DESC";
        List<ContentDTO> contentList = ContentsDataOperator.load(ctx, sql);
        boolean hasNew = false;
        if (contentList != null && contentList.size() > 0) {
            for (ContentDTO content : contentList) {
                if ("preLoad".equals(content.getLocalPath())) {
                    currentView.setId(content.getId());
                    currentView.setTag(true);
                    contents.remove(absPos);
                    contents.add(absPos, content);
                    contentIdSet += (contentIdSet.length() == 0 ? "" : ",") + content.getId();
                    currentContent = content;

                    hasNew = true;
                    break;
                } else {
                    int fileSize = FileUtils
                            .getFileSize(content.getLocalPath());
                    if (fileSize > 0) {
                        try {
                            currentView.setId(content.getId());
                            currentView.setTag(true);
                            contents.remove(absPos);
                            contents.add(absPos, content);
                            contentIdSet += (contentIdSet.length() == 0 ? "" : ",") + content.getId();
                            currentContent = content;

                            hasNew = true;
                            break;
                        } catch (Exception e) {
                            Log.e("adapter open pic:", e.toString());
                        }
                    }
                }
            }
        }

        if (!hasNew) {
            int res = 0;
            int index = (int) (Math.random() * 4);
            int defaultId = -1 - index;
            currentView.setId(defaultId);
            currentView.setTag(false);
            currentContent = new ContentDTO(defaultId);
            contents.remove(absPos);
            contents.add(absPos, currentContent);
        }

        Bitmap bm = loadBitmapFromFile(currentView, currentContent);
        currentView.setBackgroundDrawable(new BitmapDrawable(bm));
        viewPager.setCurrentItem(position + 1);

        return currentView.getId();
    }

    private boolean checkIdNew(int defaultId) {
        for (ContentDTO contentDTO : contents) {
            if (contentDTO.getId() == defaultId)
                return false;
        }
        return true;
    }

    public void recycleBitmap() {
        DisplayUtils.recycleBMMaps(bms);
    }

    public boolean isContent(int position) {
        return contents.get(position % contents.size()).getId() >= 0;
    }

    public ImageView getView(int position) {
    	ContentDTO content = contents.get(position % contents.size());
        ImageView result = new ImageView(ctx);
        result.setId(content.getId());
        result.setTag(isContent(position));
        return result;
    }

    /**
     * increat the current content view count
     * @param accountId
     * @param position
     */
    public void incViewCount(int accountId, int position) {
    	if(contents.size() > 0) {
	        ContentDTO content = contents.get(position % contents.size());
	        ImageView view = new ImageView(ctx);
	        view.setId(content.getId());
	        view.setTag(isContent(position));
	
	        if ((Boolean) view.getTag()) {
	            int contentId = view.getId();
	            if (!viewedIdSet.contains(contentId)) {
	//                ActionLogOperator.add(ctx, new AccountActionLogDTO(accountId,
	//                        ActionTypeEnum.DISPLAY, view.getId()));
	
	                ContentDTO contentItem = ContentsDataOperator.getById(ctx,
	                        contentId);
	                if (contentItem != null) {
	                    int viewCount = contentItem.getLocalViewCount() + 1;
	                    contentItem.setLocalViewCount(viewCount);
	                    ContentsDataOperator.updateWithoutNoti(ctx, contentItem);
	                }
	
	                viewedIdSet.add(contentId);
	            }
	        }
    	}
    }
}
