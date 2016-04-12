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

import android.app.ActivityOptions;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

import ds.framework.v4.Global;
import ds.framework.v4.R;
import ds.framework.v4.Settings;
import ds.framework.v4.common.Common;
import ds.framework.v4.common.Debug;
import ds.framework.v4.io.ConnectionChecker;
import ds.framework.v4.io.ConnectionChecker.ConnectionChangedListener;

abstract public class DSActivity extends AppCompatActivity
        implements ConnectionChangedListener {

    public static final String ONACTIVITYRESULT_ACTION = "DS_OnActivityResult_Action";
    public static final String TRANSPORT_ACTION = "DS_Transport_Action";
    public static final String GOBACK_ACTION = "DS_Goback_Action";
    public static final String GOBACKTO_ACTION = "DS_Gobackto_Action";
    public static final String FINISH_ALL_ACTION = "DS_Finish_All_Action";

    public static final int TRANSPORT_ACTION_CODE = 50345;
    public static final int ACTION_MODE_BAR = 0;
    public static final int ACTION_MODE_SEARCH = 1;

    private static final String TRANSITION_app_bar = "DS_TR_app_bar";
    private static final String TRANSITION_app_bar_shadow = "DS_TR_app_bar_shadow";
    private static final int TRANSITION_Z_app_bar = Integer.MAX_VALUE;

    protected static boolean mConnected = true;
    /**
     * fragments
     */
    final protected HashMap<String, DSFragmentInterface> mFragments = new LinkedHashMap<String, DSFragmentInterface>(1);
    /**
     * the root view in the activity's view hierarchy
     */
    protected ViewGroup mRootView;
    /**
     * set if you may want to go back to this specific activity and it is not handled via the activity's manifest definition
     */
    protected String mActivityId;
    protected int mReturnRequestCode;
    protected boolean mHomeVisible = false;
    TitleSpinnerItemAdapter mTitleSpinnerItemAdapter;
    /**
     * unique id for the activity instance<br/>
     * note: mainly for some template click transport issues
     */
    private int mId = -1;
    /**
     * in between onResume and onPause?
     */
    private boolean mIsRunning;
    /**
     * is the content view set yet?
     */
    private boolean mContentViewSet;
    /**
     * to prevent transport after a previous one before entering the app again
     */
    private boolean mAfterTransport;
    private Menu mMenu;
    private int mMenuResID = R.menu.x_menu_base;
    private int mMenuItemLastOrder = 99;
    /**
     * current action (bar) mode
     */
    private int mActionMode;

    /**
     * this is useful when ie. using a view pager with navigation list options for each page
     */
    private int mSelectedNavigationItem;

    private Object mTransportData = new NoTransportData();
    private Object mActivityResult;

    private boolean mHasSceneTransition = false;
    private boolean mEnterTransitionPostponed = false;
    private boolean mStartedEnterTransition = false;

    private Toolbar mAppBar;
    private View mAppBarShadow;

    /**
     * set true if shared element animation should overlap the app bar
     */
    protected boolean mExcludeAppBarFromTransportAnimation = false;

    /**
     * set true if shared element animation should overlap the phones navigation bar
     */
    protected boolean mExcludeNavigationBarFromTransportAnimation = false;

    /**
     * set true if shared element animation should overlap the status bar
     */
    protected boolean mExcludeStatusBarFromTransportAnimation = false;;

    /**
     * @param context
     * @param activityId
     */
    static public void requestRefreshActivity(Context context, String activityId) {
        Settings.putBoolean(context, "refreshActivity_" + activityId, true, true);
    }

    /**
     * @param context
     * @param activityIds
     * @param exceptId
     */
    static public void requestRefreshActivities(Context context, String[] activityIds, String exceptId) {
        if (activityIds.length == 0) {
            return;
        }
        for (String activityId : activityIds) {
            if (exceptId != null && exceptId.equals(activityId)) {
                continue;
            }
            Settings.putBoolean(context, "refreshActivity_" + activityId, true, false);
        }
        Settings.commitChanges(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // screen may have changed
        Global.clearScreenInfo();

        if (Global.getContext() == null) {

            // make sure we have context in Global
            Global.setCurrentActivity(this);
        }

        selfOnCreate(savedInstanceState);

        Settings.getInstance(this).forceLocale(this);

        if (mId == -1) {
            // so this is unique right? ... right
            mId = (int) (Math.random() * Integer.MAX_VALUE);
        }

        if (savedInstanceState != null) {

            // restore state
            onCreateRestoreInstanceState(savedInstanceState);

            // another tasks when restoring state
            selfOnRestoreState(savedInstanceState);
        }

        onEnterActivity(getIntent(), false);
    }

    protected void selfOnCreate(Bundle savedInstanceState) {
        ;
    }

    @Override
    public void onResume() {
        super.onResume();

        // actionbar title or something else may have changed
        if (mTitleSpinnerItemAdapter != null) {
            mTitleSpinnerItemAdapter.notifyDataSetChanged();
        }

        mIsRunning = true;

        onEnterActivity(getIntent(), true);
    }

    @Override
    public void onPause() {
        mIsRunning = false;

        super.onPause();

        if (Global.getCurrentActivity() == this) {
            Global.setCurrentActivity(null);
        }

        ConnectionChecker.getInstance().unregisterReceiver(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            Common.removeAllViewsRec((ViewGroup) findViewById(android.R.id.content));
        } catch (Throwable e) {
            ;
        }

        Global.onActivityDestroyed(this);
    }

    @Override
    public void finish() {
        if (mHasSceneTransition) {
            finishAfterTransition();

            // finishAfterTransition will recall finish so make sure no infinite loop
            mHasSceneTransition = false;
        } else {
            ConnectionChecker.getInstance().unregisterReceiver(this);
            Global.onActivityDestroyed(this);

            super.finish();
        }
    }

    /**
     * act when entering the activity (from anywhere)<br/>
     * get transport or back result data and act on it<br/>
     * set or start to load all needed data<br/>
     * fill the screen
     *
     * @param data
     */
    protected void onEnterActivity(Intent data, boolean inResume) {
        mAfterTransport = false;

        Global.setCurrentActivity(this);

        selfOnEnterActivity(data, inResume);

        if (data == null) {
            return;
        }

        final String action = data.getAction();

        if (FINISH_ALL_ACTION.equals(action)) {
            finishAll();
            return;
        }

        mHasSceneTransition = data.getBooleanExtra("has-scene-transition", false);
        if (mHasSceneTransition) {
            if (!mEnterTransitionPostponed) {
                postponeEnterTransition();
                mEnterTransitionPostponed = true;
            }
        } else {
            mStartedEnterTransition = true;
        }

        if (ONACTIVITYRESULT_ACTION.equals(action)) {

            // after real onActivityResult
            mActivityResult = data;
            onActivityResult(mActivityResult);

        } else if (mTransportData instanceof NoTransportData && mActivityResult == null) {
            if (TRANSPORT_ACTION.equals(action)) {

                // normal transport
                mTransportData = data.getSerializableExtra("transport-data");
                mTransportData = onTransport(mTransportData);

            } else if (GOBACK_ACTION.equals(action)) {

                // normal go back 1 step or steps are handled via manifest definition of this activity
                mActivityResult = data.getSerializableExtra("transport-data");
                onActivityResult(mActivityResult);

            } else if (GOBACKTO_ACTION.equals(action)) {

                // go back to activity with target id
                final String target = data.getStringExtra("back-target");
                if (target != null && !target.equals(mActivityId)) {

                    // go back one more
                    goBackTo(target, data.getSerializableExtra("transport-data"));
                    removeTransportData(data);
                    return;

                } else {

                    // we were the target
                    mActivityResult = data.getSerializableExtra("transport-data");
                    onActivityResult(mActivityResult);
                }
            }

            removeTransportData(data);
        }

        if (mFragments.isEmpty()) {
            attachFragments();
        }

        loadDataAndDisplay();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        View appBar = findViewById(R.id.app_bar);
        if (appBar != null && appBar instanceof Toolbar) {
            mAppBar = (Toolbar) appBar;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mAppBar.setTransitionName(TRANSITION_app_bar);
                mAppBar.setTranslationZ(TRANSITION_Z_app_bar);
            }
            mAppBarShadow = findViewById(R.id.app_bar_shadow);
            if (mAppBarShadow != null) {
                mAppBarShadow.setTransitionName(TRANSITION_app_bar_shadow);
                mAppBarShadow.setTranslationZ(TRANSITION_Z_app_bar);
            }
            setSupportActionBar(mAppBar);
        }

        if (mRootView == null) {

            // set root for now
            mRootView = (ViewGroup) findViewById(android.R.id.content);
        }

        // and content view is set
        mContentViewSet = true;

        // if any data was displayed - it should be displayed again
        invalidateDisplay();

        // load and set data and fill our screen
        loadDataAndDisplay();
    }

    /**
     * call when layouts from shared views are done and are in the activity's view structure
     */
    public void startPostponedEnterTransition() {
        if (!mStartedEnterTransition) {
            mStartedEnterTransition = true;
            final View decor = getWindow().getDecorView();
            decor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    decor.getViewTreeObserver().removeOnPreDrawListener(this);
                    startEnterTransition();
                    return true;
                }
            });
        }
    }

    /**
     *
     */
    protected void startEnterTransition() {
        DSActivity.super.startPostponedEnterTransition();
    }

    /**
     * exclude views from transition<br/>
     * call before startPostponedEnterTransition is called
     *
     * @param viewID
     * @param exclude
     */
    public void excludeEnterTarget(int viewID, boolean exclude) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getEnterTransition().excludeTarget(viewID, exclude);
        }
    }

    public Toolbar getAppBar() {
        return mAppBar;
    }

    /**
     * get the root view
     */
    public View getRootView() {
        return mRootView;
    }

    /**
     * @param activityId
     */
    public void requestRefreshActivity(String activityId) {
        requestRefreshActivity(this, activityId);
    }

    /**
     * @param activityIds
     */
    public void requestRefreshActivities(String[] activityIds) {
        requestRefreshActivities(activityIds, null);
    }

    /**
     * @param activityIds
     * @param exceptId
     */
    public void requestRefreshActivities(String[] activityIds, String exceptId) {
        requestRefreshActivities(this, activityIds, exceptId);
    }

    /**
     * act when coming back from another activity
     *
     * @param data
     */
    public void onActivityResult(Object data) {
        selfOnActivityResult(data);
    }

    /**
     * act when transporting to this activity
     *
     * @param data
     */
    protected Object onTransport(Object data) {
        return data;
    }

    /**
     * get activity id
     *
     * @return
     */
    public String getActivityId() {
        return mActivityId;
    }

    /**
     * is the content view set yet?
     *
     * @return
     */
    public boolean isContentViewSet() {
        return mContentViewSet;
    }

    /**
     * is the activity in a resumed - running stae
     *
     * @return
     */
    public boolean isRunning() {
        return mIsRunning;
    }

    @Override
    public void onNewIntent(Intent data) {
        super.onNewIntent(data);
        onEnterActivity(data, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            mReturnRequestCode = requestCode;
            if (data.getAction() == null || data.getAction().length() == 0) {
                data.setAction(ONACTIVITYRESULT_ACTION);
            }
            onEnterActivity(data, false);
        } else {
            removeTransportData(data);
        }
    }

    /**
     * @return
     */
    public int getReturnRequestCode() {
        return mReturnRequestCode;
    }

    private void removeTransportData(Intent data) {
        if (data == null) {
            return;
        }
        data.addFlags(Intent.FILL_IN_DATA | Intent.FILL_IN_ACTION);
        data.setAction("");
        data.removeExtra("transport-data");
        data.removeExtra("back-target");
    }

    /**
     *
     */
    protected void removeSavedTransportData() {
        mTransportData = new NoTransportData();
        mActivityResult = null;
    }

    public Integer getUniqueId() {
        return mId;
    }

    public Context getContext() {
        return this;
    }

    public DSActivity getScreenActivity() {
        return this;
    }

    public View inflate(int resId) {
        return getLayoutInflater().inflate(resId, null);
    }

    public View inflate(int resId, ViewGroup root) {
        return getLayoutInflater().inflate(resId, root);
    }

    public View inflate(int resId, ViewGroup root, boolean addToParent) {
        return getLayoutInflater().inflate(resId, root, addToParent);
    }

    /**
     * anything extra when starting to enter activity?
     *
     * @param data
     * @param inResume
     */
    protected void selfOnEnterActivity(Intent data, boolean inResume) {
        ;
    }

    /**
     * @param fragment
     */
    public void onFragmentAttached(DSFragmentInterface fragment) {
        final String fragmentId = fragment.getFragmentId();
        if (!mFragments.containsKey(fragmentId)) {
            mFragments.put(fragmentId, fragment);

            if (!(mTransportData instanceof NoTransportData)) {
                fragment.onTransport(mTransportData);
            }
            if (mActivityResult != null) {
                fragment.onActivityResult(fragmentId);
            }
        }
    }

    public void onFragmentDetached(DSFragmentInterface fragment) {
        final String fragmentId = fragment.getFragmentId();
        mFragments.remove(fragmentId);
    }

    @Override
    public void onStart() {
        super.onStart();

        removeSavedTransportData();
    }

    /**
     * anything extra when restoring the state?
     *
     * @param savedInstanceState
     */
    protected void selfOnRestoreState(Bundle savedInstanceState) {

        // now we need our fragments
        // fragment state will be restored in onCreateView in fragments
        if (mFragments.isEmpty()) {
            attachFragments();
        }

        mSelectedNavigationItem = savedInstanceState.getInt("DSActivity-selected-navigation-item");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("DSActivity-selected-navigation-item", mSelectedNavigationItem);
    }

    /**
     * anything extra on activity result for selt?
     *
     * @param data
     */
    protected void selfOnActivityResult(Object data) {

        // tell fragments too
        for (DSFragmentInterface fragment : mFragments.values()) {
            fragment.onActivityResult(data);
        }

    }

    /**
     *
     */
    public HashMap<String, DSFragmentInterface> getFragments() {
        return mFragments;
    }

    /**
     * get a fragment by name
     *
     * @param name
     * @return
     */
    public DSFragmentInterface getFragment(String name) {
        return mFragments.get(name);
    }

    /**
     * @param containerResID
     * @return
     */
    public DSFragmentInterface getFragmentFromManager(int containerResID) {
        DSFragmentInterface fragment = (DSFragmentInterface) getFragmentManager().findFragmentById(containerResID);
        if (fragment != null) {
            final String name = fragment.getFragmentId();
            if (name != null && !mFragments.containsKey(name)) {
                mFragments.put(name, fragment);
            }
        }
        return fragment;
    }

    /**
     * add an already attached fragment to the know fragment list
     *
     * @param fragment
     * @param fragmentId
     */
    protected void addAttachedFragment(DSFragmentInterface fragment, String fragmentId) {
        fragment.setFragmentId(fragmentId);
        mFragments.put(fragmentId, fragment);
    }

    /**
     * @param fragment
     * @param fragmentId
     * @param containerResID
     */
    protected void attachFragment(DSFragmentInterface fragment, String fragmentId, int containerResID) {
        attachFragment(fragment, fragmentId, containerResID, 0, 0, 0, 0);
    }

    protected void attachFragment(DSFragmentInterface fragment, String fragmentId, int containerResID,
                                  int animEnter, int animExit, int popEnter, int popExit) {
        if (fragmentId != null) {
            fragment.setFragmentId(fragmentId);
        }
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (animEnter != 0) {
            ft.setCustomAnimations(animEnter, animExit, popEnter, popExit);
        }
        ft.add(containerResID, (Fragment) fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    /**
     * attach starting fragments to this activity
     */
    protected void attachFragments() {

    }

    /**
     * @param fragment
     * @param fragmentId
     * @param containerResID
     */
    protected void replaceFragment(DSFragmentInterface fragment, String fragmentId, int containerResID) {
        replaceFragment(fragment, fragmentId, containerResID, 0, 0, 0, 0);
    }

    /**
     * @param fragment
     * @param fragmentId
     * @param containerResID
     * @param animEnter
     * @param animExit
     */
    protected void replaceFragment(DSFragmentInterface fragment, String fragmentId, int containerResID,
                                   int animEnter, int animExit, int popEnter, int popExit) {
        if (fragmentId != null) {
            fragment.setFragmentId(fragmentId);
        }
        final FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (animEnter != 0) {
            ft.setCustomAnimations(animEnter, animExit, popEnter, popExit);
        }
        ft.replace(containerResID, (Fragment) fragment).commit();
        mFragments.put(fragmentId, fragment);
    }

    /**
     * detach a fragment by fragmentId
     *
     * @param fragmentId
     */
    protected void detachFragment(String fragmentId) {
        detachFragment(mFragments.remove(fragmentId));
    }

    /**
     * @param fragment
     */
    protected void detachFragment(DSFragmentInterface fragment) {
        getFragmentManager().beginTransaction().detach((Fragment) fragment).commit();
    }

    /**
     * detach all fragments - a clean up of a sort
     */
    void detachFragments() {
        for (DSFragmentInterface fragment : mFragments.values()) {
            detachFragment(fragment);
        }
    }

    /**
     * @param name
     */
    protected void removeFragment(String name) {
        removeFragment(mFragments.remove(name));
    }

    /**
     * @param fragment
     */
    protected void removeFragment(DSFragmentInterface fragment) {
        if (fragment instanceof DialogFragment) {
            final DialogFragment dfragment = (DialogFragment) fragment;
            if (dfragment.getDialog() != null && dfragment.getDialog().isShowing()) {
                dfragment.dismissAllowingStateLoss();
            }
        }
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.remove((Fragment) fragment);
        ft.commit();
        mFragments.remove(fragment.getFragmentId());
    }

    /**
     * remove all fragments - a clean up of a sort
     */
    void removeFragments() {
        for (DSFragmentInterface fragment : mFragments.values()) {
            removeFragment(fragment);
        }
    }

    /**
     * load fragment data and display it<br/>
     * display only possible if content view is set and the activity is running
     */
    protected void loadDataAndDisplay() {
        if (isFinishing()) {
            return;
        }

        ConnectionChecker.getInstance().registerReceiver(this, this);

        if (Settings.getBoolean(this, "refreshActivity_" + mActivityId, false)) {
            invalidateData();
            Settings.putBoolean(this, "refreshActivity_" + mActivityId, false, true);
        }

        if (!isContentViewSet() || !isRunning()) {
            return;
        }

        loadData();

        display();
    }

    /**
     * load data
     */
    protected void loadData() {
        for (DSFragmentInterface fragment : mFragments.values()) {
            if (fragment.getContext() != null && fragment.getDataState() == DSFragment.DATA_INVALID) {
                fragment.loadData(true);
            }
        }
    }

    /**
     * display
     */
    protected void display() {
        for (DSFragmentInterface fragment : mFragments.values()) {
            if (fragment.getRootView() != null && fragment.getDataState() != DSFragment.DATA_DISPLAYED) {
                fragment.display(true);
            }
        }
    }

    /**
     * invalidate all data fragments included
     */
    public void invalidateData() {
        for (DSFragmentInterface fragment : mFragments.values()) {
            if (fragment.getDataState() != DSFragment.DATA_INVALID) {
                fragment.invalidateData(true);
            }
        }
    }

    /**
     * invalidate all fragment's data display
     */
    public void invalidateDisplay() {
        for (DSFragmentInterface fragment : mFragments.values()) {
            fragment.invalidateDisplay();
        }
    }

    /**
     * reload data and refill screen if running
     */
    public void reloadAndDisplay() {
        invalidateData();
        if (isRunning()) {
            loadDataAndDisplay();
        }
    }

    /**
     * remove all data and restart everything
     */
    public void restart() {
        for (DSFragmentInterface fragment : mFragments.values()) {
            fragment.reset();
        }
        loadDataAndDisplay();
    }

    /**
     * return the current data loaded state
     *
     * @return
     */
    protected int getDataState() {
        int state = DSFragment.DATA_DISPLAYED;
        for (DSFragmentInterface fragment : mFragments.values()) {
            if (state > fragment.getDataState()) {
                state = fragment.getDataState();
            }
        }
        return state;
    }

    /**
     * restore instance state in onCreate
     *
     * @param savedInstanceState
     */
    public void onCreateRestoreInstanceState(Bundle savedInstanceState) {

    }

// ActionBar

    /**
     * @return
     */
    public boolean isHomeVisible() {
        return mHomeVisible;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(mMenuResID, mMenu);

        for (DSFragmentInterface fragment : mFragments.values()) {
            if (fragment instanceof DSFragment && ((DSFragment) fragment).getDSActivity() == this) {
                ((DSFragment) fragment).createAndHandleOptionsMenu(this);
            }
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(mHomeVisible);

        return true;
    }

    /**
     * @return
     */
    public Menu getOptionsMenu() {
        return mMenu;
    }

    /**
     * @param id
     * @return
     */
    public MenuItem findMenuItem(int id) {
        if (mMenu == null) {
            return null;
        }
        return mMenu.findItem(id);
    }

    /**
     * @param id
     * @param titleRes
     * @param icon
     * @param order
     * @param refresh
     * @return
     */
    public MenuItem addMenuItem(int id, int titleRes, int icon, int order, boolean refresh) {
        if (order == -1) {
            order = ++mMenuItemLastOrder;
        }

        MenuItem item = mMenu.add(Menu.NONE, id, order, titleRes);
        if (icon != 0) {
            item.setIcon(icon);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        } else {
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

        return item;
    }

    /**
     * @param id
     * @param title
     * @param icon
     * @param order
     * @param refresh
     * @return
     */
    public MenuItem addMenuItem(int id, String title, int icon, int order, boolean refresh) {
        if (order == -1) {
            order = ++mMenuItemLastOrder;
        }

        MenuItem item = mMenu.add(Menu.NONE, id, order, title);
        if (icon != 0) {
            item.setIcon(icon);
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        } else {
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }

        return item;
    }

    /**
     * @param id
     */
    public void removeMenuItem(int id) {
        mMenu.removeItem(id);
    }

    /**
     *
     */
    public void hideAllMenuItem() {
        for (int i = mMenu.size() - 1; i >= 0; --i) {
            mMenu.getItem(i).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final long itemId = item.getItemId();
        if (itemId == 0) {
            return super.onOptionsItemSelected(item);
        }

        if (onMenuItemSelected(item.getItemId())) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * @param itemId
     * @return
     */
    public String getActionBarItemTitle(int itemId) {
        final MenuItem menuItem = findMenuItem(itemId);
        if (menuItem != null) {
            return menuItem.getTitle().toString();
        }
        if (mTitleSpinnerItemAdapter != null) {
            for (TitleSpinnerItem item : mTitleSpinnerItemAdapter.mItems) {
                if (item.getItemId() == itemId) {
                    return item.getTitle();
                }
            }
        }
        return null;
    }

    /**
     * @param itemId
     * @return
     */
    public boolean onMenuItemSelected(int itemId) {
        for (DSFragmentInterface fragment : mFragments.values()) {
            if (fragment.isActive() && fragment.onMenuItemSelected(itemId)) {
                trackMenuItem(itemId);
                return true;
            }
        }

        if (itemId == android.R.id.home) {
            return onActionBarHomeClicked();
        }

        return false;
    }

    /**
     * override to track menu item click
     *
     * @param itemId
     */
    public void trackMenuItem(int itemId) {
        ;
    }

    /**
     *
     */
    public boolean onActionBarHomeClicked() {
        goBack(null);
        return true;
    }

    /**
     * @return
     */
    public int getActionMode() {
        return mActionMode;
    }

    /**
     * Override if you want your own custom view for the selected navigation list title on action bar
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    protected View getNavigationListTitleView(int position, View convertView,
                                              ViewGroup parent) {
        if (convertView == null) {
            convertView = getLayoutInflater().inflate(R.layout.x_title_spinner_selected_item, null);
        }

        ((TextView) convertView.findViewById(R.id.tv_title)).setText(getSupportActionBar().getTitle());
        ((TextView) convertView.findViewById(R.id.tv_subtitle)).setText(mTitleSpinnerItemAdapter.getItem(position).mTitle);

        return convertView;
    }

// DIALOG

    public DSFragment showDialog(Class<?> dialogClass, String tag) {
        final FragmentManager fm = getFragmentManager();
        DSFragment dialog = (DSFragment) fm.findFragmentByTag(tag);

        if (dialog == null) {
            try {
                dialog = (DSFragment) dialogClass.getConstructor().newInstance();
            } catch (Throwable e) {
                Debug.logException(e);
                return null;
            }
        }

        if (!dialog.isShowing()) {
            dialog.show(fm, tag);
        }

        return dialog;
    }

    public void dismissDialog(String tag) {
        final FragmentManager fm = getFragmentManager();
        DSFragment dialog = (DSFragment) fm.findFragmentByTag(tag);

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

// TRANSPORT

    /**
     *
     */
    protected void removeAfterTransportFlag() {
        mAfterTransport = false;
    }

    @Override
    public void onBackPressed() {
        for (DSFragmentInterface fragment : mFragments.values()) {
            if (fragment.onBackPressed()) {
                return;
            }
        }

        goBack(null);
    }

    /**
     * move to target from this activity
     *
     * @param to
     */
    public void transport(Object to) {
        transport(to, null, (Object[]) null);
    }

    /**
     * move to target sending data from this activity
     *
     * @param to
     * @param data
     */
    public void transport(Object to, Object... data) {
        transport(to, null, data);
    }

    /**
     * moving to target sending data and doing transition animation with shared elements
     *
     * @param to
     * @param sharedViews
     * @param data
     */
    public void transport(Object to, Pair[] sharedViews, Object... data) {
        if (mAfterTransport) {
            return;
        }

        if (!beforeTransport(to, data)) {
            return;
        }

        mAfterTransport = true;

        final Intent intent = new Intent(this, (Class<?>) to);
        intent.addFlags(Intent.FILL_IN_DATA | Intent.FILL_IN_ACTION);
        if (data != null && data.length == 1) {
            intent.putExtra("transport-data", (Serializable) data[0]);
        } else {
            intent.putExtra("transport-data", data);
        }
        intent.setAction(TRANSPORT_ACTION);

        Bundle optionsBundle = createTransportOptionsBundle(sharedViews);

        if (optionsBundle != null) {
            intent.putExtra("has-scene-transition", true);

            // note: optionsBundle is null below LOLLIPOP
            startActivityForResult(intent, TRANSPORT_ACTION_CODE, optionsBundle);
        } else {
            startActivityForResult(intent, TRANSPORT_ACTION_CODE);
        }
    }

    /**
     * move to target sending data while finishing this activity
     *
     * @param to
     * @param data
     */
    public void forward(Object to, Object... data) {
        if (mAfterTransport) {
            return;
        }

        if (!beforeTransport(to, data)) {
            return;
        }

        mAfterTransport = true;

        // stop anything our fragments are doing
        invalidateData();

        // actual forwarding
        final Intent intent = new Intent(this, (Class<?>) to);
        intent.addFlags(Intent.FILL_IN_DATA | Intent.FILL_IN_ACTION | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        if (data != null && data.length == 1) {
            intent.putExtra("transport-data", (Serializable) data[0]);
        } else {
            intent.putExtra("transport-data", (Serializable) data);
        }
        intent.setAction(TRANSPORT_ACTION);

        finish();

        startActivity(intent);
    }

    /**
     * move to target sending data while clearing this task and creating a new
     *
     * @param to
     * @param data
     */
    public void forwardAndClear(Object to, Object... data) {
        if (mAfterTransport) {
            return;
        }

        if (!beforeTransport(to, data)) {
            return;
        }

        mAfterTransport = true;

        // stop anything our fragments are doing
        invalidateData();

        // actual forwarding
        final Intent intent = new Intent(this, (Class<?>) to);
        intent.addFlags(Intent.FILL_IN_DATA | Intent.FILL_IN_ACTION |
                Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (data != null && data.length == 1) {
            intent.putExtra("transport-data", (Serializable) data[0]);
        } else {
            intent.putExtra("transport-data", (Serializable) data);
        }
        intent.setAction(TRANSPORT_ACTION);

        finish();

        startActivity(intent);
    }


    /**
     * override to act before leaving the activity via Transport or overrule it by returning false
     *
     * @param to
     * @param data
     * @return
     */
    protected boolean beforeTransport(Object to, Object[] data) {
        return true;
    }

    /**
     * go back to previous activity creating result
     *
     * @param result
     */
    public void goBack(Object result) {
        final Intent intent = new Intent();
        intent.addFlags(Intent.FILL_IN_DATA | Intent.FILL_IN_ACTION);
        intent.putExtra("transport-data", (Serializable) result);
        intent.setAction(GOBACK_ACTION);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * go back to a previous activity creating result
     * !note: make sure the previous activity is indeed an existing activity below in the activity stack
     *
     * @param to
     * @param result
     */
    public void goBackTo(Object to, Object result) {
        if (to instanceof Class) {
            final Intent intent = new Intent(this, (Class<?>) to);
            intent.addFlags(Intent.FILL_IN_DATA | Intent.FILL_IN_ACTION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("transport-data", (Serializable) result);
            intent.setAction(GOBACK_ACTION);
            startActivity(intent);
            finish();
        } else {
            final Intent intent = new Intent();
            intent.addFlags(Intent.FILL_IN_DATA | Intent.FILL_IN_ACTION);
            intent.putExtra("transport-data", (Serializable) result);
            intent.putExtra("back-target", (String) to);
            intent.setAction(GOBACKTO_ACTION);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    /**
     *
     */
    public void finishAll() {
        final Intent intent = new Intent();
        intent.addFlags(Intent.FILL_IN_DATA | Intent.FILL_IN_ACTION);
        intent.setAction(FINISH_ALL_ACTION);
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     *
     */
    protected Bundle createTransportOptionsBundle(Pair[] sharedViews) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (sharedViews != null) {

                final View decorView = getWindow().getDecorView();
                ArrayList<Pair> sharedViewList = extendsSharedViewsForTransport(new ArrayList<>(Arrays.asList(sharedViews)));
                sharedViews = new Pair[sharedViewList.size()];
                sharedViewList.toArray(sharedViews);

                return ActivityOptions.makeSceneTransitionAnimation(this, sharedViews).toBundle();
            }
        }
        return null;
    }

    /**
     * add bar backgrounds (topbar, navigationbar, appbar, ... )
     *
     * @param sharedViewList
     * @return
     */
    protected ArrayList extendsSharedViewsForTransport(ArrayList<Pair> sharedViewList) {
        final View decor = getWindow().getDecorView();

        if (mAppBar != null && !mExcludeAppBarFromTransportAnimation) {
            sharedViewList.add(new Pair(mAppBar, TRANSITION_app_bar));
            if (mAppBarShadow != null) {
                sharedViewList.add(new Pair(mAppBarShadow, TRANSITION_app_bar_shadow));
            }
        }

        View navbar = decor.findViewById(android.R.id.navigationBarBackground);
        if (navbar != null && !mExcludeNavigationBarFromTransportAnimation) {
            sharedViewList.add(new Pair(navbar, Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME));
        }

        View statusbar = decor.findViewById(android.R.id.statusBarBackground);
        if (statusbar != null && !mExcludeStatusBarFromTransportAnimation) {
            sharedViewList.add(new Pair(statusbar, Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME));
        }

        return sharedViewList;
    }

    @Override
    public void onConnectionChanged(boolean connected) {
        if (mConnected != connected) {
            onConnectionChangedInner(connected);
            mConnected = connected;
        }
    }

    protected void onConnectionChangedInner(boolean connected) {
        if (connected) {
            onConnectionEstablished();
        } else {
            onNoConnection();
        }
    }

    public void onConnectionEstablished() {

        for (DSFragmentInterface fragment : mFragments.values()) {
            if (fragment instanceof OnConnectionChangeListener) {
                ((OnConnectionChangeListener) fragment).onConnectionEstablished();
            }
        }
    }

    /**
     * act when internet connection is needed but not exists
     */
    public void onNoConnection() {
        for (DSFragmentInterface fragment : mFragments.values()) {
            if (fragment instanceof OnConnectionChangeListener) {
                ((OnConnectionChangeListener) fragment).onNoConnection();
            }
        }
    }

    /**
     * OnNoConnectionListener
     */
    public interface OnConnectionChangeListener {
        public void onNoConnection();

        public void onConnectionEstablished();
    }

    /**
     * @class TitleSpinnerItemAdpater
     */
    class TitleSpinnerItemAdapter extends BaseAdapter {

        LayoutInflater mLayoutInflater;
        ArrayList<TitleSpinnerItem> mItems = new ArrayList<TitleSpinnerItem>();

        public TitleSpinnerItemAdapter() {
            super();

            mLayoutInflater = (LayoutInflater)
                    new ContextThemeWrapper(DSActivity.this, R.style.Theme_App_ActionBar_Widget)
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public TitleSpinnerItem getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getItemId();
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.x_title_spinner_item, null);
            }

            ((TextView) convertView.findViewById(R.id.tv_label)).setText(getItem(position).getTitle());

            return convertView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getNavigationListTitleView(position, convertView, parent);
        }
    }

    /**
     * @class NoTransportData
     */
    class NoTransportData {

    }

}
