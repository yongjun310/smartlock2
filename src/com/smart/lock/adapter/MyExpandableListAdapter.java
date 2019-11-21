package com.smart.lock.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smart.lock.R;
import com.smart.lock.activity.MainActivity;
import com.smart.lock.dto.AccountActionLogDTO;
import com.smart.lock.dto.CampaignDTO;
import com.smart.lock.dto.ContentDTO;
import com.smart.lock.utils.*;
import com.smart.lock.view.AbImageView;

import java.util.*;

/**
 * Created by john on 2015/5/29.
 */
public class MyExpandableListAdapter extends BaseExpandableListAdapter {
    private Context ctx;
    private LayoutInflater inflater;

    private HashMap<Integer, Bitmap> bitmaps;

    private List<String> groupList;

    private ArrayList<List<ContentDTO>> childList;

    private List<CampaignDTO> campaignDTOList;


    private DisplayMetrics dm;

    private Queue<GenerateBitmapBG> threadList = new LinkedList<GenerateBitmapBG>();

    public List<CampaignDTO> getCampaignDTOList() {
        return campaignDTOList;
    }

    public void setCampaignDTOList(List<CampaignDTO> campaignDTOList) {
        this.campaignDTOList = campaignDTOList;
    }

    public HashMap<Integer, Bitmap> getBitmaps() {
        return bitmaps;
    }

    public void setBitmaps(HashMap<Integer, Bitmap> bitmaps) {
        this.bitmaps = bitmaps;
    }

    public MyExpandableListAdapter(Context context, List<String> groupList, ArrayList<List<ContentDTO>> childList) {
        this.ctx = context;
        bitmaps = new HashMap<Integer, Bitmap>();
        inflater = LayoutInflater.from(context);
        this.childList = childList;
        this.groupList = groupList;
        String serverTime = SharedPreferencesUtils.getStringSP(ctx, SharedPreferencesUtils.SERVER_TIME, null);
        if (serverTime == null)
            serverTime = DisplayUtils.formatDateTimeString(Calendar.getInstance().getTime());
        serverTime = serverTime.substring(0,10);
        String sql = "SELECT * FROM campaign WHERE date(startTime) <= date('" + serverTime + "') and date(endTime) >= date('" + serverTime + "')"
                + "and status = 1 ORDER BY priority,startTime, id DESC";
        campaignDTOList = CampaignDataOperator.load(ctx, sql);
        dm = ctx.getResources().getDisplayMetrics();
    }

    @Override
    public int getGroupCount() {
        return groupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if(groupPosition == 0 && campaignDTOList.size() > 0) {
            return campaignDTOList.size();
        } else {
            if (groupPosition < childList.size() + 1) {
            	int position = groupPosition;
            	if(campaignDTOList.size() > 0)
            		position = groupPosition-1;
            	if(childList.size()>position)
            		return childList.get(position).size();
            	else 
            		return 0;
            }
            else
                return 0;
        }
    }

    @Override
    public Object getGroup(int groupPosition) {
        if(groupPosition >= 0 && groupPosition < groupList.size())
            return groupList.get(groupPosition);
        else
            return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if(groupPosition == 0 && campaignDTOList.size()>0) {
            return campaignDTOList.get(childPosition);
        } else {
            if (groupPosition >= 1 && groupPosition < groupList.size()) {
            	int position = groupPosition;
            	if(campaignDTOList.size() > 0)
            		position = groupPosition-1;
                if (childPosition >= 0 && childPosition < childList.get(position).size())
                    return childList.get(position).get(childPosition);
                else
                    return null;
            } else
                return null;
        }
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {

        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        GroupHolder groupHolder = null;
        if (convertView == null) {
            groupHolder = new GroupHolder();
            convertView = inflater.inflate(R.layout.group, null);
            convertView.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, DisplayUtils.dip2px(ctx, 44)));
            groupHolder.textView = (TextView) convertView
                    .findViewById(R.id.group);
            groupHolder.imageView = (ImageView) convertView
                    .findViewById(R.id.image);
            convertView.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) convertView.getTag();
        }
        String datetime = (String) getGroup(groupPosition);
        Date date = null;
        String text = null;
        if(groupPosition == 0  && campaignDTOList.size()>0) {
            text = datetime;
            groupHolder.imageView.setImageResource(R.drawable.icon_recommend);
        } else {
            groupHolder.imageView.setImageResource(R.drawable.icon_calendar);
            if (datetime != null) {
                date = DisplayUtils.parseDate(datetime);
            }
            text = DisplayUtils.setDisplayDate(date);
        }
        groupHolder.textView.setText(text);
        DisplayUtils.setFont(ctx, groupHolder.textView);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        ChildHolder childHolder = null;
        if (convertView == null) {
            childHolder = new ChildHolder();
            convertView = inflater.inflate(R.layout.main_list_item, null);
            childHolder.rootView = (FrameLayout) convertView.findViewById(R.id.root_view);
            childHolder.itemBG = ((AbImageView) convertView.findViewById(R.id.bg_img));
            childHolder.itemsTitle = ((TextView) convertView.findViewById(R.id.title));
            childHolder.itemLine = (ImageView) convertView.findViewById(R.id.img_line);
            DisplayUtils.setFont(ctx, childHolder.itemsTitle);
            childHolder.itemFavorite = ((ImageView) convertView.findViewById(R.id.img_favorite));
            convertView.setTag(childHolder);
        } else {
            childHolder = (ChildHolder) convertView.getTag();
            /*if (childHolder.itemBG != null) {
                if (childHolder.itemBG.getId() >= 0) {
                    int id;
                    String str = (String) childHolder.itemBG.getTag();
                    if(!TextUtils.isEmpty(str) && str.startsWith("c")) {
                        id = Integer.parseInt(str.substring(1));
                        if (id > 0) {
                            Bitmap bm = bitmaps.get(id);
                            bitmaps.remove(id);
                            DisplayUtils.recycleBitmap(bm);
                        }
                    }
                }
                childHolder.itemBG.setImageResource(R.color.imagebg);
                childHolder.itemBG.setBackgroundColor(R.color.imagebg);
            }*/
        }
        ViewGroup.LayoutParams lp = childHolder.itemBG.getLayoutParams();
        int screenWidth = dm.widthPixels - DisplayUtils.dip2px(ctx, 20);
        lp.width = screenWidth;
        if(groupPosition == 0 && campaignDTOList.size()>0) {
            CampaignDTO campaignDTO = campaignDTOList.get(childPosition);
            childHolder.itemFavorite.setVisibility(View.INVISIBLE);
            childHolder.itemLine.setVisibility(View.INVISIBLE);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.FILL_PARENT,
                    DisplayUtils.dip2px(ctx, 200));
            childHolder.rootView.setLayoutParams(params);
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            childHolder.itemBG.setTag("a" + campaignDTO.getId());
            childHolder.itemBG.setImageUrl(campaignDTO.getImage());
            childHolder.itemsTitle.setText(campaignDTO.getTitle());
        } else {
            childHolder.itemFavorite.setVisibility(View.VISIBLE);
            childHolder.itemLine.setVisibility(View.VISIBLE);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.FILL_PARENT,
                    DisplayUtils.dip2px(ctx, 340));
            childHolder.rootView.setLayoutParams(params);
            lp.height = screenWidth;

        	int position = groupPosition;
        	if(campaignDTOList.size() > 0)
        		position = groupPosition-1;
            List<ContentDTO> contentDTOList = childList.get(position);
            if(contentDTOList.size()<=childPosition) {
                Log.e("getChileView", "contentDTOList.size()<=childPosition");
            } else {
                final ContentDTO contentDTO = contentDTOList.get(childPosition);
                childHolder.itemBG.setTag("c" + contentDTO.getId());
                childHolder.itemBG.setImageUrl(contentDTO.getImage() + "?imageView2/1/w/480/h/480/q/50");
                /*GenerateBitmapBG generateBitmapBG = new GenerateBitmapBG(childHolder.itemBG, contentDTO.getTitle(), contentDTO.getId());

                String filePath = contentDTO.getLocalPath();
                if (TextUtils.isEmpty(contentDTO.getLocalPath())) {
                    filePath = FileUtils.getImageDir(ctx) + contentDTO.getId() + ".dat";
                }
                synchronized (ctx) {
                    if (threadList.size() > 3) {
                        threadList.remove();
                    }
                    threadList.add(generateBitmapBG);
                }
                generateBitmapBG.execute(new String[]{filePath});*/

                //childHolder.itemBG.setTag(position);
                childHolder.itemsTitle.setText(contentDTO.getTitle());
                int favoriteId = 0;
                final ChildHolder finalChildHolder = childHolder;
                DisplayUtils.enlargeClickArea(childHolder.itemFavorite, 50);
                childHolder.itemFavorite.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (contentDTO != null) {
//                    ActionLogOperator.add(ctx,
//                            new AccountActionLogDTO(SharedPreferencesUtils.getIntSP(ctx, SharedPreferencesUtils.ACCOUNT_ID, -1),
//                                    contentDTO.isFavorite() ? ActionTypeEnum.CANCEL_FAVOR
//                                            : ActionTypeEnum.FAVORTITE_IN_LOCK, contentDTO.getId()));

                            int accountId = SharedPreferencesUtils.getIntSP(ctx, "accountId", 0);
                            ActionLogOperator.add(ctx, new AccountActionLogDTO(accountId, contentDTO.isFavorite() ? 23 : 22, contentDTO.getId()));
                            if (!contentDTO.isFavorite()) {
                                finalChildHolder.itemFavorite.setBackgroundResource(R.drawable.favorite_clicked);
                                FavoriteUtils.favoriteContent(ctx, accountId, contentDTO.getId());
                            } else {
                                finalChildHolder.itemFavorite.setBackgroundResource(R.drawable.favorite);
                                FavoriteUtils.cancelFavoriteContent(ctx, accountId, contentDTO.getId());
                            }

                            contentDTO.setFavorite(!contentDTO.isFavorite());

                            ((MainActivity) ctx).getLikeFragment().refreshPage();
                        }

                    }

                });

                childHolder.itemFavorite.setBackgroundResource(contentDTO.isFavorite() ? 
                		R.drawable.favorite_clicked : R.drawable.favorite);
            }
        }
        childHolder.itemBG.setLayoutParams(lp);
        DisplayUtils.setFont(ctx, childHolder.itemsTitle);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    class GroupHolder {
        TextView textView;
        ImageView imageView;
    }

    class ChildHolder {
        FrameLayout rootView;
        AbImageView itemBG;
        TextView itemsTitle;
        ImageView itemLine;
        ImageView itemFavorite;
    }


    class GenerateBitmapBG extends AsyncTask<String, Integer, Bitmap> {
        ImageView imageView;
        String title = null;
        int id;

        public GenerateBitmapBG(ImageView view, String title, int id) {
            super();
            this.title = title;
            this.imageView = view;
            this.id = id;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bm = null;
            boolean threadFull = true;
            synchronized (ctx) {
                threadFull = threadList.contains((GenerateBitmapBG) this);
            }
            if (threadFull) {
                String localPath = params[0];
                bm = FileUtils
                        .loadResizedBitmap(ctx,
                                localPath,
                                dm.widthPixels,
                                dm.heightPixels, true);
                bm = DisplayUtils.cropBitmap(ctx, bm, (dm.heightPixels - dm.widthPixels - 300) / 2);
            }
            return bm;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result == null) {
                Log.e("GenerateBitmapBG", "crop Bitmap return null");
                return;
            }
            if (imageView != null && imageView.getTag() != null && (imageView.getTag().equals("c" + id))) {
                imageView.setImageBitmap(result);
                Log.d("GenerateBitmapBG", "title:" + title + " imgHeigh:" + result.getHeight());
                bitmaps.put(id, result);
            } else {
                Log.e("GenerateBitmapBG", "imageView != null:"+(imageView != null) +
                        " imageView.getTag() != null:" + (imageView.getTag() != null) +
                        " imageView.getTag().equals(title):" + (imageView.getTag().equals("c" + id)));
                DisplayUtils.recycleBitmap(result);
            }
        }
    }

}
