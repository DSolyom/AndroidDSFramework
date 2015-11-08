package ds.framework.v4.app.widget;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapterModByDS;
import android.view.ViewGroup;

import ds.framework.v4.app.DSFragment;
import ds.framework.v4.common.Debug;

/**
 * Created by solyom on 04/11/15.
 */
abstract public class FragmentPagerRecyclerAdapter extends FragmentPagerAdapterModByDS {

    int mFragmentUse[] = new int[6];
    DSFragment mSameFragments[] = new DSFragment[6];

    public FragmentPagerRecyclerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        DSFragment item = (DSFragment) super.instantiateItem(container, position);
        return item;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);

        for(int i = 0; i < 6; ++i)
            if (mFragmentUse[i] == position + 1) {
                mFragmentUse[i] = 0;
            }
    }

    @Override
    public String makeFragmentName(int viewId, long id) {
        for(int i = 0; i < 6; ++i) {
            if (mFragmentUse[i] == id + 1) {
                return super.makeFragmentName(viewId, i);
            }
        }
        for(int i = 0; i < 6; ++i) {
            if (mFragmentUse[i] == 0) {
                mFragmentUse[i] = (int) id + 1;
                return super.makeFragmentName(viewId, i);
            }
        }
        return null;
    }

    @Override
    public Fragment getItem(int position) {
        for(int i = 0; i < 6; ++i) {
            if (mFragmentUse[i] == position + 1) {
                if (mSameFragments[i] == null) {
                    mSameFragments[i] = createSameFragment();
                }
                return mSameFragments[i];
            }
        }
        return null;
    }

    protected abstract DSFragment createSameFragment();
}
