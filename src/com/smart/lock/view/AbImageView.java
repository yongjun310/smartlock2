package com.smart.lock.view;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.smart.lock.R;
import com.smart.lock.imageloader.DisplayImageOptions;
import com.smart.lock.imageloader.ImageLoader;


public class AbImageView extends ImageView {
    private final static String TAG = "AbImageView";

    private ImageLoader imageLoader = ImageLoader.getInstance();

    public AbImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setImageUrl(String url) {

        setImageBitmap(null);

        if (TextUtils.isEmpty(url)) {
            return;
        }

        String lowUrl = url.toLowerCase();
        if (lowUrl.startsWith("file:///") || lowUrl.startsWith("/")) {
            // 本地文件
            imageLoader.displayImage(null, this);

            Bitmap bitmap = createBitmap(url);
            if (bitmap != null) {
                setImageBitmap(bitmap);
            }
        } else {
            try {
                DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder()
                        .cacheInMemory().cacheOnDisc()
                        .bitmapConfig(Bitmap.Config.RGB_565);
                builder.showStubImage(R.drawable.default_img).showImageForEmptyUri(R.drawable.default_img).showImageOnFail(R.drawable.default_img);
                DisplayImageOptions options = builder.build();
                imageLoader.displayImage(url, this, options);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }

    }

    public static Bitmap createBitmap(String path) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 1;
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(path, opts);
            if (bitmap == null) {
                // 解析失败，图片下载有问题，删除重新下载
                try {
                    new File(path).delete();
                } catch (Exception e) {
                }
            }
            return bitmap;
        } catch (OutOfMemoryError e) {
            System.gc();
            return null;
        }
    }

}
