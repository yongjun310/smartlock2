package com.smart.lock.adapter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.smart.lock.R;
import com.smart.lock.handler.ListEventHandler;
import com.smart.lock.utils.DisplayUtils;
import com.smart.lock.utils.FileUtils;

/**
 * 名称：MyListViewAdapter
 * 描述：ListView自定义Adapter例子
 * @version
 */
public class CommonListViewAdapter extends BaseAdapter{
  
    private Context mContext;
    //单行的布局
    private int mResource;
    //列表展现的数据
    private List<? extends Map<String, ?>> mData;
    //Map中的key
    private String[] mFrom;
    //view的id
    private int[] mTo;
    
    private ListEventHandler listEventHandler;

    private HashMap<String, Bitmap> bitmaps;

    private Typeface type = null;

   /**
    * 构造方法
    * @param context
    * @param data 列表展现的数据
    * @param resource 单行的布局
    * @param from Map中的key
    * @param to view的id
    */
    public CommonListViewAdapter(Context context, ListEventHandler handler, List<? extends Map<String, ?>> data,
            int resource, String[] from, int[] to){
         mContext = context;
         mData = data;
         bitmaps = new HashMap<String, Bitmap>();
         listEventHandler = handler;
         mResource = resource;
         mFrom = from;
         mTo = to;
         type = Typeface.createFromAsset(mContext.getAssets(), "fonts/FZ_GBK.TTF");
    }   
    
    @Override
    public int getCount() {
        return mData.size();
    }

    private Queue<GenerateBitmapBG> threadList = new LinkedList<GenerateBitmapBG>();
    
    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position){
      return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
          final ViewHolder holder;
          if(convertView == null){
              //使用自定义的list_items作为Layout
              convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
              //使用减少findView的次数
              holder = new ViewHolder();
              holder.itemBG = ((ImageView) convertView.findViewById(mTo[3])) ;
              holder.itemsTitle = ((TextView) convertView.findViewById(mTo[1]));
              holder.itemReward = ((ImageView) convertView.findViewById(mTo[2]));
              holder.itemFavorite = ((ImageView) convertView.findViewById(mTo[0]));
              //设置标记
              convertView.setTag(holder);
          }else{
              holder = (ViewHolder) convertView.getTag();
              if (holder.itemBG != null) {
                  if (holder.itemBG.getId() >= 0) {
                      String title = holder.itemsTitle.getText().toString();
                      Bitmap bm = bitmaps.get(title);
                      bitmaps.remove(title);
                      DisplayUtils.recycleBitmap(bm);
                  }
                  holder.itemBG.setBackgroundColor(mContext.getResources().getColor(R.color.imagebg));
              }
          }
          //设置数据
          final Map<String, ?> dataSet = mData.get(position);
          if (dataSet == null) {
              return null;
          }
          //获取该行数据
          final Object data0 = dataSet.get(mFrom[0]);
          final Object data1 = dataSet.get(mFrom[1]);
          final Object data2 = dataSet.get(mFrom[2]);
        final Object data3 = dataSet.get(mFrom[3]);
          //设置数据到View
        holder.itemBG.setTag(data1.toString());
        if(FileUtils.getFileSize(data3.toString()) > 0) {
            GenerateBitmapBG generateBitmapBG = new GenerateBitmapBG(holder.itemBG, data1.toString());
            synchronized (mContext) {
                if (threadList.size() > 5) {
                    threadList.remove();
                } else {
                    threadList.add(generateBitmapBG);
                }
            }
            generateBitmapBG.execute(new String[]{data3.toString()});
        }
        holder.itemsTitle.setTypeface(type);

        //holder.itemBG.setTag(position);
        holder.itemsTitle.setText(data1.toString());
        int favoriteId = 0;
        if("0".equals(data2.toString())) {
            holder.itemReward.setVisibility(View.GONE);
        } else {
            holder.itemReward.setVisibility(View.VISIBLE);
            if("2".equals(data2)) {
                holder.itemReward.setImageResource(R.drawable.prize_read);
            } else if("3".equals(data2)) {
                holder.itemReward.setImageResource(R.drawable.prize_share);
            }
        }

        holder.itemsTitle.setText(data1.toString());
        holder.itemFavorite.setBackgroundResource("0".equals(data0.toString())?R.drawable.favorite:R.drawable.favorite_clicked);
        final int index = position;
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View arg0) {
				listEventHandler.onListItemClicked(index);
			}
        });
        return convertView;
    }
    
    /**
     * ViewHolder类
     */
    static class ViewHolder {
        ImageView itemBG;
        TextView itemsTitle;
        ImageView itemFavorite;
        ImageView itemReward;
    }

    class GenerateBitmapBG extends AsyncTask<String,Integer,Bitmap> {
        ImageView imageView;
        String title = null;
        public GenerateBitmapBG(ImageView view, String title) {
            super();
            this.title = title;
            this.imageView = view;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bm = null;
            boolean threadFull = true;
            synchronized (mContext) {
                threadList.contains((GenerateBitmapBG) this);
            }
            if(threadFull) {
                DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
                bm = FileUtils
                        .loadResizedBitmap(mContext,
                                params[0],
                                dm.widthPixels,
                                dm.heightPixels, true);
                bm = DisplayUtils.cropBitmap(mContext, bm, (dm.heightPixels - dm.widthPixels - 300) / 2);
            }
            return bm;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if(result == null)
                return;
        	if(imageView.getTag() != null && imageView.getTag().equals(title)) {
                imageView.setBackgroundDrawable(new BitmapDrawable(result));
                bitmaps.put(title, result);
            } else {
                DisplayUtils.recycleBitmap(result);
            }
        }
    }
}