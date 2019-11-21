package com.smart.lock.view;

import java.lang.reflect.Method;
import java.util.ArrayList;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.RelativeLayout;

import com.smart.lock.R;


public class AdvertiseLayout extends RelativeLayout
{
	private int IMAGE_SIZE = 4;
	
    private Gallery advertisementGallery;

    private View pointLayout;

    private GalleryPoint mSwithBtnContainer;

    private AdvertiseGalleryAdapter advertisementAdapter;

    private final Activity activity; 


    public AdvertiseLayout(Activity activity)
    {
        super(activity);
        this.activity = activity;
        init();
    }


    public static boolean galleryMoveNext(Gallery gallery)
    {

        if (gallery == null)
        {
            return false;
        }

        if (gallery.getCount() <= 0)
        {
            return false;
        }

        Class<Gallery> clazz = Gallery.class;
        try
        {
            Method moveNextMethod = clazz.getDeclaredMethod("moveNext");
            moveNextMethod.setAccessible(true);
            boolean b = (Boolean) moveNextMethod.invoke(gallery);
            return b;
        }
        catch (Exception e)
        {
        }
        return false;
    }

    public static boolean galleryMovePrevious(Gallery gallery)
    {

        if (gallery == null)
        {
            return false;
        }

        if (gallery.getCount() <= 0)
        {
            return false;
        }

        Class<Gallery> clazz = Gallery.class;
        try
        {
            Method move = clazz.getDeclaredMethod("movePrevious");
            move.setAccessible(true);
            boolean b = (Boolean) move.invoke(gallery);
            return b;
        }
        catch (Exception e)
        {
        }
        return false;
    }
    
    private void updateGallery()
    {
        int count = advertisementGallery.getCount();
        if (count > 0)
        {
            galleryMoveNext(advertisementGallery);
        }
    }

    private void init()
    {
        View view = inflate(getContext(), R.layout.advertise_gallery, null);
        advertisementGallery = (Gallery) view.findViewById(R.id.myGallery);
        addView(view);
        ArrayList<Integer> advertisementList = new ArrayList<Integer>();
        advertisementList.add(R.drawable.guide_1);
        advertisementList.add(R.drawable.guide_2);
        advertisementList.add(R.drawable.guide_3);
        advertisementList.add(R.drawable.guide_4);

        if (advertisementAdapter == null)
        {
            advertisementAdapter = new AdvertiseGalleryAdapter((Activity)getContext(), advertisementList, advertisementGallery);
            advertisementGallery.setAdapter(advertisementAdapter);
        }
        else
        {
            advertisementAdapter.setList(advertisementList);
        }
        mSwithBtnContainer = (GalleryPoint) findViewById(R.id.switcherbtn_container);
        pointLayout = findViewById(R.id.recomm_intr);
        pointLayout.setVisibility(View.INVISIBLE);

        advertisementGallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3)
            {
                setAdvertisementIndicator(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {

            }
        });
    }

    private void setAdvertisementIndicator(int position)
    {
        int size = IMAGE_SIZE;
        position = position % size;
        if (position >= 0 && position < size)
        {
            if (mSwithBtnContainer != null)
            {
                mSwithBtnContainer.setSelectedSwitchBtn(position);
            }
        }
    }

    private void addSwitchBtn()
    {
        if (mSwithBtnContainer != null)
        {
            mSwithBtnContainer.addSwitchBtn(IMAGE_SIZE);
        }

        if (advertisementGallery != null)
        {
            setAdvertisementIndicator(advertisementGallery.getSelectedItemPosition());
        }
    }

}
