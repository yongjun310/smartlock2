package com.smart.lock.view;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.smart.lock.R;



/**
 * ** @ClassName: AdvertiseGalleryAdapter
	* @Description: gallery adapter
	* @author john
	* @date 2013-12-18 上午9:41:21
 */
public class AdvertiseGalleryAdapter extends BaseAdapter
{
    private final int width;

    private final int height;

    private ArrayList<Integer> list;

    private final Activity activity;

    /**
     * RecommendCoverAdapter
     * 
     * @param activity Activity
     * @param coverList ArrayList<Cover>
     * @param parent View
     */
    public AdvertiseGalleryAdapter(Activity activity, ArrayList<Integer> aList, View parent)
    {
        this.activity = activity;
        this.list = aList;
        width = activity.getWindowManager().getDefaultDisplay().getWidth();
        height = activity.getWindowManager().getDefaultDisplay().getHeight();
        parent.setLayoutParams(new FrameLayout.LayoutParams(width, height));
    }

    public void setList(ArrayList<Integer> coverList)
    {
        this.list = coverList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount()
    {
        if (list == null || list.isEmpty())
        {
            return 0;
        }
        // return list.size();
        return Integer.MAX_VALUE;
    }

    @Override
    public Integer getItem(final int position)
    {
        if (list == null || list.isEmpty())
        {
            return null;
        }

        return list.get(position % list.size());
    }

    @Override
    public long getItemId(final int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            final LayoutInflater inflater = ((Activity) this.activity).getLayoutInflater();
            convertView = inflater.inflate(R.layout.advertise_gallery_item, null);
        }
        
        final ImageView advImageView = (ImageView) convertView.findViewById(R.id.advertise_image);

        advImageView.setBackgroundDrawable(activity.getResources().getDrawable(getItem(position)));

        return convertView;
    }

}
