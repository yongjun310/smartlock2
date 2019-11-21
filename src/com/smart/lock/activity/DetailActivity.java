package com.smart.lock.activity;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.smart.lock.R;
import com.smart.lock.adapter.LoopImageViewPagerAdapter;
import com.smart.lock.common.FavoriteTypeEnum;
import com.smart.lock.common.SlideConstants;
import com.smart.lock.dto.AccountActionLogDTO;
import com.smart.lock.dto.ContentDTO;
import com.smart.lock.utils.ActionLogOperator;
import com.smart.lock.utils.ContentsDataOperator;
import com.smart.lock.utils.DeviceUtils;
import com.smart.lock.utils.DisplayUtils;
import com.smart.lock.utils.FavoriteUtils;
import com.smart.lock.utils.FileUtils;
import com.smart.lock.utils.NetUtils;
import com.smart.lock.utils.PageChangeUtils;
import com.smart.lock.utils.SharedPreferencesUtils;
import com.smart.lock.utils.SysApplication;

/**
 * Created by 迅 on 2015/6/2.
 */
public class DetailActivity extends BaseActivity implements ViewPager.OnPageChangeListener {
    private int accountId, contentId, hasMore, assnType, bonusType, screenWidth, screenHeight, currentIndex;
    private String preActivity, image, localPath, link, title, content, bonusAmount;

    private ViewPager viewPager;
    private LoopImageViewPagerAdapter adapter;

    private RelativeLayout detailLayout, downloadLayout, shareLayout, favoriteLayout, moreLayout, backLayout;
    private ImageView favoriteImage;
    private TextView titleText, contentText, backText, downloadText, shareText, favoriteText, moreText;

    private View decorView;
    
    private Handler messHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Bitmap bm = FileUtils
                            .loadResizedBitmap(getApplicationContext(),
                                    localPath,
                                    screenWidth,
                                    screenHeight, true);
                    detailLayout.setBackgroundDrawable(new BitmapDrawable(bm));
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        SysApplication.getInstance().addActivity(this);
        //隐藏虚拟按键以便全屏
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        decorView = getWindow().getDecorView();
        if (currentapiVersion >= 14) {
            DisplayUtils.hideNavBar(decorView);
        }
        decorView.setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int i) {
                    	DisplayUtils.hideNavBar(decorView);
                    }
                });
        if (currentapiVersion >= 19) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        viewPager = (ViewPager) findViewById(R.id.viewPager_detail);
        detailLayout = (RelativeLayout) findViewById(R.id.layout_detail);
        downloadLayout = (RelativeLayout) findViewById(R.id.layout_detail_download);
        shareLayout = (RelativeLayout) findViewById(R.id.layout_detail_share);
        favoriteLayout = (RelativeLayout) findViewById(R.id.layout_detail_favorite);
        moreLayout = (RelativeLayout) findViewById(R.id.layout_detail_more);
        backLayout = (RelativeLayout) findViewById(R.id.layout_detail_back);
        favoriteImage = (ImageView) findViewById(R.id.btn_detail_favorite);
        titleText = (TextView) findViewById(R.id.txt_detail_title);
        contentText = (TextView) findViewById(R.id.txt_detail_content);
        backText = (TextView) findViewById(R.id.txt_detail_back);
        downloadText = (TextView) findViewById(R.id.txt_detail_download);
        favoriteText = (TextView) findViewById(R.id.txt_detail_favorite);
        moreText = (TextView) findViewById(R.id.txt_detail_more);
        shareText = (TextView) findViewById(R.id.txt_detail_share);

        accountId = SharedPreferencesUtils.getIntSP(this, "accountId", -1);

        Bundle bundle = this.getIntent().getExtras();
        preActivity = bundle.getString("preActivity", "MainPageFragment");
        contentId = bundle.getInt("contentId", 0);
        assnType = bundle.getInt("assnType", 0);
        bonusType = bundle.getInt("bonusType", 0);
        image = bundle.getString("image", null);
        localPath = bundle.getString("localPath", null);
        hasMore = bundle.getInt("hasMore", 0);
        link = bundle.getString("link", null);
        title = bundle.getString("title", null);
        content = bundle.getString("content", null);
        bonusAmount = bundle.getString("bonusAmount", null);

        ActionLogOperator.add(this, new AccountActionLogDTO(accountId, 24, contentId));

        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        final String filePrefix = FileUtils.getImageDir(getApplicationContext());
        File dirFile = new File(filePrefix);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        int fileSize = FileUtils
                .getFileSize(filePrefix + contentId + ".dat");
        if (fileSize > 0) {
            if (localPath == null || localPath.length() == 0) {
                localPath = filePrefix + contentId + ".dat";
                Message message = Message.obtain(messHandler, 1);
                messHandler.sendMessage(message);
            } else {
                initImage();
            }
        } else {
            //下载远程图片
            if (DeviceUtils.isNetworkAvailable(this)) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (FileUtils.downloadImage(image
                                        + String.format(
                                        SlideConstants.QINIU_PIC_PARAM,
                                        screenHeight, screenWidth),
                                filePrefix + contentId + ".dat")) {
                            localPath = filePrefix + contentId + ".dat";

                            Message message = Message.obtain(messHandler, 1);
                            messHandler.sendMessage(message);
                        }
                    }
                }).start();
            } else {
                this.finish();
            }
            ContentDTO contentDTO = ContentsDataOperator.getById(this, contentId);
            if (contentDTO != null) {
                contentDTO.setLocalPath(filePrefix + contentId + ".dat");
                ContentsDataOperator.update(this, contentDTO);
            }
        }

        initContent();

    }

    public void initContent() {
        if (hasMore == 0) {
            moreLayout.setVisibility(View.GONE);
        } else {
            moreLayout.setVisibility(View.VISIBLE);
        }

        if (assnType == FavoriteTypeEnum.FAVORITE.getValue()) {
            favoriteImage.setImageResource(R.drawable.selector_icon_detail_favorite2);
        } else {
            favoriteImage.setImageResource(R.drawable.selector_icon_detail_favorite);
        }

        if (Double.valueOf(bonusAmount) > 0) {
            findViewById(R.id.img_detail_share_prize).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.img_detail_share_prize).setVisibility(View.GONE);
        }

        titleText.setText(title);
        DisplayUtils.setFont(this, titleText);
        if (TextUtils.isEmpty(content))
            contentText.setVisibility(View.GONE);
        else
            contentText.setText(content);
        DisplayUtils.setFont(this, contentText);
        DisplayUtils.setFont(this, backText);
        DisplayUtils.setFont(this, downloadText);
        DisplayUtils.setFont(this, shareText);
        DisplayUtils.setFont(this, favoriteText);
        DisplayUtils.setFont(this, moreText);
    }

    public void initImage() {
//        Bitmap bm = FileUtils
//                .loadResizedBitmap(this,
//                        localPath,
//                        screenWidth,
//                        screenHeight, true);
//        detailLayout.setBackgroundDrawable(new BitmapDrawable(bm));

        List<ContentDTO> mContents = new ArrayList<ContentDTO>();

        currentIndex = 0;

        if (preActivity.equals("MainPageFragment")) {
            List<String> groupList = ContentsDataOperator.loadContentsDate(this);
            //不改变排序下的去重
            List<String> ids = new ArrayList<String>();
            Set<String> uniqueIdSet = new HashSet<String>();
            uniqueIdSet.addAll(groupList);
            ids.addAll(groupList);
            groupList.clear();
            for (String group : ids) {
                if (uniqueIdSet.contains(group)) {
                    groupList.add(group);
                    uniqueIdSet.remove(group);
                }
            }
            for (String date : groupList) {
                List<ContentDTO> tempList = ContentsDataOperator.loadContentsByDate(this, date);
                mContents.addAll(tempList);
            }

            for (ContentDTO contentDTO : mContents) {
                if (contentDTO.getId() == contentId) {
                    break;
                }
                currentIndex++;
            }
        } else {
            ContentDTO contentDTO = new ContentDTO();
            contentDTO.setId(contentId);
            contentDTO.setBonusAmount(new BigDecimal(bonusAmount));
            contentDTO.setContent(content);
            contentDTO.setType(bonusType);
            contentDTO.setLink(link);
            contentDTO.setFavorite(assnType == FavoriteTypeEnum.FAVORITE.getValue());
            contentDTO.setTitle(title);
            contentDTO.setImage(image);
            contentDTO.setHasMore(hasMore);
            contentDTO.setLocalPath(localPath);
            mContents.add(contentDTO);
        }

        adapter = new LoopImageViewPagerAdapter(mContents, this);
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(this);
        viewPager.setCurrentItem(currentIndex);
    }

    public void detailBack(View view) {
        NetUtils.umengSelfEvent(this, "big_image_back");
        this.finish();
    }

    public void detailDownload(View view) {
        NetUtils.umengSelfEvent(this, "big_image_save");
        ActionLogOperator.add(this, new AccountActionLogDTO(accountId, 25, contentId));
        if(!TextUtils.isEmpty(localPath)) {
	        Bitmap bitmap = FileUtils
	                .loadResizedBitmap(this,
	                        localPath,
	                        screenWidth,
	                        screenHeight, true);
	        String localFilePath = FileUtils.contentIdToLocalPath(contentId);
	        try {
	            FileUtils.saveImage(bitmap, localFilePath);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	
	        bitmap.recycle();
	
	        Toast.makeText(this, "图片已下载到" + localFilePath, Toast.LENGTH_LONG).show();
        }
    }

    public void detailShare(View view) {
        ActionLogOperator.add(this, new AccountActionLogDTO(accountId, 26, contentId));

        NetUtils.umengSelfEvent(this, "big_image_share");
        Intent intent = new Intent();
        intent.setClass(this, ShareActivity.class);
        intent.putExtra("localPath", localPath);
        intent.putExtra("shareTitle", title);
        intent.putExtra("descContent", content);
        intent.putExtra("shareImgUrl", image);
        intent.putExtra("contentId", contentId);
        startActivity(intent);
    }

    public void detailFavorite(View view) {
        ActionLogOperator.add(this, new AccountActionLogDTO(accountId, assnType == FavoriteTypeEnum.FAVORITE.getValue() ? 28 : 27, contentId));

        NetUtils.umengSelfEvent(this, "big_image_favorite");
        if (assnType == FavoriteTypeEnum.FAVORITE.getValue()) {
            favoriteImage.setImageResource(R.drawable.selector_icon_detail_favorite);
            assnType = FavoriteTypeEnum.NO_FEEL.getValue();

            if (accountId >= 0)
                FavoriteUtils.cancelFavoriteContent(this, accountId, contentId);
        } else {
            favoriteImage.setImageResource(R.drawable.selector_icon_detail_favorite2);
            assnType = FavoriteTypeEnum.FAVORITE.getValue();

            if (accountId >= 0)
                FavoriteUtils.favoriteContent(this, accountId, contentId);
        }
        SharedPreferencesUtils.putIntSP(this, "FavoriteChange", contentId);
    }

    @Override
    public void onResume() {
        super.onResume();
        ContentDTO contentDTO = ContentsDataOperator.getById(this, contentId);
        if (contentDTO != null) {
            if (contentDTO.isFavorite()) {
                assnType = FavoriteTypeEnum.FAVORITE.getValue();
                favoriteImage.setImageResource(R.drawable.selector_icon_detail_favorite2);
            } else {
                assnType = FavoriteTypeEnum.NO_FEEL.getValue();
                favoriteImage.setImageResource(R.drawable.selector_icon_detail_favorite);
            }
        }
    }


    public void detailRead(View view) {
        ActionLogOperator.add(this, new AccountActionLogDTO(accountId, 29, contentId));

        ContentDTO contentDTO = new ContentDTO();
        contentDTO.setId(contentId);
        contentDTO.setBonusAmount(new BigDecimal(bonusAmount));
        contentDTO.setLocalPath(localPath);
        contentDTO.setContent(content);
        contentDTO.setType(bonusType);
        contentDTO.setLink(link);
        contentDTO.setFavorite(assnType == FavoriteTypeEnum.FAVORITE.getValue());
        contentDTO.setTitle(title);
        contentDTO.setImage(image);
        contentDTO.setHasMore(hasMore);

        PageChangeUtils.gotoDetail(this, contentDTO, "DetailActivity", -1);
        this.finish();
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
        currentIndex = i;

        ImageView view = adapter.getView(i);
        ContentDTO contentDTO = ContentsDataOperator.getById(this, view.getId());
        contentId = contentDTO.getId();
        assnType = contentDTO.getAccountAssnType();
        bonusType = contentDTO.getType();
        image = contentDTO.getImage();
        localPath = contentDTO.getLocalPath();
        hasMore = contentDTO.getHasMore();
        link = contentDTO.getLink();
        title = contentDTO.getTitle();
        content = contentDTO.getContent();
        bonusAmount = contentDTO.getBonusAmount().toString();

        initContent();
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_MENU) { //按下的如果是BACK，同时没有重复
          //do something here
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}