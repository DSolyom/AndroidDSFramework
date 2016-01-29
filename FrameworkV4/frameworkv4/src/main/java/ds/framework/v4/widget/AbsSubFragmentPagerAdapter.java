package ds.framework.v4.widget;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.HashMap;

import ds.framework.v4.app.DSFragment;

/**
 * Created by solyom on 27/01/16.
 */
abstract public class AbsSubFragmentPagerAdapter extends AbsTemplatePagerAdapter {

    /**
     * this is where we save the fragments instance states
     * need to call (@function onSaveInstanceState(), @Function onRestoreInstanceState())
     * from parent fragment if we need these after it saves / restore it's own
     */
    private HashMap<String, Bundle> mSavedFragmentInstanceStates = new HashMap<>();

    DSFragment mParentFragment;

    public AbsSubFragmentPagerAdapter(DSFragment parentFragment) {
        super(parentFragment.getDSActivity(), 0);

        mParentFragment = parentFragment;
    }

    @Override
    public View inflatePage(ViewGroup viewParent, int position) {
        FrameLayout container = new FrameLayout(mParentFragment.getContext());
        return container;
    }

    @Override
    public void fillItem(View item, int position) {
        FrameLayout container = (FrameLayout) item;

        DSFragment fragment = (DSFragment) getItem(position);

        // attach fragment - TODO: little bit messy with position as containerViewID
        String fragmentId = fragment.getFragmentId();
        mParentFragment.attachSubFragment(position, fragment, fragmentId);
        fragment.onCreateView(((Activity) mIn).getLayoutInflater(), container, mSavedFragmentInstanceStates.get(fragmentId));

        container.addView(fragment.getRootView());
    }

    @Override
    synchronized public void destroyItem(View container, int position, Object object) {
        super.destroyItem(container, position, object);

        mParentFragment.detachSubFragment(position);
    }

    public void onSaveInstanceState(Bundle outState) {
        // TODO:
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // TODO:
    }
}
