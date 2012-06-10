package com.trcardmanager.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trcardmanager.R;
import com.trcardmanager.adapter.MovementsListViewAdapter;

public class TRCardManagerListViewBottomLoad extends ListView implements
		OnScrollListener {

	private static final int TAP_TO_REFRESH = 1;
    private static final int PULL_TO_REFRESH = 2;
    private static final int RELEASE_TO_REFRESH = 3;
    private static final int REFRESHING = 4;

    private static final String TAG = "PullToRefreshListView";
    
    private static final int M_REFRESH_TOP_VIEW_ID = 2011; 
    private static final int M_REFRESH_BOTTOM_VIEW_ID = 2012;
    
    public enum ScrollDirection{
    	UP,
    	DOWN,
    	NONE
    }

    
    private ScrollDirection scrollDirection = ScrollDirection.NONE;

    private OnRefreshListenerBottomLoad mOnRefreshListener;

    /**
     * Listener that will receive notifications every time the list scrolls.
     */
    private OnScrollListener mOnScrollListener;
    private LayoutInflater mInflater;

    private RelativeLayout mRefreshTopView;
    private RelativeLayout mRefreshBottomView;
    
    private TextView mRefreshTopViewText;
    private ImageView mRefreshTopViewImage;
    private ProgressBar mRefreshTopViewProgress;
    private TextView mRefreshTopViewLastUpdated;
    
    private TextView mRefreshBottomViewText;
    private ImageView mRefreshBottomViewImage;
    private ProgressBar mRefreshBottomViewProgress;
    private TextView mRefreshBottomViewLastUpdated;

    private int mCurrentScrollState;
    private int mRefreshHeaderState;
    private int mRefreshFooterState;

    private RotateAnimation mFlipAnimation;
    private RotateAnimation mReverseFlipAnimation;

    private int mRefreshTopViewHeight;
    private int mRefreshBottomViewHeight;
    
    private int mRefreshOriginalBottomPadding;
    private int mRefreshOriginalTopPadding;
    
    private int mLastMotionY;

    private boolean mBounceHackHeader;
    private boolean mBounceHackFooter;
	
	public TRCardManagerListViewBottomLoad(Context context) {
		super(context,null);
		init();
	}
	
	public TRCardManagerListViewBottomLoad(Context context, AttributeSet attrs) {
		super(context,attrs);
		init();
	}
	
	public TRCardManagerListViewBottomLoad(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs,defStyle);
		init();
	}
	
	private void init(){
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

        mInflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

		mRefreshTopView = (RelativeLayout) mInflater.inflate(
				R.layout.pull_to_refresh_header, this, false);
		mRefreshBottomView = (RelativeLayout) mInflater.inflate(
				R.layout.pull_to_refresh_header, this, false);
		
        mRefreshTopViewText =
            (TextView) mRefreshTopView.findViewById(R.id.pull_to_refresh_text);
        mRefreshTopViewImage =
            (ImageView) mRefreshTopView.findViewById(R.id.pull_to_refresh_image);
        mRefreshTopViewProgress =
            (ProgressBar) mRefreshTopView.findViewById(R.id.pull_to_refresh_progress);
        mRefreshTopViewLastUpdated =
            (TextView) mRefreshTopView.findViewById(R.id.pull_to_refresh_updated_at);
        
        mRefreshBottomViewText =
            (TextView) mRefreshBottomView.findViewById(R.id.pull_to_refresh_text);
        mRefreshBottomViewImage =
            (ImageView) mRefreshBottomView.findViewById(R.id.pull_to_refresh_image);
        mRefreshBottomViewProgress =
            (ProgressBar) mRefreshBottomView.findViewById(R.id.pull_to_refresh_progress);
        mRefreshBottomViewLastUpdated =
            (TextView) mRefreshBottomView.findViewById(R.id.pull_to_refresh_updated_at);
        
        mRefreshTopViewImage.setMinimumHeight(50);
        mRefreshBottomViewImage.setMinimumHeight(50);
        
        mRefreshTopView.setId(M_REFRESH_TOP_VIEW_ID);
        mRefreshBottomView.setId(M_REFRESH_BOTTOM_VIEW_ID);
        
        mRefreshTopView.setOnClickListener(new OnClickRefreshListener());
        mRefreshBottomView.setOnClickListener(new OnClickRefreshListener());
        
        mRefreshOriginalTopPadding = mRefreshTopView.getPaddingBottom();
        mRefreshOriginalBottomPadding = mRefreshBottomView.getPaddingBottom();
                 
        mRefreshHeaderState = TAP_TO_REFRESH;
        mRefreshFooterState = TAP_TO_REFRESH;

        addHeaderView(mRefreshTopView);
        addFooterView(mRefreshBottomView);

        super.setOnScrollListener(this);

        measureView(mRefreshTopView);
        measureView(mRefreshBottomView);
        
        mRefreshTopViewHeight = mRefreshTopView.getMeasuredHeight();
        mRefreshBottomViewHeight = mRefreshBottomView.getMeasuredHeight();
        
	}

	 	@Override
	    protected void onAttachedToWindow() {
	        setSelection(1);
	    }

	    @Override
	    public void setAdapter(ListAdapter adapter) {
	        super.setAdapter(adapter);
	        Log.i(TAG,"selection to 1");
	       setSelection(1);
	    }

	    /**
	     * Set the listener that will receive notifications every time the list
	     * scrolls.
	     * 
	     * @param l The scroll listener. 
	     */
	    @Override
	    public void setOnScrollListener(AbsListView.OnScrollListener l) {
	        mOnScrollListener = l;
	    }

	    /**
	     * Register a callback to be invoked when this list should be refreshed.
	     * 
	     * @param onRefreshListener The callback to run.
	     */
	    public void setOnRefreshListener(OnRefreshListenerBottomLoad onRefreshListener) {
	        mOnRefreshListener = onRefreshListener;
	    }
	    
	    
	    /**
	     * Set a text to represent when the list was last updated. 
	     * @param lastUpdated Last updated at.
	     */
	    public void setLastHeaderUpdated(CharSequence lastUpdated) {
	        if (lastUpdated != null) {
	            mRefreshTopViewLastUpdated.setVisibility(View.VISIBLE);
	            mRefreshTopViewLastUpdated.setText(lastUpdated);
	        } else {
	            mRefreshTopViewLastUpdated.setVisibility(View.GONE);
	        }
	    }
	    
	    /**
	     * Set a text to represent when the list was last updated. 
	     * @param lastUpdated Last updated at.
	     */
	    public void setLastBottomUpdated(CharSequence lastUpdated) {
	        if (lastUpdated != null) {
	            mRefreshBottomViewLastUpdated.setVisibility(View.VISIBLE);
	            mRefreshBottomViewLastUpdated.setText(lastUpdated);
	        } else {
	            mRefreshBottomViewLastUpdated.setVisibility(View.GONE);
	        }
	    }

	    @Override
	    public boolean onTouchEvent(MotionEvent event) {
	        final int y = (int) event.getY();

        	mBounceHackHeader = false;
       		mBounceHackFooter = false;

	        switch (event.getAction()) {
	            case MotionEvent.ACTION_UP:
	                if (!isVerticalScrollBarEnabled()) {
	                    setVerticalScrollBarEnabled(true);
	                }

		                if (getLastVisiblePosition() == getCount()-1 && mRefreshFooterState != REFRESHING) {
		                    if ((mRefreshBottomView.getTop() <= getHeight() - mRefreshBottomViewHeight
		                            || mRefreshBottomView.getBottom() <=getHeight())
		                            && mRefreshFooterState == RELEASE_TO_REFRESH) {
		                        // Initiate the refresh
		                        mRefreshFooterState = REFRESHING;
		                        prepareForFooterRefresh();
		                        onRefresh();
		                        Log.i(TAG," option 1");
		                    } else if (mRefreshBottomView.getTop() > getHeight() - mRefreshBottomViewHeight
		                            || mRefreshBottomView.getBottom() >= getHeight()) {
		                        // Abort refresh and scroll down below the refresh view
		                        resetFooter();
		                        //setSelection(getFirstVisiblePosition()-1);
		                        setSelection(getCount()-
		                        		(getLastVisiblePosition()-getFirstVisiblePosition()+1));
		                        Log.i(TAG,"selection to first-1 -- 2");
		                    }
		                }

		                
		                if (getFirstVisiblePosition() == 0 && mRefreshHeaderState != REFRESHING) {
		                    if ((mRefreshTopView.getBottom() >= mRefreshTopViewHeight
		                            || mRefreshTopView.getTop() >= 0)
		                            && mRefreshHeaderState == RELEASE_TO_REFRESH) {
		                        // Initiate the refresh
		                        mRefreshHeaderState = REFRESHING;
		                        prepareForHeaderRefresh();
		                        onRefresh();
		                    } else if (mRefreshTopView.getBottom() < mRefreshTopViewHeight
		                            || mRefreshTopView.getTop() <= 0) {
		                        // Abort refresh and scroll down below the refresh view
		                        resetHeader();
		                        setSelection(1);
		                    }
		                }

	                break;
	            case MotionEvent.ACTION_DOWN:
	            	mLastMotionY = y;
	                Log.i(TAG, "DOWN....");
	                break;
	            case MotionEvent.ACTION_MOVE:
	            	Log.i(TAG, "MOVE....");
	            	if(y<mLastMotionY){
	            		scrollDirection = ScrollDirection.DOWN;
	            		applyFooterPadding(event); 
	    	    	}else if(y>mLastMotionY){
	    	    		scrollDirection = ScrollDirection.UP;
	    	    		applyHeaderPadding(event);
	    	    	}
	    	        Log.i(TAG,"Scroll direcction change to "+scrollDirection.name());
	                
	                break;
	        }
	        return super.onTouchEvent(event);
	    }
	    
	    
	    private void applyHeaderPadding(MotionEvent ev) {
	    	Log.i(TAG,"Applying header padding");
	        // getHistorySize has been available since API 1
	        int pointerCount = ev.getHistorySize();

	        for (int p = 0; p < pointerCount; p++) {
	            if (mRefreshHeaderState == RELEASE_TO_REFRESH) {
	                if (isVerticalFadingEdgeEnabled()) {
	                    setVerticalScrollBarEnabled(false);
	                }

	                int historicalY = (int) ev.getHistoricalY(p);

	                // Calculate the padding to apply, we divide by 1.7 to
	                // simulate a more resistant effect during pull.
	                int topPadding = (int) (((historicalY - mLastMotionY)
	                        - mRefreshTopViewHeight) / 1.7);
	                Log.i(TAG,"Setting top padding:"+topPadding);
	                mRefreshTopView.setPadding(
	                        mRefreshTopView.getPaddingLeft(),
	                        topPadding,
	                        mRefreshTopView.getPaddingRight(),
	                        mRefreshTopView.getPaddingBottom());
	            }
	        }
	    }
	    
	    private void applyFooterPadding(MotionEvent ev) {
	    	Log.i(TAG,"Applying footer padding");
	    	// getHistorySize has been available since API 1
	        int pointerCount = ev.getHistorySize();

	        for (int p = 0; p < pointerCount; p++) {
	            if (mRefreshFooterState == RELEASE_TO_REFRESH) {
	                if (isVerticalFadingEdgeEnabled()) {
	                    setVerticalScrollBarEnabled(false);
	                }

	                int historicalY = (int) ev.getHistoricalY(p);

	                // Calculate the padding to apply, we divide by 1.7 to
	                // simulate a more resistant effect during pull.
	                int bottomPadding = (int) (((mLastMotionY - historicalY)
	                        + mRefreshBottomViewHeight) / 1.7);
	                Log.i(TAG,"Setting bottom padding:"+bottomPadding);
	                mRefreshBottomView.setPadding(
	                        mRefreshBottomView.getPaddingLeft(),
	                        mRefreshBottomView.getPaddingTop(),
	                        mRefreshBottomView.getPaddingRight(),
	                        bottomPadding);
	            }
	        }
	    }

	    /**
	     * Sets the header padding back to original size.
	     */
	    private void resetFooterPadding() {
	        mRefreshBottomView.setPadding(
	                mRefreshBottomView.getPaddingLeft(),
	                mRefreshBottomView.getPaddingTop(),
	                mRefreshBottomView.getPaddingRight(),
	                mRefreshOriginalBottomPadding);
	    }
	    
	    /**
	     * Sets the header padding back to original size.
	     */
	    private void resetHeaderPadding() {
	        mRefreshTopView.setPadding(
	                mRefreshTopView.getPaddingLeft(),
	                mRefreshOriginalTopPadding,
	                mRefreshTopView.getPaddingRight(),
	                mRefreshTopView.getPaddingBottom());
	    }
	    
	    /**
	     * Resets the header to the original state.
	     */
	    private void resetHeader() {
	        if (mRefreshHeaderState != TAP_TO_REFRESH) {
	            mRefreshHeaderState = TAP_TO_REFRESH;

	            resetHeaderPadding();

	            // Set refresh view text to the pull label
	            mRefreshTopViewText.setText(R.string.push_to_refresh_tap_label);
	            // Replace refresh drawable with arrow drawable
	            mRefreshTopViewImage.setImageResource(R.drawable.ic_pulltorefresh_arrow);
	            // Clear the full rotation animation
	            mRefreshTopViewImage.clearAnimation();
	            // Hide progress bar and arrow.
	            mRefreshTopViewImage.setVisibility(View.GONE);
	            mRefreshTopViewProgress.setVisibility(View.GONE);
	        }
	    }
	    
	    /**
	     * Resets the header to the original state.
	     */
	    private void resetFooter() {
	        if (mRefreshFooterState != TAP_TO_REFRESH) {
	            mRefreshFooterState = TAP_TO_REFRESH;

	            resetFooterPadding();

	            // Set refresh view text to the pull label
	            mRefreshBottomViewText.setText(R.string.pull_to_refresh_tap_label);
	            // Replace refresh drawable with arrow drawable
	            mRefreshBottomViewImage.setImageResource(R.drawable.ic_pushtorefresh_arrow);
	            // Clear the full rotation animation
	            mRefreshBottomViewImage.clearAnimation();
	            // Hide progress bar and arrow.
	            mRefreshBottomViewImage.setVisibility(View.GONE);
	            mRefreshBottomViewProgress.setVisibility(View.GONE);
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


	    
	    public void onScroll(AbsListView view, int firstVisibleItem,
	            int visibleItemCount, int totalItemCount) {
	    	Log.i(TAG, "onScrollListener...");
	        // When the refresh view is completely visible, change the text to say
	        // "Release to refresh..." and flip the arrow drawable.
	        if (mCurrentScrollState == SCROLL_STATE_TOUCH_SCROLL){
	                if(mRefreshHeaderState != REFRESHING && scrollDirection == ScrollDirection.DOWN) {
	                	actionScrollDown(firstVisibleItem, visibleItemCount, totalItemCount);
	                }else if(mRefreshFooterState != REFRESHING && scrollDirection == ScrollDirection.UP) {
	                	actionScrollUp(firstVisibleItem, visibleItemCount, totalItemCount);
	                }        	
	        } else if (mCurrentScrollState == SCROLL_STATE_FLING
	                && firstVisibleItem == 0
	                && mRefreshHeaderState != REFRESHING) {
	            setSelection(1);
	            mBounceHackHeader = true;	        	
	        } else if (mCurrentScrollState == SCROLL_STATE_FLING
	                && firstVisibleItem + visibleItemCount == totalItemCount
	                && mRefreshFooterState != REFRESHING) {
	        	setSelection(totalItemCount-visibleItemCount);
	            mBounceHackFooter = true;
	        //} else if (mBounceHack && mCurrentScrollState == SCROLL_STATE_FLING) {
	        } else if (mCurrentScrollState == SCROLL_STATE_FLING) {
	        	//setSelection(firstVisibleItem-1);
	        	//if(scrollDirection == ScrollDirection.UP){
	        	if(mBounceHackHeader){// && firstVisibleItem == 0){
	        		setSelection(1);
	        	}else if(mBounceHackFooter){// && firstVisibleItem + visibleItemCount == totalItemCount){
	        		setSelection(totalItemCount-visibleItemCount);
	        	}
	        }
	        if (mOnScrollListener != null) {
	            mOnScrollListener.onScroll(view, firstVisibleItem,
	                    visibleItemCount, totalItemCount);
	        }
	    }

	    
	    private void actionScrollUp(int firstVisibleItem,
	            int visibleItemCount, int totalItemCount){
	    	if (firstVisibleItem == 0) {
                mRefreshBottomViewImage.setVisibility(View.VISIBLE);
                if ((mRefreshTopView.getBottom() >= mRefreshTopViewHeight + 20
                        || mRefreshTopView.getTop() >= 0)
                        && mRefreshHeaderState != RELEASE_TO_REFRESH) {
                    mRefreshTopViewText.setText(R.string.push_to_refresh_release_label);
                    mRefreshTopViewImage.clearAnimation();
                    mRefreshTopViewImage.startAnimation(mFlipAnimation);
                    mRefreshHeaderState = RELEASE_TO_REFRESH;
                } else if (mRefreshTopView.getBottom() < mRefreshTopViewHeight + 20
                        && mRefreshHeaderState != PULL_TO_REFRESH) {
                    mRefreshTopViewText.setText(R.string.push_to_refresh_push_label);
                    if (mRefreshHeaderState != TAP_TO_REFRESH) {
                        mRefreshTopViewImage.clearAnimation();
                        mRefreshTopViewImage.startAnimation(mReverseFlipAnimation);
                    }
                    mRefreshHeaderState = PULL_TO_REFRESH;
                }
            } else {
                mRefreshTopViewImage.setVisibility(View.GONE);
                resetHeader();
            }
	    }
	    
	    private void actionScrollDown(int firstVisibleItem,
	            int visibleItemCount, int totalItemCount){
	    	Log.i(TAG,"firstVisibleItem + visibleItemCount >= totalItemCount:"+
        			firstVisibleItem+" + "+visibleItemCount+" >= "+totalItemCount);
            if (firstVisibleItem + visibleItemCount >= totalItemCount) {
                mRefreshBottomViewImage.setVisibility(View.VISIBLE);
                if ((mRefreshBottomView.getTop() <= getHeight() - mRefreshBottomViewHeight - 20
                        || mRefreshBottomView.getBottom() <= getHeight())
                        && mRefreshFooterState != RELEASE_TO_REFRESH) {
                    mRefreshBottomViewText.setText(R.string.pull_to_refresh_release_label);
                    mRefreshBottomViewImage.clearAnimation();
                    mRefreshBottomViewImage.startAnimation(mFlipAnimation);
                    mRefreshFooterState = RELEASE_TO_REFRESH;
                    Log.i(TAG,"option -- 3");
                } else if (mRefreshBottomView.getTop() > getHeight() - mRefreshBottomViewHeight - 20
                        && mRefreshFooterState != PULL_TO_REFRESH) {
                    mRefreshBottomViewText.setText(R.string.pull_to_refresh_pull_label);
                    if (mRefreshFooterState != TAP_TO_REFRESH) {
                    	 Log.i(TAG,"option -- 4A");
                        mRefreshBottomViewImage.clearAnimation();
                        mRefreshBottomViewImage.startAnimation(mReverseFlipAnimation);
                    }
                    mRefreshFooterState = PULL_TO_REFRESH;
                    Log.i(TAG,"option -- 4");
                }
            } else {
                mRefreshBottomViewImage.setVisibility(View.GONE);
                resetFooter();
                Log.i(TAG,"reset footer -- 5");
            }
	    }
	    
	   
	    public void onScrollStateChanged(AbsListView view, int scrollState) {
	        mCurrentScrollState = scrollState;
	    	
	        if (mCurrentScrollState == SCROLL_STATE_IDLE) {
	        	 if(scrollDirection == ScrollDirection.UP){
	        		 mBounceHackHeader = false;
	        		 Log.i(TAG,"Direction to UP ser mBounceHeader --> false");
	        	 }else if(scrollDirection == ScrollDirection.DOWN){
	        		 mBounceHackFooter = false;
	        		 Log.i(TAG,"Direction to DOWN ser mBounceFooter --> false");
	        	 }
	        }

	        if (mOnScrollListener != null) {
	            mOnScrollListener.onScrollStateChanged(view, scrollState);
	        }
	    }

	    
	    public void prepareForHeaderRefresh() {
	        resetHeaderPadding();

	        mRefreshTopViewImage.setVisibility(View.GONE);
	        // We need this hack, otherwise it will keep the previous drawable.
	        mRefreshTopViewImage.setImageDrawable(null);
	        mRefreshTopViewProgress.setVisibility(View.VISIBLE);

	        // Set refresh view text to the refreshing label
	        mRefreshTopViewText.setText(R.string.push_to_refresh_refreshing_label);

	        mRefreshHeaderState = REFRESHING;
	    }
	    
	    public void prepareForFooterRefresh() {
	        resetFooterPadding();

	        mRefreshBottomViewImage.setVisibility(View.GONE);
	        // We need this hack, otherwise it will keep the previous drawable.
	        mRefreshBottomViewImage.setImageDrawable(null);
	        mRefreshBottomViewProgress.setVisibility(View.VISIBLE);

	        // Set refresh view text to the refreshing label
	        mRefreshBottomViewText.setText(R.string.pull_to_refresh_refreshing_label);

	        mRefreshFooterState = REFRESHING;
	    }

	    public void onRefresh() {
	        Log.i(TAG, "onRefresh");

	        if (mOnRefreshListener != null) {
	            mOnRefreshListener.onRefresh();
	        }
	    }

	    /**
	     * Resets the list to a normal state after a refresh.
	     * @param lastUpdated Last updated at.
	     */
	    public void onRefreshComplete(CharSequence lastUpdated) {
	    	if(scrollDirection == ScrollDirection.UP){
	    		setLastHeaderUpdated(lastUpdated);
	    		Log.i(TAG,"Call onrefreshComplete for Header");
	    	}else if(scrollDirection == ScrollDirection.DOWN){
	    		setLastBottomUpdated(lastUpdated);
	    		Log.i(TAG,"Call onrefreshComplete for footer");
	    	}
	        onRefreshComplete();
	    }

	    /**
	     * Resets the list to a normal state after a refresh.
	     */
	    public void onRefreshComplete() {        
	        Log.i(TAG, "onRefreshComplete");
	        
	        if(scrollDirection == ScrollDirection.UP){
	        	Log.i(TAG,"Call onRefreshComplete up -- resetHeader");
	        	resetHeader();
	        	if(mRefreshTopView.getBottom() > 0){
		        	invalidateViews();
		        	Log.i(TAG,"onRefreshComplete up");
		        	setSelection(1);
		        }
	        }else if(scrollDirection == ScrollDirection.DOWN){
	        	Log.i(TAG,"CAll onRefreshComplete down -- resetFooter");
	        	resetFooter();
	        	if (mRefreshBottomView.getTop() < getBottom() ){
		            invalidateViews();
		        	Log.i(TAG,"onRefreshComplete down");
		        	int selectedPosition = getFirstVisiblePosition()-1>0?getFirstVisiblePosition()-1:1;
		        	setSelection(selectedPosition);
		        }
	        }
	        scrollDirection = ScrollDirection.NONE;
	    }

	    
	    
	    /**
	     * Invoked when the refresh view is clicked on. This is mainly used when
	     * there's only a few items in the list and it's not possible to drag the
	     * list.
	     */
	    private class OnClickRefreshListener implements OnClickListener {

	        public void onClick(View v) {
	        	if(v.getId() == M_REFRESH_TOP_VIEW_ID && mRefreshHeaderState != REFRESHING) {
	        		scrollDirection = ScrollDirection.UP;
            		prepareForHeaderRefresh();
            		onRefresh();
	            }else if(v.getId() == M_REFRESH_BOTTOM_VIEW_ID && mRefreshFooterState != REFRESHING) {
	            	scrollDirection = ScrollDirection.DOWN;
	            	prepareForFooterRefresh();
    	        	onRefresh();
	            }
	        }

	    }

	    /**
	     * Interface definition for a callback to be invoked when list should be
	     * refreshed.
	     */
	    public interface OnRefreshListenerBottomLoad {
	        /**
	         * Called when the list should be refreshed.
	         * <p>
	         * A call to {@link PullToRefreshListView #onRefreshComplete()} is
	         * expected to indicate that the refresh has completed.
	         */
	        public void onRefresh();
	    }
	    
	    
	    public ScrollDirection getScrollDirection() {
			return scrollDirection;
		} 
	    
	    
	    
}
