package com.smart.lock.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.smart.lock.R;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.dto.ContentDTO;
import com.smart.lock.imageloader.DisplayImageOptions;
import com.smart.lock.imageloader.ImageLoader;
import com.smart.lock.view.CustomDialog;

public class DisplayUtils {
	
	public static int px_x(float dp, int screenWidth) {
		return (int) (dp / 1080 * screenWidth);
	}

	public static int px_y(float dp, int screenHeight) {
		return (int) (dp / 1920 * screenHeight);
	}

    public static Typeface typeface = null;

    public static Typeface roboTypeface = null;
	/**
	 * 以最省内存的方式读取本地资源的图片
	 * 
	 * @param context
	 * @param resId
	 * @return
	 */
	public static Bitmap readBitMap(Context context, int resId) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		opt.inPurgeable = true;
		opt.inInputShareable = true;
		// 获取资源图片
		InputStream is = context.getResources().openRawResource(resId);
		return BitmapFactory.decodeStream(is, null, opt);
	}
	
    public static int dip2px(Context context, float dipValue)
    {
        final float scale = context.getResources().getDisplayMetrics().density;

        int px = (int) (dipValue * scale + 0.5f);

        return px;
    }
    
    public static void recycleBitmap(Bitmap bm) {
		if (bm != null && !bm.isRecycled()) {
		    bm.recycle();
		    System.gc();
		    bm = null;
		}
	}

    public static void recycleBMMaps(Map<Integer, Bitmap> bitmaps) {
		if(bitmaps != null) {
		    Iterator iter = bitmaps.entrySet().iterator();
		    while (iter.hasNext()) {
		        Map.Entry entry = (Map.Entry) iter.next();
		        Bitmap bitmap = (Bitmap) (entry.getValue());
		        DisplayUtils.recycleBitmap(bitmap);
		    }
		}
	}
    
	public static void enlargeClickArea(final View view, final int enlarge) {
	    ViewParent parent = view.getParent();
        if (!View.class.isInstance(parent))
        {
            return;
        }
        final View parentView = (View) parent;
        parentView.post(new Runnable()
        {
            @Override
            public void run()
            {
                Rect bounds = new Rect();
                view.getHitRect(bounds);
                bounds.left -= enlarge;
                bounds.right += enlarge;
                bounds.bottom += enlarge;
                bounds.top -= enlarge;
                TouchDelegate touchDelegate = new TouchDelegate(bounds, view);
                parentView.setTouchDelegate(touchDelegate);
            }
        });
	}

    /**
     * 裁剪Bitmap，自动回收原Bitmap
     * @param ctx
     * @param src
     * @param y
     * @return
     */
    public static Bitmap cropBitmap(Context ctx, Bitmap src, int y) {
        if(src == null)
            return src;
        DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        return cropBitmap(src, 0, y, screenWidth, screenWidth, true);
    }

    /**
     * @brief 裁剪Bitmap
     * @param src 源Bitmap
     * @param x 开始x坐标
     * @param y 开始y坐标
     * @param width 截取宽度
     * @param height 截取高度
     * @param isRecycle 是否回收原图像
     * @return Bitmap
     */
    public static Bitmap cropBitmap(Bitmap src, int x, int y, int width, int height, boolean isRecycle) {
        if (x == 0 && y == 0 && width == src.getWidth() && height == src.getHeight()) {
            return src;
        }
        Bitmap dst = Bitmap.createBitmap(src, x, y, width, height);
        if (isRecycle && dst != src) {
            src.recycle();
        }
        return dst;
    }

    public static void setFont(Context ctx, TextView textView) {
        if(typeface == null)
            typeface = Typeface.createFromAsset(ctx.getAssets(), "fonts/FZ_GBK.TTF");
        if(ctx != null && textView != null)
            textView.setTypeface(typeface);
    }

    public static Typeface getTypeface(Context ctx) {
        if(typeface == null)
            typeface = Typeface.createFromAsset(ctx.getAssets(), "fonts/FZ_GBK.TTF");
        return typeface;
    }

    public static void setFontRoboto(Context ctx, TextView textView) {
        if(roboTypeface == null)
            roboTypeface = Typeface.createFromAsset(ctx.getAssets(), "fonts/Roboto-Thin.ttf");
        if(ctx != null && textView != null)
            textView.setTypeface(roboTypeface);
    }


    public static String setDisplayDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        if(date != null)
            calendar.setTime(date);
        int nowMonth = calendar.get(Calendar.MONTH) + 1;
        int nowDay = calendar.get(Calendar.DAY_OF_MONTH);
        int nowWeekDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        String showStr = null;
        switch (nowWeekDay) {
            case 0:
                showStr = String.format(SlideConstants.DATE_FORMART,
                        nowMonth, nowDay, "日");
                break;

            case 1:
                showStr = String.format(SlideConstants.DATE_FORMART,
                        nowMonth, nowDay, "一");
                break;

            case 2:
                showStr = String.format(SlideConstants.DATE_FORMART,
                        nowMonth, nowDay, "二");
                break;

            case 3:
                showStr = String.format(SlideConstants.DATE_FORMART,
                        nowMonth, nowDay, "三");
                break;

            case 4:
                showStr = String.format(SlideConstants.DATE_FORMART,
                        nowMonth, nowDay, "四");
                break;

            case 5:
                showStr = String.format(SlideConstants.DATE_FORMART,
                        nowMonth, nowDay, "五");
                break;

            case 6:
                showStr = String.format(SlideConstants.DATE_FORMART,
                        nowMonth, nowDay, "六");
                break;

            default:
                break;
        }
        Log.d("displayUtils", "showStr:" + showStr);
        return showStr;
    }


    public static void setDisplayDateAndDay(TextView dateTextView, TextView dayTextView, Date date) {
        Calendar calendar = Calendar.getInstance();
        if(date != null)
            calendar.setTime(date);
        int nowMonth = calendar.get(Calendar.MONTH) + 1;
        int nowDay = calendar.get(Calendar.DAY_OF_MONTH);
        int nowWeekDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        String dayStr = null;
        switch (nowWeekDay) {
            case 0:
                dayStr = "日";
                break;

            case 1:
                dayStr = "一";
                break;

            case 2:
                dayStr = "二";
                break;

            case 3:
                dayStr = "三";
                break;

            case 4:
                dayStr = "四";
                break;

            case 5:
                dayStr = "五";
                break;

            case 6:
                dayStr = "六";
                break;

            default:
                break;
        }
        dateTextView.setText(String.format(SlideConstants.NEW_DATE_FORMART,
                nowMonth, nowDay));
        dayTextView.setText("星期" + dayStr);

    }

    public static Bitmap loadBitmapfromContent(Context ctx, ContentDTO content) {
        Bitmap bm;
        if ("preLoad".equals(content.getLocalPath())) {
            int res = 0;
            switch (content.getId()) {
                case 231:
                    res = R.drawable.pic_1;
                    break;
                case 266:
                    res = R.drawable.pic_2;
                    break;
                case 268:
                    res = R.drawable.pic_3;
                    break;
                case 362:
                    res = R.drawable.pic_4;
                    break;

                default:
                    break;
            }
            bm = DisplayUtils.readBitMap(ctx, res);
        } else {
            DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
            bm = FileUtils
                    .loadResizedBitmap(ctx,
                            content.getLocalPath(),
                            dm.widthPixels,
                            dm.heightPixels, true);
        }
        return bm;
    }

    public static  String getSubTitle(String fullTitle, int len) {
        if(fullTitle != null && fullTitle.length() > len) {
            fullTitle = fullTitle.substring(0, len) + "...";
        }
        return fullTitle;
    }


    public static void setTitleAndBackBtn(Activity activity, String title) {
        TextView titleTV = (TextView) activity.findViewById(R.id.back_text);
        titleTV.setText(title);
        DisplayUtils.setFont(activity, titleTV);
        DisplayUtils.enlargeClickArea(activity.findViewById(R.id.btn_back), 100);
    }


    public static void resortContent(List<ContentDTO> contentList) {
        List<ContentDTO> contentList2 = new ArrayList<ContentDTO>();
        List<ContentDTO> contentList6 = new ArrayList<ContentDTO>();
        List<ContentDTO> contentList1 = new ArrayList<ContentDTO>();
        List<ContentDTO> contentList3 = new ArrayList<ContentDTO>();
        List<ContentDTO> contentList5 = new ArrayList<ContentDTO>();
        List<ContentDTO> contentList4 = new ArrayList<ContentDTO>();
        if (contentList != null) {
            for(ContentDTO contentDTO:contentList) {
                List<Integer> categorys = contentDTO.getCategoryIds();
                if(categorys != null && categorys.size() > 0) {
                    switch (categorys.get(0)){
                        case 1:
                            contentList1.add(contentDTO);
                            break;
                        case 2:
                            contentList2.add(contentDTO);
                            break;
                        case 3:
                            contentList3.add(contentDTO);
                            break;
                        case 6:
                            contentList6.add(contentDTO);
                            break;
                        case 5:
                            contentList5.add(contentDTO);
                            break;
                        default:
                            contentList4.add(contentDTO);
                            break;
                    }
                } else {
                    contentList4.add(contentDTO);
                }
            }
        }
        contentList.clear();

        contentList.addAll(contentList2);
        contentList.addAll(contentList6);
        contentList.addAll(contentList1);
        contentList.addAll(contentList3);
        contentList.addAll(contentList5);
        contentList.addAll(contentList4);
    }

    /**
     * format date
     * @param date
     * @return yyyy-MM-dd
     */
    public static String formatDateString(Date date) {
        SimpleDateFormat DAY_FORMART = new SimpleDateFormat(
                "yyyy-MM-dd");
        return DAY_FORMART.format(date);
    }

    /**
     * format Datetime
     * @param datetime
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String formatDateTimeString(Date datetime) {
        SimpleDateFormat TIME_FORMART = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss");
        return TIME_FORMART.format(datetime);
    }

    /**
     * parse dateTime
     * @param strDateTime yyyy-MM-dd HH:mm:ss
     * @return Date
     */
    public static Date parseDatetime(String strDateTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long ldate = 0;
        try {
            ldate = formatter.parse(strDateTime).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date date = new Date(ldate);
        return date;
    }

    /**
     * parse Date
     * @param strDate yyyy-MM-dd
     * @return Date
     */
    public static Date parseDate(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        long ldate = 0;
        try {
            ldate = formatter.parse(strDate).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date date = new Date(ldate);
        return date;
    }


    public static void showExitDialog(final Activity activity) {

        CustomDialog.Builder customBuilder = new
                CustomDialog.Builder(activity);
        customBuilder.setTitle("退出")
                .setMessage("确定要退出？")
                .setPositiveButton("是",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                activity.setResult(0);
                                activity.finish();
                            }
                        })
                .setNegativeButton("否",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
        CustomDialog customDialog = customBuilder.create();
        customDialog.show();
    }


    public static Bitmap smallToShareSize(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        matrix.postScale((float) 480.0 / width, (float) 480.0 / width); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0,
                width, height, matrix, true);
        Bitmap sizedBmp = DisplayUtils.cropBitmap(resizeBmp, 0, 0, 480, 640, true);
        Bitmap retBmp = DisplayUtils.compressImage(sizedBmp);
        DisplayUtils.recycleBitmap(sizedBmp);
        DisplayUtils.recycleBitmap(resizeBmp);
        return retBmp;
    }

    public static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 50, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 50;
        while ( baos.toByteArray().length / 1024>50) {	//循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inMutable = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, bmOptions);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    public static Bitmap small(Bitmap bitmap, float scale) {
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        return resizeBmp;
    }


    public static BitmapDrawable blur(Context ctx, Bitmap bkg) {
        long start = System.currentTimeMillis();
        float scaleFactor = 5;
        float radius = 20;
        DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int[] location = new int[2];
        /*rootView.getLocationOnScreen(location);
        int y = location[1] + rootView.getBottom();*/

        Bitmap overlay = Bitmap.createBitmap((int) (screenWidth / scaleFactor),
                (int) (dm.heightPixels / scaleFactor), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.translate(0, 0);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        if (bkg != null && !bkg.isRecycled()) {
            canvas.drawBitmap(bkg, 0, 0, paint);

            overlay = FastBlur.doBlur(overlay, (int) radius, true);
            Log.d("blur", "blur end:" + (System.currentTimeMillis() - start));
            return new BitmapDrawable(ctx.getResources(), overlay);
        } else {
            return null;
        }
    }

    public static void displayImage(String url, ImageView iv) {
        try {
            DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder()
                    .cacheInMemory().cacheOnDisc()
                    .bitmapConfig(Bitmap.Config.RGB_565);
            //builder.showStubImage(R.drawable.loading).showImageForEmptyUri(R.drawable.loading).showImageOnFail(R.drawable.loading);
            DisplayImageOptions options = builder.build();
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(url, iv, options);
        } catch (Exception e) {
            Log.e("displayImage", e.getMessage());
        }
    }


    public static Bitmap getBitmapById(Context ctx, int contentId) {
        Bitmap bm;
        int res = 0;
        switch (contentId) {
            case -11:
                res = R.drawable.default_11;
                break;
            case -10:
                res = R.drawable.default_10;
                break;
            case -9:
                res = R.drawable.default_9;
                break;
            case -8:
                res = R.drawable.default_8;
                break;
            case -7:
                res = R.drawable.default_7;
                break;
            case -6:
                res = R.drawable.default_6;
                break;
            case -5:
                res = R.drawable.default_5;
                break;
            case -4:
                res = R.drawable.default_4;
                break;
            case -3:
                res = R.drawable.default_3;
                break;
            case -2:
                res = R.drawable.default_2;
                break;
            case -1:
                res = R.drawable.default_1;
                break;
            case -12:
                res = R.drawable.default_guide;
                break;
            default:
                break;
        }
        bm = DisplayUtils.readBitMap(ctx, res);
        return bm;
    }
    
    public static String getLineInfo()
    {
        StackTraceElement ste = new Throwable().getStackTrace()[1];
        return ste.getFileName() + ": Line " + ste.getLineNumber();
    }

    public static void hideNavBar(View view) {
		int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= 14) {
        	view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE
                    | View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
	}

    public static void showNavBar(View view) {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= 14) {
        	view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }
}
