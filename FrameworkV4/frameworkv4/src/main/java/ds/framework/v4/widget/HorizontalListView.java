/*
	Copyright 2012 Dániel Sólyom

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package ds.framework.v4.widget;

import java.util.ArrayList;

import ds.framework.v4.common.Debug;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Scroller;

public class HorizontalListView extends AdapterView<ListAdapter> {
	
    private int mNextLeftIndex = -1;
    private int mNextRightIndex = 0;

    private int mCurrentX;
    protected int mDeltaX;
    
    protected ListAdapter mAdapter;
	private DataSetObserver mDataObserver = new DataSetObserver();
	protected boolean mDataSetChanged;

	private OnItemSelectedListener mOnItemSelectedListener;
	private OnItemClickListener mOnItemClickListener;
	private OnItemLongClickListener mOnItemLongClickListener;

	private Scroller mScroller;
	private OnGestureListener mOnGesture = new OnGestureListener(); 
	private GestureDetector mGesture;
	
	private final RecycleBin mRecycler = new RecycleBin();
	private boolean mChildrenDirty;
	private boolean mInLayout;

	public boolean mSelectOnTap = true; // TODO: make it changeable from layout

	private boolean mInTouchMode = true;
	
	private int mSelected = INVALID_POSITION;
	private boolean mForceStopScrolling;

	/**
	 * set true if children need to fill at least the width of this list view<br/>
	 * this actually gives a minimum width to all children equals to the (list view width / adapter count)
	 * so it is possible that children would fill the list view without this flag set to true but 
	 * with it, they will be even wider
	 */
	protected boolean mChildrenFillWidth = false; // TODO: make it changeable from layout
	
	private int mCount = -1;
	
	private Runnable mRequestLayoutRunnable = new Runnable() {
        
    	@Override
        public void run() {
            requestLayout();
        }
    };
	private boolean mLayoutRequestSent;
    
    public HorizontalListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        mScroller = new Scroller(getContext());
        mGesture = new GestureDetector(getContext(), mOnGesture);
    }

	@Override
	public ListAdapter getAdapter() {
		return mAdapter;
	}

	@Override
	public View getSelectedView() {
		return null;
	}
	
	@Override
	public int getFirstVisiblePosition() {
        return mNextLeftIndex + 1;
    }

	@Override
	public void setAdapter(ListAdapter adapter) {
		if (mAdapter == adapter) {
			return;
		}
		if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataObserver);
        }
        mAdapter = adapter;

        if (mAdapter != null) {
        	mAdapter.registerDataSetObserver(mDataObserver);
        	mCount = mAdapter.getCount();
        } else {
        	mCount = 0;
        }

       	reset();
	}

    private synchronized void reset() {
        mNextLeftIndex = -1;
        mNextRightIndex = 0;
        mChildrenDirty = false;
        
        mSelected = INVALID_POSITION;
        
        mScroller.forceFinished(true);
        mCurrentX = 0;
        mDeltaX = 0;

        removeAllViewsInLayout();
        mRecycler.clear();
        requestLayout();
    }
    
    public void selectPosition(int position, final boolean smooth) {
    	position = lookForSelectablePosition(position);
    	if (position == INVALID_POSITION) {
    		return;
    	}
    	changeSelectState(position - mNextLeftIndex - 1, true);
    	if (mDataSetChanged) {
    		final int pos = position;
    		post(new Runnable() {

				@Override
				public void run() {
					scrollToPosition(pos, smooth);
				}
    			
    		});
    	} else {
    		scrollToPosition(position, smooth);
    	}
    }

    /**
     * scrolls the shortest amount needed for the item at position to be fully on the screen
     * 
     * @param position
     * @param smooth
     */
    public synchronized void scrollToPosition(int position, boolean smooth) {
    	if (!smooth || mChildrenDirty) {
    		setSelectionFromLeft(position, 0);
    		return;
    	}
    	position = lookForSelectablePosition(position);
    	if (position == INVALID_POSITION) {
    		return;
    	}
    	Integer scrollAmount = scrollAmountForPosition(position);
    	if (scrollAmount == null) {
    		
    		// TODO: smooth
    		setHardSelectionFromLeft(position, 0);
    		return;
    	}

    	if (scrollAmount >= 0) {
    		
    		// count amount needed for the selected child to be fully on the screen
    		mDeltaX = getChildAt(position - mNextLeftIndex - 1).getWidth() - (getWidth() - scrollAmount);
    		if (mDeltaX < 0) {
    			
    			// it is already on the screen
        		mDeltaX = 0;
    		}
    	} else {
    		
    		// selected child is a bit out on the left side of the screen
    		mDeltaX = scrollAmount;
    	}
    	if (mDeltaX != 0) {
    		if (smooth) {
    			scrollBy(mDeltaX);
    		} else {
    			requestLayout();
    		}
    	}
    }
    
    @Override
    public void setSelection(int position) {
    	setSelectionFromLeft(position, 0);
    }
    
    public void setSelectionFromLeft(int position, int offset) {
    	if (!mInTouchMode) {
    		
    	}
    	position = lookForSelectablePosition(position);
    	if (position == INVALID_POSITION) {
    		return;
    	}
    	if (!mChildrenDirty) {
    		final Integer scrollAmount = scrollAmountForPosition(position);
    		if (scrollAmount != null) {
    			mDeltaX = scrollAmount - offset;
    			requestLayout();
    			return;
    		}
    	}
		setHardSelectionFromLeft(position, offset);
    }
    
    private void setHardSelectionFromLeft(int position, int offset) {
    	mNextLeftIndex = position - 1;
    	mNextRightIndex = position;
    	mScroller.forceFinished(true);
    	mCurrentX = 0;
    	mDeltaX = -offset;
    	mChildrenDirty = true;
    	
    	requestLayout();
    }
	
	private Integer scrollAmountForPosition(int position) {
		if (mNextLeftIndex < position && mNextRightIndex > position) {
			return getChildAt(position - mNextLeftIndex - 1).getLeft();
		}
		return null;
	}

	int lookForSelectablePosition(int position) {
        final ListAdapter adapter = mAdapter;
        if (adapter == null) {
            return INVALID_POSITION;
        }

        final int count = mCount;

        if (position < 0 || position >= count) {
            return INVALID_POSITION;
        }
        return position;
    }
	
	@Override
	public void requestLayout() {
		if (mInLayout) {
			return;
		}
		mLayoutRequestSent = false;
		
		super.requestLayout();
	}
	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec); 
        
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY) {
            int height = 0;

            final int childCount = getChildCount();
            for(int i = 0; i < childCount; i++) {
                View v = getChildAt(i);
                v.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
                if(v.getMeasuredHeight() > height) {
                    height = v.getMeasuredHeight();
                }
            }
            
            if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
                int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
                if(maxHeight < height) {
                    height = maxHeight;
                }
            }

            setMeasuredDimension(getMeasuredWidth(), height);
        }
    }
	 
	@Override
    protected synchronized void onLayout(boolean changed, int left, int top, int right, int bottom) {
		mInLayout = true;
		
        super.onLayout(changed, left, top, right, bottom);

        if (mAdapter == null || mCount == 0) {
        	mInLayout = false;
            return;
        }

        if (mChildrenDirty) {
        	scrapAllViews();
        }
        if (mDataSetChanged) {
        	mDataSetChanged = false;
        	final View child = getChildAt(0);
        	int leftOffset = 0;
        	if (child != null) {
        		leftOffset = child.getLeft();
        	}
        	scrapAllViews();
        	mDeltaX = -leftOffset;
        } else if (mScroller.computeScrollOffset()) {
        	
        	if (mForceStopScrolling) {
        		
        		// force stop scrolling
        		mScroller.forceFinished(true);
        		mForceStopScrolling = false;
        	} else {

        		// scrolling
        		mDeltaX = mScroller.getCurrX() - mCurrentX;
        	}
        } else {
        	setInTouchMode(true);
        }
        
        // fill right gap (when moving left)
        int spaceLeft = fillRightGap();
        
        // fill left gap (when moving right)
        spaceLeft = fillLeftGap(spaceLeft);
        
        // and set position of all children while scraping all which went off screen
        positionChildren(spaceLeft);
      
        mCurrentX += mDeltaX;
        
        if (!mScroller.isFinished()) {
            post(mRequestLayoutRunnable);
            
        }        
        
        mInLayout = false;
	}

	private int fillRightGap() {
		View child = getChildAt(getChildCount() - 1);
		int right = 0;
		if (child != null) {
			right = child.getRight();
		}
		int spaceLeft = getWidth() + mDeltaX - right;
		final int count = mCount;
		while(spaceLeft > 0 && mNextRightIndex < count) {
			child = createAndAddChild(mNextRightIndex++, -1);
            spaceLeft -= child.getMeasuredWidth();
		}
		if (mNextRightIndex == count && mDeltaX != 0 && spaceLeft >= 0) {
			
			// got the rightmost view while scrolling and still have space left?
			// stop scrolling and move back so the rightmost view's right edge is on the right 'wall'
			mDeltaX -= spaceLeft;
			mScroller.forceFinished(true);
			return 0;
		}

		return spaceLeft > 0 ? spaceLeft : 0;
	}
	
	private int fillLeftGap(int spaceLeft) {
		View child = getChildAt(0);

		if (!mChildrenDirty && child != null) {
			spaceLeft += child.getLeft();
		}
		spaceLeft -= mDeltaX;
		while(spaceLeft > 0 && mNextLeftIndex >= 0) {
			child = createAndAddChild(mNextLeftIndex--, 0);
            spaceLeft -= child.getMeasuredWidth();
		}
		if (mNextLeftIndex == -1 && mDeltaX != 0 && spaceLeft >= 0) {

			// got the leftmost view while scrolling and still have space left?
			// stop scrolling and move back so the leftmost view's left edge is on the left 'wall'
			mDeltaX += spaceLeft;
			spaceLeft = 0;
			mScroller.forceFinished(true);
		}
		return spaceLeft;
	}
	
    private View createAndAddChild(int position, int viewPos) {
		final View child = mAdapter.getView(position, mRecycler.getScrapView(), this);
		child.setClickable(child.isClickable() || mOnItemClickListener != null);
		
		// set child's selected state
		child.setSelected(mSelected == position);
		
		// add child
        LayoutParams params = child.getLayoutParams();
        if (params == null) {
            params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        }

        addViewInLayout(child, viewPos, params, true);
        
        child.measure(MeasureSpec.makeMeasureSpec(getWidth(), MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
        
        final int childCount = getChildCount();
        if (mChildrenFillWidth && !mAdapter.isEmpty() && childCount == mCount) {
        	
        	// fill the remaining space
        	int rw = getWidth();
        	
        	if (childCount == 1) {
        		getChildAt(0).setMinimumWidth(rw);
	        } else {
	        	
	        	final View[] children = new View[childCount];
	        	final int[] ws = new int[childCount];	// width of the children atm
	        	for(int i = 0; i < childCount; ++i) {
	        		children[i] = getChildAt(i);
	        		ws[i] = children[i].getMeasuredWidth();
	        		rw -= ws[i];
	        	}
	        	int avgIncrease = 1;
	        	while(rw > 0 && avgIncrease > 0) {

	        		// find smallest children
	        		final int sc[] = new int[childCount + 1];
	        		int cw = Integer.MAX_VALUE; // current smallest width
	        		int scat = -1;
	        		int mw = 0;
	        		for(int i = 0; i < childCount; ++i) {
	        			if (ws[i] < cw) {
	        				mw = cw;
	        				scat = 1;
	        				sc[0] = i;
	        				sc[1] = -1;
	        				cw = ws[i];
	        			} else if (ws[i] == cw) {
	        				sc[scat] = i;
	        				sc[++scat] = -1;
	        			} else if (ws[i] < mw) {
	        				mw = ws[i];
	        			}
	        		}
	        			// sc now contains smallest children's position with an ending -1
	        			// scat now has the smallest chilren count
	        			// mw has the next width bigger then the smallest children's width
	        		avgIncrease = Math.min(rw / scat, mw - ws[sc[0]]);
	        		for(int i = 0; i < scat; ++i) {
	        			ws[sc[i]] += avgIncrease;
	        		}
	        		rw -= avgIncrease * scat;
	        	}

	        	final int mwidth = getWidth();
	        	for(int i = 0; i < childCount; ++i) {
	        		children[i].setMinimumWidth(ws[i]);
	        		children[i].measure(MeasureSpec.makeMeasureSpec(mwidth, MeasureSpec.UNSPECIFIED),
	                        MeasureSpec.makeMeasureSpec(getHeight(), MeasureSpec.AT_MOST));
	        	}
	        }
        }

        if (!mLayoutRequestSent) {
        	mLayoutRequestSent = true;
        	post(new Runnable() {
        		@Override
        		public void run() {
        			requestLayout();
        		}
        	});
        }
        
        return child;
    }
	
	private void positionChildren(int spaceLeft) {
		final int width = getWidth();
        int childCount = getChildCount();
        int cleft = spaceLeft < 0 ? spaceLeft : 0;
        int cright = cleft;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            int childWidth = child.getMeasuredWidth();
            cright += childWidth;
            if (cleft >= width) {
            	
            	// child no longer on screen (went out right) - scrap it
            	mRecycler.scrapChild(child);
            	--childCount;
            	--i;
            	--mNextRightIndex;
            	cleft = cright;
            	continue;
            }
            if (cright < 0) {
            	
            	// child no longer on screen (went out left) - scrap it
            	mRecycler.scrapChild(child);
            	--childCount;
            	--i;
            	++mNextLeftIndex;
            	cleft = cright;
            	continue;
            }
            child.layout(cleft, 0, cright, child.getMeasuredHeight());
            cleft = cright;
        }
        mChildrenDirty = false;
	}
	
	private void scrapAllViews() {
		final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
        	mRecycler.scrapChild(getChildAt(0));
        }
    	if (mNextLeftIndex > mCount - 2) {
    		mNextLeftIndex = mCount - 2;
    		if (mNextLeftIndex < -1) {
    			mNextLeftIndex = -1;
    		}
    	}
    	mNextRightIndex = mNextLeftIndex + 1;
    	mChildrenDirty = true;
    	mCurrentX = 0;
    	mScroller.forceFinished(true);
	}
	
	private void setInTouchMode(boolean in) {
		if (mAdapter != null && in == false) {
			
			// remove children pressed state
			final int childCount = getChildCount();
			for(int i = 0; i < childCount; ++i) {
				getChildAt(i).setPressed(false);
			}
		}
		mInTouchMode = in;
	}
	
	@Override
    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
        mOnItemSelectedListener = listener;
    }
    
    @Override
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener){
        mOnItemClickListener = listener;
    }
    
    @Override
    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }
    
    public synchronized void scrollTo(int x) {
    	setInTouchMode(false);
    	final int nextPos = mCurrentX + mDeltaX;
        mScroller.startScroll(nextPos, 0, x - nextPos, 0);
        requestLayout();
    }
    
    /**
     * stops current scrolling and starts scrolling by x
     * 
     * @param x
     */
    public synchronized void scrollBy(int x) {
    	setInTouchMode(false);
    	mDeltaX = 0;
    	mScroller.startScroll(mCurrentX, 0, x, 0);
        requestLayout();
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean handled = super.dispatchTouchEvent(ev);
        handled |= mGesture.onTouchEvent(ev);
        return handled;
    }
	
    protected boolean onDown(MotionEvent e) {
    	if (!mScroller.isFinished()) {
    		mForceStopScrolling = true;   
    	} else {
    		setInTouchMode(true);
    		int[] pos = new int[2];
        	HorizontalListView.this.getLocationOnScreen(pos);
            final int position = getChildPositionAtPoint((int) e.getRawX() - pos[0], (int) e.getRawY() - pos[1]);
            if (position != INVALID_POSITION) {
            	getChildAt(position).setPressed(true);
            }
    	}
        return true;
    }
    
    protected boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
            float velocityY) {
    	synchronized(HorizontalListView.this) {
    		setInTouchMode(false);
    		mScroller.fling(mCurrentX + mDeltaX, 0, (int) -velocityX, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
    	}
    	requestLayout();
    
    	return true;
    }
    
    /**
     * get view child position at point
     * 
     * @param x
     * @param y
     * @return
     */
    protected int getChildPositionAtPoint(int x, int y) {
    	Rect viewRect = new Rect();
        int childCount = getChildCount();
        for(int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            viewRect.set(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
            if (viewRect.contains(x, y)) {
            	return i;
            }
        }
        return INVALID_POSITION;
    }
    
    /**
     * change select state by view position (viewPos can be negative)
     * 
     * @param viewPos
     * @param selected
     */
    private void changeSelectState(int viewPos, boolean selected) {
    	if (selected) {
    		if (mSelected != INVALID_POSITION) {
    			changeSelectState(mSelected - mNextLeftIndex - 1, false);
    		}
    		mSelected = mNextLeftIndex + 1 + viewPos;
    	} else if (mSelected == mNextLeftIndex + 1 + viewPos) {
    		mSelected = INVALID_POSITION;
    	}
    	final View child = getChildAt(viewPos);
    	if (child != null) {
    		child.setSelected(selected);
    	}
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
    	if (!mInTouchMode || !mScroller.isFinished()) {
    		return true;
    	}

        return super.onInterceptTouchEvent(e);
    }

    /**
     * OnGestureListener
     */
    class OnGestureListener extends GestureDetector.SimpleOnGestureListener {

    	@Override
		public boolean onDown(MotionEvent e) {
    		return HorizontalListView.this.onDown(e);
    	}
    	
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            return HorizontalListView.this.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            synchronized(HorizontalListView.this) {
            	setInTouchMode(false);
                mDeltaX = (int) distanceX;
            }
            
            getParent().requestDisallowInterceptTouchEvent(true);
            
            requestLayout();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
        	if (!mInTouchMode) {
        		return false;
        	}
        	int[] pos = new int[2];
        	HorizontalListView.this.getLocationOnScreen(pos);
            final int position = getChildPositionAtPoint((int) e.getRawX() - pos[0], (int) e.getRawY() - pos[1]);
            if (position != INVALID_POSITION) {
            	final View tapped = getChildAt(position);
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(
                    		HorizontalListView.this, 
                    		tapped, 
                    		mNextLeftIndex + 1 + position, 
                    		mAdapter.getItemId( mNextLeftIndex + 1 + position)
                    );
                }
                if (mSelectOnTap) {
                	final int childCount = getChildCount();
                	for(int i = 0; i < childCount; i++) {
                		changeSelectState(i, i == position);
                	}
                	if (mOnItemSelectedListener != null) {
                        mOnItemSelectedListener.onItemSelected(
                        		HorizontalListView.this, 
                        		tapped, 
                        		mNextLeftIndex + 1 + position,
                        		mAdapter.getItemId( mNextLeftIndex + 1 + position)
                        );
                	}
                }
            }
            return true;
        }
        
        @Override
        public void onLongPress(MotionEvent e) {
        	if (!mInTouchMode) {
        		return;
        	}
        	int[] pos = new int[2];
        	HorizontalListView.this.getLocationOnScreen(pos);
            final int position = getChildPositionAtPoint((int) e.getRawX() - pos[0], (int) e.getRawY() - pos[1]);
            if (position != INVALID_POSITION) {
            	final View pressed = getChildAt(position);
	            if (mOnItemLongClickListener != null) {
	                mOnItemLongClickListener.onItemLongClick(
	                		HorizontalListView.this, 
	                		pressed, 
	                		mNextLeftIndex + 1 + position, 
	                		mAdapter.getItemId(mNextLeftIndex + 1 + position)
	                );
	            }
            }
        }

    };
    
    /**
     * RecycleBin
     */
    class RecycleBin {
    	
    	private final ArrayList<View> mScrapViews = new ArrayList<View>();
    	private int mMaxScrapCount;
    	
    	void scrapChild(View child) {
    		removeViewInLayout(child);
    		if (mScrapViews.size() < mMaxScrapCount) {
    			child.setClickable(false);
    			child.setSelected(false);
    			child.setPressed(false);
    			mScrapViews.add(child);
    		}
    	}
    	
    	View getScrapView() {
    		if (!mScrapViews.isEmpty()) {
    			return mScrapViews.remove(0);
    		} else {
    			++mMaxScrapCount;
    			return null;
    		}
    	}
    	
    	void clear() {
    		mMaxScrapCount = 0;
    		mScrapViews.clear();
    	}
    }
	
    /**
     * DataSetObserver
     */
	class DataSetObserver extends android.database.DataSetObserver {

        @Override
        public void onChanged() {
            synchronized(HorizontalListView.this) {
                mDataSetChanged = true;
            }
            
            mCount = mAdapter != null ? mAdapter.getCount() : 0;
            
            invalidate();
            requestLayout();
        }

        @Override
        public void onInvalidated() {
        	mCount = -1;
            reset();
            invalidate();
            requestLayout();
        }
        
    };
    
}
