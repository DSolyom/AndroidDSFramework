/**
 * Copyright © 2013 Dániel Sólyom
 */

package ds.framework.v4.app;

import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapterModByDS;
import android.support.v4.view.ViewPagerModByDS.OnPageChangeListener;
import android.view.Menu;

import com.google.samples.apps.iosched.ui.widget.SlidingTabLayoutModByDS;

import ds.framework.v4.Global;
import ds.framework.v4.R;
import ds.framework.v4.data.AbsAsyncData;
import ds.framework.v4.widget.DSFragmentViewPager;
import ds.framework.v4.widget.DSViewPagerTitlebar;

abstract public class AbsDSFragmentViewPagerActivity extends DSActivity implements OnPageChangeListener {

    protected final FragmentPagerAdapterModByDS mAdapter;

    protected int mCurrentPage;

    protected DSFragmentViewPager mViewPager;
    protected SlidingTabLayoutModByDS mSlidingTabLayout;

    private DSViewPagerTitlebar mViewPagerTitlebar;

    protected AbsAsyncData mViewPagerAdapterData;

    protected boolean mGoBackToFirstPageFirst;

    public AbsDSFragmentViewPagerActivity() {
        super();
        mAdapter = createViewPagerAdapter();
        mViewPagerAdapterData = createViewPagerAdapterData();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getOptionsMenu() != null && mViewPager != null) {

            // only resume if options menu is created - menu option items can only be created after that
            // which occures when the view pager resumes
            mViewPager.onResume();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // hiding all menu items as only the current fragments items should be visible
        // if you need activity related items, you should manualy change visibility of those items
        // after this
        hideAllMenuItem();

        if (mViewPager != null) {

            // menu option items can only be created after menu is created so call onResume on the view pager
            // as we could not create them before and it will call the fragments onActive where these items are created
            mViewPager.onResume();
        }

        return true;
    }

    @Override
    public void onPause() {
        if (mViewPager != null) {
            mViewPager.onPause();
        }

        super.onPause();
    }

    @Override
    public void invalidateData() {
        super.invalidateData();

        if (mViewPager != null) {
            for (int i = mAdapter.getCount() - 1; i >= 0; --i) {
                ((DSFragment) mAdapter.getItemFromManager(mViewPager, i)).invalidateData(true);
            }
        }
    }

    @Override
    public void restart() {
        if (mViewPager != null) {
            for (int i = mAdapter.getCount() - 1; i >= 0; --i) {
                final DSFragment fragment = ((DSFragment) mAdapter.getItemFromManager(mViewPager, i));
                if (i == mCurrentPage) {
                    fragment.stopSearch();
                }
                fragment.reset();
            }
        }
        mAdapter.reset();
        super.restart();
    }

    @Override
    protected void attachFragments() {
        ; // no own fragments
    }

    @Override
    public void onActivityResult(Object data) {
        if (data instanceof Integer) {

            // select a page on activity result
            mCurrentPage = (Integer) data;
            if (mViewPager != null) {
                mViewPager.setCurrentItem(mCurrentPage, false);
            }
        }

        super.onActivityResult(data);
    }

    @Override
    public void loadData() {
        if (mViewPager == null) {
            mViewPager = (DSFragmentViewPager) findViewById(R.id.view_pager);
        }

        if (mViewPager != null && mViewPagerAdapterData != null && !mViewPagerAdapterData.isValid()) {
            mViewPagerAdapterData.loadIfNeeded(new AbsAsyncData.OnDataLoadListener() {
                @Override
                public void onDataLoadStart(AbsAsyncData data, int loadId) {

                }

                @Override
                public void onDataLoaded(AbsAsyncData data, int loadId) {
                    onViewPagerAdapterDataLoaded();

                    setAdapterToViewPager();
                }

                @Override
                public void onDataLoadFailed(AbsAsyncData data, int loadId) {

                }

                @Override
                public void onDataLoadInterrupted(AbsAsyncData data, int loadId) {

                }
            }, -1);
        }

        super.loadData();
    }

    @Override
    public void display() {
        super.display();

        if (mViewPager == null) {

            // no view pager (yet) - nothing to do
            return;
        }

        mViewPagerTitlebar = (DSViewPagerTitlebar) findViewById(R.id.view_pager_title_bar);
        mSlidingTabLayout = (SlidingTabLayoutModByDS) findViewById(R.id.sliding_tab);
        if (mSlidingTabLayout != null) {
            mSlidingTabLayout.setDistributeEvenly(false);
        }

        if (mViewPagerAdapterData == null) {
            ensureFragmentsAndTitles();
        }

        if (mAdapter.getCount() == 0) {
            return;
        }

        setAdapterToViewPager();
    }

    private void setAdapterToViewPager() {
        if (mViewPager != null) {
            mViewPager.setAdapter(mAdapter);
            mViewPager.setPageMargin((int) (1 * Global.getDipMultiplier()));

            if (mViewPagerTitlebar != null) {
                mViewPagerTitlebar.setViewPager(mViewPager);
                mViewPager.setOnPageChangeListener(mViewPagerTitlebar);
                mViewPagerTitlebar.setOnPageChangeListener(this);
            }
            if (mSlidingTabLayout != null) {
                mSlidingTabLayout.setViewPager(mViewPager);
                mSlidingTabLayout.setOnPageChangeListener(this);
            }
        }

        // note that mIntentPageData can still not be null if the mIntentPageId was pointing to a fragment in this activity

        // restore saved current page
        mViewPager.setCurrentItem(mCurrentPage, false);
        final DSFragment currentFragment = (DSFragment) mAdapter.getItemFromManager(mViewPager, mCurrentPage);
        if (currentFragment != null) {
            currentFragment.setActive(true);
            currentFragment.loadDataAndDisplay();
        }
    }

    @Override
    public boolean onMenuItemSelected(int itemId) {
        if (mViewPager != null && mAdapter.getCount() > mCurrentPage) {

            // current fragment's items
            DSFragment fragment = (DSFragment) mAdapter.getItemFromManager(mViewPager, mCurrentPage);
            if (((DSFragment) fragment).onMenuItemSelected(itemId)) {
                return true;
            }
        }

        return super.onMenuItemSelected(itemId);
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        ;
    }

    @Override
    public void onPageSelected(int page) {
        mCurrentPage = page;
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        ;
    }

    @Override
    public void onBackPressed() {
        boolean doNothing = false;

        if (mViewPager == null) {
            for (int i = mAdapter.getCount() - 1; i >= 0; --i) {
                final DSFragment fragment = ((DSFragment) mAdapter.getItemFromManager(mViewPager, i));
                if (fragment != null && fragment.isActive() && fragment.onBackPressed()) {
                    doNothing = true;
                }
            }
        }
        if (doNothing) {
            return;
        }

        try {
            if (getActionMode() != ACTION_MODE_SEARCH && mCurrentPage != 0 && mGoBackToFirstPageFirst) {
                if (mViewPager != null) {
                    mViewPager.setCurrentItem(0, true);
                }
            } else {
                super.onBackPressed();
            }
        } catch(Throwable e) {
            super.onBackPressed();
        }
    }

    /**
     *
     * @param page
     * @param scroll
     */
    public void selectPage(int page, boolean scroll) {
        if (mViewPager != null && mViewPager.getAdapter() != null) {
            mViewPager.setCurrentItem(page);
        } else {
            mCurrentPage = page;
        }
    }

    /**
     * change the title for a fragment
     * !note: only use it when mViewPagerAdapterData == null
     *
     * @param forFragment
     * @param titleResID
     */
    public void changeTitleFor(DSFragment forFragment, int titleResID) {

        assert(mViewPagerAdapterData == null);

        if (forFragment == null || !forFragment.isActive()) {
            return;
        }

        ensureFragmentsAndTitles();

        forFragment.setFragmentTitle(getString(titleResID));

        mViewPagerTitlebar.invalidate();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("view-pager-current-page", mCurrentPage);
    }

    @Override
    public void onCreateRestoreInstanceState(Bundle savedInstanceState) {
        super.onCreateRestoreInstanceState(savedInstanceState);

        mCurrentPage = savedInstanceState.getInt("view-pager-current-page", 0);
    }

    /**
     * override to create fragments/titles if they're hardcoded
     */
    protected void ensureFragmentsAndTitles() {
    }

    /**
     * give adapter data when async data is needed for the adapter
     *
     * @return
     */
    protected AbsAsyncData createViewPagerAdapterData() {
        return null;
    }

    /**
     * override if view pager adapter data is given to handle when it's loaded
     */
    protected void onViewPagerAdapterDataLoaded() {
        mViewPager.setAdapter(mAdapter);
    }

    protected abstract FragmentPagerAdapterModByDS createViewPagerAdapter();
}