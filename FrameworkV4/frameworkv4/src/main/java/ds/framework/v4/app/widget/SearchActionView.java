package ds.framework.v4.app.widget;

import ds.framework.v4.Global;
import ds.framework.v4.R;
import ds.framework.v4.app.DSActivity;
import ds.framework.v4.common.Common;
import ds.framework.v4.common.Debug;
import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class SearchActionView extends LinearLayout {

	private TextView mSearchEditText;
	private TextWatcher mTextChangedListener;
	private String mOldSearch;
	private boolean mHasPendingSearch;
	
	private OnSearchActionListener mListener;
	
	private View mContainerExpanded;
	private View mSearchButton;
	
	private boolean mIsOpen;
	
	private int mNextSearchDueIn = 500;

    private int mAppBarContentInsetLeft;
    private int mAppBarContentInsetRight;

    private boolean mToolbarHadUpButton;

    public SearchActionView(Context context) {
		this(context, null);
	}
	
	public SearchActionView(Context context, AttributeSet attrs) {
		super(context, attrs);

		LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.x_search_action_view, this, true);
        
        mContainerExpanded = findViewById(R.id.container_expanded);
        mSearchButton = findViewById(R.id.search_button);
        
        mSearchButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					DSActivity activity = (DSActivity) Global.getCurrentActivity();
					activity.trackMenuItem(R.string.x_Search);
				} catch(Throwable e) {
					;
				}
				
				open();
			}
		});

		findViewById(R.id.search_action_btn_reset).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				stopSearch();
				close();
			}
			
		});

		mSearchEditText = (TextView) findViewById(R.id.search_action_text);
		mTextChangedListener = new TextWatcher() {

			private Runnable mStartSearchRunnable = new StartSearchRunnable();

			@Override
			public void afterTextChanged(Editable e) {
				removeCallbacks(mStartSearchRunnable);
				postDelayed(mStartSearchRunnable, mNextSearchDueIn);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
			
			class StartSearchRunnable implements Runnable {
					
				@Override
				public void run() {
					startSearch(false);
				}
					
			};
		};
	}
	
	public void setOnSearchActionListener(OnSearchActionListener listener) {
		mListener = listener;
	}
	
	/**
	 * 
	 * @param value
	 */
	public void setNextSearchDueIn(int value) {
		mNextSearchDueIn = value;
	}
	
	/**
	 * 
	 */
	public void open() {
        if (mIsOpen) {
            return;
        }

        DSActivity activity = (DSActivity) Global.getCurrentActivity();
        if (activity == null) {
            return;
        }

        mToolbarHadUpButton = activity.isHomeVisible();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);

		mContainerExpanded.setVisibility(View.VISIBLE);
		mSearchButton.setVisibility(View.GONE);
		
		mSearchEditText.addTextChangedListener(mTextChangedListener);
		mSearchEditText.clearFocus();
		mSearchEditText.requestFocus();
		Common.openKeyboard(getContext(), mSearchEditText);
		
		mSearchEditText.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                try {
                    if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                        startSearch(true);
                        Common.closeKeyboard(v.getContext(), v);
                    }
                } catch (Exception e) {
                    Debug.logException(e);
                }
                return false;
            }
        });
		
		mListener.onSearchOpened();

        Toolbar appBar = ((DSActivity) Global.getCurrentActivity()).getAppBar();
        if (appBar != null) {
            mAppBarContentInsetLeft = appBar.getContentInsetLeft();
            mAppBarContentInsetRight = appBar.getContentInsetRight();
            appBar.setContentInsetsAbsolute(0, 0);
        }
		
		mIsOpen = true;
	}
	
	/**
	 * 
	 */
	public void close() {
		close(true);
	}
	
	public void close(boolean removeSearchText) {
        DSActivity activity = (DSActivity) Global.getCurrentActivity();
        if (activity == null) {
            return;
        }

        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(mToolbarHadUpButton);

		if (removeSearchText) {
			stopSearch();
		}
		
		mSearchEditText.clearFocus();
		
		final TextView dst = (TextView) mSearchEditText;
		dst.setOnFocusChangeListener(null);
		dst.setOnEditorActionListener(null);
		dst.setOnKeyListener(null);
		if (mTextChangedListener != null) {
			dst.removeTextChangedListener(mTextChangedListener);
		}
		
		if (removeSearchText) {
			mOldSearch = null;
		}
		
		mContainerExpanded.setVisibility(View.GONE);
		mSearchButton.setVisibility(View.VISIBLE);
		
		mListener.onSearchClosed();

        Toolbar appBar = ((DSActivity) Global.getCurrentActivity()).getAppBar();
        if (appBar != null) {
            appBar.setContentInsetsAbsolute(mAppBarContentInsetLeft, mAppBarContentInsetRight);
        }
		
		mIsOpen = false;
	}
	
	/**
	 * 
	 */
	public boolean isOpen() {
		return mIsOpen;
	}

	/**
	 * 
	 * @param hint
	 */
	public void setHint(int hint) {
		((EditText) mSearchEditText).setHint(hint);
	}
	
	/**
	 * 
	 * @param searchText
	 */
	public void setSearchText(String searchText) {
		((TextView) mSearchEditText).setText(searchText);
	}
	
	/**
	 * 
	 * @param hint
	 */
	public void setHint(String hint) {
		((EditText) mSearchEditText).setHint(hint);
	}

	/**
	 * 
	 */
	public void clear() {
		((TextView) mSearchEditText).setText("");
	}

	public void stopSearch() {
		clear();
		startSearch(true);
	}
	
	private void startSearch(boolean finalTouch) {
		mHasPendingSearch = false;

		if (mSearchEditText == null) {
			return;
		}
		final String newSearch = mSearchEditText.getText().toString();
		if (newSearch.equals(mOldSearch) && !finalTouch) {
			return;
		}
		mOldSearch = newSearch;
		try {
			// start the actual search
			mListener.onSearchAction(getSearchText(), finalTouch);
		} catch(NullPointerException e) {
			;
		}
	}
	
	public String getSearchText() {
		return ((EditText) mSearchEditText).getText().toString();
	}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if (!mIsOpen) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        // TODO: it should take the width of the toolbar (need to check?)
        // for now its always the width of the screen
        super.onMeasure(MeasureSpec.makeMeasureSpec((int) Global.getScreenWidth(), MeasureSpec.EXACTLY), heightMeasureSpec);
    }
	
	public static interface OnSearchActionListener {
		public void onSearchOpened();
		public void onSearchClosed();
		public void onSearchAction(String text, boolean finalTouch);
	}
}
