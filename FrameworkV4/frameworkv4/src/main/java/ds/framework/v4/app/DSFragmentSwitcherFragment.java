package ds.framework.v4.app;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

abstract public class DSFragmentSwitcherFragment extends DSFragment {

	private int mCurrentFragmentPosition = 0;
	
	/**
	 * switch options descriptors to select next fragment<br/>
	 * make sure you add as many as sub fragments to switch to
	 */
	private ArrayList<SwitchOption> mSwitchOptions = new ArrayList<SwitchOption>();
	
	/**
	 * action bar items to select next fragment with
	 */
	private ArrayList<MenuItem> mActionBarSwitchItems = new ArrayList<MenuItem>();

	/**
	 * order of the sub fragments
	 */
	private HashMap<Integer, Integer> mOrder;
	
	public DSFragmentSwitcherFragment() {
		super();
	}
	
	public DSFragmentSwitcherFragment(boolean isDialog) {
		super(isDialog);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		showCurrentFragment();
		
		return mRootView;
	}
	
	/**
	 * 
	 * @param containerViewID
	 * @param fragment
	 * @param name
	 * @param position
	 */
	protected void attachSubFragment(int containerViewID, DSFragment fragment, String name, int position) {
		super.attachSubFragment(containerViewID, fragment, name);
		
		mOrder.put(position, containerViewID);
	}
	
	/**
	 * 
	 * @param option
	 */
	protected void addSwitchOption(SwitchOption option) {
		mSwitchOptions.add(option);
	}
	
	@Override
	protected void createActionBarItems(Menu menu) {
		super.createActionBarItems(menu);
		
		final DSActivity activity = getDSActivity();
		
		for(SwitchOption option : mSwitchOptions) {
			MenuItem item = activity.findMenuItem(option.optionID);
			if (item == null) {
				item = activity.addMenuItem(
						option.optionID, 
						option.titleResID, 
						option.iconResID,
						option.position,
						false
				);
				mActionBarSwitchItems.add(item);
			}
		}
	}
	
	@Override
	protected void handleActionBarItems(boolean active) {
		super.handleActionBarItems(active);
		
		handleSwitchOptions(active);
	}
	
	@Override
	public boolean onMenuItemSelected(int itemId) {
		for(MenuItem switchItem : mActionBarSwitchItems) {
			if (switchItem.getItemId() == itemId) {
				
				// switch to next sub fragment
				int nextPosition = mCurrentFragmentPosition + 1;
				if (nextPosition == getSubFragments().size()) {
					nextPosition = 0;
				}
				switchToFragment(nextPosition);
				return true;
			}
		}
		return super.onMenuItemSelected(itemId);
	}
	
	/**
	 * manage switch options like icon/text in the action bar/overflow menu
	 */
	protected void handleSwitchOptions(boolean active) {
		final DSActivity activity = getDSActivity();
		
		final int switchItemsSize = mActionBarSwitchItems.size();
		for(int i = 0; i < switchItemsSize; ++i) {
			mActionBarSwitchItems.get(i).setVisible(active && i == mCurrentFragmentPosition);
		}
	}
	
	/**
	 * 
	 * @param position
	 */
	public void switchToFragment(int position) {
		if (position == mCurrentFragmentPosition) {
			return;
		}
		
		HashMap<Integer, DSFragment> subfragments = getSubFragments();
		
		if (subfragments.isEmpty()) {
			
			// sub fragments not yet created
			mCurrentFragmentPosition = position;
			return;	
		}
		
		if (subfragments.get(mOrder.get(mCurrentFragmentPosition)).isActive()) {
			subfragments.get(mOrder.get(mCurrentFragmentPosition)).setActive(false);
		}
		
		mCurrentFragmentPosition = position;
		
		if (subfragments.get(mOrder.get(mCurrentFragmentPosition)).isActive() != isActive()) {
			subfragments.get(mOrder.get(mCurrentFragmentPosition)).setActive(isActive());
		}
		
		showCurrentFragment();
		
		handleSwitchOptions(isActive());
	}
	
	/**
	 * 
	 */
	private void showCurrentFragment() {
		HashMap<Integer, DSFragment> subfragments = getSubFragments();
		
		for(int i = 0; i < mOrder.size(); ++i) {
			DSFragment subfragment = subfragments.get(mOrder.get(i));
			subfragment.getRootView().setVisibility(i == mCurrentFragmentPosition ? View.VISIBLE : View.GONE);
			++i;
		}
	}
	
	@Override
	public void setActive(boolean active) {
		super.setActive(active);
		
		if (active && getDSActivity() != null) {
			switchToFragment(mCurrentFragmentPosition);
		}
	}
	
	@Override
	protected void setActiveSubFragments(boolean active) {
		
		// only set current fragment to active
		final HashMap<Integer, DSFragment> subFragments = getSubFragments();
		subFragments.get(mOrder.get(mCurrentFragmentPosition)).setActive(active);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("current-fragment-position", mCurrentFragmentPosition);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mCurrentFragmentPosition = savedInstanceState.getInt("current-fragment-position"); 
	}
	
	/**
	 * @class SwitchOption
	 */
	public static class SwitchOption {	
		private int position;
		private int optionID;
		private int iconResID;
		private int titleResID; 
		
		public SwitchOption(int optionID, int titleResID, int iconResID) {
			this.optionID = optionID;
			this.iconResID = iconResID;
			this.titleResID = titleResID;
		}
	}
}
