package com.smart.lock.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.smart.lock.R;
import com.smart.lock.common.SlideConstants;


public class PullToRefreshListView extends ListView implements OnScrollListener {

    private static final int TAP_TO_REFRESH = 1;
    private static final int PULL_TO_REFRESH = 2;
    private static final int RELEASE_TO_REFRESH = 3;
    private static final int REFRESHING = 4;

    private static final String TAG = "PullToRefreshListView";

    private OnRefreshListener mOnRefreshListener;

    /**
     * Listener that will receive notifications every time the list scrolls.
     */
    private OnScrollListener mOnScrollListener;
    private LayoutInflater mInflater;

    private RelativeLayout mRefreshHeaderView, mRefreshFooterView;
    private TextView mRefreshViewText;
    private ImageView mRefreshViewImage, mFooterRefreshViewImage, mRefreshViewProgress;
    private TextView mRefreshViewLastUpdated;

    private int mCurrentScrollState;
    private int mRefreshState;

    private RotateAnimation mFlipAnimation;
    private RotateAnimation mReverseFlipAnimation;
    private RotateAnimation rotateAnimation;

    private int mRefreshViewHeight;
    private int mRefreshOriginalTopPadding;
    private int mLastMotionY;

    private boolean mBounceHack, hasLoadAll = false;

    private int getLastVisiblePosition, lastVisiblePositionY;

    public PullToRefreshListView(Context context) {
        super(context);
        init(context);
    }

    public PullToRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PullToRefreshListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setHasLoadAll(boolean hasLoadAll) {
        this.hasLoadAll = hasLoadAll;
    }

    public boolean isHasLoadAll() {
        return hasLoadAll;
    }

    public RelativeLayout getmRefreshFooterView() {
        return mRefreshFooterView;
    }

    private void init(Context context) {
        // Load all of the animations we need in code rather than through XML
        mFlipAnimation = new RotateAnimation(0, -180,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mFlipAnimation.setInterpolator(new LinearInterpolator());
        mFlipAnimation.setDuration(250);
        mFlipAnimation.setFillAfter(true);
        mReverseFlipAnimation = new RotateAnimation(-180, 0,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
        mReverseFlipAnimation.setDuration(250);
        mReverseFlipAnimation.setFillAfter(true);
        rotateAnimation = new RotateAnimation(0, 359,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(500);
        rotateAnimation.setRepeatCount(-1);
        rotateAnimation.setRepeatMode(Animation.RESTART);
        rotateAnimation.setFillAfter(true);


        mInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

		mRefreshHeaderView = (RelativeLayout) mInflater.inflate(
                R.layout.pull_to_refresh_header, this, false);

        mRefreshFooterView = (RelativeLayout) mInflater.inflate(
                R.layout.pull_to_refresh_footer, this, false);

        mRefreshViewText =
                (TextView) mRefreshHeaderView.findViewById(R.id.pull_to_refresh_text);
        mRefreshViewImage =
            (ImageView) mRefreshHeaderView.findViewById(R.id.pull_to_refresh_image);
        mRefreshViewProgress =
            (ImageView) mRefreshHeaderView.findViewById(R.id.pull_to_refresh_progress);
        mRefreshViewProgress.setVisibility(View.GONE);
        mRefreshViewLastUpdated =
            (TextView) mRefreshHeaderView.findViewById(R.id.pull_to_refresh_updated_at);

        mFooterRefreshViewImage = (ImageView) mRefreshFooterView.findViewById(R.id.pull_to_refresh_progress);
        mFooterRefreshViewImage.startAnimation(rotateAnimation);
        mRefreshViewImage.setMinimumHeight(20);
        mRefreshHeaderView.setOnClickListener(new OnClickRefreshListener());
        mRefreshOriginalTopPadding = mRefreshHeaderView.getPaddingTop();

        mRefreshState = TAP_TO_REFRESH;
        addHeaderView(mRefreshHeaderView);
        addFooterView(mRefreshFooterView);

        super.setOnScrollListener(this);

        measureView(mRefreshHeaderView);
        mRefreshViewHeight = mRefreshHeaderView.getMeasuredHeight();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setSelection(1);
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        setSelection(1);
    }

    /**
     * Set the listener that will receive notifications every time the list
     * scrolls.
     * 
     * @param l The scroll listener. 
     */
    @Override
    public void setOnScrollListener(OnScrollListener l) {
        mOnScrollListener = l;
    }

    /**
     * Register a callback to be invoked when this list should be refreshed.
     * 
     * @param onRefreshListener The callback to run.
     */
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }

    /**
     * Set a text to represent when the list was last updated. 
     * @param lastUpdated Last updated at.
     */
    public void setLastUpdated(CharSequence lastUpdated) {
        if (lastUpdated != null) {
            mRefreshViewLastUpdated.setVisibility(View.VISIBLE);
            mRefreshViewLastUpdated.setText(lastUpdated);
        } else {
            mRefreshViewLastUpdated.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int y = (int) event.getY();
        mBounceHack = false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                if (!isVerticalScrollBarEnabled()) {
                    setVerticalScrollBarEnabled(true);
                }
                if (getFirstVisiblePosition() == 0 && mRefreshState != REFRESHING) {
                    if ((mRefreshHeaderView.getBottom() >= mRefreshViewHeight
                            || mRefreshHeaderView.getTop() >= 0)
                            && mRefreshState == RELEASE_TO_REFRESH) {
                        // Initiate the refresh
                        mRefreshState = REFRESHING;
                        prepareForRefresh();
                        onRefresh(false);
                    } else if (mRefreshHeaderView.getBottom() < mRefreshViewHeight
                            || mRefreshHeaderView.getTop() <= 0) {
                        // Abort refresh and scroll down below the refresh view
                        resetHeader();
                        setSelection(1);
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:
                mLastMotionY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                applyHeaderPadding(event);
                break;
        }
        return super.onTouchEvent(event);
    }

    private void applyHeaderPadding(MotionEvent ev) {
        // getHistorySize has been available since API 1
        int pointerCount = ev.getHistorySize();

        Log.d("pulltorefresh", "applyHeaderPadding State:" + mRefreshState);
        for (int p = 0; p < pointerCount; p++) {
            if (mRefreshState == RELEASE_TO_REFRESH) {
                if (isVerticalFadingEdgeEnabled()) {
                    setVerticalScrollBarEnabled(false);
                }

                int historicalY = (int) ev.getHistoricalY(p);

                // Calculate the padding to apply, we divide by 1.7 to
                // simulate a more resistant effect during pull.
                int topPadding = (int) (((historicalY - mLastMotionY)
                        - mRefreshViewHeight) / 6.7);
                Log.d("pulltorefresh", "topPadding:" + topPadding);
                mRefreshHeaderView.setPadding(
                        mRefreshHeaderView.getPaddingLeft(),
                        topPadding,
                        mRefreshHeaderView.getPaddingRight(),
                        mRefreshHeaderView.getPaddingBottom());
            }
        }
    }

    /**
     * Sets the header padding back to original size.
     */
    private void resetHeaderPadding() {
        mRefreshHeaderView.setPadding(
                mRefreshHeaderView.getPaddingLeft(),
                mRefreshOriginalTopPadding,
                mRefreshHeaderView.getPaddingRight(),
                mRefreshHeaderView.getPaddingBottom());
    }

    /**
     * Resets the header to the original state.
     */
    private void resetHeader() {
        if (mRefreshState != TAP_TO_REFRESH) {
            mRefreshState = TAP_TO_REFRESH;

            resetHeaderPadding();

            // Set refresh view text to the pull label
            mRefreshViewText.setText(R.string.pull_to_refresh_refreshing_label);
            // Replace refresh drawable with arrow drawable
            mRefreshViewImage.setImageResource(R.drawable.arrow_down);
            // Clear the full rotation animation
            mRefreshViewImage.clearAnimation();
            // Hide progress bar and arrow.
            mRefreshViewImage.setVisibility(View.GONE);
            mRefreshViewProgress.clearAnimation();
            mRefreshViewProgress.setVisibility(View.GONE);
        }
    }

    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0,
                0 + 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
        // When the refresh view is completely visible, change the text to say
        // "Release to refresh..." and flip the arrow drawable.
        if (mCurrentScrollState == SCROLL_STATE_TOUCH_SCROLL
                && mRefreshState != REFRESHING) {
            if (firstVisibleItem == 0) {
                mRefreshViewImage.setVisibility(View.VISIBLE);
                if ((mRefreshHeaderView.getBottom() >= mRefreshViewHeight
                        || mRefreshHeaderView.getTop() >= 0)
                        && mRefreshState != RELEASE_TO_REFRESH) {
                    mRefreshViewText.setText(R.string.pull_to_refresh_release_label);
                    mRefreshViewImage.clearAnimation();
                    mRefreshViewImage.startAnimation(mFlipAnimation);
                    mRefreshState = RELEASE_TO_REFRESH;
                    Log.d("pulltorefresh", "pull_to_refresh_release_label State:" + mRefreshState +
                            "mRefreshHeaderView.getBottom():" + mRefreshHeaderView.getBottom() +
                            "\nmRefreshViewHeight:" + mRefreshViewHeight +
                            "\nmRefreshHeaderView.getTop()" + mRefreshHeaderView.getTop());
                } else if (mRefreshHeaderView.getBottom() < mRefreshViewHeight
                        && mRefreshState != PULL_TO_REFRESH) {
                    mRefreshViewText.setText(R.string.pull_to_refresh_pull_label);
                    if (mRefreshState != TAP_TO_REFRESH) {
                        mRefreshViewImage.clearAnimation();
                        mRefreshViewImage.startAnimation(mReverseFlipAnimation);
                    }
                    mRefreshState = PULL_TO_REFRESH;
                    Log.d("pulltorefresh", "pull_to_refresh_pull_label State:" + mRefreshState +
                            "mRefreshHeaderView.getBottom():" + mRefreshHeaderView.getBottom() +
                            "\nmRefreshViewHeight:" + mRefreshViewHeight );
                }
            } else {
                mRefreshViewImage.setVisibility(View.GONE);
                resetHeader();
            }
        } else if (mCurrentScrollState == SCROLL_STATE_FLING
                && firstVisibleItem == 0
                && mRefreshState != REFRESHING) {
            setSelection(1);
            mBounceHack = true;
        } else if (mBounceHack && mCurrentScrollState == SCROLL_STATE_FLING) {
            setSelection(1);
        }

        if (mOnScrollListener != null) {
            mOnScrollListener.onScroll(view, firstVisibleItem,
                    visibleItemCount, totalItemCount);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        mCurrentScrollState = scrollState;

        if (mCurrentScrollState == SCROLL_STATE_IDLE) {
            mBounceHack = false;
            //滚动到底部
            if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
                if(view.getCount() != SlideConstants.MAX_LOCAL_CONTENT_SIZE-1 && !hasLoadAll) {
                    View v = (View) view.getChildAt(view.getChildCount() - 1);
                    int[] location = new int[2];
                    v.getLocationOnScreen(location);//获取在整个屏幕内的绝对坐标
                    int y = location[1];
                    getLastVisiblePosition = view.getLastVisiblePosition();
                    lastVisiblePositionY = y;
                    onRefresh(true);
                } else {
                    hasLoadAll = true;
                }
                return;
            }
            //未滚动到底部，第二次拖至底部都初始化
            getLastVisiblePosition=0;
            lastVisiblePositionY=0;
        }

        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    public void prepareForRefresh() {
        resetHeaderPadding();

        mRefreshViewImage.setVisibility(View.GONE);
        // We need this hack, otherwise it will keep the previous drawable.
        mRefreshViewImage.setImageDrawable(null);
        mRefreshViewProgress.startAnimation(rotateAnimation);
        mRefreshViewProgress.setVisibility(View.VISIBLE);

        // Set refresh view text to the refreshing label
        mRefreshViewText.setText(R.string.pull_to_refresh_refreshing_label);

        mRefreshState = REFRESHING;
    }

    public void onRefresh(boolean isLoadMore) {
        Log.d(TAG, "onRefresh");

        if (mOnRefreshListener != null) {
            mOnRefreshListener.onRefresh(isLoadMore);
        }
    }

    /**
     * Resets the list to a normal state after a refresh.
     * @param lastUpdated Last updated at.
     */
    public void onRefreshComplete(CharSequence lastUpdated) {
        setLastUpdated(lastUpdated);
        onRefreshComplete();
        resetHeaderPadding();
    }

    /**
     * Resets the list to a normal state after a refresh.
     */
    public void onRefreshComplete() {        
        Log.d(TAG, "onRefreshComplete");

        resetHeader();
        invalidateViews();
        // If refresh view is visible when loading completes, scroll down to
        // the next item.
        if (getFirstVisiblePosition() == 0) {
            invalidateViews();
            setSelection(1);
        }
    }

    /**
     * Invoked when the refresh view is clicked on. This is mainly used when
     * there's only a few items in the list and it's not possible to drag the
     * list.
     */
    private class OnClickRefreshListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (mRefreshState != REFRESHING) {
                prepareForRefresh();
                onRefresh(true);
            }
        }

    }

    /**
     * Interface definition for a callback to be invoked when list should be
     * refreshed.
     */
    public interface OnRefreshListener {
        /**
         * Called when the list should be refreshed.
         * <p>
         * A call to {@link PullToRefreshListView #onRefreshComplete()} is
         * expected to indicate that the refresh has completed.
         */
        public void onRefresh(boolean isLoadMore);
    }
}
