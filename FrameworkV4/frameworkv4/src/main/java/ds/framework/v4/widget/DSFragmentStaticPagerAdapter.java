package ds.framework.v4.widget;

import java.util.ArrayList;

import android.support.v13.app.FragmentPagerAdapterModByDS;
import android.app.Fragment;
import android.app.FragmentManager;

import ds.framework.v4.app.DSFragment;

public class DSFragmentStaticPagerAdapter extends FragmentPagerAdapterModByDS {

	final private ArrayList<Fragment> mItems = new ArrayList<Fragment>();
	
	public DSFragmentStaticPagerAdapter(FragmentManager fm) {
		super(fm);
	}
	
	/**
	 * add fragment
	 * 
	 * @param fragment
	 */
	public void addFragment(Fragment fragment) {
		mItems.add(fragment);
	}
	
	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Fragment getItem(int position) {
		return mItems.get(position);
	}
	
	/**
	 * 
	 */
	public void reset() {
		mItems.clear();
		notifyDataSetChanged();
	}

	@Override
	public CharSequence getPageTitle(int position) {
        return ((DSFragment) getItem(position)).getFragmentTitle();
	}
}
