/*
	Copyright 2013 Dániel Sólyom

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
package ds.framework.v4.app;

import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import ds.framework.v4.R;
import ds.framework.v4.app.widget.SearchActionView;
import ds.framework.v4.app.widget.SearchActionView.OnSearchActionListener;
import ds.framework.v4.template.Template;

abstract public class DSFragment extends DialogFragment 
		implements DSFragmentInterface, OnSearchActionListener {

	public final static int DATA_INVALID = 0;
	public final static int DATA_LOADING = 1;
	public final static int DATA_LOADED = 2;

	public final static int DATA_DISPLAYED = 4;

	/**
	 * the current data state<br/>
	 * <br/>
	 * DATA_INVALID - data is invalid and needs to be loaded
	 * DATA_LOADING - data is being loaded atm
	 * DATA_LOADED	- data is loaded
	 * DATA_DISPLAYED - data is loaded and displayed
	 */
	protected int mDataState = DATA_INVALID;

	protected Template mTemplate;

	/**
	 * used to identify this fragment (like when saving state)<br/>
	 * only use this for fragments not having a tag - !set it before onAttach
	 */
	protected String mFragmentId;

    /**
     * title of the fragment - used when a fragment can have it's own title (ie. in a viewpager-tab context)
     */
    protected String mFragmentTitle;

    /**
     * ths root view's layout resource id (to be inflated)
     */
    protected int mRootViewLayoutResID;

	/**
	 * the root view in the fragment's view hierarchy
	 */
	protected View mRootView;

	/**
	 * is fragment state restored?
	 */
	private boolean mStateRestored;

	/**
	 * flag to tell if the fragment is active when resumed<br/>
	 * set to true as default
	 */
	protected boolean mActiveDefault = true;

	protected boolean mActive = false;

	/**
	 * map of sub fragments
	 */
	private final HashMap<Integer, DSFragment> mSubFragments = new HashMap<Integer, DSFragment>();

	/**
	 * parent fragment of subfragment
	 */
	private DSFragment mParent;

	/**
	 * root view id of subfragment (only used if mContainerViewID is unset)
	 */
	private Integer mRootViewID;

	/**
	 * parent of root view for subfragment
	 */
	private Integer mContainerViewID;

	/**
	 * are action bar items created
	 */
	boolean mActionBarItemsCreated;

	/**
	 * can search content using action bar SearchView
	 */
	protected boolean mSearchable;

	/**
	 * hint for search if mSearchable = true
	 */
	protected String mSearchHint;

	private String mSearchText;
	MenuItem mMenuSearchItem;
	private boolean mSearchOpened;

	/**
	 * should remove search text and reset search when back pressed while searching?
	 */
	protected boolean mRemoveSearchOnBack = true;

	/**
	 *
	 */
	public DSFragment() {
		super();

		setShowsDialog(false);
	}

	/**
	 *
	 * @param isDialog
	 */
	public DSFragment(boolean isDialog) {
		super();

        setShowsDialog(isDialog);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			onRestoreInstanceState(savedInstanceState);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (mFragmentId == null) {
			mFragmentId = getTag();
		}

		mActive = mActiveDefault;

		((DSActivity) activity).onFragmentAttached(this);

		attachSubFragmentsInner();
	}

	/**
	 *
	 */
	public void onDetach() {
		mActive = false;

		super.onDetach();
	}

	/**
	 *
	 */
	final public void attachSubFragmentsInner() {
		if (mSubFragments.isEmpty()) {
			attachSubFragments();
		}
	}

	/**
	 * Override to attach sub fragments
	 */
	public void attachSubFragments() {
		;
	}

	/**
	 *
	 * @return
	 */
	public HashMap<Integer, DSFragment> getSubFragments() {
		return mSubFragments;
	}

	/**
	 *
	 * @param containerResID
	 * @return
	 */
	public DSFragment findSubFragmentByContainer(int containerResID) {
		return mSubFragments.get(containerResID);
	}

	/**
	 *
	 * @param parent
	 */
	protected void setParent(DSFragment parent) {
		mParent = parent;
	}

	/**
	 *
	 * @return
	 */
	public DSFragment getParent() {
		return mParent;
	}

	/**
	 *
	 * @return
	 */
	public DSActivity getDSActivity() {
		if (mParent != null) {
			return mParent.getDSActivity();
		}
		return (DSActivity) getActivity();
	}

	/**
	 * attach subfragment
	 *
	 * @param containerViewID
	 * @param fragment
	 * @param name
	 */
	protected void attachSubFragment(int containerViewID, DSFragment fragment, String name) {
		fragment.setContainerViewID(containerViewID);

		fragment.setFragmentId(name);
		mSubFragments.put(containerViewID, fragment);
		fragment.setParent(this);
	}

	/**
	 *
	 * @param id
	 */
	public void setRootViewID(int id) {
		mRootViewID = id;
	}

	/**
	 *
	 * @param id
	 */
	public void setContainerViewID(int id) {
		mContainerViewID = id;
	}

	/**
	 *
	 */
	public Integer getContainerViewID() {
		return mContainerViewID;
	}

	/**
	 *
	 * @param rootView
	 */
	public void setRootView(View rootView) {
		mRootView = rootView;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (savedInstanceState != null && !mStateRestored) {
			onRestoreInstanceState(savedInstanceState);
			mStateRestored = true;
		}

		mRootView = getRootView(inflater, container);

		onViewCreated(mRootView);

		for(Integer containerViewID : mSubFragments.keySet()) {
			final DSFragment subfragment = mSubFragments.get(containerViewID);
			if (containerViewID != null) {
				final ViewGroup subfragmentContainer = (ViewGroup) mRootView.findViewById(containerViewID);
				final View subfragmentRootView = subfragment.getRootView(inflater, subfragmentContainer);
				subfragmentContainer.addView(subfragmentRootView);
				subfragment.setRootView(subfragmentRootView);
			}
			subfragment.onViewCreated(mRootView);
		}

		loadData(true);

		display(true);

		return mRootView;
	}

	/**
	 * called from onCreateView after root view is created and started data loading but before display
	 */
	protected void onViewCreated(View rootView) {
		if (mRootView == null) {
			if (mRootViewID != null) {
				mRootView = rootView.findViewById(mRootViewID);
			} else {
				mRootView = rootView;
			}
		}

		mTemplate = createTemplate();
	}

	protected Template createTemplate() {
		return new Template((ActivityInterface) getDSActivity(), mRootView);
	}

	@Override
	public void onResume() {
		if (mParent == null) {
			super.onResume();
		}

		final DSActivity activity = getDSActivity();
		if (mActive && activity.getOptionsMenu() != null) {

			// only handle options menu if active
			createAndHandleOptionsMenu(activity);
		}

		loadDataAndDisplay();

		for(DSFragment subfragment : mSubFragments.values()) {
            subfragment.onResume();
		}
	}

	@Override
	public void onPause() {
		if (mParent == null) {
			super.onPause();
		}

		for(DSFragment subfragment : mSubFragments.values()) {
            subfragment.onPause();
		}
	}

	@Override
	public void onDestroyView() {
		if (mParent == null) {
            super.onDestroyView();
		}

		mTemplate = null;
		mRootView = null;
		if (mDataState == DATA_DISPLAYED) {
			mDataState = DATA_LOADED;
		}

		for(DSFragment subfragment : mSubFragments.values()) {
			subfragment.onDestroyView();
		}
	}

	@Override
	public void onDestroy() {
		if (mParent == null) {
			super.onDestroy();
		}

		mDataState = DATA_INVALID;

		for(DSFragment subfragment : mSubFragments.values()) {
			subfragment.onDestroy();
		}
	}

	/**
	 *
	 * @return
	 */
	public View getRootView() {
		return mRootView;
	}

	/**
	 *
	 * @param inflater
	 * @param container
	 * @return
	 */
	protected View getRootView(LayoutInflater inflater, ViewGroup container) {
        if (mRootViewLayoutResID != 0) {
            final View rootView = inflater.inflate(mRootViewLayoutResID, container, false);
            return rootView;
        }
        return null;
    }

	/**
	 * set the fragment id prior attach
	 *
	 * @param id
	 */
	public void setFragmentId(String id) {
		mFragmentId = id;
	}

	/**
	 *
	 * @return
	 */
	public String getFragmentId() {
		return mFragmentId;
	}

    /**
     *
     * @return
     */
    public String getFragmentTitle() {
        return mFragmentTitle;
    }

    /**
     *
     * @param title
     */
    public void setFragmentTitle(String title) {
        mFragmentTitle = title;
    }
	
	/**
	 * get rootView's or container activity's context
	 * 
	 * @return
	 */
	public Context getContext() {
		if (mRootView != null) {
			return mRootView.getContext();
		}
		return getDSActivity();
	}
	
	/**
	 * get data state
	 * 
	 * @return
	 */
	public int getDataState() {
		return mDataState;
	}
	
	/**
	 * load data and display it, but only if needed<br/>
	 * make sure it is only called after the activities SetContentView()
	 */
	public void loadDataAndDisplay() {
		if (getContext() == null) {
			
			// we may need context for this which we might not have at the moment
			return;
		}
		
		if (mRootView == null) {
			return;
		}

		loadData(true);

		display(true);
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean canLoadData() {
		return mDataState == DATA_INVALID;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean canDisplay() {
		return mDataState != DATA_DISPLAYED;
	}
	
	/**
	 * the drawback with this is that you have to call load data manually in sub fragments when
	 * this fragment loads data in the background as you don't want to call this (super.loadData)
	 */
	public void loadData(boolean subfragmentToo) {
		if (canLoadData()) {
			loadData();
		}
		
		if (subfragmentToo) {
			for(DSFragment subfragment : mSubFragments.values()) {
				if (subfragment.getDataState() == DATA_INVALID) {
					subfragment.loadData(true);	
				}
			}
		}
	}
	
	/**
	 * 
	 */
	public void display(boolean subfragmentToo) {
		if (canDisplay()) {
			display();
		}
		
		if (subfragmentToo) {
			for(DSFragment subfragment : mSubFragments.values()) {
				if (subfragment.getDataState() != DATA_DISPLAYED) {
					subfragment.display(true);
				}
			}
		}
	}
	
	/**
	 *
	 */
	protected void loadData() {
		onDataLoaded();
	}

	/**
	 * 
	 */
	public void invalidateData(boolean subfragmentToo) {
		invalidateData();
		
		if (subfragmentToo) {
			for(DSFragment subfragment : mSubFragments.values()) {
				if (subfragment.getDataState() != DATA_INVALID) {
					subfragment.invalidateData(true);
				}
			}
		}
	}

	/**
	 * invalidate data so it is needed to be loaded again 
	 */
	public void invalidateData() {
		mDataState = DATA_INVALID;
	}
	
	/**
	 * 
	 * @param subfragmentToo
	 */
	public void reloadData(boolean subfragmentToo) {
		reloadData();
		
		if (subfragmentToo) {
			for(DSFragment subfragment : mSubFragments.values()) {
				if (subfragment.getDataState() != DATA_INVALID) {
					subfragment.reloadData(true);
				}
			}
		}
	}
	
	/**
	 * invalidate and reload data
	 */
	public void reloadData() {
		invalidateData();
		
		if (getContext() == null || mRootView == null) {
			
			// we may need context for this which we might not have at the moment
			return;
		}
		
		if (canLoadData()) {
			loadData();
		}
	}
	
	/**
	 * invalidate, reload and display data</br>
	 * use it when you want to reload async data but also want display to be called
	 */
	public void reloadDataAndDisplay() {
		reloadData();
		
		if (mRootView != null && canDisplay()) {
			display();
		}
	}
	
	/**
	 * invalidate display of data so it needs to be displayed again
	 */
	public void invalidateDisplay() {
		if (mDataState == DATA_DISPLAYED) {
			mDataState = DATA_LOADED;
		}
	}
	
	/**
	 * invalidate display of data so it needs to be displayed again
	 * 
	 * @param subfragmentsToo
	 */
	public void invalidateDisplay(boolean subfragmentsToo) {
		invalidateDisplay();
		
		if (subfragmentsToo) {
			for(DSFragment subfragment : mSubFragments.values()) {
				subfragment.invalidateDisplay(true);
			}
		}
	}
	
	/**
	 * refresh display
	 */
	public void refresh() {
		invalidateDisplay();

		if (mRootView != null && canDisplay()) {
			display();
		}
	}
	
	/**
	 * call this when data is loaded in the background
	 */
	protected void onDataLoaded() {
		mDataState = DATA_LOADED;
		
		invalidateDisplay();
		if (mRootView != null && canDisplay()) {
			display();
		}
	}
	
	/**
	 * override to display data
	 */
	public void display() {
		if (mDataState == DATA_LOADED) {
			mDataState = DATA_DISPLAYED;
		}
	}
	
	/**
	 * get data state to save in onSaveInstanceState
	 * 
	 * @return
	 */
	protected int getDataStateToSave() {
		return DATA_INVALID;
	}

	/**
	 * restart all data
	 */
	public void reset() {
		invalidateData();
	}

	/**
	 * to tell the fragment about activity result
	 * 
	 * @param data
	 */
	public void onActivityResult(Object data) {
		for(DSFragment subfragment : mSubFragments.values()) {
			subfragment.onActivityResult(data);
		}
	}

	/**
	 * to tell the fragment about transport
	 * 
	 * @param data
	 */
	public void onTransport(Object data) {
		for(DSFragment subfragment : mSubFragments.values()) {
			subfragment.onTransport(data);
		}
	}
	
	/**
	 * use this if any activation required (like when using a view pager)
	 * 
	 * @param active
	 */
	public void setActive(boolean active) {
		mActiveDefault = active;
		
		if (!active || isAdded()) {
			mActive = mActiveDefault;
		}
		
		if (!mActive) {
			stopSearch();
		}

		setActiveSubFragments(active);
		
		final DSActivity activity = getDSActivity();
		
		if (activity == null) {
			return;
		}

		if (activity.getOptionsMenu() != null) {
			createAndHandleOptionsMenu(activity);
		}
	}
	
	/**
	 * 
	 * @param activity
	 */
	void createAndHandleOptionsMenu(DSActivity activity) {
		if (!mActionBarItemsCreated) {
			mActionBarItemsCreated = true;
			createActionBarItems(activity.getOptionsMenu());
		}
		
		handleActionBarItems(mActive);
	}

	/**
	 * 
	 * @param active
	 */
	protected void setActiveSubFragments(boolean active) {
		for(DSFragment subfragment : mSubFragments.values()) {
			subfragment.setActive(active);
		}
	}

	/**
	 * use this if any activation required (like when using a view pager)
	 * 
	 * @return
	 */
	public boolean isActive() {
		return mActive;
	}

	/**
	 * act when back is pressed - return true if handled - false for normal behavior
	 * 
	 * @return
	 */
	public boolean onBackPressed() {
		if (isSearching()) {
			stopSearch(mRemoveSearchOnBack);
			return true;
		}
		
		boolean handled = false;
		for(DSFragment subfragment : mSubFragments.values()) {
			handled |= subfragment.onBackPressed();
		}
		return handled;
	}
	
	/**
	 * save fragment state<br/>
	 * all ids put in the outState should be unique in the application
	 * 
	 * @param outState
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("fragment-id", mFragmentId);
		outState.putInt(mFragmentId + "-data_state", getDataStateToSave());
		outState.putBoolean(mFragmentId + "-searchable", mSearchable);
		outState.putBoolean(mFragmentId + "-search_opened", mSearchOpened);
		outState.putString(mFragmentId + "-search_text", mSearchText);
		
		for(DSFragment subfragment : mSubFragments.values()) {
			Bundle subOutState = new Bundle();
			subfragment.onSaveInstanceState(subOutState);
			outState.putBundle(subfragment.getFragmentId(), subOutState);
		}
	}
	
	/**
	 * restore saved instance state
	 * 
	 * @param savedInstanceState
	 */
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		mDataState = savedInstanceState.getInt(mFragmentId + "-data_state");
		mSearchable = savedInstanceState.getBoolean(mFragmentId + "-searchable");
		mSearchOpened = savedInstanceState.getBoolean(mFragmentId + "-search_opened");
		mSearchText = savedInstanceState.getString(mFragmentId + "-search_text");
		
		for(DSFragment subfragment : mSubFragments.values()) {
			subfragment.onRestoreInstanceState(savedInstanceState.getBundle(subfragment.getFragmentId()));
		}
	}
	
// Dialog
	
	/**
	 * 
	 * @return
	 */
	public boolean isShowing() {
		final Dialog dialog = getDialog();
		return dialog != null && dialog.isShowing();
	}
	
// ActionBar
	
	/**
	 * 
	 * @param id
	 * @param titleRes
	 * @param icon
	 * @param order
	 * @param refresh
	 * @return
	 */
	public MenuItem addMenuItem(int id, int titleRes, int icon, int order, boolean refresh) {
		final DSActivity activity = getDSActivity();
		return activity.addMenuItem(id, titleRes, icon, order, refresh);
	}
	
	/**
	 * 
	 * @param id
	 * @param title
	 * @param icon
	 * @param order
	 * @param refresh
	 * @return
	 */
	public MenuItem addMenuItem(int id, String title, int icon, int order, boolean refresh) {
		final DSActivity activity = getDSActivity();
		return activity.addMenuItem(id, title, icon, order, refresh);
	}
	
	/**
	 * create action bar items for the first time (called from setActive(true))
	 * 
	 * @param menu
	 */
	protected void createActionBarItems(Menu menu) {
		final DSActivity activity = getDSActivity();
		if (mSearchable) {
			if (activity.findMenuItem(R.string.x_Search) == null) {
				final MenuItem searchItem = activity.addMenuItem(
						R.string.x_Search, 
						R.string.x_Search, 
						R.drawable.x_ic_action_search, 
						0, 
						false
				);
				searchItem.setActionView(createSearchActionView());
			}
		}
	}

	/**
	 * create the action view to show when searching on the action bar
	 * 
	 * @return
	 */
	public View createSearchActionView() {
		return new SearchActionView(getContext());
	}
	
	/**
	 * handle action bar items (visibility etc...) (called from setActive after changing state)
	 *
	 * @param active
	 */
	protected void handleActionBarItems(boolean active) {
		if (!mSearchable) {
			return;
		}
		
		final DSActivity activity = getDSActivity();
		
		if (activity == null) {
			return;
		}
		
		final MenuItem searchItem = activity.findMenuItem(R.string.x_Search);
		if (searchItem != null) {
			final SearchActionView searchActionView = (SearchActionView) searchItem.getActionView();
			searchActionView.setHint(mSearchHint);
			searchActionView.setOnSearchActionListener(this);
			searchItem.setVisible((active || mSearchOpened) && mSearchable);
			
			if (!searchActionView.isOpen() && mSearchText != null && mSearchText.length() > 0 && mSearchOpened) {
				searchActionView.setSearchText(mSearchText);
				searchActionView.open();
			}
		}
	}
	
	/**
	 * called when a menu item is selected (like overflow menu item, title spinner item or popup menu item)
	 * !use this instead of onOptionsItemSelected and functions like that 
	 * 
	 * @param itemId
	 * @return
	 */
	public boolean onMenuItemSelected(int itemId) {
		if (itemId == android.R.id.home) {
			return onActionBarHomeClicked();
		}
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean onActionBarHomeClicked() {
		if (mSearchOpened) {
			stopSearch();
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isSearching() {
		return mSearchOpened;
	}
	
	/**
	 * 
	 */
	public void startSearch() {
		if (!mActive) {
			return;
		}
		
		for(DSFragment subfragment : mSubFragments.values()) {
			subfragment.startSearch();
		}
		
		if (!mSearchable || isSearching()) {
			return;
		}
		
		((SearchActionView) getDSActivity().findMenuItem(R.string.x_Search).getActionView()).open();
	}
	
	/**
	 * 
	 */
	public void stopSearch() {
		stopSearch(true);
	}
	
	public void stopSearch(boolean removeSearchText) {
		final DSActivity activity = getDSActivity();
		if (activity == null) {
			return;
		}
		
		for(DSFragment subfragment : mSubFragments.values()) {
			subfragment.stopSearch(removeSearchText);
		}
		
		if (!isSearching()) {
			return;
		}
		
		final MenuItem searchItem = activity.findMenuItem(R.string.x_Search);
		
		if (searchItem != null) {
			((SearchActionView) searchItem.getActionView()).close(removeSearchText);
		}
	}
	
	/**
	 * 
	 * @param finalTouch
	 */
	protected void reloadForSearch(boolean finalTouch) {
		;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getSearchText() {
		return mSearchText == null ? "" : mSearchText;
	}
	
	/**
	 * 
	 */
	public void setSearchText(String text) {
		mSearchText = text;
		
		final MenuItem searchItem = getDSActivity().findMenuItem(R.string.x_Search);
		if (searchItem != null) {
			final SearchActionView searchActionView = (SearchActionView) searchItem.getActionView();
			if (searchActionView.isOpen()) {
				searchActionView.setSearchText(mSearchText);
			}
		}

	}
	
// implement OnSearchActionListener
	
	@Override
	public void onSearchOpened() {
		mSearchOpened = true;
	}
	
	@Override
	public void onSearchAction(String text, boolean finalTouch) {
		mSearchText = text == null || text.length() == 0 ? null : text;
		reloadForSearch(finalTouch);
	}
	
	@Override
	public void onSearchClosed() {
		mSearchOpened = false;
	}
}
